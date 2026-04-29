// interview-frontend/src/composables/useAudioStream.ts

import { ref, onUnmounted, type Ref } from 'vue'

export interface UseAudioStreamReturn {
  isSupported: boolean
  isListening: Ref<boolean>
  volume: Ref<number>
  error: Ref<string | null>
  recordingBlob: Ref<Blob | null>
  start: () => Promise<void>
  stop: () => Promise<Blob | null>
}

export function useAudioStream(): UseAudioStreamReturn {
  const isSupported = !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia)
  const isListening = ref(false)
  const volume = ref(0)
  const error = ref<string | null>(null)
  const recordingBlob = ref<Blob | null>(null)

  let mediaStream: MediaStream | null = null
  let analyser: AnalyserNode | null = null
  let animationId: number | null = null
  let sourceNode: MediaStreamAudioSourceNode | null = null
  let audioContext: AudioContext | null = null
  let workletNode: AudioWorkletNode | null = null
  let recordedChunks: Float32Array[] = []

  const getVolume = () => {
    if (!analyser) return 0
    const dataArray = new Uint8Array(analyser.frequencyBinCount)
    analyser.getByteFrequencyData(dataArray)
    const sum = dataArray.reduce((a, b) => a + b, 0)
    const avg = sum / dataArray.length
    return Math.min(100, Math.round((avg / 255) * 100))
  }

  const updateVolume = () => {
    if (isListening.value && analyser) {
      volume.value = getVolume()
      animationId = requestAnimationFrame(updateVolume)
    }
  }

  // 将 Float32Array PCM 数据转换为 WAV 格式
  const encodeWAV = (samples: Float32Array[], sampleRate: number): Blob => {
    // 合并所有 chunks
    const totalLength = samples.reduce((sum, chunk) => sum + chunk.length, 0)
    const mergedSamples = new Float32Array(totalLength)
    let offset = 0
    for (const chunk of samples) {
      mergedSamples.set(chunk, offset)
      offset += chunk.length
    }

    // 转换为 16-bit PCM
    const buffer = new ArrayBuffer(44 + mergedSamples.length * 2)
    const view = new DataView(buffer)

    // WAV header
    const writeString = (offset: number, str: string) => {
      for (let i = 0; i < str.length; i++) {
        view.setUint8(offset + i, str.charCodeAt(i))
      }
    }

    writeString(0, 'RIFF')
    view.setUint32(4, 36 + mergedSamples.length * 2, true)
    writeString(8, 'WAVE')
    writeString(12, 'fmt ')
    view.setUint32(16, 16, true) // fmt chunk size
    view.setUint16(20, 1, true) // audio format (PCM)
    view.setUint16(22, 1, true) // channels
    view.setUint32(24, sampleRate, true) // sample rate
    view.setUint32(28, sampleRate * 2, true) // byte rate
    view.setUint16(32, 2, true) // block align
    view.setUint16(34, 16, true) // bits per sample
    writeString(36, 'data')
    view.setUint32(40, mergedSamples.length * 2, true)

    // PCM data
    let dataOffset = 44
    for (let i = 0; i < mergedSamples.length; i++) {
      const sample = Math.max(-1, Math.min(1, mergedSamples[i]))
      const intSample = sample < 0 ? sample * 0x8000 : sample * 0x7FFF
      view.setInt16(dataOffset, intSample, true)
      dataOffset += 2
    }

    return new Blob([buffer], { type: 'audio/wav' })
  }

  const start = async () => {
    if (!isSupported) {
      error.value = '浏览器不支持麦克风'
      return
    }

    try {
      error.value = null
      recordedChunks = []
      recordingBlob.value = null

      // 获取麦克风权限
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
      analyser = audioContext.createAnalyser()
      analyser.fftSize = 256

      sourceNode = audioContext.createMediaStreamSource(mediaStream)

      // 注册 AudioWorklet 处理器
      try {
        await audioContext.audioWorklet.addModule('/audio-recorder-worklet.js')
        workletNode = new AudioWorkletNode(audioContext, 'audio-recorder-processor')

        workletNode.port.onmessage = (event) => {
          if (event.data.type === 'audio-data') {
            recordedChunks.push(event.data.data)
          }
        }

        sourceNode.connect(workletNode)
        workletNode.connect(audioContext.destination)
      } catch (e) {
        // 如果 worklet 加载失败，使用 ScriptProcessorNode 作为备选
        console.warn('AudioWorklet 加载失败，使用备选方案:', e)
        const scriptProcessor = audioContext.createScriptProcessor(4096, 1, 1)
        scriptProcessor.onaudioprocess = (event) => {
          const inputData = event.inputBuffer.getChannelData(0)
          recordedChunks.push(new Float32Array(inputData))
        }
        sourceNode.connect(scriptProcessor)
        scriptProcessor.connect(audioContext.destination)
      }

      // 同时连接到分析器用于音量显示
      sourceNode.connect(analyser)

      isListening.value = true
      updateVolume()
    } catch (e: any) {
      console.error('获取麦克风失败:', e)
      if (e.name === 'NotAllowedError') {
        error.value = '麦克风权限被拒绝，请授权后重试'
      } else if (e.name === 'NotFoundError') {
        error.value = '未找到麦克风设备'
      } else {
        error.value = `获取麦克风失败: ${e.message}`
      }
      isListening.value = false
    }
  }

  const stop = async (): Promise<Blob | null> => {
    if (animationId) {
      cancelAnimationFrame(animationId)
      animationId = null
    }

    if (sourceNode) {
      sourceNode.disconnect()
      sourceNode = null
    }

    if (workletNode) {
      workletNode.disconnect()
      workletNode = null
    }

    if (audioContext) {
      audioContext.close()
      const sampleRate = audioContext.sampleRate
      audioContext = null

      if (mediaStream) {
        mediaStream.getTracks().forEach(track => track.stop())
        mediaStream = null
      }

      analyser = null
      isListening.value = false
      volume.value = 0

      // 编码为 WAV
      if (recordedChunks.length > 0) {
        const wavBlob = encodeWAV(recordedChunks, sampleRate)
        recordingBlob.value = wavBlob
        return wavBlob
      }
    } else {
      if (mediaStream) {
        mediaStream.getTracks().forEach(track => track.stop())
        mediaStream = null
      }
    }

    return null
  }

  onUnmounted(() => {
    if (mediaStream) {
      mediaStream.getTracks().forEach(track => track.stop())
    }
    if (audioContext) {
      audioContext.close()
    }
  })

  return {
    isSupported,
    isListening,
    volume,
    error,
    recordingBlob,
    start,
    stop
  }
}