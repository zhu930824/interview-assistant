// interview-frontend/src/composables/useRealtimeSpeech.ts

import { ref, onUnmounted, type Ref } from 'vue'

export interface UseRealtimeSpeechReturn {
  isSupported: boolean
  isListening: Ref<boolean>
  isConnected: Ref<boolean>
  interimText: Ref<string>
  finalText: Ref<string>
  error: Ref<string | null>
  connect: () => Promise<void>
  disconnect: () => void
  startRecording: () => Promise<void>
  stopRecording: () => void
}

export function useRealtimeSpeech(): UseRealtimeSpeechReturn {
  const isSupported = !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia)
  const isListening = ref(false)
  const isConnected = ref(false)
  const interimText = ref('')
  const finalText = ref('')
  const error = ref<string | null>(null)

  let ws: WebSocket | null = null
  let mediaStream: MediaStream | null = null
  let audioContext: AudioContext | null = null
  let workletNode: AudioWorkletNode | null = null
  let sourceNode: MediaStreamAudioSourceNode | null = null

  const connect = async (): Promise<void> => {
    if (isConnected.value) return

    const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
    ws = new WebSocket(`${protocol}://${location.host}/ws/realtime-speech`)

    ws.onopen = () => {
      isConnected.value = true
      error.value = null
      console.log('实时语音 WebSocket 已连接')
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.error) {
          error.value = data.error
        } else if (data.status === 'started') {
          console.log('转录流已启动')
        } else if (data.status === 'completed') {
          finalText.value = data.fullText || ''
          interimText.value = ''
        } else if (data.text) {
          if (data.isFinal) {
            finalText.value += data.text
          } else {
            interimText.value = data.text
          }
        }
      } catch (e) {
        console.error('解析消息失败:', e)
      }
    }

    ws.onerror = (e) => {
      console.error('WebSocket 错误:', e)
      error.value = 'WebSocket 连接错误'
    }

    ws.onclose = () => {
      isConnected.value = false
      console.log('WebSocket 已关闭')
    }
  }

  const disconnect = () => {
    if (ws) {
      ws.close()
      ws = null
    }
    isConnected.value = false
  }

  const startRecording = async () => {
    if (!isConnected.value) {
      await connect()
    }

    try {
      error.value = null
      interimText.value = ''
      finalText.value = ''

      // 获取麦克风
      mediaStream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
          sampleRate: 16000
        }
      })

      // 创建音频上下文
      audioContext = new AudioContext({ sampleRate: 16000 })
      sourceNode = audioContext.createMediaStreamSource(mediaStream)

      // 使用 AudioWorklet 处理音频
      try {
        await audioContext.audioWorklet.addModule('/audio-recorder-worklet.js')
        workletNode = new AudioWorkletNode(audioContext, 'audio-recorder-processor')

        workletNode.port.onmessage = (event) => {
          if (event.data.type === 'audio-data' && ws?.readyState === WebSocket.OPEN) {
            // 发送 PCM 数据到服务器
            const pcmData = float32ToPcm16(event.data.data)
            ws.send(pcmData)
          }
        }

        sourceNode.connect(workletNode)
      } catch (e) {
        // 备选方案：使用 ScriptProcessorNode
        console.warn('AudioWorklet 加载失败，使用备选方案:', e)
        const scriptProcessor = audioContext.createScriptProcessor(4096, 1, 1)
        scriptProcessor.onaudioprocess = (event) => {
          if (ws?.readyState === WebSocket.OPEN) {
            const inputData = event.inputBuffer.getChannelData(0)
            const pcmData = float32ToPcm16(new Float32Array(inputData))
            ws.send(pcmData)
          }
        }
        sourceNode.connect(scriptProcessor)
      }

      isListening.value = true
    } catch (e: any) {
      console.error('启动录音失败:', e)
      if (e.name === 'NotAllowedError') {
        error.value = '麦克风权限被拒绝'
      } else {
        error.value = `启动录音失败: ${e.message}`
      }
    }
  }

  const stopRecording = () => {
    if (ws?.readyState === WebSocket.OPEN) {
      ws.send('stop')
    }

    if (workletNode) {
      workletNode.disconnect()
      workletNode = null
    }

    if (sourceNode) {
      sourceNode.disconnect()
      sourceNode = null
    }

    if (audioContext) {
      audioContext.close()
      audioContext = null
    }

    if (mediaStream) {
      mediaStream.getTracks().forEach(track => track.stop())
      mediaStream = null
    }

    isListening.value = false
  }

  // Float32 转 PCM16
  const float32ToPcm16 = (float32: Float32Array): ArrayBuffer => {
    const buffer = new ArrayBuffer(float32.length * 2)
    const view = new DataView(buffer)
    for (let i = 0; i < float32.length; i++) {
      const sample = Math.max(-1, Math.min(1, float32[i]))
      view.setInt16(i * 2, sample < 0 ? sample * 0x8000 : sample * 0x7FFF, true)
    }
    return buffer
  }

  onUnmounted(() => {
    stopRecording()
    disconnect()
  })

  return {
    isSupported,
    isListening,
    isConnected,
    interimText,
    finalText,
    error,
    connect,
    disconnect,
    startRecording,
    stopRecording
  }
}