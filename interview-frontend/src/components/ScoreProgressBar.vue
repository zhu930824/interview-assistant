<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  /** 维度名称 */
  label: string
  /** 当前得分 */
  score: number
  /** 满分 */
  maxScore: number
  /** 颜色主题 */
  color?: 'purple' | 'blue' | 'green' | 'cyan' | 'orange'
}

const props = withDefaults(defineProps<Props>(), {
  color: 'purple'
})

/** 计算百分比 */
const percentage = computed(() => {
  if (props.maxScore <= 0) return 0
  return Math.round((props.score / props.maxScore) * 100)
})

/** 颜色映射 */
const colorMap: Record<string, { bg: string; gradient: string }> = {
  purple: { bg: 'rgba(139, 92, 246, 0.15)', gradient: 'linear-gradient(90deg, #7c3aed 0%, #8b5cf6 100%)' },
  blue: { bg: 'rgba(37, 99, 235, 0.15)', gradient: 'linear-gradient(90deg, #2563eb 0%, #3b82f6 100%)' },
  green: { bg: 'rgba(5, 150, 105, 0.15)', gradient: 'linear-gradient(90deg, #059669 0%, #10b981 100%)' },
  cyan: { bg: 'rgba(6, 182, 212, 0.15)', gradient: 'linear-gradient(90deg, #0891b2 0%, #06b6d4 100%)' },
  orange: { bg: 'rgba(217, 119, 6, 0.15)', gradient: 'linear-gradient(90deg, #d97706 0%, #f59e0b 100%)' }
}

const colorStyle = computed(() => colorMap[props.color] || colorMap.purple)
</script>

<template>
  <div class="score-progress-item">
    <div class="score-progress-header">
      <span class="score-progress-label">{{ label }}</span>
      <span class="score-progress-value">
        <strong>{{ score }}</strong>
        <span class="score-progress-max"> / {{ maxScore }}</span>
      </span>
    </div>
    <div class="score-progress-bar" :style="{ background: colorStyle.bg }">
      <div
        class="score-progress-fill"
        :style="{ width: `${percentage}%`, background: colorStyle.gradient }"
      />
    </div>
  </div>
</template>

<style scoped>
.score-progress-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.score-progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score-progress-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}

.score-progress-value {
  font-size: 13px;
  font-family: 'Geist Mono', 'SF Mono', monospace;
}

.score-progress-value strong {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.score-progress-max {
  color: var(--text-muted);
  font-size: 12px;
}

.score-progress-bar {
  height: 6px;
  border-radius: 3px;
  overflow: hidden;
}

.score-progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.8s ease-out;
}
</style>
