// interview-frontend/src/composables/useSpeechRecognition.ts

import { ref, type Ref } from 'vue'
import { createSpeechService, type SpeechService } from '../services/SpeechService'

export interface UseSpeechRecognitionReturn {
  isSupported: boolean
  isListening: Ref<boolean>
  interimText: Ref<string>
  error: Ref<string | null>
  start: () => void
  stop: () => void
}

export function useSpeechRecognition(): UseSpeechRecognitionReturn {
  const service: SpeechService = createSpeechService()
  const isListening = ref(false)
  const interimText = ref('')
  const error = ref<string | null>(null)

  const start = () => {
    if (!service.isSupported) {
      error.value = '浏览器不支持语音识别，请使用 Chrome 或 Edge'
      return
    }
    error.value = null
    interimText.value = ''

    service.start({
      lang: 'zh-CN',
      onResult: (result) => {
        if (result.isFinal) {
          interimText.value = result.text
        } else {
          // interim 结果，实时更新
          interimText.value = result.text
        }
      },
      onError: (errorMsg) => {
        error.value = errorMsg
        isListening.value = false
      },
      onStatusChange: (status) => {
        isListening.value = status === 'listening'
      }
    })
  }

  const stop = () => {
    service.stop()
    isListening.value = false
  }

  return {
    isSupported: service.isSupported,
    isListening,
    interimText,
    error,
    start,
    stop
  }
}
