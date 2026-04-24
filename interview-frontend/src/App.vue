<script setup lang="ts">
import { ConfigProvider } from 'ant-design-vue'
import { RouterView } from 'vue-router'
import { useTheme } from './theme'

const { themeConfig } = useTheme()
</script>

<template>
  <ConfigProvider :theme="themeConfig">
    <RouterView />
  </ConfigProvider>
</template>
