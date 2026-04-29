// interview-frontend/src/services/SpeechService.ts

export interface SpeechRecognitionResult {
  text: string
  isFinal: boolean
}

export interface SpeechServiceOptions {
  lang?: string
  onResult?: (result: SpeechRecognitionResult) => void
  onError?: (error: string) => void
  onStatusChange?: (status: 'idle' | 'listening') => void
}

export interface SpeechService {
  readonly isSupported: boolean
  readonly status: 'idle' | 'listening'
  start(options: SpeechServiceOptions): void
  stop(): void
}

// 浏览器原生语音识别实现
export class WebSpeechService implements SpeechService {
  private _status: 'idle' | 'listening' = 'idle'
  private recognition: any = null
  private currentOptions: SpeechServiceOptions = {}
  private finalTranscript: string = ''

  readonly isSupported: boolean

  constructor() {
    const SpeechRecognitionAPI = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    this.isSupported = !!SpeechRecognitionAPI
  }

  get status(): 'idle' | 'listening' {
    return this._status
  }

  start(options: SpeechServiceOptions): void {
    if (!this.isSupported) {
      options.onError?.('浏览器不支持语音识别')
      return
    }

    this.currentOptions = options
    this.finalTranscript = '' // 重置累积文本
    const SpeechRecognitionAPI = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    this.recognition = new SpeechRecognitionAPI()

    this.recognition.lang = options.lang || 'zh-CN'
    this.recognition.continuous = true // 持续录音
    this.recognition.interimResults = true

    this.recognition.onstart = () => {
      this._status = 'listening'
      this.currentOptions.onStatusChange?.('listening')
    }

    this.recognition.onresult = (event: any) => {
      let interimTranscript = ''
      // 遍历所有结果，累积 final 结果
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript
        if (event.results[i].isFinal) {
          this.finalTranscript += transcript
        } else {
          interimTranscript += transcript
        }
      }
      // 返回累积的 final 文本 + 当前 interim 文本
      const fullText = this.finalTranscript + interimTranscript
      const isFinal = event.results[event.results.length - 1]?.isFinal ?? false
      this.currentOptions.onResult?.({
        text: fullText,
        isFinal
      })
    }

    this.recognition.onerror = (event: any) => {
      this._status = 'idle'
      this.currentOptions.onStatusChange?.('idle')
      const errorMessage = this.getErrorMessage(event.error)
      this.currentOptions.onError?.(errorMessage)
    }

    this.recognition.onend = () => {
      this._status = 'idle'
      this.currentOptions.onStatusChange?.('idle')
    }

    try {
      this.recognition.start()
    } catch (e) {
      this.currentOptions.onError?.('启动语音识别失败')
    }
  }

  stop(): void {
    if (this.recognition) {
      this.recognition.stop()
      this.recognition = null
    }
    this._status = 'idle'
    this.currentOptions.onStatusChange?.('idle')
  }

  private getErrorMessage(error: string): string {
    const errorMessages: Record<string, string> = {
      'not-allowed': '麦克风权限被拒绝，请授权后重试',
      'no-speech': '未检测到语音，请重试',
      'audio-capture': '无法获取音频，请检查麦克风',
      'network': '网络错误，请检查网络连接',
      'aborted': '语音识别已取消',
      'service-not-allowed': '语音服务不可用'
    }
    return errorMessages[error] || `语音识别错误: ${error}`
  }
}

// 工厂函数，预留云服务扩展
export function createSpeechService(): SpeechService {
  return new WebSpeechService()
}
