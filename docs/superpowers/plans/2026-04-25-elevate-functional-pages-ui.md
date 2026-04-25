# Elevate Functional Pages UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade 5 functional pages to match HomePage's premium glassmorphism aesthetic with animated stat cards.

**Architecture:** Add shared CSS classes for animated stats and glass cards, then update each Vue page to use them. CSS-first approach with minimal JavaScript for count-up animations.

**Tech Stack:** Vue 3, Ant Design Vue 4.2.6, CSS animations

---

### Task 1: Add Animated Stat Card CSS Classes

**Files:**
- Modify: `interview-frontend/src/styles/dashboard.css`

- [ ] **Step 1: Add animated stat card styles to dashboard.css**

Append to end of `interview-frontend/src/styles/dashboard.css`:

```css
/* ===========================================
   Animated Stat Cards for Functional Pages
   =========================================== */

.stat-card-animated {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  padding: 20px;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
}

.stat-card-animated::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
}

.stat-card-animated:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.2);
}

/* Gradient accent bar at top */
.stat-card-animated::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
}

.stat-card-animated.gradient-purple::after {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card-animated.gradient-green::after {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-card-animated.gradient-orange::after {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card-animated.gradient-blue::after {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

/* Icon container with gradient background */
.stat-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
}

.stat-icon-wrapper.gradient-purple {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
}

.stat-icon-wrapper.gradient-green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  box-shadow: 0 4px 15px rgba(17, 153, 142, 0.35);
}

.stat-icon-wrapper.gradient-orange {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 4px 15px rgba(240, 147, 251, 0.35);
}

.stat-icon-wrapper.gradient-blue {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 4px 15px rgba(79, 172, 254, 0.35);
}

.stat-icon-wrapper .anticon {
  font-size: 20px;
  color: #ffffff;
}

/* Animated number */
.stat-number-animated {
  font-family: 'Geist Mono', 'SF Mono', -apple-system, monospace;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.stat-label-animated {
  font-family: 'Inter', -apple-system, sans-serif;
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* Progress bar */
.stat-progress-bar {
  width: 100%;
  height: 4px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 2px;
  margin-top: 12px;
  overflow: hidden;
}

.stat-progress-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 1s ease-out;
}

.stat-progress-fill.gradient-purple {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.stat-progress-fill.gradient-green {
  background: linear-gradient(90deg, #11998e 0%, #38ef7d 100%);
}

.stat-progress-fill.gradient-orange {
  background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%);
}

.stat-progress-fill.gradient-blue {
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
}

/* Trend indicator */
.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  font-weight: 500;
}

.stat-trend.positive {
  color: var(--accent-success);
}

.stat-trend.negative {
  color: var(--accent-error);
}

/* Glass section card */
.glass-section-card {
  background: var(--glass-bg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
  transition: all 0.3s ease;
}

.glass-section-card:hover {
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.12);
}

/* Page title styling */
.page-title-animated {
  font-family: 'Geist', 'SF Pro Display', -apple-system, sans-serif;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
  letter-spacing: -0.01em;
}

/* Count-up animation keyframes */
@keyframes countUpFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.count-up-enter {
  animation: countUpFadeIn 0.5s ease-out forwards;
}

/* Pulse animation for icons */
@keyframes iconPulse {
  0%, 100% {
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
  }
  50% {
    box-shadow: 0 4px 25px rgba(102, 126, 234, 0.5);
  }
}

.stat-icon-wrapper.pulse {
  animation: iconPulse 2s ease-in-out infinite;
}

.stat-icon-wrapper.gradient-green.pulse {
  animation-name: iconPulseGreen;
}

@keyframes iconPulseGreen {
  0%, 100% {
    box-shadow: 0 4px 15px rgba(17, 153, 142, 0.35);
  }
  50% {
    box-shadow: 0 4px 25px rgba(17, 153, 142, 0.5);
  }
}
```

- [ ] **Step 2: Commit CSS changes**

```bash
git add interview-frontend/src/styles/dashboard.css
git commit -m "style: add animated stat card CSS classes for functional pages"
```

---

### Task 2: Create AnimatedStatCard Vue Component

**Files:**
- Create: `interview-frontend/src/components/AnimatedStatCard.vue`

- [ ] **Step 1: Create AnimatedStatCard component**

Create file `interview-frontend/src/components/AnimatedStatCard.vue`:

```vue
<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'

interface Props {
  value: number
  label: string
  gradient?: 'purple' | 'green' | 'orange' | 'blue'
  icon?: string
  progress?: number
  trend?: { value: number; label: string }
  animateNumber?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  gradient: 'purple',
  animateNumber: true,
})

const displayValue = ref(0)
const hasAnimated = ref(false)

const formattedValue = computed(() => {
  if (props.value >= 1000000) {
    return (props.value / 1000000).toFixed(1) + 'M'
  }
  if (props.value >= 1000) {
    return (props.value / 1000).toFixed(1) + 'K'
  }
  return displayValue.value.toLocaleString()
})

const animateCountUp = () => {
  if (!props.animateNumber || hasAnimated.value) return
  hasAnimated.value = true
  
  const duration = 1000
  const start = 0
  const end = props.value
  const startTime = performance.now()
  
  const animate = (currentTime: number) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeOut = 1 - Math.pow(1 - progress, 3)
    displayValue.value = Math.floor(start + (end - start) * easeOut)
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  
  requestAnimationFrame(animate)
}

onMounted(() => {
  displayValue.value = props.value
  setTimeout(animateCountUp, 100)
})

watch(() => props.value, (newVal) => {
  displayValue.value = newVal
  hasAnimated.value = false
  setTimeout(animateCountUp, 100)
})
</script>

<template>
  <div :class="['stat-card-animated', `gradient-${gradient}`]">
    <div :class="['stat-icon-wrapper', `gradient-${gradient}`, 'pulse']">
      <span class="stat-icon">{{ icon }}</span>
    </div>
    
    <div class="stat-number-animated count-up-enter">{{ formattedValue }}</div>
    <div class="stat-label-animated">{{ label }}</div>
    
    <div v-if="progress !== undefined" class="stat-progress-bar">
      <div
        :class="['stat-progress-fill', `gradient-${gradient}`]"
        :style="{ width: `${Math.min(progress, 100)}%` }"
      />
    </div>
    
    <div v-if="trend" :class="['stat-trend', trend.value >= 0 ? 'positive' : 'negative']">
      <span>{{ trend.value >= 0 ? '↑' : '↓' }} {{ Math.abs(trend.value) }}%</span>
      <span>{{ trend.label }}</span>
    </div>
  </div>
</template>
```

- [ ] **Step 2: Commit component**

```bash
git add interview-frontend/src/components/AnimatedStatCard.vue
git commit -m "feat: add AnimatedStatCard Vue component with count-up animation"
```

---

### Task 3: Upgrade InterviewPage.vue

**Files:**
- Modify: `interview-frontend/src/pages/InterviewPage.vue`

- [ ] **Step 1: Update script section with new imports and stats**

Replace the script section of `interview-frontend/src/pages/InterviewPage.vue`:

```vue
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
  CalendarOutlined,
  CheckCircleOutlined,
  TrophyOutlined,
  ClockCircleOutlined,
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
```

- [ ] **Step 2: Update template section with animated stats**

Replace the template section of `interview-frontend/src/pages/InterviewPage.vue`:

```vue
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
```

- [ ] **Step 3: Commit InterviewPage changes**

```bash
git add interview-frontend/src/pages/InterviewPage.vue
git commit -m "feat: upgrade InterviewPage with animated stat cards and glass styling"
```

---

### Task 4: Upgrade ResumePage.vue

**Files:**
- Modify: `interview-frontend/src/pages/ResumePage.vue`

- [ ] **Step 1: Update script section**

Replace the script section of `interview-frontend/src/pages/ResumePage.vue`:

```vue
<script setup lang="ts">
import { computed, onMounted, ref, h } from 'vue'
import {
  Row, Col, Card, Table, Tag, Button, ButtonGroup,
  Descriptions, DescriptionsItem, Popconfirm, Spin, Empty, message,
  TypographyTitle, Upload, UploadDragger, Progress,
} from 'ant-design-vue'
import {
  InboxOutlined,
  DownloadOutlined,
  RedoOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CalendarOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, ResumeRecord } from '../types'
import { formatDateTime, formatResumeStatus } from '../utils/display'
import AnimatedStatCard from '../components/AnimatedStatCard.vue'

const resumes = ref<ResumeRecord[]>([])
const stats = ref<Record<string, number>>({})
const loading = ref(false)
const uploading = ref(false)

const selectedResume = computed(() => resumes.value[0] ?? null)

const weekCount = computed(() => {
  const weekAgo = new Date()
  weekAgo.setDate(weekAgo.getDate() - 7)
  return resumes.value.filter(r => new Date(r.createdAt) >= weekAgo).length
})

const load = async () => {
  loading.value = true
  try {
    const [resumeRes, statsRes] = await Promise.all([
      http.get<ApiResponse<ResumeRecord[]>>('/resumes'),
      http.get<ApiResponse<Record<string, number>>>('/resumes/stats'),
    ])
    resumes.value = resumeRes.data.data
    stats.value = statsRes.data.data
  } finally {
    loading.value = false
  }
}

const customUpload = async (options: any) => {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    await http.post('/resumes', formData)
    message.success('上传成功')
    await load()
    options.onSuccess()
  } catch {
    options.onError()
  } finally {
    uploading.value = false
  }
}

const retryResume = async (id: number) => {
  await http.post(`/resumes/${id}/retry`)
  message.success('已重新提交分析')
  await load()
}

const downloadReport = (id: number) => {
  window.open(`/api/resumes/${id}/report`, '_blank')
}

const columns = [
  { title: '文件名', dataIndex: 'fileName', key: 'fileName' },
  { title: '候选人', dataIndex: 'candidateName', key: 'candidateName' },
  { title: '目标岗位', dataIndex: 'targetPosition', key: 'targetPosition' },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    customRender: ({ text }: { text: string }) => {
      const colorMap: Record<string, string> = {
        PENDING: 'default',
        ANALYZING: 'processing',
        COMPLETED: 'success',
        FAILED: 'error',
      }
      return h(Tag, { color: colorMap[text] ?? 'default' }, () => formatResumeStatus(text))
    },
  },
  {
    title: '进度',
    dataIndex: 'progress',
    key: 'progress',
    customRender: ({ text }: { text: number }) => h(Progress, { percent: text, size: 'small' }),
  },
  {
    title: '操作',
    key: 'action',
    customRender: ({ record }: { record: ResumeRecord }) => {
      return h('div', { style: 'display: flex; gap: 8px' }, [
        h(Button, { type: 'link', size: 'small', onClick: () => downloadReport(record.id) }, () => [h(DownloadOutlined), ' 下载']),
        h(Popconfirm, {
          title: '确定重新分析？',
          onConfirm: () => retryResume(record.id),
        }, () => h(Button, { type: 'link', size: 'small', disabled: record.status !== 'FAILED' }, () => [h(RedoOutlined), ' 重试'])),
      ])
    },
  },
]

onMounted(load)
</script>
```

- [ ] **Step 2: Update template section**

Replace the template section of `interview-frontend/src/pages/ResumePage.vue`:

```vue
<template>
  <Spin :spinning="loading">
    <div style="display: flex; flex-direction: column; gap: 24px">
      <h1 class="page-title-animated">简历分析</h1>
      
      <Row :gutter="[16, 16]">
        <Col :xs="12" :sm="6">
          <AnimatedStatCard
            :value="stats.total ?? 0"
            label="总简历数"
            gradient="purple"
            :icon="'📄'"
          />
        </Col>
        <Col :xs="12" :sm="6">
          <AnimatedStatCard
            :value="stats.completed ?? 0"
            label="已完成"
            gradient="green"
            :icon="'✓'"
          />
        </Col>
        <Col :xs="12" :sm="6">
          <AnimatedStatCard
            :value="stats.failed ?? 0"
            label="失败"
            gradient="orange"
            :icon="'⚠'"
          />
        </Col>
        <Col :xs="12" :sm="6">
          <AnimatedStatCard
            :value="weekCount"
            label="本周新增"
            gradient="blue"
            :icon="'📅'"
          />
        </Col>
      </Row>

      <Card class="glass-section-card" title="简历上传" style="border: none">
        <UploadDragger :custom-request="customUpload" :show-upload-list="false" :disabled="uploading">
          <p><InboxOutlined style="font-size: 48px; color: #6366f1" /></p>
          <p style="font-size: 16px; font-weight: 500">点击或拖拽简历文件到此区域上传</p>
          <p style="color: var(--text-secondary); font-size: 13px">支持 PDF、Word、图片格式</p>
        </UploadDragger>
      </Card>

      <Row :gutter="[16, 16]">
        <Col :xs="24" :lg="14">
          <Card class="glass-section-card" title="分析队列" style="border: none">
            <Table :columns="columns" :data-source="resumes" row-key="id" :pagination="false" size="middle">
              <template #emptyText><Empty description="暂无简历数据" /></template>
            </Table>
          </Card>
        </Col>
        <Col :xs="24" :lg="10">
          <Card class="glass-section-card" title="最新分析结果" v-if="selectedResume" style="border: none">
            <Descriptions :column="1" size="small" bordered>
              <DescriptionsItem label="候选人">{{ selectedResume.candidateName }}</DescriptionsItem>
              <DescriptionsItem label="目标岗位">{{ selectedResume.targetPosition }}</DescriptionsItem>
              <DescriptionsItem label="更新时间">{{ formatDateTime(selectedResume.updatedAt) }}</DescriptionsItem>
              <DescriptionsItem label="分析摘要">{{ selectedResume.analysisSummary }}</DescriptionsItem>
            </Descriptions>
            <div style="margin-top: 16px">
              <TypographyTitle :level="5">优势亮点</TypographyTitle>
              <div style="display: flex; flex-wrap: wrap; gap: 6px">
                <Tag v-for="item in selectedResume.strengths" :key="item" color="green">{{ item }}</Tag>
              </div>
            </div>
            <div style="margin-top: 12px">
              <TypographyTitle :level="5">风险提示</TypographyTitle>
              <div style="display: flex; flex-wrap: wrap; gap: 6px">
                <Tag v-for="item in selectedResume.risks" :key="item" color="red">{{ item }}</Tag>
              </div>
            </div>
          </Card>
          <Card v-else class="glass-section-card" style="border: none"><Empty description="暂无分析结果" /></Card>
        </Col>
      </Row>
    </div>
  </Spin>
</template>
```

- [ ] **Step 3: Commit ResumePage changes**

```bash
git add interview-frontend/src/pages/ResumePage.vue
git commit -m "feat: upgrade ResumePage with animated stat cards and glass styling"
```

---

### Task 5: Upgrade VoiceInterviewPage.vue

**Files:**
- Modify: `interview-frontend/src/pages/VoiceInterviewPage.vue`

- [ ] **Step 1: Update script section**

Replace the script section of `interview-frontend/src/pages/VoiceInterviewPage.vue`:

```vue
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
  AudioOutlined,
  PauseCircleOutlined,
  PlayCircleOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, VoiceInterviewSession } from '../types'
import { translateVoiceEvent } from '../utils/display'
import AnimatedStatCard from '../components/AnimatedStatCard.vue'

const sessions = ref<VoiceInterviewSession[]>([])
const sessionId = ref('')
const transcript = ref('')
const events = ref<string[]>([])
let socket: WebSocket | null = null

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

onMounted(load)
</script>
```

- [ ] **Step 2: Update template section**

Replace the template section of `interview-frontend/src/pages/VoiceInterviewPage.vue`:

```vue
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
        <Button type="primary" @click="createSession" style="border-radius: 10px">
          <template #icon><PlusOutlined /></template>
          创建会话
        </Button>
        <Button @click="connect" style="border-radius: 10px">
          <template #icon><LinkOutlined /></template>
          连接 WebSocket
        </Button>
        <Button @click="pause" style="border-radius: 10px">
          <template #icon><PauseOutlined /></template>
          暂停
        </Button>
        <Button @click="resume" style="border-radius: 10px">
          <template #icon><CaretRightOutlined /></template>
          恢复
        </Button>
        <Button @click="downloadReport" style="border-radius: 10px">
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
        <Card class="glass-section-card" title="手动提交转写" style="border: none">
          <a-textarea v-model:value="transcript" :rows="4" placeholder="输入识别后的语音文本或模拟字幕" style="margin-bottom: 12px; border-radius: 12px" />
          <Button type="primary" @click="sendTranscript" style="border-radius: 10px">提交文本</Button>

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
```

- [ ] **Step 3: Commit VoiceInterviewPage changes**

```bash
git add interview-frontend/src/pages/VoiceInterviewPage.vue
git commit -m "feat: upgrade VoiceInterviewPage with animated stat cards and glass styling"
```

---

### Task 6: Upgrade SchedulePage.vue

**Files:**
- Modify: `interview-frontend/src/pages/SchedulePage.vue`

- [ ] **Step 1: Update script section**

Replace the script section of `interview-frontend/src/pages/SchedulePage.vue`:

```vue
<script setup lang="ts">
import { computed, onMounted, ref, h } from 'vue'
import {
  Row, Col, Card, Table, Tag, Button, ButtonGroup,
  Collapse, CollapsePanel, List, ListItem, Textarea, Empty, message,
} from 'ant-design-vue'
import {
  ThunderboltOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, InterviewSchedule } from '../types'
import { formatDateTime, formatScheduleStatus } from '../utils/display'
import AnimatedStatCard from '../components/AnimatedStatCard.vue'

const schedules = ref<InterviewSchedule[]>([])
const overview = ref<Record<string, number>>({})
const calendar = ref<Record<string, InterviewSchedule[]>>({})
const invitation = ref('')

const monthCount = computed(() => {
  const now = new Date()
  const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
  return schedules.value.filter(s => new Date(s.startTime) >= monthStart).length
})

const load = async () => {
  const [scheduleRes, overviewRes, calendarRes] = await Promise.all([
    http.get<ApiResponse<InterviewSchedule[]>>('/interview-schedules'),
    http.get<ApiResponse<Record<string, number>>>('/interview-schedules/overview'),
    http.get<ApiResponse<Record<string, InterviewSchedule[]>>>('/interview-schedules/calendar'),
  ])
  schedules.value = scheduleRes.data.data
  overview.value = overviewRes.data.data
  calendar.value = calendarRes.data.data
}

const parseInvitation = async () => {
  if (!invitation.value.trim()) return
  await http.post('/interview-schedules/parse', { content: invitation.value })
  invitation.value = ''
  message.success('邀请解析成功')
  await load()
}

const updateStatus = async (id: number, status: string) => {
  await http.post(`/interview-schedules/${id}/status`, { status })
  message.success('状态已更新')
  await load()
}

const columns = [
  { title: '公司/岗位', key: 'company', customRender: ({ record }: { record: InterviewSchedule }) => `${record.company} / ${record.position}` },
  { title: '来源', dataIndex: 'source', key: 'source' },
  { title: '时间', key: 'time', customRender: ({ record }: { record: InterviewSchedule }) => formatDateTime(record.startTime) },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    customRender: ({ text }: { text: string }) => {
      const colorMap: Record<string, string> = {
        PENDING: 'processing',
        COMPLETED: 'success',
        CANCELED: 'default',
        EXPIRED: 'warning',
      }
      return h(Tag, { color: colorMap[text] ?? 'default' }, () => formatScheduleStatus(text))
    },
  },
  { title: '会议链接', dataIndex: 'meetingLink', key: 'meetingLink', ellipsis: true },
  {
    title: '操作',
    key: 'action',
    customRender: ({ record }: { record: InterviewSchedule }) => {
      return h(ButtonGroup, {}, () => [
        h(Button, { size: 'small', onClick: () => updateStatus(record.id, 'PENDING') }, () => '待面试'),
        h(Button, { size: 'small', onClick: () => updateStatus(record.id, 'COMPLETED') }, () => '完成'),
        h(Button, { size: 'small', danger: true, onClick: () => updateStatus(record.id, 'CANCELED') }, () => '取消'),
      ])
    },
  },
]

onMounted(load)
</script>
```

- [ ] **Step 2: Update template section**

Replace the template section of `interview-frontend/src/pages/SchedulePage.vue`:

```vue
<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <h1 class="page-title-animated">面试安排</h1>
    
    <Row :gutter="[16, 16]">
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="overview.total ?? 0"
          label="总数"
          gradient="purple"
          :icon="'📋'"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="overview.completed ?? 0"
          label="已完成"
          gradient="green"
          :icon="'✓'"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="overview.pending ?? 0"
          label="待面试"
          gradient="orange"
          :icon="'🕐'"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="monthCount"
          label="本月安排"
          gradient="blue"
          :icon="'📅'"
        />
      </Col>
    </Row>

    <Card class="glass-section-card" title="邀请解析" style="border: none">
      <a-textarea v-model:value="invitation" :rows="4" placeholder="粘贴飞书、腾讯会议、Zoom 或邮件中的面试邀请内容" style="margin-bottom: 12px; border-radius: 12px" />
      <Button type="primary" @click="parseInvitation" style="border-radius: 10px">
        <template #icon><ThunderboltOutlined /></template>
        解析邀请
      </Button>
    </Card>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="14">
        <Card class="glass-section-card" title="安排列表" style="border: none">
          <Table :columns="columns" :data-source="schedules" row-key="id" :pagination="false" size="middle">
            <template #emptyText><Empty description="暂无面试安排" /></template>
          </Table>
        </Card>
      </Col>
      <Col :xs="24" :lg="10">
        <Card class="glass-section-card" title="按日期查看" style="border: none">
          <Collapse v-if="Object.keys(calendar).length" accordion>
            <CollapsePanel v-for="(items, day) in calendar" :key="day" :header="`${day} (${items.length} 场)`">
              <List :data-source="items" size="small">
                <template #renderItem="{ item }">
                  <ListItem>
                    {{ item.company }} / {{ item.position }} / {{ formatDateTime(item.startTime) }}
                  </ListItem>
                </template>
              </List>
            </CollapsePanel>
          </Collapse>
          <Empty v-else description="暂无日历数据" />
        </Card>
      </Col>
    </Row>
  </div>
</template>
```

- [ ] **Step 3: Commit SchedulePage changes**

```bash
git add interview-frontend/src/pages/SchedulePage.vue
git commit -m "feat: upgrade SchedulePage with animated stat cards and glass styling"
```

---

### Task 7: Upgrade KnowledgePage.vue

**Files:**
- Modify: `interview-frontend/src/pages/KnowledgePage.vue`

- [ ] **Step 1: Update script section**

Replace the script section of `interview-frontend/src/pages/KnowledgePage.vue`:

```vue
<script setup lang="ts">
import { computed, onMounted, ref, h } from 'vue'
import {
  Row, Col, Card, Table, Tag,
  List, ListItem, Textarea, Empty, message, TypographyParagraph, TypographyTitle, Upload, UploadDragger, Button,
} from 'ant-design-vue'
import {
  InboxOutlined,
  SendOutlined,
  DownloadOutlined,
  BookOutlined,
  AppstoreOutlined,
  MessageOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, KnowledgeDocument } from '../types'
import { translateKnowledgeLine } from '../utils/display'
import AnimatedStatCard from '../components/AnimatedStatCard.vue'

const docs = ref<KnowledgeDocument[]>([])
const stats = ref<Record<string, number>>({})
const question = ref('')
const streamOutput = ref<string[]>([])
const uploading = ref(false)

const categoryCount = computed(() => {
  const categories = new Set(docs.value.flatMap(d => d.keywords ?? []))
  return categories.size
})

const lastUpdate = computed(() => {
  if (docs.value.length === 0) return '无'
  const latest = docs.value.reduce((a, b) => 
    new Date(a.updatedAt) > new Date(b.updatedAt) ? a : b
  )
  return new Date(latest.updatedAt).toLocaleDateString()
})

const load = async () => {
  const [docRes, statsRes] = await Promise.all([
    http.get<ApiResponse<KnowledgeDocument[]>>('/knowledge-bases'),
    http.get<ApiResponse<Record<string, number>>>('/knowledge-bases/stats'),
  ])
  docs.value = docRes.data.data
  stats.value = statsRes.data.data
}

const customUpload = async (options: any) => {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    await http.post('/knowledge-bases', formData)
    message.success('上传成功')
    await load()
    options.onSuccess()
  } catch {
    options.onError()
  } finally {
    uploading.value = false
  }
}

const ask = () => {
  streamOutput.value = []
  const source = new EventSource(
    `/api/knowledge-bases/chat?question=${encodeURIComponent(question.value)}`
  )
  source.onmessage = (event) => {
    streamOutput.value.push(translateKnowledgeLine(event.data))
  }
  source.onerror = () => source.close()
  message.info('问答已开始')
}

const download = (id: number) => {
  window.open(`/api/knowledge-bases/${id}/download`, '_blank')
}

const columns = [
  { title: '文件名', dataIndex: 'fileName', key: 'fileName' },
  { title: '摘要', dataIndex: 'summary', key: 'summary', ellipsis: true },
  {
    title: '分块数',
    dataIndex: 'chunks',
    key: 'chunks',
    customRender: ({ text }: { text: number }) => h(Tag, { color: 'blue' }, () => `${text} 个分块`),
  },
  { title: 'Token 估算', dataIndex: 'tokenEstimate', key: 'tokenEstimate' },
  {
    title: '关键词',
    dataIndex: 'keywords',
    key: 'keywords',
    customRender: ({ text }: { text: string[] }) => {
      return h('div', { style: 'display: flex; gap: 4px; flex-wrap: wrap' },
        text.map((k: string) => h(Tag, { key: k }, () => k))
      )
    },
  },
  {
    title: '操作',
    key: 'action',
    customRender: ({ record }: { record: KnowledgeDocument }) => {
      return h(Button, { type: 'link', size: 'small', onClick: () => download(record.id) }, () => [h(DownloadOutlined), ' 下载'])
    },
  },
]

onMounted(load)
</script>
```

- [ ] **Step 2: Update template section**

Replace the template section of `interview-frontend/src/pages/KnowledgePage.vue`:

```vue
<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <h1 class="page-title-animated">知识库</h1>
    
    <Row :gutter="[16, 16]">
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="stats.totalDocuments ?? 0"
          label="文档数"
          gradient="purple"
          :icon="'📚'"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="categoryCount"
          label="分类数"
          gradient="green"
          :icon="'📁'"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="streamOutput.length"
          label="问答次数"
          gradient="orange"
          :icon="'💬'"
        />
      </Col>
      <Col :xs="12" :sm="6">
        <AnimatedStatCard
          :value="stats.totalTokens ?? 0"
          label="Token 估算"
          gradient="blue"
          :icon="'🔢'"
        />
      </Col>
    </Row>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="12">
        <Card class="glass-section-card" title="上传资料" style="border: none">
          <UploadDragger :custom-request="customUpload" :show-upload-list="false" :disabled="uploading">
            <p><InboxOutlined style="font-size: 48px; color: #6366f1" /></p>
            <p style="font-size: 16px; font-weight: 500">点击或拖拽文件到此区域上传</p>
            <p style="color: var(--text-secondary); font-size: 13px">支持 PDF、Word、Markdown 格式</p>
          </UploadDragger>
        </Card>
      </Col>
      <Col :xs="24" :lg="12">
        <Card class="glass-section-card" title="发起提问" style="border: none">
          <a-textarea v-model:value="question" :rows="4" placeholder="输入一个与知识库相关的问题" style="margin-bottom: 12px; border-radius: 12px" />
          <Button type="primary" @click="ask" style="border-radius: 10px">
            <template #icon><SendOutlined /></template>
            开始流式问答
          </Button>
          <div v-if="streamOutput.length" style="margin-top: 16px; padding: 16px; background: rgba(99, 102, 241, 0.05); border-radius: 12px; max-height: 200px; overflow: auto; border: 1px solid rgba(99, 102, 241, 0.1)">
            <TypographyParagraph v-for="(line, i) in streamOutput" :key="i" style="margin: 4px 0">{{ line }}</TypographyParagraph>
          </div>
        </Card>
      </Col>
    </Row>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="14">
        <Card class="glass-section-card" title="资料列表" style="border: none">
          <Table :columns="columns" :data-source="docs" row-key="id" :pagination="false" size="middle">
            <template #emptyText><Empty description="暂无资料" /></template>
          </Table>
        </Card>
      </Col>
      <Col :xs="24" :lg="10">
        <Card class="glass-section-card" title="问答记录" style="border: none">
          <List v-if="streamOutput.length > 0" :data-source="streamOutput" size="small">
            <template #renderItem="{ item }">
              <ListItem>{{ item }}</ListItem>
            </template>
          </List>
          <Empty v-else description="暂无问答记录" />
        </Card>
      </Col>
    </Row>
  </div>
</template>
```

- [ ] **Step 3: Commit KnowledgePage changes**

```bash
git add interview-frontend/src/pages/KnowledgePage.vue
git commit -m "feat: upgrade KnowledgePage with animated stat cards and glass styling"
```

---

### Task 8: Final Commit and Verification

**Files:**
- None (verification task)

- [ ] **Step 1: Verify all changes are committed**

```bash
git status
```

Expected: Clean working tree

- [ ] **Step 2: Create final commit if any remaining changes**

```bash
git add -A && git commit -m "feat: complete UI elevation for all functional pages with glassmorphism aesthetic"
```

---

## Self-Review Checklist

- [x] Spec coverage: All 5 pages covered with animated stats and glass styling
- [x] Placeholder scan: No TBD or TODO items
- [x] Type consistency: AnimatedStatCard props match usage in all pages
