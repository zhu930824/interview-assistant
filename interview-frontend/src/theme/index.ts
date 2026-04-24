import { computed, ref } from 'vue'
import { theme } from 'ant-design-vue'

export type ThemeMode = 'light' | 'dark'

const STORAGE_KEY = 'theme-mode'

function getSystemPreference(): ThemeMode {
  if (window.matchMedia?.('(prefers-color-scheme: dark)').matches) {
    return 'dark'
  }
  return 'light'
}

function getStoredMode(): ThemeMode {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored === 'light' || stored === 'dark') return stored
  return getSystemPreference()
}

const mode = ref<ThemeMode>(getStoredMode())

export function useTheme() {
  const isDark = computed(() => mode.value === 'dark')

  const toggleTheme = () => {
    mode.value = mode.value === 'light' ? 'dark' : 'light'
    localStorage.setItem(STORAGE_KEY, mode.value)
  }

  const themeConfig = computed(() => ({
    algorithm: isDark.value ? theme.darkAlgorithm : theme.defaultAlgorithm,
    token: {
      colorPrimary: isDark.value ? '#3b82f6' : '#1677ff',
      colorSuccess: '#52c41a',
      colorWarning: '#faad14',
      colorError: isDark.value ? '#ff8e9f' : '#ff4d4f',
      borderRadius: 8,
    },
  }))

  return { mode, isDark, toggleTheme, themeConfig }
}
