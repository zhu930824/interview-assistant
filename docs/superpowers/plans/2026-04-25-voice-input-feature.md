# 语音输入功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为语音面试页面添加语音识别输入功能，用户点击按钮开始录音，识别结果显示在输入框，用户编辑后提交。

**Architecture:** 前端使用 Web Speech API 进行语音识别，通过服务层封装接口预留扩展，组合式函数管理状态，页面组件集成 UI。

**Tech Stack:** Vue 3 Composition API, TypeScript, Web Speech API, Ant Design Vue

---

## Task 1: 创建语音服务层

**Files:**
- Create: `interview-frontend/src/services/SpeechService.ts`

- [ ] **Step 1: 创建 services 目录和 SpeechService.ts 文件**

```typescript
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
  private recognition: SpeechRecognition | null = null
  private currentOptions: SpeechServiceOptions = {}

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
    const SpeechRecognitionAPI = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    this.recognition = new SpeechRecognitionAPI()

    this.recognition.lang = options.lang || 'zh-CN'
    this.recognition.continuous = false
    this.recognition.interimResults = true

    this.recognition.onstart = () => {
      this._status = 'listening'
      this.currentOptions.onStatusChange?.('listening')
    }

    this.recognition.onresult = (event: any) => {
      const result = event.results[event.results.length - 1]
      const transcript = result[0].transcript
      this.currentOptions.onResult?.({
        text: transcript,
        isFinal: result.isFinal
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
```

- [ ] **Step 2: 验证 TypeScript 编译**

Run: `cd interview-frontend && npx tsc --noEmit`
Expected: 无编译错误

- [ ] **Step 3: Commit**

```bash
git add interview-frontend/src/services/SpeechService.ts
git commit -m "feat: add SpeechService for voice recognition

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 2: 创建语音识别组合式函数

**Files:**
- Create: `interview-frontend/src/composables/useSpeechRecognition.ts`

- [ ] **Step 1: 创建 composables 目录和 useSpeechRecognition.ts 文件**

```typescript
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
```

- [ ] **Step 2: 验证 TypeScript 编译**

Run: `cd interview-frontend && npx tsc --noEmit`
Expected: 无编译错误

- [ ] **Step 3: Commit**

```bash
git add interview-frontend/src/composables/useSpeechRecognition.ts
git commit -m "feat: add useSpeechRecognition composable

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 3: 更新 VoiceInterviewPage 页面

**Files:**
- Modify: `interview-frontend/src/pages/VoiceInterviewPage.vue`

- [ ] **Step 1: 导入组件和组合式函数，添加语音控制状态**

在 `<script setup lang="ts">` 顶部导入区域添加：

```typescript
import { useSpeechRecognition } from '../composables/useSpeechRecognition'
import { AudioOutlined, AudioMutedOutlined } from '@ant-design/icons-vue'
```

在 `let socket: WebSocket | null = null` 后添加：

```typescript
const {
  isSupported: speechSupported,
  isListening,
  interimText,
  error: speechError,
  start: startSpeech,
  stop: stopSpeech
} = useSpeechRecognition()

const showInterim = ref(false)
```

- [ ] **Step 2: 添加监听器，将识别结果填入 transcript**

在 `onMounted(load)` 之前添加：

```typescript
// 监听语音识别结果
watch(interimText, (newVal) => {
  if (newVal && isListening.value === false) {
    // 录音结束，将结果填入输入框
    transcript.value = newVal
    showInterim.value = false
  } else if (newVal && isListening.value) {
    // 录音中，显示临时结果
    showInterim.value = true
  }
})

// 监听错误
watch(speechError, (newVal) => {
  if (newVal) {
    message.error(newVal)
  }
})

// 开始录音
const handleStartRecord = () => {
  startSpeech()
}

// 停止录音
const handleStopRecord = () => {
  stopSpeech()
}
```

需要在导入中添加 `watch`：

```typescript
import { computed, onMounted, ref, watch } from 'vue'
```

- [ ] **Step 3: 更新模板，添加语音控制按钮和状态显示**

在按钮区域 `<Button @click="downloadReport"` 之后添加语音控制按钮：

```vue
<Button
  v-if="!isListening"
  type="primary"
  :disabled="!speechSupported"
  @click="handleStartRecord"
  style="border-radius: 10px"
>
  <template #icon><AudioOutlined /></template>
  {{ speechSupported ? '开始录音' : '语音不可用' }}
</Button>
<Button
  v-else
  danger
  @click="handleStopRecord"
  style="border-radius: 10px"
>
  <template #icon><AudioMutedOutlined /></template>
  停止录音
</Button>
```

在 Textarea 组件下方添加录音状态显示区域：

```vue
<!-- 录音状态指示 -->
<div v-if="isListening" style="margin-top: 12px; display: flex; align-items: center; gap: 8px;">
  <span class="recording-indicator"></span>
  <TypographyText type="secondary">正在录音...</TypographyText>
</div>

<!-- 实时识别结果 -->
<div v-if="showInterim && interimText" style="margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 8px;">
  <TypographyText type="secondary" style="font-style: italic;">{{ interimText }}</TypographyText>
</div>
```

- [ ] **Step 4: 添加录音动画 CSS**

在 `<template>` 后添加 `<style>` 块：

```vue
<style scoped>
.recording-indicator {
  width: 12px;
  height: 12px;
  background-color: #ff4d4f;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}
</style>
```

- [ ] **Step 5: 手动测试语音功能**

1. 运行 `cd interview-frontend && npm run dev`
2. 打开浏览器访问语音面试页面
3. 点击「开始录音」按钮，浏览器应请求麦克风权限
4. 说话后观察实时识别结果显示
5. 点击「停止录音」，文字应填入输入框
6. 编辑后点击「提交文本」，验证提交功能正常

- [ ] **Step 6: Commit**

```bash
git add interview-frontend/src/pages/VoiceInterviewPage.vue
git commit -m "feat: integrate voice input into VoiceInterviewPage

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## 任务概览

| Task | 文件 | 描述 |
|------|------|------|
| Task 1 | `services/SpeechService.ts` | 语音识别服务层 |
| Task 2 | `composables/useSpeechRecognition.ts` | Vue 组合式函数 |
| Task 3 | `pages/VoiceInterviewPage.vue` | 页面集成 |
