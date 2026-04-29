<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useTheme } from '../theme'
import {
  FileAddOutlined,
  SolutionOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  RocketOutlined,
  AudioOutlined,
  DatabaseOutlined,
  CloudServerOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  TeamOutlined,
  StarOutlined,
  BulbOutlined,
  GlobalOutlined,
  TrophyOutlined,
  ArrowRightOutlined,
} from '@ant-design/icons-vue'

const { isDark } = useTheme()

// Animated counter
const animatedStats = ref({
  resumes: 0,
  interviews: 0,
  schedules: 0,
  accuracy: 0
})

const targetStats = {
  resumes: 128,
  interviews: 32,
  schedules: 14,
  accuracy: 92
}

// Real-time data visualization
const liveMetrics = ref({
  activeUsers: 0,
  interviewsToday: 0,
  avgResponseTime: 0,
  successRate: 0
})

// Simulated live data updates
onMounted(() => {
  // Animate hero stats
  const duration = 1500
  const steps = 60
  const interval = duration / steps

  let step = 0
  const timer = setInterval(() => {
    step++
    const progress = step / steps
    const easeOut = 1 - Math.pow(1 - progress, 3)

    animatedStats.value = {
      resumes: Math.round(targetStats.resumes * easeOut),
      interviews: Math.round(targetStats.interviews * easeOut),
      schedules: Math.round(targetStats.schedules * easeOut),
      accuracy: Math.round(targetStats.accuracy * easeOut)
    }

    if (step >= steps) clearInterval(timer)
  }, interval)

  // Simulate live metrics
  liveMetrics.value = {
    activeUsers: 247,
    interviewsToday: 68,
    avgResponseTime: 1.2,
    successRate: 99.9
  }

  // Update live metrics periodically
  setInterval(() => {
    liveMetrics.value.activeUsers += Math.floor(Math.random() * 5) - 2
    liveMetrics.value.interviewsToday += Math.floor(Math.random() * 3)
  }, 5000)
})

const heroStats = [
  { key: 'resumes', label: '本周新增简历', icon: FileAddOutlined, color: '#6366F1', trend: '+12%' },
  { key: 'interviews', label: '进行中模拟面试', icon: SolutionOutlined, color: '#8B5CF6', trend: '+8%' },
  { key: 'schedules', label: '待处理面试安排', icon: CalendarOutlined, color: '#0284C7', trend: '-3%' },
  { key: 'accuracy', label: '知识库命中率', icon: CheckCircleOutlined, color: '#059669', suffix: '%', trend: '+2%' },
]

const featureModules = [
  {
    title: '简历管理',
    desc: '智能解析多格式简历，自动提取关键信息，支持去重校验与异步分析。',
    icon: FileAddOutlined,
    link: '/resumes',
    gradient: 'gradient-1',
    highlight: 'PDF/Word'
  },
  {
    title: '模拟面试',
    desc: 'Skill 驱动智能出题，阶段时长联动，追问策略与统一评估体系。',
    icon: RocketOutlined,
    link: '/interviews',
    gradient: 'gradient-2',
    highlight: 'AI 评估'
  },
  {
    title: '语音面试',
    desc: '实时语音对话，字幕同步回显，支持暂停恢复与手动提交。',
    icon: AudioOutlined,
    link: '/voice-interviews',
    gradient: 'gradient-3',
    highlight: '语音识别'
  },
  {
    title: '面试安排',
    desc: '智能解析邀请内容，日程视图管理，状态流转与提醒通知。',
    icon: CalendarOutlined,
    link: '/schedules',
    gradient: 'gradient-4',
    highlight: '智能日程'
  },
  {
    title: '知识库',
    desc: '文档上传分块向量化，RAG 检索增强，流式问答体验。',
    icon: DatabaseOutlined,
    link: '/knowledge',
    gradient: 'gradient-5',
    highlight: 'RAG'
  },
  {
    title: '运营视图',
    desc: '全链路数据沉淀，统一后台面板，可视化运营洞察。',
    icon: CloudServerOutlined,
    link: '/',
    gradient: 'gradient-6',
    highlight: '数据洞察'
  },
]

// Chart data for visualization
const chartData = [42, 58, 74, 61, 88, 72, 85, 56, 92, 68, 78, 88]
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const trustBadges = [
  { icon: SafetyCertificateOutlined, label: '企业级安全', desc: 'ISO 27001 认证' },
  { icon: GlobalOutlined, label: '全球部署', desc: '多区域数据中心' },
  { icon: TrophyOutlined, label: '行业领先', desc: 'Top 3 招聘工具' },
  { icon: TeamOutlined, label: '10,000+ 企业', desc: '用户信赖之选' },
]

const capabilities = [
  '多格式简历解析',
  'Redis Stream 异步流',
  'Skill 驱动出题',
  '统一评估引擎',
  '实时语音问答',
  'RAG 检索问答',
]
</script>

<template>
  <div class="dashboard-wrapper">
    <!-- Mesh gradient background -->
    <div class="mesh-gradient"></div>

    <!-- Hero Section with Real-Time Data Visualization -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-badge">
          <SafetyCertificateOutlined />
          <span>AI 驱动的面试管理平台</span>
        </div>
        <h1 class="hero-title">一站式面试流程<br/>智能化管理解决方案</h1>
        <p class="hero-subtitle">
          从简历解析到模拟面试，从语音对话到知识库检索，全面提升招聘效率与候选人体验
        </p>
        <div class="hero-cta-group">
          <RouterLink to="/interviews" class="hero-cta-primary">
            <span>立即开始</span>
            <ArrowRightOutlined />
          </RouterLink>
          <RouterLink to="/knowledge" class="hero-cta-secondary">
            了解更多
          </RouterLink>
        </div>
      </div>

      <!-- Real-Time Live Metrics Panel -->
      <div class="hero-live-panel">
        <div class="live-panel-header">
          <div class="live-dot"></div>
          <span>实时数据</span>
        </div>
        <div class="live-metrics">
          <div class="live-metric">
            <div class="live-metric-value">{{ liveMetrics.activeUsers }}</div>
            <div class="live-metric-label">在线用户</div>
          </div>
          <div class="live-metric-divider"></div>
          <div class="live-metric">
            <div class="live-metric-value">{{ liveMetrics.interviewsToday }}</div>
            <div class="live-metric-label">今日面试</div>
          </div>
          <div class="live-metric-divider"></div>
          <div class="live-metric">
            <div class="live-metric-value">{{ liveMetrics.avgResponseTime }}s</div>
            <div class="live-metric-label">响应时间</div>
          </div>
          <div class="live-metric-divider"></div>
          <div class="live-metric">
            <div class="live-metric-value">{{ liveMetrics.successRate }}%</div>
            <div class="live-metric-label">系统可用率</div>
          </div>
        </div>
      </div>

      <!-- Floating decorative elements -->
      <div class="hero-float-1"></div>
      <div class="hero-float-2"></div>
    </section>

    <!-- Hero Stats Grid -->
    <section class="stats-section">
      <div class="hero-stats-grid">
        <div
          v-for="(stat, index) in heroStats"
          :key="stat.key"
          class="hero-stat-card glass-card-enhanced"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <div class="stat-card-header">
            <div class="hero-stat-icon" :style="{ background: `linear-gradient(135deg, ${stat.color} 0%, ${stat.color}99 100%)` }">
              <component :is="stat.icon" />
            </div>
            <div class="stat-trend" :class="stat.trend?.startsWith('+') ? 'positive' : 'negative'">
              {{ stat.trend }}
            </div>
          </div>
          <div class="hero-stat-body">
            <div class="hero-stat-value">
              {{ animatedStats[stat.key as keyof typeof animatedStats] }}{{ stat.suffix || '' }}
            </div>
            <div class="hero-stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-card-glow" :style="{ background: stat.color }"></div>
        </div>
      </div>
    </section>

    <!-- Live Data Visualization -->
    <section class="section-spacing">
      <div class="data-grid">
        <!-- Enhanced Chart Card -->
        <div class="glass-card chart-card">
          <div class="card-header">
            <div class="card-header-left">
              <h3 class="card-title">面试趋势分析</h3>
              <span class="card-badge">
                <div class="badge-dot"></div>
                实时更新
              </span>
            </div>
            <div class="card-actions">
              <button class="chart-filter active">周</button>
              <button class="chart-filter">月</button>
            </div>
          </div>
          <div class="chart-container">
            <div
              v-for="(height, i) in chartData"
              :key="i"
              class="chart-bar"
              :style="{
                height: height + '%',
                '--bar-index': i,
                animationDelay: `${i * 0.05}s`
              }"
            >
              <div class="chart-bar-inner"></div>
              <span class="chart-tooltip">{{ height }} 场面试</span>
            </div>
          </div>
          <div class="chart-labels">
            <span v-for="(day, i) in weekDays" :key="i">{{ day }}</span>
          </div>
        </div>

        <!-- Enhanced Quick Stats -->
        <div class="quick-stats">
          <div class="stat-item glass-card-mini">
            <div class="stat-icon-wrapper gradient-blue">
              <SolutionOutlined />
            </div>
            <div class="stat-content">
              <div class="stat-value counter-animated">68</div>
              <div class="stat-title">本周面试总数</div>
            </div>
            <div class="stat-progress">
              <div class="stat-progress-fill" style="width: 68%"></div>
            </div>
          </div>
          <div class="stat-item glass-card-mini">
            <div class="stat-icon-wrapper gradient-green">
              <CheckCircleOutlined />
            </div>
            <div class="stat-content">
              <div class="stat-value counter-animated">76%</div>
              <div class="stat-title">平均通过率</div>
            </div>
            <div class="stat-progress">
              <div class="stat-progress-fill green" style="width: 76%"></div>
            </div>
          </div>
          <div class="stat-item glass-card-mini">
            <div class="stat-icon-wrapper gradient-orange">
              <ThunderboltOutlined />
            </div>
            <div class="stat-content">
              <div class="stat-value counter-animated">35</div>
              <div class="stat-title">平均面试时长(分钟)</div>
            </div>
            <div class="stat-progress">
              <div class="stat-progress-fill orange" style="width: 45%"></div>
            </div>
          </div>
          <div class="stat-item glass-card-mini">
            <div class="stat-icon-wrapper gradient-purple">
              <StarOutlined />
            </div>
            <div class="stat-content">
              <div class="stat-value counter-animated">4.8</div>
              <div class="stat-title">用户满意度评分</div>
            </div>
            <div class="stat-stars">
              <StarOutlined v-for="i in 5" :key="i" :class="{ filled: i <= 4 }" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Feature Highlights -->
    <section class="section-spacing">
      <div class="features-grid">
        <RouterLink
          v-for="(feature, index) in featureModules"
          :key="feature.title"
          :to="feature.link"
          class="feature-card glass-card-interactive"
          :style="{ animationDelay: `${index * 0.08}s` }"
        >
          <div class="feature-header">
            <div class="feature-icon" :class="feature.gradient">
              <component :is="feature.icon" />
            </div>
            <span class="feature-badge">{{ feature.highlight }}</span>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.desc }}</p>
          <div class="feature-footer">
            <span class="feature-link">立即体验</span>
            <ArrowRightOutlined class="feature-arrow" />
          </div>
        </RouterLink>
      </div>
    </section>

    <!-- Platform Capabilities -->
    <section class="section-spacing">
      <div class="capabilities-card glass-card">
        <div class="capabilities-content">
          <div class="capabilities-tags">
            <span
              v-for="(cap, i) in capabilities"
              :key="cap"
              class="capability-tag"
              :style="{ animationDelay: `${i * 0.05}s` }"
            >
              {{ cap }}
            </span>
          </div>
        </div>
        <div class="capabilities-visual">
          <div class="visual-ring ring-1"></div>
          <div class="visual-ring ring-2"></div>
          <div class="visual-ring ring-3"></div>
          <div class="visual-center">
            <BulbOutlined />
          </div>
        </div>
      </div>
    </section>

    <!-- Trust Badges -->
    <section class="trust-section-enhanced">
      <div
        v-for="badge in trustBadges"
        :key="badge.label"
        class="trust-card glass-card-mini"
      >
        <div class="trust-icon">
          <component :is="badge.icon" />
        </div>
        <div class="trust-content">
          <span class="trust-label">{{ badge.label }}</span>
          <span class="trust-desc">{{ badge.desc }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
@import '../styles/dashboard.css';

.dashboard-wrapper {
  position: relative;
  min-height: 100%;
  padding-bottom: 48px;
}

/* Hero Section Enhanced */
.hero-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 40px;
  position: relative;
}

@media (max-width: 1024px) {
  .hero-section {
    flex-direction: column;
  }
}

.hero-content {
  flex: 1;
  max-width: 600px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(37, 99, 235, 0.15);
  border: 1px solid rgba(37, 99, 235, 0.3);
  border-radius: var(--radius-xl);
  font-size: 13px;
  font-weight: 500;
  color: #60a5fa;
  margin-bottom: 24px;
}

.hero-title {
  font-family: 'Geist', 'SF Pro Display', -apple-system, sans-serif;
  font-size: 48px;
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1.15;
  color: #ffffff;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #a5b4fc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-family: 'Inter', -apple-system, sans-serif;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.75);
  line-height: 1.7;
  margin-bottom: 32px;
  max-width: 520px;
}

.hero-cta-group {
  display: flex;
  gap: var(--space-4, 16px);
  flex-wrap: wrap;
}

.hero-cta-primary {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2, 8px);
  padding: var(--space-3, 12px) var(--space-6, 24px);
  background: var(--color-primary, #6366F1);
  border-radius: var(--radius-md, 8px);
  color: white;
  font-weight: 500;
  font-size: 14px;
  text-decoration: none;
  transition: all var(--transition-fast, 150ms ease);
}

.hero-cta-primary:hover {
  background: var(--color-primary-hover, #4F46E5);
}

.hero-cta-secondary {
  display: inline-flex;
  align-items: center;
  padding: var(--space-3, 12px) var(--space-6, 24px);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-md, 8px);
  color: white;
  font-weight: 500;
  font-size: 14px;
  text-decoration: none;
  transition: all var(--transition-fast, 150ms ease);
}

.hero-cta-secondary:hover {
  background: rgba(255, 255, 255, 0.15);
}

/* Live Panel */
.hero-live-panel {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg, 12px);
  padding: var(--space-5, 20px);
  min-width: 340px;
}

@media (max-width: 1024px) {
  .hero-live-panel {
    width: 100%;
    min-width: auto;
  }
}

.live-panel-header {
  display: flex;
  align-items: center;
  gap: var(--space-2, 8px);
  margin-bottom: var(--space-4, 16px);
  font-size: 13px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.6);
}

.live-dot {
  width: 6px;
  height: 6px;
  background: #059669;
  border-radius: 50%;
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.1); }
}

.live-metrics {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.live-metric {
  text-align: center;
}

.live-metric-value {
  font-family: var(--font-mono, 'JetBrains Mono', monospace);
  font-size: 22px;
  font-weight: 700;
  color: white;
  line-height: 1;
}

.live-metric-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: var(--space-1, 4px);
}

.live-metric-divider {
  width: 1px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
}

/* Floating elements - minimal */
.hero-float-1,
.hero-float-2 {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  animation: floatParticle 15s ease-in-out infinite;
}

.hero-float-1 {
  width: 300px;
  height: 300px;
  background: rgba(99, 102, 241, 0.15);
  top: -10%;
  right: 5%;
}

.hero-float-2 {
  width: 200px;
  height: 200px;
  background: rgba(5, 150, 105, 0.12);
  bottom: -10%;
  left: 10%;
  animation-delay: -5s;
}

@keyframes floatParticle {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(15px, -10px); }
}

/* Stats Section */
.stats-section {
  margin-top: var(--space-6, 24px);
}

.hero-stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4, 16px);
}

@media (max-width: 1000px) {
  .hero-stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .hero-stats-grid {
    grid-template-columns: 1fr;
  }
}

.glass-card-enhanced {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 24px;
  position: relative;
  overflow: hidden;
  animation: fadeSlideUp 0.6s ease-out forwards;
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-card-enhanced::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
}

.glass-card-enhanced:hover {
  transform: translateY(-6px);
  box-shadow: 0 25px 50px -12px rgba(37, 99, 235, 0.2);
}

.hero-stat-card {
  font-family: -apple-system, BlinkMacSystemFont, sans-serif;
}

.stat-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.hero-stat-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.25);
}

.stat-trend {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: var(--radius-xl);
}

.stat-trend.positive {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.stat-trend.negative {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.hero-stat-body {
  position: relative;
  z-index: 1;
}

.hero-stat-value {
  font-family: var(--font-mono, 'JetBrains Mono', monospace);
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.hero-stat-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.stat-card-glow {
  position: absolute;
  bottom: -40px;
  right: -40px;
  width: 100px;
  height: 100px;
  opacity: 0.08;
  filter: blur(40px);
  border-radius: 50%;
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Section styles - unified spacing */
.section-spacing {
  margin-top: 24px;
}

/* Data visualization */
.data-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

@media (max-width: 1000px) {
  .data-grid {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  padding: 28px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-title {
  font-family: 'Geist', 'SF Pro Display', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.card-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  padding: 6px 12px;
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
  border-radius: var(--radius-xl);
  font-weight: 500;
}

.badge-dot {
  width: 6px;
  height: 6px;
  background: #10b981;
  border-radius: 50%;
  animation: pulse-dot 2s ease-in-out infinite;
}

.card-actions {
  display: flex;
  gap: 4px;
}

.chart-filter {
  padding: 6px 12px;
  font-size: 13px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.chart-filter.active {
  background: var(--glass-bg);
  color: var(--text-primary);
  font-weight: 500;
}

.chart-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 200px;
  padding: 20px 0;
}

.chart-bar {
  flex: 1;
  height: 100%;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chart-bar-inner {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--color-primary, #6366F1);
  border-radius: var(--radius-sm, 6px);
  animation: barGrow 1s ease-out forwards;
  animation-delay: calc(var(--bar-index) * 0.05s);
  transform-origin: bottom;
  height: 100%;
}

@keyframes barGrow {
  from { transform: scaleY(0); opacity: 0; }
  to { transform: scaleY(1); opacity: 1; }
}

.chart-bar:hover .chart-bar-inner {
  filter: brightness(1.1);
}

.chart-tooltip {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%) scale(0.9);
  background: var(--text-primary);
  color: white;
  padding: 6px 12px;
  border-radius: var(--radius-md, 8px);
  font-size: 12px;
  font-weight: 500;
  opacity: 0;
  transition: all var(--transition-fast, 150ms ease);
  white-space: nowrap;
  pointer-events: none;
}

.chart-bar:hover .chart-tooltip {
  opacity: 1;
  transform: translateX(-50%) scale(1);
}

.chart-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

/* Quick Stats Enhanced */
.quick-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

@media (max-width: 600px) {
  .quick-stats {
    grid-template-columns: 1fr;
  }
}

.glass-card-mini {
  background: var(--glass-bg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  padding: 20px;
  transition: all 0.3s ease;
}

.glass-card-mini:hover {
  box-shadow: 0 8px 32px rgba(37, 99, 235, 0.12);
}

.stat-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-family: var(--font-mono, 'JetBrains Mono', monospace);
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.stat-title {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.stat-progress {
  width: 100%;
  height: 3px;
  background: var(--color-muted);
  border-radius: 2px;
  margin-top: 8px;
  overflow: hidden;
}

.stat-progress-fill {
  height: 100%;
  background: var(--color-primary, #6366F1);
  border-radius: 2px;
  transition: width 1s ease-out;
}

.stat-progress-fill.green {
  background: var(--color-success, #059669);
}

.stat-progress-fill.orange {
  background: var(--color-warning, #D97706);
}

.stat-stars {
  display: flex;
  gap: 2px;
  margin-top: var(--space-2, 8px);
}

.stat-stars .anticon {
  font-size: 14px;
  color: rgba(217, 119, 6, 0.3);
}

.stat-stars .anticon.filled {
  color: #D97706;
}

/* Icon wrappers - Flat design */
.stat-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md, 8px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
  flex-shrink: 0;
}

.stat-icon-wrapper.gradient-blue {
  background: #6366F1;
}

.stat-icon-wrapper.gradient-green {
  background: #059669;
}

.stat-icon-wrapper.gradient-orange {
  background: #D97706;
}

.stat-icon-wrapper.gradient-purple {
  background: #8B5CF6;
}

/* Features grid */
.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-5, 20px);
}

@media (max-width: 1100px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 700px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
}

.glass-card-interactive {
  background: var(--glass-bg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 28px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  text-decoration: none;
  display: block;
  animation: fadeSlideUp 0.6s ease-out forwards;
  opacity: 0;
}

.glass-card-interactive::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.08) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.4s ease;
}

.glass-card-interactive:hover::before {
  opacity: 1;
}

.glass-card-interactive:hover {
  transform: translateY(-6px);
  box-shadow: 0 25px 50px -12px rgba(37, 99, 235, 0.2);
  border-color: rgba(37, 99, 235, 0.2);
}

.feature-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md, 8px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

/* Flat design icons - solid colors */
.feature-icon.gradient-1 {
  background: #6366F1;
}

.feature-icon.gradient-2 {
  background: #8B5CF6;
}

.feature-icon.gradient-3 {
  background: #059669;
}

.feature-icon.gradient-4 {
  background: #D97706;
}

.feature-icon.gradient-5 {
  background: #EC4899;
}

.feature-icon.gradient-6 {
  background: #0284C7;
}

.feature-badge {
  font-size: 11px;
  padding: 4px 10px;
  background: var(--color-primary-light, rgba(99, 102, 241, 0.1));
  color: var(--color-primary, #6366F1);
  border-radius: var(--radius-xl);
  font-weight: 500;
}

.feature-title {
  font-family: var(--font-sans, 'Inter', -apple-system, sans-serif);
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.feature-desc {
  font-family: var(--font-sans, 'Inter', -apple-system, sans-serif);
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.feature-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border, #E2E8F0);
}

.feature-link {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-primary, #6366F1);
}

.feature-arrow {
  font-size: 14px;
  color: var(--color-primary, #6366F1);
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.3s ease;
}

.glass-card-interactive:hover .feature-arrow {
  opacity: 1;
  transform: translateX(0);
}

/* Capabilities */
.capabilities-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 48px;
  gap: 48px;
}

@media (max-width: 900px) {
  .capabilities-card {
    flex-direction: column;
    text-align: center;
  }
}

.capabilities-content {
  flex: 1;
}

.capabilities-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

@media (max-width: 600px) {
  .capabilities-card {
    padding: 32px 24px;
  }

  .capabilities-tags {
    justify-content: center;
  }
}

.capability-tag {
  padding: 10px 20px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.08) 0%, rgba(37, 99, 235, 0.02) 100%);
  border: 1px solid rgba(37, 99, 235, 0.15);
  border-radius: var(--radius-xl);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  animation: fadeSlideUp 0.5s ease-out forwards;
  opacity: 0;
}

.capabilities-visual {
  position: relative;
  width: 160px;
  height: 160px;
  flex-shrink: 0;
}

.visual-ring {
  position: absolute;
  border: 2px solid rgba(37, 99, 235, 0.15);
  border-radius: 50%;
  animation: ringPulse 4s ease-in-out infinite;
}

.ring-1 { inset: 0; animation-delay: 0s; }
.ring-2 { inset: 20px; animation-delay: 0.5s; }
.ring-3 { inset: 40px; animation-delay: 1s; }

@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 0.4; }
  50% { transform: scale(1.03); opacity: 0.8; }
}

.visual-center {
  position: absolute;
  inset: 54px;
  background: var(--color-primary, #6366F1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

/* Trust Section */
.trust-section-enhanced {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4, 16px);
  margin-top: var(--space-10, 40px);
}

@media (max-width: 900px) {
  .trust-section-enhanced {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 500px) {
  .trust-section-enhanced {
    grid-template-columns: 1fr;
  }
}

.trust-card {
  display: flex;
  align-items: center;
  gap: var(--space-4, 16px);
  padding: var(--space-4, 16px);
}

.trust-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md, 8px);
  background: var(--color-muted, #F1F5F9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.trust-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.trust-label {
  font-family: 'Geist', 'SF Pro Display', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.trust-desc {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
