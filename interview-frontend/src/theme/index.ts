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

// Update HTML attribute for CSS variables
const updateThemeAttribute = () => {
  document.documentElement.setAttribute('data-theme', mode.value)
}

// Initialize on load
updateThemeAttribute()

export function useTheme() {
  const isDark = computed(() => mode.value === 'dark')

  const toggleTheme = () => {
    mode.value = mode.value === 'light' ? 'dark' : 'light'
    localStorage.setItem(STORAGE_KEY, mode.value)
    updateThemeAttribute()
  }

  const themeConfig = computed(() => ({
    algorithm: isDark.value ? theme.darkAlgorithm : theme.defaultAlgorithm,
    token: {
      // Primary colors - sophisticated indigo/violet palette
      colorPrimary: '#6366f1',
      colorInfo: '#6366f1',
      colorSuccess: '#10b981',
      colorWarning: '#f59e0b',
      colorError: '#ef4444',

      // Refined border radius
      borderRadius: 12,

      // Enhanced typography
      fontFamily: "'Geist', 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
      fontSize: 14,

      // Rocused backgrounds
      colorBgContainer: isDark.value ? '#1e1f2e' : '#ffffff',
      colorBgLayout: isDark.value ? '#0f0f1a' : '#f8fafc',
      colorBgElevated: isDark.value ? '#252638' : '#ffffff',

      // Border colors
      colorBorder: isDark.value ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)',
      colorBorderSecondary: isDark.value ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.03)',

      // Text colors
      colorText: isDark.value ? '#f1f5f9' : '#1e293b',
      colorTextSecondary: isDark.value ? '#94a3b8' : '#64748b',

      // Shadows
      boxShadow: isDark.value
        ? '0 4px 24px rgba(0, 0, 0, 0.4)'
        : '0 4px 24px rgba(0, 0, 0, 0.08)',
      boxShadowSecondary: isDark.value
        ? '0 8px 32px rgba(0, 0, 0, 0.5)'
        : '0 8px 32px rgba(0, 0, 0, 0.1)',
    },
    components: {
      Card: {
        borderRadiusLG: 20,
        boxShadowTertiary: '0 8px 32px rgba(99, 102, 241, 0.08)',
      },
      Button: {
        borderRadius: 12,
        controlHeight: 44,
        fontWeight: 600,
      },
      Input: {
        borderRadius: 12,
        controlHeight: 44,
      },
      Select: {
        borderRadius: 12,
        controlHeight: 44,
      },
    },
  }))

  return { mode, isDark, toggleTheme, themeConfig }
}
