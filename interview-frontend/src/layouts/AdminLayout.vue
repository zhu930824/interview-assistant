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
      :width="220"
      :collapsed-width="64"
      :trigger="null"
      style="position: sticky; top: 0; height: 100vh; overflow: auto"
    >
      <div style="padding: 16px; text-align: center">
        <RouterLink to="/" style="color: inherit; text-decoration: none">
          <h2 v-if="!collapsed" style="margin: 0; font-size: 16px; white-space: nowrap">
            面试管理
          </h2>
          <h2 v-else style="margin: 0; font-size: 18px">IM</h2>
        </RouterLink>
      </div>
      <Menu
        mode="inline"
        :selected-keys="selectedKeys"
        theme="dark"
        style="border-inline-end: none"
      >
        <MenuItem v-for="item in menuItems" :key="item.key">
          <RouterLink :to="item.key" style="color: inherit; display: flex; align-items: center; gap: 8px; text-decoration: none">
            <component :is="item.icon" />
            <span v-if="!collapsed">{{ item.label }}</span>
          </RouterLink>
        </MenuItem>
      </Menu>
      <div style="padding: 12px 16px; position: absolute; bottom: 0; width: 100%">
        <TypographyText type="secondary" style="font-size: 12px">
          {{ collapsed ? 'v0.1' : '系统在线 · v0.0.1' }}
        </TypographyText>
      </div>
    </LayoutSider>

    <Layout>
      <LayoutHeader
        style="
          background: transparent;
          padding: 0 24px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          height: 56px;
          line-height: 56px;
          border-bottom: 1px solid var(--ant-color-border-secondary);
          position: sticky;
          top: 0;
          z-index: 10;
        "
      >
        <div style="display: flex; align-items: center; gap: 16px">
          <component
            :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined"
            style="font-size: 18px; cursor: pointer"
            @click="collapsed = !collapsed"
          />
          <Breadcrumb>
            <BreadcrumbItem v-for="item in breadcrumbItems" :key="item">
              {{ item }}
            </BreadcrumbItem>
          </Breadcrumb>
        </div>

        <div style="display: flex; align-items: center; gap: 16px">
          <InputSearch
            placeholder="全局搜索"
            style="width: 220px"
            @search="onSearch"
          />
          <Switch
            :checked="isDark"
            @change="toggleTheme"
          >
            <template #checkedChildren><BulbFilled /></template>
            <template #unCheckedChildren><BulbOutlined /></template>
          </Switch>
          <Dropdown>
            <Avatar style="cursor: pointer" :icon="h(UserOutlined)" />
            <template #overlay>
              <DropdownMenu>
                <DropdownMenu.Item key="settings">设置</DropdownMenu.Item>
                <DropdownMenu.Item key="logout">退出</DropdownMenu.Item>
              </DropdownMenu>
            </template>
          </Dropdown>
        </div>
      </LayoutHeader>

      <LayoutContent style="padding: 24px; min-height: 280px">
        <RouterView />
      </LayoutContent>
    </Layout>
  </Layout>
</template>
