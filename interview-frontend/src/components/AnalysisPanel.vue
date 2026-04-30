<script setup lang="ts">
import { computed } from 'vue'
import { Card, TypographyTitle, Tag, Button, Spin, Empty } from 'ant-design-vue'
import { DownloadOutlined, ReloadOutlined, CheckCircleOutlined, ExclamationCircleOutlined, ClockCircleOutlined, FileTextOutlined, UploadOutlined } from '@ant-design/icons-vue'
import RadarChart from './RadarChart.vue'
import ScoreProgressBar from './ScoreProgressBar.vue'
import type { ResumeAnalysis, AnalyzeStatus, ResumeSuggestion } from '../types'
import { formatDateTime } from '../utils/display'

interface Props {
  /** 分析结果 */
  analysis?: ResumeAnalysis | null
  /** 分析状态 */
  analyzeStatus?: AnalyzeStatus
  /** 分析错误信息 */
  analyzeError?: string
  /** 是否正在导出 */
  exporting?: boolean
  /** 是否正在重新分析 */
  reanalyzing?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  exporting: false,
  reanalyzing: false
})

const emit = defineEmits<{
  (e: 'export'): void
  (e: 'reanalyze'): void
}>()

/** 按优先级分组建议 */
const suggestionsByPriority = computed(() => {
  if (!props.analysis?.suggestions) return { high: [], medium: [], low: [] }

  const suggestions = props.analysis.suggestions
  return {
    high: suggestions.filter((s: ResumeSuggestion) => s.priority === '高'),
    medium: suggestions.filter((s: ResumeSuggestion) => s.priority === '中'),
    low: suggestions.filter((s: ResumeSuggestion) => s.priority === '低')
  }
})

/** 是否正在解析中 */
const isParsing = computed(() => {
  return props.analyzeStatus === 'UPLOADED' || props.analyzeStatus === 'PARSING'
})

/** 是否正在分析中 */
const isProcessing = computed(() => {
  return props.analyzeStatus === 'PENDING' || props.analyzeStatus === 'ANALYZING' || props.analyzeStatus === 'PARSED'
})

/** 是否解析失败 */
const isParseFailed = computed(() => {
  return props.analyzeStatus === 'PARSE_FAILED'
})

/** 是否分析失败 */
const isAnalysisFailed = computed(() => {
  if (props.analyzeStatus === 'ANALYSIS_FAILED' || props.analyzeStatus === 'FAILED') return true
  if (props.analyzeStatus === 'COMPLETED' && !isAnalysisValid.value) return true
  return false
})

/** 是否显示失败状态 */
const isFailed = computed(() => {
  return isParseFailed.value || isAnalysisFailed.value
})

/** 分析结果是否有效 */
const isAnalysisValid = computed(() => {
  if (!props.analysis) return false
  if (props.analysis.overallScore < 10) return false
  if (!props.analysis.summary) return false
  // 检测错误关键词
  const errorKeywords = ['I/O error', '分析过程中出现错误', '简历分析失败', 'Remote host terminated']
  return !errorKeywords.some(k => props.analysis?.summary?.includes(k))
})

/** 优先级颜色 */
const getPriorityColor = (priority: string) => {
  const colors: Record<string, string> = {
    '高': 'error',
    '中': 'warning',
    '低': 'processing'
  }
  return colors[priority] || 'default'
}

/** 类别颜色 */
const getCategoryColor = (category: string) => {
  const colors: Record<string, string> = {
    '项目': 'purple',
    '技能': 'blue',
    '内容': 'green',
    '格式': 'magenta',
    '结构': 'cyan',
    '表达': 'orange'
  }
  return colors[category] || 'default'
}
</script>

<template>
  <!-- 解析中状态 -->
  <div v-if="isParsing" class="analysis-status-card">
    <div class="analysis-status-icon processing">
      <FileTextOutlined />
    </div>
    <h3>{{ analyzeStatus === 'PARSING' ? '正在解析文档...' : '已上传，即将解析' }}</h3>
    <p>{{ analyzeStatus === 'PARSING' ? '正在提取简历文本内容' : '简历文件已上传成功' }}</p>
    <p class="analysis-status-hint">页面将自动刷新显示进度</p>
  </div>

  <!-- 分析中状态 -->
  <div v-else-if="isProcessing" class="analysis-status-card">
    <div class="analysis-status-icon processing">
      <ClockCircleOutlined />
    </div>
    <h3>{{ analyzeStatus === 'ANALYZING' ? 'AI 正在分析中...' : '等待分析' }}</h3>
    <p>{{ analyzeStatus === 'ANALYZING' ? '请稍候，AI 正在对您的简历进行深度分析' : '简历解析完成，即将开始 AI 分析' }}</p>
    <p class="analysis-status-hint">页面将自动刷新显示分析结果</p>
  </div>

  <!-- 解析失败状态 -->
  <div v-else-if="isParseFailed" class="analysis-status-card">
    <div class="analysis-status-icon failed">
      <ExclamationCircleOutlined />
    </div>
    <h3>文档解析失败</h3>
    <p>无法读取简历内容，请检查文件格式是否正确</p>
    <div v-if="analyzeError" class="analysis-error-box">
      {{ analyzeError }}
    </div>
    <p class="analysis-status-hint">支持 PDF、DOCX、TXT 格式的简历文件</p>
  </div>

  <!-- 分析失败状态 -->
  <div v-else-if="isAnalysisFailed" class="analysis-status-card">
    <div class="analysis-status-icon failed">
      <ExclamationCircleOutlined />
    </div>
    <h3>分析失败</h3>
    <p>AI 服务暂时不可用，请稍后重试</p>
    <div v-if="analyzeError || analysis?.summary" class="analysis-error-box">
      {{ analyzeError || analysis?.summary }}
    </div>
    <Button type="primary" :loading="reanalyzing" @click="emit('reanalyze')">
      <template #icon><ReloadOutlined /></template>
      {{ reanalyzing ? '重新分析中...' : '重新分析' }}
    </Button>
  </div>

  <!-- 分析成功 -->
  <div v-else-if="analysis" class="analysis-result">
    <div class="analysis-grid">
      <!-- 核心评价 -->
      <Card class="glass-section-card analysis-card" :bordered="false">
        <template #title>
          <div class="analysis-card-header">
            <span class="analysis-card-title">
              <CheckCircleOutlined style="margin-right: 8px; color: var(--color-success)" />
              核心评价
            </span>
            <Button type="default" size="small" :loading="exporting" @click="emit('export')">
              <template #icon><DownloadOutlined /></template>
              {{ exporting ? '导出中...' : '导出报告' }}
            </Button>
          </div>
        </template>

        <div class="analysis-summary">
          <p class="analysis-summary-text">{{ analysis.summary }}</p>

          <div class="analysis-score-grid">
            <div class="analysis-score-box">
              <span class="analysis-score-label">总分</span>
              <span class="analysis-score-value">{{ analysis.overallScore }}</span>
              <span class="analysis-score-max">/ 100</span>
            </div>
            <div class="analysis-score-box">
              <span class="analysis-score-label">分析时间</span>
              <span class="analysis-score-time">{{ formatDateTime(analysis.analyzedAt) }}</span>
            </div>
          </div>

          <!-- 优势亮点 -->
          <div v-if="analysis.strengths?.length" class="analysis-strengths">
            <span class="analysis-section-label">优势亮点</span>
            <div class="analysis-tags">
              <Tag v-for="s in analysis.strengths" :key="s" color="success">{{ s }}</Tag>
            </div>
          </div>
        </div>
      </Card>

      <!-- 多维度评分 -->
      <Card class="glass-section-card analysis-card" :bordered="false">
        <template #title>
          <span class="analysis-card-title">
            <CheckCircleOutlined style="margin-right: 8px; color: var(--color-primary)" />
            多维度评分
          </span>
        </template>

        <RadarChart :score-detail="analysis.scoreDetail" :height="280" />

        <div class="analysis-progress-list">
          <ScoreProgressBar
            label="项目经验"
            :score="analysis.scoreDetail.projectScore"
            :max-score="40"
            color="purple"
          />
          <ScoreProgressBar
            label="技能匹配"
            :score="analysis.scoreDetail.skillMatchScore"
            :max-score="20"
            color="blue"
          />
          <ScoreProgressBar
            label="内容完整性"
            :score="analysis.scoreDetail.contentScore"
            :max-score="15"
            color="green"
          />
          <ScoreProgressBar
            label="结构清晰度"
            :score="analysis.scoreDetail.structureScore"
            :max-score="15"
            color="cyan"
          />
          <ScoreProgressBar
            label="表达专业性"
            :score="analysis.scoreDetail.expressionScore"
            :max-score="10"
            color="orange"
          />
        </div>
      </Card>
    </div>

    <!-- 改进建议 -->
    <Card class="glass-section-card analysis-card" :bordered="false" style="margin-top: 16px">
      <template #title>
        <span class="analysis-card-title">
          <CheckCircleOutlined style="margin-right: 8px; color: var(--color-warning)" />
          改进建议
          <span class="analysis-suggestion-count">({{ analysis.suggestions?.length || 0 }} 条)</span>
        </span>
      </template>

      <div v-if="analysis.suggestions?.length" class="analysis-suggestions">
        <!-- 高优先级 -->
        <div v-if="suggestionsByPriority.high.length" class="suggestion-group">
          <div class="suggestion-group-header high">
            <Tag color="error">高优先级</Tag>
            <span class="suggestion-group-count">{{ suggestionsByPriority.high.length }} 条</span>
          </div>
          <div class="suggestion-list">
            <div v-for="(s, i) in suggestionsByPriority.high" :key="`high-${i}`" class="suggestion-card high">
              <div class="suggestion-tags">
                <Tag color="error" size="small">高</Tag>
                <Tag :color="getCategoryColor(s.category)" size="small">{{ s.category }}</Tag>
              </div>
              <p class="suggestion-issue">{{ s.issue }}</p>
              <p class="suggestion-recommendation">{{ s.recommendation }}</p>
            </div>
          </div>
        </div>

        <!-- 中优先级 -->
        <div v-if="suggestionsByPriority.medium.length" class="suggestion-group">
          <div class="suggestion-group-header medium">
            <Tag color="warning">中优先级</Tag>
            <span class="suggestion-group-count">{{ suggestionsByPriority.medium.length }} 条</span>
          </div>
          <div class="suggestion-list">
            <div v-for="(s, i) in suggestionsByPriority.medium" :key="`medium-${i}`" class="suggestion-card medium">
              <div class="suggestion-tags">
                <Tag color="warning" size="small">中</Tag>
                <Tag :color="getCategoryColor(s.category)" size="small">{{ s.category }}</Tag>
              </div>
              <p class="suggestion-issue">{{ s.issue }}</p>
              <p class="suggestion-recommendation">{{ s.recommendation }}</p>
            </div>
          </div>
        </div>

        <!-- 低优先级 -->
        <div v-if="suggestionsByPriority.low.length" class="suggestion-group">
          <div class="suggestion-group-header low">
            <Tag color="processing">低优先级</Tag>
            <span class="suggestion-group-count">{{ suggestionsByPriority.low.length }} 条</span>
          </div>
          <div class="suggestion-list">
            <div v-for="(s, i) in suggestionsByPriority.low" :key="`low-${i}`" class="suggestion-card low">
              <div class="suggestion-tags">
                <Tag color="processing" size="small">低</Tag>
                <Tag :color="getCategoryColor(s.category)" size="small">{{ s.category }}</Tag>
              </div>
              <p class="suggestion-issue">{{ s.issue }}</p>
              <p class="suggestion-recommendation">{{ s.recommendation }}</p>
            </div>
          </div>
        </div>
      </div>

      <Empty v-else description="暂无改进建议" />
    </Card>
  </div>
</template>

<style scoped>
.analysis-status-card {
  background: var(--glass-bg);
  backdrop-filter: blur(16px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  padding: 48px;
  text-align: center;
}

.analysis-status-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.analysis-status-icon.processing {
  background: rgba(99, 102, 241, 0.1);
  color: var(--color-primary);
}

.analysis-status-icon.failed {
  background: rgba(220, 38, 38, 0.1);
  color: var(--color-error);
}

.analysis-status-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.analysis-status-card p {
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.analysis-status-hint {
  font-size: 12px;
  color: var(--text-muted);
}

.analysis-error-box {
  background: rgba(220, 38, 38, 0.08);
  border: 1px solid rgba(220, 38, 38, 0.2);
  border-radius: var(--radius-md);
  padding: 12px;
  margin: 16px 0;
  color: var(--color-error);
  font-size: 13px;
  text-align: left;
}

.analysis-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 900px) {
  .analysis-grid {
    grid-template-columns: 1fr;
  }
}

.analysis-card {
  height: fit-content;
}

.analysis-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.analysis-card-title {
  display: flex;
  align-items: center;
  font-weight: 600;
}

.analysis-suggestion-count {
  font-size: 12px;
  color: var(--text-muted);
  margin-left: 8px;
}

.analysis-summary {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.analysis-summary-text {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-primary);
  margin: 0;
}

.analysis-score-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.analysis-score-box {
  background: linear-gradient(135deg, rgba(5, 150, 105, 0.08) 0%, rgba(16, 185, 129, 0.04) 100%);
  border-radius: var(--radius-md);
  padding: 16px;
}

.analysis-score-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-success);
  margin-bottom: 4px;
}

.analysis-score-value {
  font-size: 32px;
  font-weight: 700;
  font-family: 'Geist Mono', monospace;
  color: var(--text-primary);
}

.analysis-score-max {
  font-size: 14px;
  color: var(--text-muted);
}

.analysis-score-time {
  font-size: 13px;
  color: var(--text-primary);
}

.analysis-section-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-success);
  margin-bottom: 8px;
}

.analysis-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.analysis-progress-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.suggestion-group {
  margin-bottom: 20px;
}

.suggestion-group:last-child {
  margin-bottom: 0;
}

.suggestion-group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}

.suggestion-group-count {
  font-size: 12px;
  color: var(--text-muted);
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.suggestion-card {
  padding: 14px;
  border-radius: var(--radius-md);
  border: 1px solid;
}

.suggestion-card.high {
  background: rgba(220, 38, 38, 0.04);
  border-color: rgba(220, 38, 38, 0.15);
}

.suggestion-card.medium {
  background: rgba(217, 119, 6, 0.04);
  border-color: rgba(217, 119, 6, 0.15);
}

.suggestion-card.low {
  background: rgba(37, 99, 235, 0.04);
  border-color: rgba(37, 99, 235, 0.15);
}

.suggestion-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
}

.suggestion-issue {
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px;
}

.suggestion-recommendation {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}
</style>
