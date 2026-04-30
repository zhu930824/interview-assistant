<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Card, Button, Spin, Skeleton, Descriptions, DescriptionsItem, message, Empty, Popconfirm } from 'ant-design-vue'
import { ArrowLeftOutlined, FileTextOutlined, ClockCircleOutlined, DownloadOutlined, ReloadOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import AnalysisPanel from '../components/AnalysisPanel.vue'
import { http } from '../api/http'
import type { ApiResponse, ResumeDetail, ResumeAnalysis, AnalyzeStatus } from '../types'
import { formatDateTime, formatResumeStatus } from '../utils/display'

const route = useRoute()
const router = useRouter()

const resumeId = computed(() => {
  const id = route.params.id
  const numId = Number(id)
  if (!id || isNaN(numId)) {
    console.error('Invalid resume id:', id)
    return null
  }
  return numId
})

const loading = ref(true)
const resume = ref<ResumeDetail | null>(null)
const exporting = ref(false)
const reanalyzing = ref(false)

// 轮询相关
let pollingTimer: ReturnType<typeof setInterval> | null = null

/** 加载简历详情 */
const loadResume = async (silent = false) => {
  if (!resumeId.value) {
    message.error('简历ID无效')
    router.push('/resumes')
    return
  }
  if (!silent) loading.value = true
  try {
    const res = await http.get<ApiResponse<ResumeDetail>>(`/resumes/${resumeId.value}`)
    resume.value = res.data.data
  } catch {
    if (!silent) {
      message.error('加载简历详情失败')
    }
  } finally {
    if (!silent) loading.value = false
  }
}

/** 轮询检查分析状态 */
const startPolling = () => {
  stopPolling()
  pollingTimer = setInterval(() => {
    const status = resume.value?.status
    if (['UPLOADED', 'PARSING', 'PARSED', 'ANALYZING', 'PENDING'].includes(status || '')) {
      loadResume(true)
    } else {
      stopPolling()
    }
  }, 5000)
}

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

/** 监听状态变化启动/停止轮询 */
watch(() => resume.value?.status, (status) => {
  if (['UPLOADED', 'PARSING', 'PARSED', 'ANALYZING', 'PENDING'].includes(status || '')) {
    startPolling()
  }
})

/** 最新分析结果 */
const latestAnalysis = computed<ResumeAnalysis | undefined>(() => {
  if (!resume.value?.analyses?.length) return undefined
  return resume.value.analyses[0]
})

/** 导出报告 */
const handleExport = async () => {
  exporting.value = true
  try {
    // 使用 window.open 直接下载
    window.open(`/api/resumes/${resumeId.value}/report`, '_blank')
  } catch {
    message.error('导出失败')
  } finally {
    exporting.value = false
  }
}

/** 重新分析 */
const handleReanalyze = async () => {
  reanalyzing.value = true
  try {
    await http.post(`/resumes/${resumeId.value}/retry`)
    message.success('已提交重新分析')
    await loadResume()
  } catch {
    message.error('操作失败')
  } finally {
    reanalyzing.value = false
  }
}

/** 删除简历 */
const handleDelete = async () => {
  try {
    await http.delete(`/resumes/${resumeId.value}`)
    message.success('删除成功')
    router.push('/resumes')
  } catch {
    message.error('删除失败')
  }
}

/** 返回列表 */
const goBack = () => {
  router.push('/resumes')
}

onMounted(() => {
  loadResume()
})
</script>

<template>
  <Spin :spinning="loading">
    <div class="resume-detail-page">
      <!-- 顶部导航栏 -->
      <div class="detail-header">
        <div class="detail-header-left">
          <Button type="text" @click="goBack" class="back-btn">
            <ArrowLeftOutlined />
          </Button>
          <div class="detail-title">
            <h1>
              <FileTextOutlined style="margin-right: 8px" />
              {{ resume?.fileName || '简历详情' }}
            </h1>
            <p v-if="resume">
              <ClockCircleOutlined style="margin-right: 4px" />
              上传于 {{ formatDateTime(resume.createdAt) }}
            </p>
          </div>
        </div>
        <div class="detail-header-right">
          <Button @click="handleExport" :loading="exporting">
            <template #icon><DownloadOutlined /></template>
            {{ exporting ? '导出中...' : '导出报告' }}
          </Button>
          <Popconfirm title="确定删除这份简历？" @confirm="handleDelete">
            <Button danger>
              <template #icon><DeleteOutlined /></template>
              删除
            </Button>
          </Popconfirm>
        </div>
      </div>

      <!-- 简历基本信息 -->
      <Card v-if="resume" class="glass-section-card" :bordered="false" style="margin-bottom: 16px">
        <Descriptions :column="{ xs: 1, sm: 2, md: 4 }" size="small">
          <DescriptionsItem label="候选人">{{ resume.candidateName || '-' }}</DescriptionsItem>
          <DescriptionsItem label="目标岗位">{{ resume.targetPosition || '-' }}</DescriptionsItem>
          <DescriptionsItem label="状态">
            <span :class="`status-${resume.status?.toLowerCase()}`">
              {{ formatResumeStatus(resume.status) }}
            </span>
          </DescriptionsItem>
          <DescriptionsItem label="处理进度">
            <span v-if="resume.status === 'UPLOADED'">已上传，即将解析</span>
            <span v-else-if="resume.status === 'PARSING'">正在解析文档...</span>
            <span v-else-if="resume.status === 'PARSED'">解析完成，等待分析</span>
            <span v-else-if="resume.status === 'ANALYZING'">AI 分析中...</span>
            <span v-else-if="resume.status === 'COMPLETED'">已完成</span>
            <span v-else-if="resume.status === 'PARSE_FAILED'" class="status-failed">解析失败</span>
            <span v-else-if="resume.status === 'ANALYSIS_FAILED'" class="status-failed">分析失败</span>
            <span v-else-if="resume.status === 'FAILED'" class="status-failed">失败</span>
            <span v-else>等待处理</span>
          </DescriptionsItem>
        </Descriptions>
      </Card>

      <!-- 分析结果区域 -->
      <AnalysisPanel
        v-if="resume"
        :analysis="latestAnalysis"
        :analyze-status="resume.status"
        :analyze-error="resume.analyzeError"
        :exporting="exporting"
        :reanalyzing="reanalyzing"
        @export="handleExport"
        @reanalyze="handleReanalyze"
      />

      <Empty v-if="!loading && !resume" description="简历不存在或已被删除" />
    </div>
  </Spin>
</template>

<style scoped>
.resume-detail-page {
  padding: 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.detail-header-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.back-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
}

.detail-title h1 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
}

.detail-title p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 4px 0 0;
}

.detail-header-right {
  display: flex;
  gap: 8px;
}

.status-completed {
  color: var(--color-success);
}

.status-processing, .status-analyzing {
  color: var(--color-primary);
}

.status-failed, .status-error {
  color: var(--color-error);
}

@media (max-width: 600px) {
  .detail-header {
    flex-direction: column;
  }

  .detail-header-right {
    width: 100%;
  }
}
</style>
