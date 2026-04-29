<script setup lang="ts">
import { computed, ref, h, onMounted } from 'vue'
import { RouterView, RouterLink, useRoute, useRouter } from 'vue-router'
import {
  Layout,
  LayoutSider,
  LayoutHeader,
  LayoutContent,
  Breadcrumb,
  BreadcrumbItem,
  InputSearch,
  Avatar,
  Dropdown,
  Menu as DropdownMenu,
} from 'ant-design-vue'
import {
  DashboardOutlined,
  FileTextOutlined,
  SolutionOutlined,
  AudioOutlined,
  CalendarOutlined,
  BookOutlined,
  BulbOutlined,
  BulbFilled,
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  LogoutOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue'
import { useTheme } from '../theme'
import { useAuth } from '../stores/auth'
import '../styles/dashboard.css'

const route = useRoute()
const router = useRouter()
const { isDark, toggleTheme } = useTheme()
const auth = useAuth()

const collapsed = ref(false)

onMounted(() => {
  auth.fetchUser()
})

const menuItems = [
  { key: '/', icon: DashboardOutlined, label: '面试中心' },
  { key: '/resumes', icon: FileTextOutlined, label: '简历管理' },
  { key: '/interviews', icon: SolutionOutlined, label: '模拟面试' },
  { key: '/voice-interviews', icon: AudioOutlined, label: '语音面试' },
  { key: '/schedules', icon: CalendarOutlined, label: '面试安排' },
  { key: '/knowledge', icon: BookOutlined, label: '知识库' },
]

const selectedKeys = computed(() => [route.path])
const breadcrumbItems = computed(() => {
  const meta = route.meta as { breadcrumb?: string[] }
  return meta.breadcrumb ?? ['首页']
})

const onSearch = (value: string) => {
  console.log('Search:', value)
}

const handleMenuClick = ({ key }: { key: string | number }) => {
  if (key === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<template>
  <Layout style="min-height: 100vh">
    <LayoutSider
      v-model:collapsed="collapsed"
      collapsible
      :width="220"
      :collapsed-width="64"
      :trigger="null"
      class="sider-custom"
    >
      <div class="sider-brand">
        <RouterLink to="/" class="sider-brand-link">
          <div class="brand-icon">
            <span>IM</span>
          </div>
          <span class="brand-text" :class="{ 'brand-text-hidden': collapsed }">面试管理</span>
        </RouterLink>
      </div>

      <nav class="sider-nav">
        <RouterLink
          v-for="item in menuItems"
          :key="item.key"
          :to="item.key"
          class="nav-item"
          :class="{ 'nav-item-active': selectedKeys.includes(item.key) }"
        >
          <div class="nav-icon">
            <component :is="item.icon" />
          </div>
          <span class="nav-label" :class="{ 'nav-label-hidden': collapsed }">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="sider-footer">
        <span class="sider-version">{{ collapsed ? 'v0.1' : 'v0.1.0' }}</span>
      </div>
    </LayoutSider>

    <Layout class="layout-main">
      <LayoutHeader class="header-custom">
        <div class="header-left">
          <button class="collapse-btn" @click="collapsed = !collapsed">
            <component :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined" />
          </button>
          <Breadcrumb class="header-breadcrumb">
            <BreadcrumbItem v-for="item in breadcrumbItems" :key="item">
              {{ item }}
            </BreadcrumbItem>
          </Breadcrumb>
        </div>

        <div class="header-right">
          <InputSearch
            placeholder="全局搜索..."
            class="header-search"
            @search="onSearch"
          />
          <div class="theme-toggle" @click="toggleTheme">
            <component :is="isDark ? BulbFilled : BulbOutlined" class="theme-icon" />
          </div>
          <Dropdown @click.prevent>
            <div class="user-info">
              <Avatar class="header-avatar" :icon="h(UserOutlined)" />
              <span class="user-name">{{ auth.user.value?.username || '用户' }}</span>
            </div>
            <template #overlay>
              <DropdownMenu @click="handleMenuClick">
                <DropdownMenu.Item key="settings">
                  <SettingOutlined /> 设置
                </DropdownMenu.Item>
                <DropdownMenu.Item key="logout">
                  <LogoutOutlined /> 退出登录
                </DropdownMenu.Item>
              </DropdownMenu>
            </template>
          </Dropdown>
        </div>
      </LayoutHeader>

      <LayoutContent class="content-custom">
        <RouterView />
      </LayoutContent>
    </Layout>
  </Layout>
</template>

<style scoped>
.sider-custom {
  position: sticky !important;
  top: 0;
  height: 100vh;
  overflow: hidden;
  background: var(--color-card) !important;
  border-right: 1px solid var(--color-border);
  transition: width var(--transition-normal);
}

.sider-custom :deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* Brand */
.sider-brand {
  padding: 20px 16px;
  border-bottom: 1px solid var(--color-border);
  min-height: 72px;
  display: flex;
  align-items: center;
}

.sider-brand-link {
  display: flex;
  align-items: center;
  gap: 12px;
  color: inherit;
  text-decoration: none;
  width: 100%;
}

.brand-icon {
  width: 32px;
  height: 32px;
  min-width: 32px;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: white;
}

.brand-text {
  font-family: var(--font-sans);
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  opacity: 1;
  transition: opacity 0.2s ease, width 0.2s ease;
}

.brand-text-hidden {
  opacity: 0;
  width: 0;
}

/* Navigation */
.sider-nav {
  flex: 1;
  padding: 12px 8px;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  margin-bottom: 4px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.nav-item:hover {
  background: var(--color-muted);
  color: var(--text-primary);
}

.nav-item-active {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.nav-item-active:hover {
  background: var(--color-primary-light);
}

.nav-icon {
  width: 20px;
  height: 20px;
  min-width: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.nav-label {
  font-family: var(--font-sans);
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  opacity: 1;
  transition: opacity 0.2s ease;
}

.nav-label-hidden {
  opacity: 0;
  width: 0;
}

/* Footer */
.sider-footer {
  padding: 16px;
  border-top: 1px solid var(--color-border);
  text-align: center;
}

.sider-version {
  font-size: 12px;
  color: var(--text-muted);
}

/* Layout Main */
.layout-main {
  background: var(--color-background) !important;
}

/* Header */
.header-custom {
  background: var(--color-card) !important;
  border-bottom: 1px solid var(--color-border);
  padding: 0 24px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px !important;
  position: sticky;
  top: 0;
  z-index: 100;
}

[data-theme='dark'] .sider-custom {
  background: var(--color-card) !important;
}

[data-theme='dark'] .header-custom {
  background: var(--color-card) !important;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.collapse-btn:hover {
  background: var(--color-muted);
  color: var(--text-primary);
}

.header-breadcrumb {
  font-size: 14px;
}

.header-breadcrumb :deep(.ant-breadcrumb-link) {
  color: var(--text-secondary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-search {
  width: 200px;
}

.header-search :deep(.ant-input) {
  border-radius: var(--radius-md) !important;
  background: var(--color-muted);
  border: 1px solid var(--color-border);
}

.header-search :deep(.ant-input:focus) {
  border-color: var(--color-primary) !important;
}

.theme-toggle {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: transparent;
  border: 1px solid var(--color-border);
  transition: all var(--transition-fast);
}

.theme-toggle:hover {
  background: var(--color-muted);
}

.theme-icon {
  font-size: 16px;
  color: var(--text-secondary);
}

.header-avatar {
  cursor: pointer;
  background: var(--color-primary) !important;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.user-info:hover {
  background: var(--color-muted);
}

.user-name {
  font-family: var(--font-sans);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .user-name {
    display: none;
  }
}

.content-custom {
  padding: 24px !important;
  min-height: 280px;
}

@media (max-width: 768px) {
  .header-search {
    display: none;
  }
  .content-custom {
    padding: 16px !important;
  }
}
</style>
