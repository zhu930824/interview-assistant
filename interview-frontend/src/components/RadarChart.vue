<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { ScoreDetail } from '../types'

interface Props {
  /** 评分详情 */
  scoreDetail: ScoreDetail
  /** 图表高度 */
  height?: number
}

const props = withDefaults(defineProps<Props>(), {
  height: 320
})

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

/** 雷达图数据 */
const radarData = computed(() => {
  const { projectScore, skillMatchScore, contentScore, structureScore, expressionScore } = props.scoreDetail
  return [
    { name: '表达专业性', value: expressionScore, max: 10 },
    { name: '技能匹配', value: skillMatchScore, max: 20 },
    { name: '内容完整性', value: contentScore, max: 15 },
    { name: '结构清晰度', value: structureScore, max: 15 },
    { name: '项目经验', value: projectScore, max: 40 }
  ]
})

/** 初始化图表 */
const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

/** 更新图表配置 */
const updateChart = () => {
  if (!chartInstance) return

  const isDark = document.documentElement.getAttribute('data-theme') === 'dark'

  const option: echarts.EChartsOption = {
    radar: {
      indicator: radarData.value.map(item => ({
        name: item.name,
        max: item.max
      })),
      shape: 'polygon',
      splitNumber: 4,
      axisName: {
        color: isDark ? '#94a3b8' : '#64748b',
        fontSize: 12,
        fontWeight: 500
      },
      splitLine: {
        lineStyle: {
          color: isDark ? 'rgba(148, 163, 184, 0.2)' : 'rgba(100, 116, 139, 0.15)'
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: isDark
            ? ['rgba(99, 102, 241, 0.05)', 'rgba(99, 102, 241, 0.1)', 'rgba(99, 102, 241, 0.05)', 'rgba(99, 102, 241, 0.1)']
            : ['rgba(99, 102, 241, 0.02)', 'rgba(99, 102, 241, 0.05)', 'rgba(99, 102, 241, 0.02)', 'rgba(99, 102, 241, 0.05)']
        }
      },
      axisLine: {
        lineStyle: {
          color: isDark ? 'rgba(148, 163, 184, 0.3)' : 'rgba(100, 116, 139, 0.2)'
        }
      }
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: radarData.value.map(item => item.value),
            name: '评分',
            areaStyle: {
              color: 'rgba(99, 102, 241, 0.25)'
            },
            lineStyle: {
              color: '#6366f1',
              width: 2
            },
            itemStyle: {
              color: '#6366f1',
              borderColor: '#fff',
              borderWidth: 2
            },
            symbol: 'circle',
            symbolSize: 8
          }
        ],
        emphasis: {
          lineStyle: {
            width: 3
          }
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

/** 响应窗口大小变化 */
const handleResize = () => {
  chartInstance?.resize()
}

/** 监听数据变化 */
watch(() => props.scoreDetail, () => {
  updateChart()
}, { deep: true })

/** 监听主题变化 */
const observer = new MutationObserver(() => {
  updateChart()
})

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme']
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  observer.disconnect()
  chartInstance?.dispose()
})
</script>

<template>
  <div ref="chartRef" :style="{ width: '100%', height: `${height}px` }" />
</template>
