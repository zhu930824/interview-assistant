# 语音输入功能设计

## 背景

当前语音面试页面仅支持手动输入文本提交，需要添加语音识别功能，让用户通过语音输入自动转成文字后再提交。

## 需求

1. 用户点击按钮开始录音，再次点击停止录音
2. 使用 Web Speech API 进行语音识别
3. 识别完成后文字显示在输入框，用户可编辑后再手动提交
4. 预留扩展接口，后续可接入云服务 ASR

## 技术方案

### Web Speech API

浏览器原生语音识别接口，无需后端改动。

```typescript
const recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)()
recognition.lang = 'zh-CN'
recognition.continuous = false
recognition.interimResults = true
```

**兼容性**：Chrome、Edge、Safari 支持；Firefox 需要在 `about:config` 开启。

### 组件结构

```
interview-frontend/src/
├── services/
│   └── SpeechService.ts           # 语音识别服务实现
├── composables/
│   └── useSpeechRecognition.ts    # Vue 组合式函数
└── pages/
    └── VoiceInterviewPage.vue     # 添加语音控制 UI
```

### 接口设计

```typescript
// services/SpeechService.ts

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

// 浏览器原生实现
export class WebSpeechService implements SpeechService {
  // 使用 window.SpeechRecognition 或 window.webkitSpeechRecognition
}

// 工厂函数，预留扩展
export function createSpeechService(): SpeechService {
  return new WebSpeechService()
}
```

```typescript
// composables/useSpeechRecognition.ts

export interface UseSpeechRecognitionReturn {
  isSupported: boolean
  isListening: Ref<boolean>
  interimText: Ref<string>
  finalText: Ref<string>
  error: Ref<string | null>
  start: () => void
  stop: () => void
}

export function useSpeechRecognition(): UseSpeechRecognitionReturn
```

### UI 改动

**按钮区域新增：**
- 「开始录音」按钮（默认状态）
- 「停止录音」按钮（录音中，红色）

**录音状态显示：**
- 录音中的脉冲动画指示器
- 实时显示临时识别结果（灰色文字）

### 交互流程

```
用户点击「开始录音」
    ↓
isListening = true
显示录音指示器
    ↓
Web Speech API 开始识别
    ↓
实时更新 interimText（灰色临时文字）
    ↓
用户点击「停止录音」或 API 自动结束
    ↓
最终结果填入 transcript 输入框
用户可编辑后点击「提交文本」
```

### 错误处理

| 错误 | 处理 |
|-----|------|
| 浏览器不支持 | 按钮禁用，提示用户切换浏览器 |
| 麦克风权限被拒 | 提示用户授权麦克风 |
| 识别失败 | 显示错误提示，允许重试 |

## 改动文件

| 文件 | 改动 |
|-----|------|
| `src/services/SpeechService.ts` | 新建 |
| `src/composables/useSpeechRecognition.ts` | 新建 |
| `src/pages/VoiceInterviewPage.vue` | 添加语音控制 UI |

## 后续扩展

后续接入云服务 ASR 时：
1. 新增 `CloudSpeechService` 实现 `SpeechService` 接口
2. 通过配置切换 `createSpeechService()` 返回的实例
3. 前端组件代码无需改动
