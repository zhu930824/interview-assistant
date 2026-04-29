<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  Row, Col, Card, Button, Space, TypographyText, Timeline, TimelineItem,
  List, ListItem, Descriptions, DescriptionsItem, Textarea, Empty, message, Tag,
} from 'ant-design-vue'
import {
  PlusOutlined,
  LinkOutlined,
  PauseOutlined,
  CaretRightOutlined,
  DownloadOutlined,
  AudioOutlined,
  AudioMutedOutlined,
} from '@ant-design/icons-vue'
import { useSpeechRecognition } from '../composables/useSpeechRecognition'
import { http } from '../api/http'
import type { ApiResponse, VoiceInterviewSession } from '../types'
import { translateVoiceEvent } from '../utils/display'
import AnimatedStatCard from '../components/AnimatedStatCard.vue'

const sessions = ref<VoiceInterviewSession[]>([])
const sessionId = ref('')
const transcript = ref('')
const events = ref<string[]>([])
let socket: WebSocket | null = null

const {
  isSupported: speechSupported,
  isListening,
  interimText,
  error: speechError,
  start: startSpeech,
  stop: stopSpeech
} = useSpeechRecognition()

const showInterim = ref(false)

const currentSession = computed(
  () => sessions.value.find((s) => s.sessionId === sessionId.value) ?? null
)

const activeCount = computed(() => sessions.value.filter(s => !s.paused).length)
const pausedCount = computed(() => sessions.value.filter(s => s.paused).length)
const totalDuration = computed(() => {
  const total = sessions.value.reduce((sum, s) => sum + s.submittedTurns * 2, 0)
  return Math.round(total)
})

const load = async () => {
  const res = await http.get<ApiResponse<VoiceInterviewSession[]>>('/voice-interviews')
  sessions.value = res.data.data
  if (!sessionId.value && sessions.value.length > 0) {
    sessionId.value = sessions.value[0].sessionId
  }
}

const refreshDetail = async () => {
  if (!sessionId.value) return
  const res = await http.get<ApiResponse<VoiceInterviewSession>>(
    `/voice-interviews/${sessionId.value}`
  )
  const updated = res.data.data
  sessions.value = [
    updated,
    ...sessions.value.filter((s) => s.sessionId !== updated.sessionId),
  ]
}

const createSession = async () => {
  const res = await http.post<ApiResponse<VoiceInterviewSession>>('/voice-interviews')
  sessionId.value = res.data.data.sessionId
  events.value.unshift(`会话已创建：${sessionId.value}`)
  message.success('语音会话已创建')
  await load()
}

const connect = () => {
  if (!sessionId.value) return
  socket?.close()
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
  socket = new WebSocket(
    `${protocol}://${location.host}/ws/voice-interview?sessionId=${sessionId.value}`
  )
  socket.onmessage = (event) => {
    events.value.unshift(translateVoiceEvent(event.data))
    void refreshDetail()
  }
  message.info('WebSocket 已连接')
}

const sendTranscript = async () => {
  if (!sessionId.value || !transcript.value.trim()) return
  await http.post(`/voice-interviews/${sessionId.value}/submit`, { transcript: transcript.value })
  socket?.send(transcript.value)
  transcript.value = ''
  await refreshDetail()
}

const pause = async () => {
  if (!sessionId.value) return
  await http.post(`/voice-interviews/${sessionId.value}/pause`)
  events.value.unshift('会话已暂停')
  await refreshDetail()
}

const resume = async () => {
  if (!sessionId.value) return
  await http.post(`/voice-interviews/${sessionId.value}/resume`)
  events.value.unshift('会话已恢复')
  await refreshDetail()
}

const downloadReport = () => {
  if (!sessionId.value) return
  window.open(`/api/voice-interviews/${sessionId.value}/report`, '_blank')
}

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

onMounted(load)
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <h1 class="page-title-animated">语音面试</h1>

    <Row :gutter="[16, 16]">
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="sessions.length"
          label="总会话数"
          gradient="purple"
          :icon="'🎙'"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="pausedCount"
          label="已暂停"
          gradient="orange"
          :icon="'⏸'"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="activeCount"
          label="进行中"
          gradient="green"
          :icon="'▶'"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="totalDuration"
          label="总时长(分钟)"
          gradient="blue"
          :icon="'⏱'"
        />
      </Col>
    </Row>

    <Card class="glass-section-card" style="border: none">
      <Space wrap>
        <Button type="primary" @click="createSession" style="border-radius: var(--radius-md)">
          <template #icon><PlusOutlined /></template>
          创建会话
        </Button>
        <Button @click="connect" style="border-radius: var(--radius-md)">
          <template #icon><LinkOutlined /></template>
          连接 WebSocket
        </Button>
        <Button @click="pause" style="border-radius: var(--radius-md)">
          <template #icon><PauseOutlined /></template>
          暂停
        </Button>
        <Button @click="resume" style="border-radius: var(--radius-md)">
          <template #icon><CaretRightOutlined /></template>
          恢复
        </Button>
        <Button @click="downloadReport" style="border-radius: var(--radius-md)">
          <template #icon><DownloadOutlined /></template>
          下载报告
        </Button>
        <Button
          v-if="!isListening"
          type="primary"
          :disabled="!speechSupported"
          @click="handleStartRecord"
          style="border-radius: var(--radius-md)"
        >
          <template #icon><AudioOutlined /></template>
          {{ speechSupported ? '开始录音' : '语音不可用' }}
        </Button>
        <Button
          v-else
          danger
          @click="handleStopRecord"
          style="border-radius: var(--radius-md)"
        >
          <template #icon><AudioMutedOutlined /></template>
          停止录音
        </Button>
      </Space>
      <div style="margin-top: 12px">
        <TypographyText type="secondary">当前会话：{{ sessionId || '尚未选择' }}</TypographyText>
      </div>
    </Card>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="12">
        <Card class="glass-section-card" title="手动提交转写" style="border: none">
          <a-textarea v-model:value="transcript" :rows="4" placeholder="输入识别后的语音文本或模拟字幕" style="margin-bottom: 12px; border-radius: var(--radius-lg)" />
          <Button type="primary" @click="sendTranscript" style="border-radius: var(--radius-md)">提交文本</Button>

          <!-- 录音状态指示 -->
          <div v-if="isListening" style="margin-top: 12px; display: flex; align-items: center; gap: 8px;">
            <span class="recording-indicator"></span>
            <TypographyText type="secondary">正在录音...</TypographyText>
          </div>

          <!-- 实时识别结果 -->
          <div v-if="showInterim && interimText" style="margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: var(--radius-md);">
            <TypographyText type="secondary" style="font-style: italic;">{{ interimText }}</TypographyText>
          </div>

          <div v-if="currentSession" style="margin-top: 16px">
            <Descriptions :column="1" size="small" bordered>
              <DescriptionsItem label="暂停状态">
                <Tag :color="currentSession.paused ? 'warning' : 'success'">
                  {{ currentSession.paused ? '已暂停' : '进行中' }}
                </Tag>
              </DescriptionsItem>
              <DescriptionsItem label="当前题目">{{ currentSession.currentQuestion }}</DescriptionsItem>
              <DescriptionsItem label="已提交轮次">{{ currentSession.submittedTurns }}</DescriptionsItem>
            </Descriptions>
          </div>
        </Card>
      </Col>

      <Col :xs="24" :lg="12">
        <Card class="glass-section-card" title="实时事件流" style="border: none">
          <Timeline v-if="events.length">
            <TimelineItem v-for="(item, index) in events.slice(0, 20)" :key="index" color="blue">
              {{ item }}
            </TimelineItem>
          </Timeline>
          <Empty v-else description="暂无事件" />
        </Card>
      </Col>
    </Row>

    <Row :gutter="[16, 16]" v-if="currentSession">
      <Col :xs="24" :lg="12">
        <Card class="glass-section-card" title="字幕记录" style="border: none">
          <List v-if="currentSession.subtitles.length > 0" :data-source="currentSession.subtitles" size="small">
            <template #renderItem="{ item }">
              <ListItem>{{ item }}</ListItem>
            </template>
          </List>
          <Empty v-else description="暂无字幕" />
        </Card>
      </Col>
      <Col :xs="24" :lg="12">
        <Card class="glass-section-card" title="AI 追问" style="border: none">
          <List v-if="currentSession.aiReplies.length > 0" :data-source="currentSession.aiReplies" size="small">
            <template #renderItem="{ item }">
              <ListItem>{{ item }}</ListItem>
            </template>
          </List>
          <Empty v-else description="暂无追问" />
        </Card>
      </Col>
    </Row>
  </div>
</template>

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
