<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  Row, Col, Card, Button, Space, TypographyText, Timeline, TimelineItem,
  List, ListItem, Descriptions, DescriptionsItem, Textarea, Empty, message,
} from 'ant-design-vue'
import {
  PlusOutlined,
  LinkOutlined,
  PauseOutlined,
  CaretRightOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, VoiceInterviewSession } from '../types'
import { translateVoiceEvent } from '../utils/display'

const sessions = ref<VoiceInterviewSession[]>([])
const sessionId = ref('')
const transcript = ref('')
const events = ref<string[]>([])
let socket: WebSocket | null = null

const currentSession = computed(
  () => sessions.value.find((s) => s.sessionId === sessionId.value) ?? null
)

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

onMounted(load)
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <Card>
      <Space wrap>
        <Button type="primary" @click="createSession">
          <template #icon><PlusOutlined /></template>
          创建会话
        </Button>
        <Button @click="connect">
          <template #icon><LinkOutlined /></template>
          连接 WebSocket
        </Button>
        <Button @click="pause">
          <template #icon><PauseOutlined /></template>
          暂停
        </Button>
        <Button @click="resume">
          <template #icon><CaretRightOutlined /></template>
          恢复
        </Button>
        <Button @click="downloadReport">
          <template #icon><DownloadOutlined /></template>
          下载报告
        </Button>
      </Space>
      <div style="margin-top: 12px">
        <TypographyText type="secondary">当前会话：{{ sessionId || '尚未选择' }}</TypographyText>
      </div>
    </Card>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="12">
        <Card title="手动提交转写">
          <a-textarea v-model:value="transcript" :rows="4" placeholder="输入识别后的语音文本或模拟字幕" style="margin-bottom: 12px" />
          <Button type="primary" @click="sendTranscript">提交文本</Button>

          <div v-if="currentSession" style="margin-top: 16px">
            <Descriptions :column="1" size="small" bordered>
              <DescriptionsItem label="暂停状态">{{ currentSession.paused ? '已暂停' : '进行中' }}</DescriptionsItem>
              <DescriptionsItem label="当前题目">{{ currentSession.currentQuestion }}</DescriptionsItem>
              <DescriptionsItem label="已提交轮次">{{ currentSession.submittedTurns }}</DescriptionsItem>
            </Descriptions>
          </div>
        </Card>
      </Col>

      <Col :xs="24" :lg="12">
        <Card title="实时事件流">
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
        <Card title="字幕记录">
          <List v-if="currentSession.subtitles.length > 0" :data-source="currentSession.subtitles" size="small">
            <template #renderItem="{ item }">
              <ListItem>{{ item }}</ListItem>
            </template>
          </List>
          <Empty v-else description="暂无字幕" />
        </Card>
      </Col>
      <Col :xs="24" :lg="12">
        <Card title="AI 追问">
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
