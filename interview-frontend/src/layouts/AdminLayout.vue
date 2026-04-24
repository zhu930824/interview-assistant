<script setup lang="ts">
import { computed, ref, h } from 'vue'
import { RouterView, RouterLink, useRoute } from 'vue-router'
import {
  Layout,
  LayoutSider,
  LayoutHeader,
  LayoutContent,
  Menu,
  MenuItem,
  Breadcrumb,
  BreadcrumbItem,
  InputSearch,
  Switch,
  Avatar,
  Dropdown,
  Menu as DropdownMenu,
  TypographyText,
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
} from '@ant-design/icons-vue'
import { useTheme } from '../theme'
import '../styles/dashboard.css'

const route = useRoute()
const { isDark, toggleTheme } = useTheme()

const collapsed = ref(false)

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
</script>

<template>
  <Layout style="min-height: 100vh">
    <LayoutSider
      v-model:collapsed="collapsed"
      collapsible
      :width="240"
      :collapsed-width="72"
      :trigger="null"
      class="sider-custom"
    >
      <div class="sider-brand">
        <RouterLink to="/" class="sider-brand-link">
          <div class="brand-icon">
            <span>IM</span>
          </div>
          <transition name="fade">
            <span v-if="!collapsed" class="brand-text">面试管理</span>
          </transition>
        </RouterLink>
      </div>

      <Menu
        mode="inline"
        :selected-keys="selectedKeys"
        theme="dark"
        class="sider-menu"
      >
        <MenuItem v-for="item in menuItems" :key="item.key" class="sider-menu-item">
          <RouterLink :to="item.key" class="sider-menu-link">
            <component :is="item.icon" class="sider-menu-icon" />
            <span v-if="!collapsed">{{ item.label }}</span>
          </RouterLink>
        </MenuItem>
      </Menu>

      <div class="sider-footer">
        <TypographyText type="secondary" class="sider-version">
          {{ collapsed ? 'v0.1' : '系统在线 · v0.1.0' }}
        </TypographyText>
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
            placeholder="全局搜索"
            class="header-search"
            @search="onSearch"
          />
          <div class="theme-toggle">
            <Switch
              :checked="isDark"
              @change="toggleTheme"
              class="theme-switch"
            >
              <template #checkedChildren><BulbFilled /></template>
              <template #unCheckedChildren><BulbOutlined /></template>
            </Switch>
          </div>
          <Dropdown>
            <Avatar class="header-avatar" :icon="h(UserOutlined)" />
            <template #overlay>
              <DropdownMenu>
                <DropdownMenu.Item key="settings">设置</DropdownMenu.Item>
                <DropdownMenu.Item key="logout">退出</DropdownMenu.Item>
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
  overflow: auto;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

.sider-custom :deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
}

.sider-brand {
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.sider-brand-link {
  display: flex;
  align-items: center;
  gap: 12px;
  color: inherit;
  text-decoration: none;
}

.brand-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.brand-text {
  font-size: 16px;
  font-weight: 600;
  color: white;
  white-space: nowrap;
}

.sider-menu {
  flex: 1;
  border-inline-end: none !important;
  padding: 8px;
}

.sider-menu-item {
  border-radius: 10px !important;
  margin-bottom: 4px !important;
}

.sider-menu-link {
  color: inherit !important;
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
  text-decoration: none !important;
}

.sider-menu-icon {
  font-size: 18px !important;
}

.sider-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.sider-version {
  font-size: 12px !important;
}

.layout-main {
  background: var(--colorBgLayout, #f8fafc) !important;
}

.header-custom {
  background: rgba(255, 255, 255, 0.8) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 0 28px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px !important;
  line-height: 64px !important;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

[data-theme='dark'] .header-custom {
  background: rgba(15, 15, 26, 0.8) !important;
  border-bottom-color: rgba(255, 255, 255, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: var(--text-primary);
  transition: all 0.2s ease;
}

.collapse-btn:hover {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

[data-theme='dark'] .collapse-btn {
  border-color: rgba(255, 255, 255, 0.08);
}

.header-breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-search {
  width: 240px;
}

.header-search :deep(.ant-input) {
  border-radius: 10px !important;
}

.theme-toggle {
  display: flex;
  align-items: center;
}

.theme-switch {
  background: rgba(99, 102, 241, 0.15) !important;
}

.header-avatar {
  cursor: pointer;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%) !important;
}

.content-custom {
  padding: 32px !important;
  min-height: 280px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .header-search {
    display: none;
  }
  .content-custom {
    padding: 20px !important;
  }
}
</style>
