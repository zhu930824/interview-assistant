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

    <div class="stat-progress-bar">
      <div
        v-if="progress !== undefined"
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
