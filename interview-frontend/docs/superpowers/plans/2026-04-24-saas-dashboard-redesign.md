# SaaS 仪表盘重设计 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 全量重写面试管理系统前端，使用 Ant Design Vue 组件体系，支持深色/浅色主题切换，SaaS 仪表盘风格。

**Architecture:** 保留 API 层/types/utils 不动，删除手写 CSS 和 PageHeader 组件，新增主题系统和布局组件，重写 App.vue/main.ts 和全部 6 个页面。路由改为嵌套路由，AdminLayout 作为父路由布局。

**Tech Stack:** Vue 3, TypeScript, Vite, ant-design-vue, @ant-design/icons-vue, dayjs, vue-router 4

---

## File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `src/theme/index.ts` | Create | 主题配置：深色/浅色算法 + 自定义 token + 主题切换 composable |
| `src/layouts/AdminLayout.vue` | Create | 全局布局：Sider + Header + Content |
| `src/App.vue` | Rewrite | 仅挂载 ConfigProvider + RouterView |
| `src/main.ts` | Rewrite | 注册 Ant Design + 路由 |
| `src/pages/HomePage.vue` | Rewrite | 首页混合布局仪表盘 |
| `src/pages/ResumePage.vue` | Rewrite | 简历管理页 |
| `src/pages/InterviewPage.vue` | Rewrite | 模拟面试页 |
| `src/pages/VoiceInterviewPage.vue` | Rewrite | 语音面试页 |
| `src/pages/SchedulePage.vue` | Rewrite | 面试安排页 |
| `src/pages/KnowledgePage.vue` | Rewrite | 知识库页 |
| `src/styles.css` | Delete | 手写 CSS 全部移除 |
| `src/components/PageHeader.vue` | Delete | 由 Ant Design 组件替代 |

---

### Task 1: Install Dependencies

**Files:**
- Modify: `package.json`

- [ ] **Step 1: Install ant-design-vue, icons, and dayjs**

```bash
npm install ant-design-vue @ant-design/icons-vue dayjs
```

- [ ] **Step 2: Verify installation**

Run: `npm ls ant-design-vue @ant-design/icons-vue dayjs`
Expected: All three packages listed with versions

- [ ] **Step 3: Commit**

```bash
git add package.json package-lock.json
git commit -m "chore: add ant-design-vue, icons-vue, and dayjs dependencies"
```

---

### Task 2: Create Theme System

**Files:**
- Create: `src/theme/index.ts`

- [ ] **Step 1: Write theme configuration and composable**

```typescript
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
```

- [ ] **Step 2: Verify TypeScript compiles**

Run: `npx vue-tsc --noEmit`
Expected: No errors related to `src/theme/index.ts`

- [ ] **Step 3: Commit**

```bash
git add src/theme/index.ts
git commit -m "feat: add theme system with light/dark mode toggle"
```

---

### Task 3: Rewrite App.vue and main.ts

**Files:**
- Rewrite: `src/App.vue`
- Rewrite: `src/main.ts`

- [ ] **Step 1: Rewrite main.ts**

```typescript
import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from './layouts/AdminLayout.vue'
import HomePage from './pages/HomePage.vue'
import ResumePage from './pages/ResumePage.vue'
import InterviewPage from './pages/InterviewPage.vue'
import VoiceInterviewPage from './pages/VoiceInterviewPage.vue'
import SchedulePage from './pages/SchedulePage.vue'
import KnowledgePage from './pages/KnowledgePage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AdminLayout,
      children: [
        {
          path: '',
          component: HomePage,
          meta: { title: '面试中心', breadcrumb: ['首页'] }
        },
        {
          path: 'resumes',
          component: ResumePage,
          meta: { title: '简历管理', breadcrumb: ['首页', '简历管理'] }
        },
        {
          path: 'interviews',
          component: InterviewPage,
          meta: { title: '模拟面试', breadcrumb: ['首页', '模拟面试'] }
        },
        {
          path: 'voice-interviews',
          component: VoiceInterviewPage,
          meta: { title: '语音面试', breadcrumb: ['首页', '语音面试'] }
        },
        {
          path: 'schedules',
          component: SchedulePage,
          meta: { title: '面试安排', breadcrumb: ['首页', '面试安排'] }
        },
        {
          path: 'knowledge',
          component: KnowledgePage,
          meta: { title: '知识库', breadcrumb: ['首页', '知识库'] }
        }
      ]
    }
  ]
})

const app = createApp(App)
app.use(Antd)
app.use(router)
app.mount('#app')

export { router }
```

- [ ] **Step 2: Rewrite App.vue**

```vue
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
```

- [ ] **Step 3: Delete old styles.css import reference and file**

```bash
rm src/styles.css
rm src/components/PageHeader.vue
```

- [ ] **Step 4: Verify dev server starts**

Run: `npm run dev`
Expected: Vite dev server starts without errors (pages will be blank until AdminLayout exists)

- [ ] **Step 5: Commit**

```bash
git add src/App.vue src/main.ts
git rm src/styles.css src/components/PageHeader.vue
git commit -m "feat: rewrite App.vue and main.ts with Ant Design Vue, remove old CSS and PageHeader"
```

---

### Task 4: Create AdminLayout

**Files:**
- Create: `src/layouts/AdminLayout.vue`

- [ ] **Step 1: Write AdminLayout component**

```vue
<script setup lang="ts">
import { computed, ref } from 'vue'
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
  SearchOutlined,
  SunOutlined,
  MoonOutlined,
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
            <template #checkedChildren><MoonOutlined /></template>
            <template #unCheckedChildren><SunOutlined /></template>
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
```

- [ ] **Step 2: Fix import — add `h` from vue**

Update the `<script setup>` imports to include `h`:

```typescript
import { computed, ref, h } from 'vue'
```

- [ ] **Step 3: Verify dev server renders layout**

Run: `npm run dev`
Expected: Layout renders with sidebar, header, and empty content area. Sidebar navigation works. Theme toggle switches.

- [ ] **Step 4: Commit**

```bash
git add src/layouts/AdminLayout.vue
git commit -m "feat: add AdminLayout with Sider, Header, theme toggle, and breadcrumbs"
```

---

### Task 5: Rewrite HomePage

**Files:**
- Rewrite: `src/pages/HomePage.vue`

- [ ] **Step 1: Write HomePage component**

```vue
<script setup lang="ts">
import { Row, Col, Card, Statistic, List, ListItem, ListItemMeta, Checkbox, Tag, Timeline, TimelineItem, Button, TypographyTitle, TypographyParagraph, TypographyText } from 'ant-design-vue'
import {
  FileAddOutlined,
  SolutionOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  RocketOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  AudioOutlined,
  DatabaseOutlined,
  CloudServerOutlined,
  ApiOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'
import { RouterLink } from 'vue-router'

const overviewStats = [
  { title: '本周新增简历', value: 128, icon: FileAddOutlined, suffix: '' },
  { title: '进行中模拟面试', value: 32, icon: SolutionOutlined, suffix: '' },
  { title: '待处理面试安排', value: 14, icon: CalendarOutlined, suffix: '' },
  { title: '知识库命中率', value: 92, icon: CheckCircleOutlined, suffix: '%' },
]

const todoList = [
  '完成字节专项模拟面试的二轮追问配置',
  '确认今晚 19:30 的 Zoom 技术面链接有效',
  '补充系统设计方向知识库文档并重新向量化',
  '导出候选人王晨的最新模拟面试评估报告',
]

const moduleCards = [
  { title: '简历管理', text: '处理简历上传、内容解析、去重校验、异步分析与 PDF 报告导出。', icon: FileAddOutlined, link: '/resumes' },
  { title: '模拟面试', text: '支持 Skill 驱动出题、阶段时长联动、追问策略和统一评估。', icon: RocketOutlined, link: '/interviews' },
  { title: '语音面试', text: '提供实时语音对话、字幕回显、暂停恢复与手动提交能力。', icon: AudioOutlined, link: '/voice-interviews' },
  { title: '面试安排', text: '解析邀请内容、展示日程视图、管理状态流转和面试提醒。', icon: CalendarOutlined, link: '/schedules' },
  { title: '知识库', text: '完成文档上传、分块向量化、RAG 检索增强和流式问答。', icon: DatabaseOutlined, link: '/knowledge' },
  { title: '运营视图', text: '沉淀简历、面试、安排和知识使用情况，形成统一后台数据面板。', icon: CloudServerOutlined, link: '/' },
]

const capabilities = [
  '多格式简历解析',
  'Redis Stream 异步流',
  'Skill 驱动出题',
  '统一评估引擎',
  '实时语音问答',
  'RAG 检索问答',
]

const workflowSteps = [
  { title: '简历管理', desc: '先在简历管理完成上传与解析，再根据分析结果决定是否进入模拟面试。' },
  { title: '面试训练', desc: '对于重点候选人，优先结合知识库补充岗位材料，再进入文字或语音面试训练。' },
  { title: '安排协同', desc: '最后通过面试安排页统一管理邀请、提醒和状态流转，避免流程散落。' },
]

const pricingPlans = [
  {
    name: '免费版',
    price: '¥0',
    period: '/月',
    features: ['5 份简历分析/月', '3 次模拟面试/月', '基础知识库', '邮件支持'],
    featured: false,
  },
  {
    name: '专业版',
    price: '¥99',
    period: '/月',
    features: ['无限简历分析', '无限模拟面试', '语音面试', 'RAG 流式问答', '优先支持'],
    featured: true,
  },
  {
    name: '企业版',
    price: '¥399',
    period: '/月',
    features: ['全部专业版功能', '团队协作', 'API 接入', '自定义品牌', '专属客户经理'],
    featured: false,
  },
]
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <!-- Hero stats -->
    <Row :gutter="[16, 16]">
      <Col v-for="stat in overviewStats" :key="stat.title" :xs="24" :sm="12" :lg="6">
        <Card hoverable>
          <Statistic :title="stat.title" :value="stat.value" :suffix="stat.suffix">
            <template #prefix>
              <component :is="stat.icon" style="margin-right: 4px" />
            </template>
          </Statistic>
        </Card>
      </Col>
    </Row>

    <!-- Chart + Todo -->
    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="14">
        <Card title="面试趋势">
          <div style="display: flex; align-items: flex-end; gap: 12px; height: 180px; padding: 16px 0">
            <div
              v-for="(h, i) in [42, 60, 74, 56, 88, 68, 80, 52, 90, 65, 78, 85]"
              :key="i"
              style="flex: 1; border-radius: 6px 6px 2px 2px; transition: height 0.3s"
              :style="{
                height: h + '%',
                background: 'linear-gradient(180deg, var(--ant-color-primary), var(--ant-color-primary-bg))',
                opacity: 0.85,
              }"
            />
          </div>
          <Row :gutter="16" style="margin-top: 12px">
            <Col :span="8">
              <Card size="small">
                <Statistic title="本周面试" :value="68" />
              </Card>
            </Col>
            <Col :span="8">
              <Card size="small">
                <Statistic title="通过率" :value="76" suffix="%" />
              </Card>
            </Col>
            <Col :span="8">
              <Card size="small">
                <Statistic title="平均时长" :value="35" suffix="分钟" />
              </Card>
            </Col>
          </Row>
        </Card>
      </Col>
      <Col :xs="24" :lg="10">
        <Card title="今日待办" :extra="Tag">
          <template #extra><Tag>4 项</Tag></template>
          <List :data-source="todoList" size="small">
            <template #renderItem="{ item }">
              <ListItem>
                <ListItemMeta>
                  <template #title>
                    <TypographyText>{{ item }}</TypographyText>
                  </template>
                </ListItemMeta>
              </ListItem>
            </template>
          </List>
        </Card>
      </Col>
    </Row>

    <!-- Feature cards -->
    <Card title="模块入口">
      <Row :gutter="[16, 16]">
        <Col v-for="card in moduleCards" :key="card.title" :xs="24" :sm="12" :lg="8">
          <Card hoverable style="cursor: pointer; height: 100%">
            <RouterLink :to="card.link" style="color: inherit; text-decoration: none">
              <Card.Meta>
                <template #avatar>
                  <component :is="card.icon" style="font-size: 28px; color: var(--ant-color-primary)" />
                </template>
                <template #title>{{ card.title }}</template>
                <template #description>{{ card.text }}</template>
              </Card.Meta>
            </RouterLink>
          </Card>
        </Col>
      </Row>
    </Card>

    <!-- Bottom row: capabilities + workflow -->
    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="12">
        <Card title="平台能力">
          <div style="display: flex; flex-wrap: wrap; gap: 8px">
            <Tag v-for="cap in capabilities" :key="cap" color="blue">{{ cap }}</Tag>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :lg="12">
        <Card title="推荐操作流程">
          <Timeline>
            <TimelineItem v-for="step in workflowSteps" :key="step.title" color="blue">
              <TypographyTitle :level="5" style="margin: 0">{{ step.title }}</TypographyTitle>
              <TypographyParagraph type="secondary" style="margin: 4px 0 0">{{ step.desc }}</TypographyParagraph>
            </TimelineItem>
          </Timeline>
        </Card>
      </Col>
    </Row>

    <!-- Pricing -->
    <Card title="版本定价">
      <Row :gutter="[16, 16]">
        <Col v-for="plan in pricingPlans" :key="plan.name" :xs="24" :sm="8">
          <Card
            :bordered="plan.featured"
            :style="plan.featured ? 'border-color: var(--ant-color-primary); box-shadow: 0 0 0 1px var(--ant-color-primary)' : ''"
          >
            <div v-if="plan.featured" style="margin-bottom: 12px">
              <Tag color="blue">推荐</Tag>
            </div>
            <TypographyTitle :level="3" style="margin: 0">{{ plan.name }}</TypographyTitle>
            <div style="margin: 12px 0">
              <TypographyTitle :level="2" style="margin: 0; display: inline">{{ plan.price }}</TypographyTitle>
              <TypographyText type="secondary">{{ plan.period }}</TypographyText>
            </div>
            <List :data-source="plan.features" size="small" :split="false">
              <template #renderItem="{ item }">
                <ListItem style="padding: 4px 0; border: none">
                  <TypographyText>{{ item }}</TypographyText>
                </ListItem>
              </template>
            </List>
            <Button type="primary" block style="margin-top: 16px" :ghost="!plan.featured">
              {{ plan.featured ? '立即开始' : '选择版本' }}
            </Button>
          </Card>
        </Col>
      </Row>
    </Card>
  </div>
</template>
```

- [ ] **Step 2: Verify HomePage renders**

Run: `npm run dev`
Expected: Homepage shows all 5 sections. Statistics, bar chart, todo list, module cards, capabilities, timeline, pricing all render. Theme toggle works.

- [ ] **Step 3: Commit**

```bash
git add src/pages/HomePage.vue
git commit -m "feat: rewrite HomePage with SaaS dashboard layout, statistics, pricing, and features"
```

---

### Task 6: Rewrite ResumePage

**Files:**
- Rewrite: `src/pages/ResumePage.vue`

- [ ] **Step 1: Write ResumePage component**

```vue
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  Row, Col, Card, Statistic, Upload, UploadDragger, Button, Table, Tag, Progress,
  Descriptions, DescriptionsItem, Popconfirm, Spin, Empty, message,
} from 'ant-design-vue'
import {
  InboxOutlined,
  DownloadOutlined,
  RedoOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, ResumeRecord } from '../types'
import { formatDateTime, formatResumeStatus } from '../utils/display'

const resumes = ref<ResumeRecord[]>([])
const stats = ref<Record<string, number>>({})
const loading = ref(false)
const uploading = ref(false)

const selectedResume = computed(() => resumes.value[0] ?? null)

const load = async () => {
  loading.value = true
  try {
    const [resumeRes, statsRes] = await Promise.all([
      http.get<ApiResponse<ResumeRecord[]>>('/resumes'),
      http.get<ApiResponse<Record<string, number>>>('/resumes/stats'),
    ])
    resumes.value = resumeRes.data.data
    stats.value = statsRes.data.data
  } finally {
    loading.value = false
  }
}

const customUpload = async (options: any) => {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    await http.post('/resumes', formData)
    message.success('上传成功')
    await load()
    options.onSuccess()
  } catch {
    options.onError()
  } finally {
    uploading.value = false
  }
}

const retryResume = async (id: number) => {
  await http.post(`/resumes/${id}/retry`)
  message.success('已重新提交分析')
  await load()
}

const downloadReport = (id: number) => {
  window.open(`/api/resumes/${id}/report`, '_blank')
}

const columns = [
  { title: '文件名', dataIndex: 'fileName', key: 'fileName' },
  { title: '候选人', dataIndex: 'candidateName', key: 'candidateName' },
  { title: '目标岗位', dataIndex: 'targetPosition', key: 'targetPosition' },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    customRender: ({ text }: { text: string }) => {
      const colorMap: Record<string, string> = {
        PENDING: 'default',
        ANALYZING: 'processing',
        COMPLETED: 'success',
        FAILED: 'error',
      }
      return h(Tag, { color: colorMap[text] ?? 'default' }, () => formatResumeStatus(text))
    },
  },
  {
    title: '进度',
    dataIndex: 'progress',
    key: 'progress',
    customRender: ({ text }: { text: number }) => h(Progress, { percent: text, size: 'small' }),
  },
  {
    title: '操作',
    key: 'action',
    customRender: ({ record }: { record: ResumeRecord }) => {
      return h('div', { style: 'display: flex; gap: 8px' }, [
        h(Button, { type: 'link', size: 'small', onClick: () => downloadReport(record.id) }, () => [h(DownloadOutlined), ' 下载']),
        h(Popconfirm, {
          title: '确定重新分析？',
          onConfirm: () => retryResume(record.id),
        }, () => h(Button, { type: 'link', size: 'small', disabled: record.status !== 'FAILED' }, () => [h(RedoOutlined), ' 重试'])),
      ])
    },
  },
]

import { h } from 'vue'

onMounted(load)
</script>

<template>
  <Spin :spinning="loading">
    <div style="display: flex; flex-direction: column; gap: 24px">
      <Row :gutter="[16, 16]">
        <Col :xs="24" :sm="8">
          <Card><Statistic title="总简历数" :value="stats.total ?? 0" /></Card>
        </Col>
        <Col :xs="24" :sm="8">
          <Card><Statistic title="已完成" :value="stats.completed ?? 0" /></Card>
        </Col>
        <Col :xs="24" :sm="8">
          <Card><Statistic title="失败" :value="stats.failed ?? 0" /></Card>
        </Col>
      </Row>

      <Card title="简历上传">
        <UploadDragger :custom-request="customUpload" :show-upload-list="false" :disabled="uploading">
          <p><InboxOutlined style="font-size: 48px; color: var(--ant-color-primary)" /></p>
          <p>点击或拖拽简历文件到此区域上传</p>
        </UploadDragger>
      </Card>

      <Row :gutter="[16, 16]">
        <Col :xs="24" :lg="14">
          <Card title="分析队列">
            <Table :columns="columns" :data-source="resumes" row-key="id" :pagination="false" size="middle">
              <template #emptyText><Empty description="暂无简历数据" /></template>
            </Table>
          </Card>
        </Col>
        <Col :xs="24" :lg="10">
          <Card title="最新分析结果" v-if="selectedResume">
            <Descriptions :column="1" size="small" bordered>
              <DescriptionsItem label="候选人">{{ selectedResume.candidateName }}</DescriptionsItem>
              <DescriptionsItem label="目标岗位">{{ selectedResume.targetPosition }}</DescriptionsItem>
              <DescriptionsItem label="更新时间">{{ formatDateTime(selectedResume.updatedAt) }}</DescriptionsItem>
              <DescriptionsItem label="分析摘要">{{ selectedResume.analysisSummary }}</DescriptionsItem>
            </Descriptions>
            <div style="margin-top: 16px">
              <TypographyTitle :level="5">优势亮点</TypographyTitle>
              <div style="display: flex; flex-wrap: wrap; gap: 6px">
                <Tag v-for="item in selectedResume.strengths" :key="item" color="green">{{ item }}</Tag>
              </div>
            </div>
            <div style="margin-top: 12px">
              <TypographyTitle :level="5">风险提示</TypographyTitle>
              <div style="display: flex; flex-wrap: wrap; gap: 6px">
                <Tag v-for="item in selectedResume.risks" :key="item" color="red">{{ item }}</Tag>
              </div>
            </div>
          </Card>
          <Card v-else><Empty description="暂无分析结果" /></Card>
        </Col>
      </Row>
    </div>
  </Spin>
</template>
```

Note: Add `import { TypographyTitle } from 'ant-design-vue'` to the imports at the top if not already present.

- [ ] **Step 2: Verify ResumePage renders**

Run: `npm run dev`, navigate to `/resumes`
Expected: Stats row, upload dragger, table, and detail card render. Upload triggers API call.

- [ ] **Step 3: Commit**

```bash
git add src/pages/ResumePage.vue
git commit -m "feat: rewrite ResumePage with Ant Design table, upload dragger, and descriptions"
```

---

### Task 7: Rewrite InterviewPage

**Files:**
- Rewrite: `src/pages/InterviewPage.vue`

- [ ] **Step 1: Write InterviewPage component**

```vue
<script setup lang="ts">
import { computed, onMounted, ref, h } from 'vue'
import {
  Row, Col, Card, Statistic, Form, FormItem, Select, SelectOption,
  InputNumber, Button, List, ListItem, Tag, Alert, Space, Spin,
  Empty, message, TypographyParagraph, TypographyTitle,
} from 'ant-design-vue'
import {
  PlusOutlined,
  SendOutlined,
  FileSearchOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, InterviewDirectionOption, InterviewSession } from '../types'
import { formatDirection, formatInterviewStatus, formatStage } from '../utils/display'

const sessions = ref<InterviewSession[]>([])
const directions = ref<InterviewDirectionOption[]>([])
const summary = ref<Record<string, number>>({})
const direction = ref('JAVA_BACKEND')
const totalMinutes = ref(45)
const followUpRounds = ref(1)
const answer = ref('')
const selectedId = ref<number | null>(null)
const creating = ref(false)

const selectedSession = computed(
  () => sessions.value.find((s) => s.id === selectedId.value) ?? sessions.value[0] ?? null
)

const currentQuestion = computed(() =>
  selectedSession.value?.askedQuestions.find(
    (q) => q.id === selectedSession.value?.currentQuestionId
  ) ?? null
)

const load = async () => {
  const [sessionRes, dirRes, centerRes] = await Promise.all([
    http.get<ApiResponse<InterviewSession[]>>('/interviews'),
    http.get<ApiResponse<InterviewDirectionOption[]>>('/interviews/directions'),
    http.get<ApiResponse<{ summary: Record<string, number> }>>('/interviews/center'),
  ])
  sessions.value = sessionRes.data.data
  directions.value = dirRes.data.data
  summary.value = centerRes.data.data.summary
  if (!selectedId.value && sessions.value.length > 0) {
    selectedId.value = sessions.value[0].id
  }
}

const createSession = async () => {
  creating.value = true
  try {
    const res = await http.post<ApiResponse<InterviewSession>>('/interviews', {
      direction: direction.value,
      totalMinutes: totalMinutes.value,
      followUpRounds: followUpRounds.value,
    })
    selectedId.value = res.data.data.id
    message.success('面试会话已创建')
    await load()
  } finally {
    creating.value = false
  }
}

const submitAnswer = async () => {
  if (!selectedSession.value || !answer.value.trim()) return
  const res = await http.post<ApiResponse<InterviewSession>>(
    `/interviews/${selectedSession.value.id}/answer`,
    { answer: answer.value }
  )
  answer.value = ''
  const updated = res.data.data
  sessions.value = sessions.value.map((s) => (s.id === updated.id ? updated : s))
  selectedId.value = updated.id
}

const evaluate = async () => {
  if (!selectedSession.value) return
  const res = await http.post<ApiResponse<InterviewSession>>(
    `/interviews/${selectedSession.value.id}/evaluate`,
    { transcript: selectedSession.value.transcript }
  )
  const updated = res.data.data
  sessions.value = sessions.value.map((s) => (s.id === updated.id ? updated : s))
  message.success('评估结果已生成')
}

const downloadReport = () => {
  if (!selectedSession.value) return
  window.open(`/api/interviews/${selectedSession.value.id}/report`, '_blank')
}

onMounted(load)
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <Row :gutter="[16, 16]">
      <Col :xs="24" :sm="8">
        <Card><Statistic title="总会话数" :value="summary.totalSessions ?? 0" /></Card>
      </Col>
      <Col :xs="24" :sm="8">
        <Card><Statistic title="进行中" :value="summary.activeSessions ?? 0" /></Card>
      </Col>
      <Col :xs="24" :sm="8">
        <Card><Statistic title="已完成" :value="summary.completedSessions ?? 0" /></Card>
      </Col>
    </Row>

    <Card title="创建面试">
      <Form layout="inline">
        <FormItem label="面试方向">
          <Select v-model:value="direction" style="width: 180px">
            <SelectOption v-for="d in directions" :key="d.code" :value="d.code">
              {{ formatDirection(d.code) }}
            </SelectOption>
          </Select>
        </FormItem>
        <FormItem label="总时长(分钟)">
          <InputNumber v-model:value="totalMinutes" :min="10" :max="180" />
        </FormItem>
        <FormItem label="追问轮次">
          <InputNumber v-model:value="followUpRounds" :min="0" :max="5" />
        </FormItem>
        <FormItem>
          <Button type="primary" :loading="creating" @click="createSession">
            <template #icon><PlusOutlined /></template>
            创建面试
          </Button>
        </FormItem>
      </Form>
    </Card>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="10">
        <Card title="会话列表">
          <List :data-source="sessions" size="small">
            <template #renderItem="{ item }">
              <ListItem
                style="cursor: pointer"
                :style="selectedId === item.id ? 'background: var(--ant-color-primary-bg); border-radius: 8px; padding: 8px 12px' : 'padding: 8px 12px'"
                @click="selectedId = item.id"
              >
                <ListItem.Meta>
                  <template #title>
                    <span>{{ formatDirection(item.direction) }}</span>
                  </template>
                  <template #description>
                    <Tag :color="item.status === 'COMPLETED' ? 'success' : 'processing'">
                      {{ formatInterviewStatus(item.status) }}
                    </Tag>
                    <span style="margin-left: 8px">总时长 {{ item.totalMinutes }} 分钟 · 追问 {{ item.followUpRounds }} 轮</span>
                  </template>
                </ListItem.Meta>
              </ListItem>
            </template>
            <template #empty><Empty description="暂无面试会话" /></template>
          </List>
        </Card>
      </Col>

      <Col :xs="24" :lg="14">
        <Card title="面试工作区" v-if="selectedSession">
          <Alert
            v-if="currentQuestion"
            :message="currentQuestion.content"
            type="info"
            show-icon
            style="margin-bottom: 16px"
          />
          <Alert v-else message="所有题目已完成，可以直接生成评估。" type="success" show-icon style="margin-bottom: 16px" />

          <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px">
            <Tag v-for="(minutes, stage) in selectedSession.stageDurations" :key="stage" color="blue">
              {{ formatStage(String(stage)) }} {{ minutes }} 分钟
            </Tag>
          </div>

          <a-textarea
            v-model:value="answer"
            :rows="4"
            placeholder="输入你的回答内容"
            style="margin-bottom: 12px"
          />

          <Space style="margin-bottom: 16px">
            <Button type="primary" :disabled="!currentQuestion" @click="submitAnswer">
              <template #icon><SendOutlined /></template>
              提交回答
            </Button>
            <Button @click="evaluate">
              <template #icon><FileSearchOutlined /></template>
              生成评估
            </Button>
            <Button @click="downloadReport">
              <template #icon><DownloadOutlined /></template>
              下载报告
            </Button>
          </Space>

          <TypographyTitle :level="5}>会话记录</TypographyTitle>
          <TypographyParagraph>
            <pre style="background: var(--ant-color-bg-layout); padding: 12px; border-radius: 8px; overflow: auto; max-height: 200px; white-space: pre-wrap">{{ selectedSession.transcript || '暂无作答记录。' }}</pre>
          </TypographyParagraph>

          <TypographyTitle :level="5}>评估结果</TypographyTitle>
          <TypographyParagraph>
            <pre style="background: var(--ant-color-bg-layout); padding: 12px; border-radius: 8px; overflow: auto; max-height: 200px; white-space: pre-wrap">{{ selectedSession.evaluation || '还没有生成评估结果。' }}</pre>
          </TypographyParagraph>
        </Card>
        <Card v-else><Empty description="请选择或创建一个面试会话" /></Card>
      </Col>
    </Row>
  </div>
</template>
```

- [ ] **Step 2: Verify InterviewPage renders**

Run: `npm run dev`, navigate to `/interviews`
Expected: Stats, form, session list, and workspace render. Form creates sessions.

- [ ] **Step 3: Commit**

```bash
git add src/pages/InterviewPage.vue
git commit -m "feat: rewrite InterviewPage with Ant Design form, list, and workspace"
```

---

### Task 8: Rewrite VoiceInterviewPage

**Files:**
- Rewrite: `src/pages/VoiceInterviewPage.vue`

- [ ] **Step 1: Write VoiceInterviewPage component**

```vue
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  Row, Col, Card, Button, Space, TypographyText, Timeline, TimelineItem,
  List, ListItem, Descriptions, DescriptionsItem, Textarea, Empty, message,
} from 'ant-design-vue'
import {
  PlusOutlined,
  LinkOutlined,
  PauseOutlined,
  CaretRightOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, VoiceInterviewSession } from '../types'
import { translateVoiceEvent } from '../utils/display'

const sessions = ref<VoiceInterviewSession[]>([])
const sessionId = ref('')
const transcript = ref('')
const events = ref<string[]>([])
let socket: WebSocket | null = null

const currentSession = computed(
  () => sessions.value.find((s) => s.sessionId === sessionId.value) ?? null
)

const load = async () => {
  const res = await http.get<ApiResponse<VoiceInterviewSession[]>>('/voice-interviews')
  sessions.value = res.data.data
  if (!sessionId.value && sessions.value.length > 0) {
    sessionId.value = sessions.value[0].sessionId
  }
}

const refreshDetail = async () => {
  if (!sessionId.value) return
  const res = await http.get<ApiResponse<VoiceInterviewSession>>(
    `/voice-interviews/${sessionId.value}`
  )
  const updated = res.data.data
  sessions.value = [
    updated,
    ...sessions.value.filter((s) => s.sessionId !== updated.sessionId),
  ]
}

const createSession = async () => {
  const res = await http.post<ApiResponse<VoiceInterviewSession>>('/voice-interviews')
  sessionId.value = res.data.data.sessionId
  events.value.unshift(`会话已创建：${sessionId.value}`)
  message.success('语音会话已创建')
  await load()
}

const connect = () => {
  if (!sessionId.value) return
  socket?.close()
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
  socket = new WebSocket(
    `${protocol}://${location.host}/ws/voice-interview?sessionId=${sessionId.value}`
  )
  socket.onmessage = (event) => {
    events.value.unshift(translateVoiceEvent(event.data))
    void refreshDetail()
  }
  message.info('WebSocket 已连接')
}

const sendTranscript = async () => {
  if (!sessionId.value || !transcript.value.trim()) return
  await http.post(`/voice-interviews/${sessionId.value}/submit`, { transcript: transcript.value })
  socket?.send(transcript.value)
  transcript.value = ''
  await refreshDetail()
}

const pause = async () => {
  if (!sessionId.value) return
  await http.post(`/voice-interviews/${sessionId.value}/pause`)
  events.value.unshift('会话已暂停')
  await refreshDetail()
}

const resume = async () => {
  if (!sessionId.value) return
  await http.post(`/voice-interviews/${sessionId.value}/resume`)
  events.value.unshift('会话已恢复')
  await refreshDetail()
}

const downloadReport = () => {
  if (!sessionId.value) return
  window.open(`/api/voice-interviews/${sessionId.value}/report`, '_blank')
}

onMounted(load)
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <Card>
      <Space wrap>
        <Button type="primary" @click="createSession">
          <template #icon><PlusOutlined /></template>
          创建会话
        </Button>
        <Button @click="connect">
          <template #icon><LinkOutlined /></template>
          连接 WebSocket
        </Button>
        <Button @click="pause">
          <template #icon><PauseOutlined /></template>
          暂停
        </Button>
        <Button @click="resume">
          <template #icon><CaretRightOutlined /></template>
          恢复
        </Button>
        <Button @click="downloadReport">
          <template #icon><DownloadOutlined /></template>
          下载报告
        </Button>
      </Space>
      <div style="margin-top: 12px">
        <TypographyText type="secondary">当前会话：{{ sessionId || '尚未选择' }}</TypographyText>
      </div>
    </Card>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="12">
        <Card title="手动提交转写">
          <a-textarea v-model:value="transcript" :rows="4" placeholder="输入识别后的语音文本或模拟字幕" style="margin-bottom: 12px" />
          <Button type="primary" @click="sendTranscript">提交文本</Button>

          <div v-if="currentSession" style="margin-top: 16px">
            <Descriptions :column="1" size="small" bordered>
              <DescriptionsItem label="暂停状态">{{ currentSession.paused ? '已暂停' : '进行中' }}</DescriptionsItem>
              <DescriptionsItem label="当前题目">{{ currentSession.currentQuestion }}</DescriptionsItem>
              <DescriptionsItem label="已提交轮次">{{ currentSession.submittedTurns }}</DescriptionsItem>
            </Descriptions>
          </div>
        </Card>
      </Col>

      <Col :xs="24" :lg="12">
        <Card title="实时事件流">
          <Timeline v-if="events.length">
            <TimelineItem v-for="(item, index) in events.slice(0, 20)" :key="index" color="blue">
              {{ item }}
            </TimelineItem>
          </Timeline>
          <Empty v-else description="暂无事件" />
        </Card>
      </Col>
    </Row>

    <Row :gutter="[16, 16]" v-if="currentSession">
      <Col :xs="24" :lg="12">
        <Card title="字幕记录">
          <List :data-source="currentSession.subtitles" size="small">
            <template #renderItem="{ item }">
              <ListItem>{{ item }}</ListItem>
            </template>
            <template #empty><Empty description="暂无字幕" /></template>
          </List>
        </Card>
      </Col>
      <Col :xs="24" :lg="12">
        <Card title="AI 追问">
          <List :data-source="currentSession.aiReplies" size="small">
            <template #renderItem="{ item }">
              <ListItem>{{ item }}</ListItem>
            </template>
            <template #empty><Empty description="暂无追问" /></template>
          </List>
        </Card>
      </Col>
    </Row>
  </div>
</template>
```

- [ ] **Step 2: Verify VoiceInterviewPage renders**

Run: `npm run dev`, navigate to `/voice-interviews`
Expected: Control bar, submit form, event timeline, and subtitle/AI reply lists render.

- [ ] **Step 3: Commit**

```bash
git add src/pages/VoiceInterviewPage.vue
git commit -m "feat: rewrite VoiceInterviewPage with Ant Design timeline, descriptions, and controls"
```

---

### Task 9: Rewrite SchedulePage

**Files:**
- Rewrite: `src/pages/SchedulePage.vue`

- [ ] **Step 1: Write SchedulePage component**

```vue
<script setup lang="ts">
import { onMounted, ref, h } from 'vue'
import {
  Row, Col, Card, Statistic, Table, Tag, Button, ButtonGroup,
  Collapse, CollapsePanel, List, ListItem, Textarea, Empty, message,
} from 'ant-design-vue'
import {
  ThunderboltOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, InterviewSchedule } from '../types'
import { formatDateTime, formatScheduleStatus } from '../utils/display'

const schedules = ref<InterviewSchedule[]>([])
const overview = ref<Record<string, number>>({})
const calendar = ref<Record<string, InterviewSchedule[]>>({})
const invitation = ref('')

const load = async () => {
  const [scheduleRes, overviewRes, calendarRes] = await Promise.all([
    http.get<ApiResponse<InterviewSchedule[]>>('/interview-schedules'),
    http.get<ApiResponse<Record<string, number>>>('/interview-schedules/overview'),
    http.get<ApiResponse<Record<string, InterviewSchedule[]>>>('/interview-schedules/calendar'),
  ])
  schedules.value = scheduleRes.data.data
  overview.value = overviewRes.data.data
  calendar.value = calendarRes.data.data
}

const parseInvitation = async () => {
  if (!invitation.value.trim()) return
  await http.post('/interview-schedules/parse', { content: invitation.value })
  invitation.value = ''
  message.success('邀请解析成功')
  await load()
}

const updateStatus = async (id: number, status: string) => {
  await http.post(`/interview-schedules/${id}/status`, { status })
  message.success('状态已更新')
  await load()
}

const columns = [
  { title: '公司/岗位', key: 'company', customRender: ({ record }: { record: InterviewSchedule }) => `${record.company} / ${record.position}` },
  { title: '来源', dataIndex: 'source', key: 'source' },
  { title: '时间', key: 'time', customRender: ({ record }: { record: InterviewSchedule }) => formatDateTime(record.startTime) },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    customRender: ({ text }: { text: string }) => {
      const colorMap: Record<string, string> = {
        PENDING: 'processing',
        COMPLETED: 'success',
        CANCELED: 'default',
        EXPIRED: 'warning',
      }
      return h(Tag, { color: colorMap[text] ?? 'default' }, () => formatScheduleStatus(text))
    },
  },
  { title: '会议链接', dataIndex: 'meetingLink', key: 'meetingLink', ellipsis: true },
  {
    title: '操作',
    key: 'action',
    customRender: ({ record }: { record: InterviewSchedule }) => {
      return h(ButtonGroup, {}, () => [
        h(Button, { size: 'small', onClick: () => updateStatus(record.id, 'PENDING') }, () => '待面试'),
        h(Button, { size: 'small', onClick: () => updateStatus(record.id, 'COMPLETED') }, () => '完成'),
        h(Button, { size: 'small', danger: true, onClick: () => updateStatus(record.id, 'CANCELED') }, () => '取消'),
      ])
    },
  },
]

onMounted(load)
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <Row :gutter="[16, 16]">
      <Col :xs="12" :sm="6">
        <Card><Statistic title="总数" :value="overview.total ?? 0" /></Card>
      </Col>
      <Col :xs="12" :sm="6">
        <Card><Statistic title="待面试" :value="overview.pending ?? 0" /></Card>
      </Col>
      <Col :xs="12" :sm="6">
        <Card><Statistic title="已完成" :value="overview.completed ?? 0" /></Card>
      </Col>
      <Col :xs="12" :sm="6">
        <Card><Statistic title="已过期" :value="overview.expired ?? 0" /></Card>
      </Col>
    </Row>

    <Card title="邀请解析">
      <a-textarea v-model:value="invitation" :rows="4" placeholder="粘贴飞书、腾讯会议、Zoom 或邮件中的面试邀请内容" style="margin-bottom: 12px" />
      <Button type="primary" @click="parseInvitation">
        <template #icon><ThunderboltOutlined /></template>
        解析邀请
      </Button>
    </Card>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="14">
        <Card title="安排列表">
          <Table :columns="columns" :data-source="schedules" row-key="id" :pagination="false" size="middle">
            <template #emptyText><Empty description="暂无面试安排" /></template>
          </Table>
        </Card>
      </Col>
      <Col :xs="24" :lg="10">
        <Card title="按日期查看">
          <Collapse v-if="Object.keys(calendar).length" accordion>
            <CollapsePanel v-for="(items, day) in calendar" :key="day" :header="`${day} (${items.length} 场)`">
              <List :data-source="items" size="small">
                <template #renderItem="{ item }">
                  <ListItem>
                    {{ item.company }} / {{ item.position }} / {{ formatDateTime(item.startTime) }}
                  </ListItem>
                </template>
              </List>
            </CollapsePanel>
          </Collapse>
          <Empty v-else description="暂无日历数据" />
        </Card>
      </Col>
    </Row>
  </div>
</template>
```

- [ ] **Step 2: Verify SchedulePage renders**

Run: `npm run dev`, navigate to `/schedules`
Expected: Stats, invitation parser, table, and calendar collapse render.

- [ ] **Step 3: Commit**

```bash
git add src/pages/SchedulePage.vue
git commit -m "feat: rewrite SchedulePage with Ant Design table, collapse calendar, and invitation parser"
```

---

### Task 10: Rewrite KnowledgePage

**Files:**
- Rewrite: `src/pages/KnowledgePage.vue`

- [ ] **Step 1: Write KnowledgePage component**

```vue
<script setup lang="ts">
import { onMounted, ref, h } from 'vue'
import {
  Row, Col, Card, Statistic, Upload, UploadDragger, Button, Table, Tag,
  List, ListItem, Textarea, Empty, message, TypographyParagraph,
} from 'ant-design-vue'
import {
  InboxOutlined,
  UploadOutlined,
  SendOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, KnowledgeDocument } from '../types'
import { translateKnowledgeLine } from '../utils/display'

const docs = ref<KnowledgeDocument[]>([])
const stats = ref<Record<string, number>>({})
const question = ref('')
const streamOutput = ref<string[]>([])
const file = ref<File | null>(null)
const uploading = ref(false)

const load = async () => {
  const [docRes, statsRes] = await Promise.all([
    http.get<ApiResponse<KnowledgeDocument[]>>('/knowledge-bases'),
    http.get<ApiResponse<Record<string, number>>>('/knowledge-bases/stats'),
  ])
  docs.value = docRes.data.data
  stats.value = statsRes.data.data
}

const customUpload = async (options: any) => {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    await http.post('/knowledge-bases', formData)
    message.success('上传成功')
    await load()
    options.onSuccess()
  } catch {
    options.onError()
  } finally {
    uploading.value = false
  }
}

const ask = () => {
  streamOutput.value = []
  const source = new EventSource(
    `/api/knowledge-bases/chat?question=${encodeURIComponent(question.value)}`
  )
  source.onmessage = (event) => {
    streamOutput.value.push(translateKnowledgeLine(event.data))
  }
  source.onerror = () => source.close()
  message.info('问答已开始')
}

const download = (id: number) => {
  window.open(`/api/knowledge-bases/${id}/download`, '_blank')
}

const columns = [
  { title: '文件名', dataIndex: 'fileName', key: 'fileName' },
  { title: '摘要', dataIndex: 'summary', key: 'summary', ellipsis: true },
  {
    title: '分块数',
    dataIndex: 'chunks',
    key: 'chunks',
    customRender: ({ text }: { text: number }) => h(Tag, { color: 'blue' }, () => `${text} 个分块`),
  },
  { title: 'Token 估算', dataIndex: 'tokenEstimate', key: 'tokenEstimate' },
  {
    title: '关键词',
    dataIndex: 'keywords',
    key: 'keywords',
    customRender: ({ text }: { text: string[] }) => {
      return h('div', { style: 'display: flex; gap: 4px; flex-wrap: wrap' },
        text.map((k: string) => h(Tag, { key: k }, () => k))
      )
    },
  },
  {
    title: '操作',
    key: 'action',
    customRender: ({ record }: { record: KnowledgeDocument }) => {
      return h(Button, { type: 'link', size: 'small', onClick: () => download(record.id) }, () => [h(DownloadOutlined), ' 下载'])
    },
  },
]

onMounted(load)
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <Row :gutter="[16, 16]">
      <Col :xs="24" :sm="8">
        <Card><Statistic title="文档数" :value="stats.totalDocuments ?? 0" /></Card>
      </Col>
      <Col :xs="24" :sm="8">
        <Card><Statistic title="分块数" :value="stats.totalChunks ?? 0" /></Card>
      </Col>
      <Col :xs="24" :sm="8">
        <Card><Statistic title="估算 Token" :value="stats.totalTokens ?? 0" /></Card>
      </Col>
    </Row>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="12">
        <Card title="上传资料">
          <UploadDragger :custom-request="customUpload" :show-upload-list="false" :disabled="uploading">
            <p><InboxOutlined style="font-size: 48px; color: var(--ant-color-primary)" /></p>
            <p>点击或拖拽文件到此区域上传</p>
          </UploadDragger>
        </Card>
      </Col>
      <Col :xs="24" :lg="12">
        <Card title="发起提问">
          <a-textarea v-model:value="question" :rows="4" placeholder="输入一个与知识库相关的问题" style="margin-bottom: 12px" />
          <Button type="primary" @click="ask">
            <template #icon><SendOutlined /></template>
            开始流式问答
          </Button>
          <div v-if="streamOutput.length" style="margin-top: 16px; padding: 12px; background: var(--ant-color-bg-layout); border-radius: 8px; max-height: 200px; overflow: auto">
            <TypographyParagraph v-for="(line, i) in streamOutput" :key="i" style="margin: 4px 0">{{ line }}</TypographyParagraph>
          </div>
        </Card>
      </Col>
    </Row>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="14">
        <Card title="资料列表">
          <Table :columns="columns" :data-source="docs" row-key="id" :pagination="false" size="middle">
            <template #emptyText><Empty description="暂无资料" /></template>
          </Table>
        </Card>
      </Col>
      <Col :xs="24" :lg="10">
        <Card title="问答记录">
          <List :data-source="streamOutput" size="small">
            <template #renderItem="{ item }">
              <ListItem>{{ item }}</ListItem>
            </template>
            <template #empty><Empty description="暂无问答记录" /></template>
          </List>
        </Card>
      </Col>
    </Row>
  </div>
</template>
```

- [ ] **Step 2: Verify KnowledgePage renders**

Run: `npm run dev`, navigate to `/knowledge`
Expected: Stats, upload dragger, question form, document table, and chat output render.

- [ ] **Step 3: Commit**

```bash
git add src/pages/KnowledgePage.vue
git commit -m "feat: rewrite KnowledgePage with Ant Design upload, table, and stream chat"
```

---

### Task 11: Verify Build and Clean Up

**Files:**
- Verify all files compile
- Remove any remaining references to old CSS

- [ ] **Step 1: Run TypeScript check**

```bash
npx vue-tsc --noEmit
```

Expected: No errors

- [ ] **Step 2: Run production build**

```bash
npm run build
```

Expected: Build completes successfully

- [ ] **Step 3: Verify dev server runs end-to-end**

Run: `npm run dev`
Expected: All 6 pages render, navigation works, theme toggle works, no console errors

- [ ] **Step 4: Commit final state**

```bash
git add -A
git commit -m "chore: verify build and clean up after full Ant Design Vue rewrite"
```
