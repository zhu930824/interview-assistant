<script setup lang="ts">
import { computed, onMounted, ref, h } from 'vue'
import {
  Row, Col, Card, Form, FormItem, Select, SelectOption,
  InputNumber, Button, List, ListItem, ListItemMeta, Tag, Alert, Space,
  Empty, message, TypographyParagraph, TypographyTitle,
} from 'ant-design-vue'
import {
  PlusOutlined,
  SendOutlined,
  FileSearchOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, InterviewDirectionOption, InterviewSession } from '../types'
import { formatDirection, formatInterviewStatus, formatStage } from '../utils/display'
import AnimatedStatCard from '../components/AnimatedStatCard.vue'

const sessions = ref<InterviewSession[]>([])
const directions = ref<InterviewDirectionOption[]>([])
const summary = ref<Record<string, number>>({})
const direction = ref('JAVA_BACKEND')
const totalMinutes = ref(45)
const followUpRounds = ref(1)
const answer = ref('')
const selectedId = ref<number | null>(null)
const creating = ref(false)

const selectedSession = computed(
  () => sessions.value.find((s) => s.id === selectedId.value) ?? sessions.value[0] ?? null
)

const currentQuestion = computed(() =>
  selectedSession.value?.askedQuestions.find(
    (q) => q.id === selectedSession.value?.currentQuestionId
  ) ?? null
)

const todayCount = computed(() => {
  const today = new Date().toDateString()
  return sessions.value.filter(s => new Date(s.createdAt).toDateString() === today).length
})

const avgScore = computed(() => {
  const completed = sessions.value.filter(s => s.status === 'COMPLETED')
  if (completed.length === 0) return 0
  return Math.round(completed.reduce((sum, s) => sum + (s.score ?? 75), 0) / completed.length)
})

const load = async () => {
  const [sessionRes, dirRes, centerRes] = await Promise.all([
    http.get<ApiResponse<InterviewSession[]>>('/interviews'),
    http.get<ApiResponse<InterviewDirectionOption[]>>('/interviews/directions'),
    http.get<ApiResponse<{ summary: Record<string, number> }>>('/interviews/center'),
  ])
  sessions.value = sessionRes.data.data
  directions.value = dirRes.data.data
  summary.value = centerRes.data.data.summary
  if (!selectedId.value && sessions.value.length > 0) {
    selectedId.value = sessions.value[0].id
  }
}

const createSession = async () => {
  creating.value = true
  try {
    const res = await http.post<ApiResponse<InterviewSession>>('/interviews', {
      direction: direction.value,
      totalMinutes: totalMinutes.value,
      followUpRounds: followUpRounds.value,
    })
    selectedId.value = res.data.data.id
    message.success('面试会话已创建')
    await load()
  } finally {
    creating.value = false
  }
}

const submitAnswer = async () => {
  if (!selectedSession.value || !answer.value.trim()) return
  const res = await http.post<ApiResponse<InterviewSession>>(
    `/interviews/${selectedSession.value.id}/answer`,
    { answer: answer.value }
  )
  answer.value = ''
  const updated = res.data.data
  sessions.value = sessions.value.map((s) => (s.id === updated.id ? updated : s))
  selectedId.value = updated.id
}

const evaluate = async () => {
  if (!selectedSession.value) return
  const res = await http.post<ApiResponse<InterviewSession>>(
    `/interviews/${selectedSession.value.id}/evaluate`,
    { transcript: selectedSession.value.transcript }
  )
  const updated = res.data.data
  sessions.value = sessions.value.map((s) => (s.id === updated.id ? updated : s))
  message.success('评估结果已生成')
}

const downloadReport = () => {
  if (!selectedSession.value) return
  window.open(`/api/interviews/${selectedSession.value.id}/report`, '_blank')
}

onMounted(load)
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <h1 class="page-title-animated">面试模拟</h1>

    <Row :gutter="[16, 16]">
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="summary.totalSessions ?? 0"
          label="总会话数"
          gradient="purple"
          :icon="'📅'"
          :progress="Math.min((summary.totalSessions ?? 0) / 100 * 100, 100)"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="summary.completedSessions ?? 0"
          label="已完成"
          gradient="green"
          :icon="'✓'"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="avgScore"
          label="平均分"
          gradient="orange"
          :icon="'⭐'"
          :progress="avgScore"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="todayCount"
          label="今日新增"
          gradient="blue"
          :icon="'🕐'"
        />
      </Col>
    </Row>

    <Card class="glass-section-card" title="创建面试" style="border: none">
      <Form layout="inline">
        <FormItem label="面试方向">
          <Select v-model:value="direction" style="width: 180px">
            <SelectOption v-for="d in directions" :key="d.code" :value="d.code">
              {{ formatDirection(d.code) }}
            </SelectOption>
          </Select>
        </FormItem>
        <FormItem label="总时长(分钟)">
          <InputNumber v-model:value="totalMinutes" :min="10" :max="180" />
        </FormItem>
        <FormItem label="追问轮次">
          <InputNumber v-model:value="followUpRounds" :min="0" :max="5" />
        </FormItem>
        <FormItem>
          <Button type="primary" :loading="creating" @click="createSession">
            <template #icon><PlusOutlined /></template>
            创建面试
          </Button>
        </FormItem>
      </Form>
    </Card>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="10">
        <Card class="glass-section-card" title="会话列表" style="border: none">
          <List v-if="sessions.length > 0" :data-source="sessions" size="small">
            <template #renderItem="{ item }">
              <ListItem
                style="cursor: pointer; border-radius: 8px; transition: all 0.2s"
                :style="selectedId === item.id ? 'background: rgba(99, 102, 241, 0.1); padding: 8px 12px' : 'padding: 8px 12px'"
                @click="selectedId = item.id"
              >
                <ListItemMeta>
                  <template #title>
                    <span>{{ formatDirection(item.direction) }}</span>
                  </template>
                  <template #description>
                    <Tag :color="item.status === 'COMPLETED' ? 'success' : 'processing'">
                      {{ formatInterviewStatus(item.status) }}
                    </Tag>
                    <span style="margin-left: 8px">总时长 {{ item.totalMinutes }} 分钟 · 追问 {{ item.followUpRounds }} 轮</span>
                  </template>
                </ListItemMeta>
              </ListItem>
            </template>
          </List>
          <Empty v-else description="暂无面试会话" />
        </Card>
      </Col>

      <Col :xs="24" :lg="14">
        <Card class="glass-section-card" title="面试工作区" v-if="selectedSession" style="border: none">
          <Alert
            v-if="currentQuestion"
            :message="currentQuestion.content"
            type="info"
            show-icon
            style="margin-bottom: 16px; border-radius: 12px"
          />
          <Alert v-else message="所有题目已完成，可以直接生成评估。" type="success" show-icon style="margin-bottom: 16px; border-radius: 12px" />

          <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px">
            <Tag v-for="(minutes, stage) in selectedSession.stageDurations" :key="stage" color="blue">
              {{ formatStage(String(stage)) }} {{ minutes }} 分钟
            </Tag>
          </div>

          <a-textarea
            v-model:value="answer"
            :rows="4"
            placeholder="输入你的回答内容"
            style="margin-bottom: 12px; border-radius: 12px"
          />

          <Space style="margin-bottom: 16px">
            <Button type="primary" :disabled="!currentQuestion" @click="submitAnswer">
              <template #icon><SendOutlined /></template>
              提交回答
            </Button>
            <Button @click="evaluate">
              <template #icon><FileSearchOutlined /></template>
              生成评估
            </Button>
            <Button @click="downloadReport">
              <template #icon><DownloadOutlined /></template>
              下载报告
            </Button>
          </Space>

          <TypographyTitle :level="5">会话记录</TypographyTitle>
          <TypographyParagraph>
            <pre style="background: rgba(99, 102, 241, 0.05); padding: 16px; border-radius: 12px; overflow: auto; max-height: 200px; white-space: pre-wrap; border: 1px solid rgba(99, 102, 241, 0.1)">{{ selectedSession.transcript || '暂无作答记录。' }}</pre>
          </TypographyParagraph>

          <TypographyTitle :level="5">评估结果</TypographyTitle>
          <TypographyParagraph>
            <pre style="background: rgba(16, 185, 129, 0.05); padding: 16px; border-radius: 12px; overflow: auto; max-height: 200px; white-space: pre-wrap; border: 1px solid rgba(16, 185, 129, 0.1)">{{ selectedSession.evaluation || '还没有生成评估结果。' }}</pre>
          </TypographyParagraph>
        </Card>
        <Card v-else class="glass-section-card" style="border: none"><Empty description="请选择或创建一个面试会话" /></Card>
      </Col>
    </Row>
  </div>
</template>
