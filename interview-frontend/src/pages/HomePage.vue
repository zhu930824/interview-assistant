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
  ApiOutlined,
  StarOutlined,
  CrownOutlined,
  BulbOutlined,
  HeartOutlined,
  GlobalOutlined,
  TrophyOutlined,
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

// Animate counters on mount
onMounted(() => {
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
})

const heroStats = [
  { key: 'resumes', label: '本周新增简历', icon: FileAddOutlined, color: '#6366f1' },
  { key: 'interviews', label: '进行中模拟面试', icon: SolutionOutlined, color: '#8b5cf6' },
  { key: 'schedules', label: '待处理面试安排', icon: CalendarOutlined, color: '#ec4899' },
  { key: 'accuracy', label: '知识库命中率', icon: CheckCircleOutlined, color: '#10b981', suffix: '%' },
]

const featureModules = [
  {
    title: '简历管理',
    desc: '智能解析多格式简历，自动提取关键信息，支持去重校验与异步分析。',
    icon: FileAddOutlined,
    link: '/resumes',
    gradient: 'gradient-1'
  },
  {
    title: '模拟面试',
    desc: 'Skill 驱动智能出题，阶段时长联动，追问策略与统一评估体系。',
    icon: RocketOutlined,
    link: '/interviews',
    gradient: 'gradient-2'
  },
  {
    title: '语音面试',
    desc: '实时语音对话，字幕同步回显，支持暂停恢复与手动提交。',
    icon: AudioOutlined,
    link: '/voice-interviews',
    gradient: 'gradient-3'
  },
  {
    title: '面试安排',
    desc: '智能解析邀请内容，日程视图管理，状态流转与提醒通知。',
    icon: CalendarOutlined,
    link: '/schedules',
    gradient: 'gradient-4'
  },
  {
    title: '知识库',
    desc: '文档上传分块向量化，RAG 检索增强，流式问答体验。',
    icon: DatabaseOutlined,
    link: '/knowledge',
    gradient: 'gradient-5'
  },
  {
    title: '运营视图',
    desc: '全链路数据沉淀，统一后台面板，可视化运营洞察。',
    icon: CloudServerOutlined,
    link: '/',
    gradient: 'gradient-6'
  },
]

const chartData = [42, 58, 74, 61, 88, 72, 85, 56, 92, 68, 78, 88]

const trustBadges = [
  { icon: SafetyCertificateOutlined, label: '企业级安全' },
  { icon: GlobalOutlined, label: '全球部署' },
  { icon: TrophyOutlined, label: '行业领先' },
  { icon: HeartOutlined, label: '10,000+ 用户信赖' },
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

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <p class="hero-eyebrow">AI 驱动的面试管理平台</p>
        <h1 class="hero-title">一站式面试流程<br/>智能化管理解决方案</h1>
        <p class="hero-subtitle">
          从简历解析到模拟面试，从语音对话到知识库检索，全面提升招聘效率与候选人体验
        </p>

        <!-- Hero Stats Grid -->
        <div class="hero-stats-grid">
          <div
            v-for="(stat, index) in heroStats"
            :key="stat.key"
            class="hero-stat-card"
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
            <div class="hero-stat-icon" :style="{ background: stat.color }">
              <component :is="stat.icon" />
            </div>
            <div class="hero-stat-value">
              {{ animatedStats[stat.key as keyof typeof animatedStats] }}{{ stat.suffix || '' }}
            </div>
            <div class="hero-stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>

      <!-- Floating decorative elements -->
      <div class="hero-float-1"></div>
      <div class="hero-float-2"></div>
      <div class="hero-float-3"></div>
    </section>

    <!-- Live Data Visualization -->
    <section class="section-spacing">
      <div class="section-header">
        <h2 class="section-title">实时数据概览</h2>
        <p class="section-subtitle">本周面试活动与关键指标</p>
      </div>

      <div class="data-grid">
        <!-- Chart Card -->
        <div class="glass-card chart-card">
          <div class="card-header">
            <h3 class="card-title">面试趋势分析</h3>
            <span class="card-badge">实时更新</span>
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
              <span class="chart-tooltip">{{ height }} 场</span>
            </div>
          </div>
          <div class="chart-labels">
            <span v-for="i in 12" :key="i">{{ i }}周前</span>
          </div>
        </div>

        <!-- Quick Stats -->
        <div class="quick-stats">
          <div class="stat-card stat-info">
            <div class="stat-icon" style="background: rgba(99, 102, 241, 0.1); color: #6366f1;">
              <SolutionOutlined />
            </div>
            <div class="stat-value counter-animated">68</div>
            <div class="stat-title">本周面试总数</div>
          </div>
          <div class="stat-card stat-success">
            <div class="stat-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
              <CheckCircleOutlined />
            </div>
            <div class="stat-value counter-animated">76%</div>
            <div class="stat-title">平均通过率</div>
          </div>
          <div class="stat-card stat-warning">
            <div class="stat-icon" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">
              <ThunderboltOutlined />
            </div>
            <div class="stat-value counter-animated">35</div>
            <div class="stat-title">平均面试时长(分钟)</div>
          </div>
          <div class="stat-card stat-purple">
            <div class="stat-icon" style="background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">
              <StarOutlined />
            </div>
            <div class="stat-value counter-animated">4.8</div>
            <div class="stat-title">用户满意度评分</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Feature Highlights -->
    <section class="section-spacing">
      <div class="section-header">
        <h2 class="section-title">核心功能模块</h2>
        <p class="section-subtitle">覆盖面试全流程的智能化解决方案</p>
      </div>

      <div class="features-grid">
        <RouterLink
          v-for="(feature, index) in featureModules"
          :key="feature.title"
          :to="feature.link"
          class="feature-card"
          :style="{ animationDelay: `${index * 0.08}s` }"
        >
          <div class="feature-icon" :class="feature.gradient">
            <component :is="feature.icon" />
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.desc }}</p>
          <div class="feature-arrow">→</div>
        </RouterLink>
      </div>
    </section>

    <!-- Platform Capabilities -->
    <section class="section-spacing">
      <div class="capabilities-card glass-card">
        <div class="capabilities-content">
          <h2 class="section-title">平台核心能力</h2>
          <p class="section-subtitle">为现代招聘团队打造的智能化基础设施</p>
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
    <section class="trust-section">
      <div
        v-for="badge in trustBadges"
        :key="badge.label"
        class="trust-badge"
      >
        <div class="trust-icon">
          <component :is="badge.icon" />
        </div>
        <span>{{ badge.label }}</span>
      </div>
    </section>
  </div>
</template>

<style scoped>
@import '../styles/dashboard.css';

.dashboard-wrapper {
  position: relative;
  min-height: 100%;
}

/* Hero specific styles */
.hero-eyebrow {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.6);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 16px;
}

.hero-stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 40px;
}

@media (max-width: 900px) {
  .hero-stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 500px) {
  .hero-stats-grid {
    grid-template-columns: 1fr;
  }
}

.hero-stat-card {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
  animation: fadeSlideUp 0.6s ease-out forwards;
  opacity: 0;
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  margin-bottom: 16px;
}

.hero-stat-value {
  font-family: 'Geist Mono', 'SF Mono', ui-monospace, monospace;
  font-size: 32px;
  font-weight: 700;
  color: white;
  line-height: 1;
}

.hero-stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 8px;
}

.hero-float-1,
.hero-float-2,
.hero-float-3 {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  animation: floatParticle 8s ease-in-out infinite;
}

.hero-float-1 {
  width: 300px;
  height: 300px;
  background: rgba(99, 102, 241, 0.3);
  top: 10%;
  right: 10%;
}

.hero-float-2 {
  width: 200px;
  height: 200px;
  background: rgba(139, 92, 246, 0.2);
  bottom: 20%;
  left: 5%;
  animation-delay: -3s;
}

.hero-float-3 {
  width: 150px;
  height: 150px;
  background: rgba(236, 72, 153, 0.2);
  top: 50%;
  right: 25%;
  animation-delay: -5s;
}

@keyframes floatParticle {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -20px) scale(1.1); }
  66% { transform: translate(-20px, 15px) scale(0.9); }
}

/* Section styles */
.section-spacing {
  margin-top: 48px;
}

.section-header {
  margin-bottom: 32px;
}

.section-title {
  font-family: 'Geist', 'SF Pro Display', -apple-system, sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.section-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
}

/* Data visualization */
.data-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

@media (max-width: 900px) {
  .data-grid {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  padding: 32px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-title {
  font-family: 'Geist', 'SF Pro Display', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.card-badge {
  font-size: 12px;
  padding: 4px 12px;
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
  border-radius: 20px;
}

.chart-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  font-size: 11px;
  color: var(--text-secondary);
}

.chart-tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: var(--text-primary);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  opacity: 0;
  transition: opacity 0.2s;
  white-space: nowrap;
}

.chart-bar {
  position: relative;
  cursor: pointer;
}

.chart-bar:hover .chart-tooltip {
  opacity: 1;
}

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

/* Features grid */
.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

@media (max-width: 1000px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
}

.feature-card {
  display: block;
  text-decoration: none;
  animation: fadeSlideUp 0.6s ease-out forwards;
  opacity: 0;
}

.feature-arrow {
  margin-top: 20px;
  font-size: 18px;
  color: var(--accent-primary);
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.feature-card:hover .feature-arrow {
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

@media (max-width: 800px) {
  .capabilities-card {
    flex-direction: column;
    text-align: center;
  }
}

.capabilities-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 24px;
}

.capability-tag {
  padding: 10px 20px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.05) 100%);
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 24px;
  font-size: 14px;
  color: var(--text-primary);
  animation: fadeSlideUp 0.5s ease-out forwards;
  opacity: 0;
}

.capabilities-visual {
  position: relative;
  width: 180px;
  height: 180px;
  flex-shrink: 0;
}

.visual-ring {
  position: absolute;
  border: 2px solid rgba(99, 102, 241, 0.2);
  border-radius: 50%;
  animation: ringPulse 4s ease-in-out infinite;
}

.ring-1 { inset: 0; animation-delay: 0s; }
.ring-2 { inset: 20px; animation-delay: 0.5s; }
.ring-3 { inset: 40px; animation-delay: 1s; }

@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.05); opacity: 1; }
}

.visual-center {
  position: absolute;
  inset: 60px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}
</style>
