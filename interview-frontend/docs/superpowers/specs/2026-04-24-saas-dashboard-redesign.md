# 面试管理系统 SaaS 仪表盘重设计

## 概述

将现有面试管理系统从纯 CSS 手写样式全量重写为 Ant Design Vue 组件体系，支持深色/浅色主题切换，采用混合布局的 SaaS 仪表盘风格。

## 技术选型

| 项目 | 选择 |
|------|------|
| UI 组件库 | `ant-design-vue` |
| 图标库 | `@ant-design/icons-vue` |
| 日期库 | `dayjs` (Ant Design 依赖) |
| 主题切换 | Ant Design `ConfigProvider` + `theme` API |

## 项目结构

### 保留文件（不改动）
- `src/api/http.ts` - axios 实例
- `src/types/index.ts` - 所有类型定义
- `src/utils/display.ts` - 格式化工具函数

### 删除文件
- `src/styles.css` - 全部手写 CSS
- `src/components/PageHeader.vue` - 由 Ant Design 组件替代

### 新增文件
- `src/theme/index.ts` - 主题配置（深色/浅色算法 + 自定义 token）
- `src/layouts/AdminLayout.vue` - 管理后台布局（Sider + Header + Content）

### 重写文件
- `src/App.vue` - 仅挂载 ConfigProvider + RouterView
- `src/main.ts` - 注册 Ant Design
- `src/pages/HomePage.vue`
- `src/pages/ResumePage.vue`
- `src/pages/InterviewPage.vue`
- `src/pages/VoiceInterviewPage.vue`
- `src/pages/SchedulePage.vue`
- `src/pages/KnowledgePage.vue`

### 最终结构
```
src/
  api/http.ts          (保留)
  types/index.ts       (保留)
  utils/display.ts     (保留)
  theme/index.ts       (新增)
  layouts/AdminLayout.vue  (新增)
  pages/HomePage.vue       (重写)
  pages/ResumePage.vue     (重写)
  pages/InterviewPage.vue  (重写)
  pages/VoiceInterviewPage.vue (重写)
  pages/SchedulePage.vue   (重写)
  pages/KnowledgePage.vue  (重写)
  App.vue              (重写)
  main.ts              (重写)
```

## 主题系统

### 切换机制
- 使用 `ant-design-vue` 的 `ConfigProvider` + `theme` API
- `defaultAlgorithm` 对应浅色主题
- `darkAlgorithm` 对应深色主题
- 通过 `ref` 存储当前主题模式，`ConfigProvider` 动态绑定

### 自定义 Token
| Token | 浅色值 | 深色值 | 用途 |
|-------|--------|--------|------|
| `colorPrimary` | `#1677ff` | `#3b82f6` | 主色调 |
| `colorSuccess` | `#52c41a` | `#52c41a` | 成功状态 |
| `colorWarning` | `#faad14` | `#faad14` | 警告状态 |
| `colorError` | `#ff4d4f` | `#ff8e9f` | 错误状态 |
| `borderRadius` | `8` | `8` | 圆角 |

### 存储
- 主题偏好存 `localStorage`，key 为 `theme-mode`
- 页面加载时读取，默认跟随系统 `prefers-color-scheme`

### 切换入口
- 放在 `AdminLayout` 的 Header 右侧
- 使用 `Switch` 组件，带太阳/月亮图标

## 全局布局（AdminLayout）

### 组件结构
- `a-layout` 根容器
- `a-layout-sider` 左侧导航（可折叠）
- `a-layout-header` 顶部栏
- `a-layout-content` 内容区

### Sider（侧边栏）
- 宽度 `220px`，折叠后 `64px`
- 顶部：品牌 Logo + 名称（折叠后仅 Logo）
- 导航菜单：`a-menu`，6 个菜单项
  - 首页（DashboardOutlined）
  - 简历管理（FileTextOutlined）
  - 模拟面试（SolutionOutlined）
  - 语音面试（AudioOutlined）
  - 面试安排（CalendarOutlined）
  - 知识库（BookOutlined）
- 底部：系统状态/版本信息

### Header（顶栏）
- 左侧：`a-breadcrumb` 面包屑导航
- 右侧：
  - `a-input-search` 全局搜索
  - 主题切换开关
  - `a-avatar` + `a-dropdown` 用户入口（预留）

### Content（内容区）
- 内边距 `24px`
- 背景色：浅色 `#f5f5f5`，深色 `#141414`
- 渲染 `<RouterView />`

### 响应式
- 屏幕 `< 768px`：Sider 收起为抽屉模式
- Header 始终固定顶部

## 首页设计（HomePage）

### 混合布局结构

#### 1. Hero 数据总览区
- `a-row` + `a-col` 横向排列 4 个 `a-statistic` 卡片
- 卡片用 `a-card` 包裹，`hoverable` 效果
- 数据项：
  - 本周新增简历 `128`
  - 进行中模拟面试 `32`
  - 待处理面试安排 `14`
  - 知识库命中率 `92%`
- 每项带 `@ant-design/icons-vue` 图标

#### 2. 图表/统计区
- 左侧：`a-card` 包裹 CSS 柱状图
- 右侧：`a-card` + `a-list` 展示"今日待办"，每项前带 `a-checkbox`

#### 3. 功能卡片网格
- `a-row` + `a-col` 3 列布局
- 每个模块用 `a-card` + `a-card-meta`
- 卡片 `hoverable`，点击跳转路由

#### 4. 底部双栏
- 左栏：`a-tag` 展示平台能力（6 个标签）
- 右栏：`a-timeline` 展示 3 步操作流程

#### 5. 定价表
- `a-row` + `a-col` 3 列
- 3 个 `a-card`：免费版/专业版/企业版
- 中间卡片用主题色高亮标识"推荐"
- 每卡片内：`a-tag` + 价格 + `a-list` 特性 + `a-button` CTA

## 子页面设计

### 简历管理（ResumePage）

**顶部统计行：** `a-row` + 3 个 `a-statistic`（总数/已完成/失败）

**上传区：** `a-card`
- `a-upload-dragger` 拖拽上传
- `a-button` type="primary" 上传按钮

**双栏内容：** `a-row` + `a-col` span=12
- 左栏 - 分析队列：`a-table`
  - 列：文件名、候选人、目标岗位、状态（`a-tag`）、进度（`a-progress`）、操作
  - 操作：`a-button` 下载报告 / `a-popconfirm` 确认重新分析
- 右栏 - 最新分析详情：`a-card`
  - `a-descriptions` 展示解析结果
  - 优势亮点：`a-tag` color="green"
  - 风险提示：`a-tag` color="red"

### 模拟面试（InterviewPage）

**顶部统计行：** 3 个 `a-statistic`

**创建面试区：** `a-card` + `a-form`
- 面试方向：`a-select`
- 总时长：`a-input-number`
- 追问轮次：`a-input-number`

**双栏工作区：**
- 左栏 - 会话列表：`a-list` + `a-list-item`（选中态高亮）
- 右栏 - 面试工作区：`a-card`
  - 当前题目：`a-alert`
  - 阶段时长：`a-tag` 横排
  - 作答输入：`a-textarea`
  - 操作按钮：`a-space` 包裹 3 个 `a-button`
  - 会话记录/评估结果：`a-typography-paragraph`

### 语音面试（VoiceInterviewPage）

**控制栏：** `a-card` + `a-space`
- 5 个操作按钮（不同 `type` 和 `icon`）
- 当前会话 ID：`a-typography-text`

**双栏：**
- 左栏：`a-card` - `a-textarea` + `a-descriptions` 状态
- 右栏：`a-card` - `a-timeline` 实时事件流

**有会话时：**
- 左栏：`a-list` 字幕记录
- 右栏：`a-list` AI 追问

### 面试安排（SchedulePage）

**顶部统计行：** 4 个 `a-statistic`

**邀请解析区：** `a-card` - `a-textarea` + `a-button`

**双栏：**
- 左栏 - 安排列表：`a-table`
  - 列：公司/岗位、来源、时间、状态 `a-tag`、会议链接、操作
  - 操作：`a-button-group` 切换状态
- 右栏 - 日历视图：`a-card` + `a-collapse` 按日期分组

### 知识库（KnowledgePage）

**顶部统计行：** 3 个 `a-statistic`

**双栏上传/提问：**
- 左栏：`a-card` - `a-upload-dragger` + `a-button`
- 右栏：`a-card` - `a-textarea` + `a-button` + 流式输出区

**双栏内容：**
- 左栏 - 资料列表：`a-table`
- 右栏 - 问答记录：`a-list`

## API 与数据流

### API 层
- 保持 `src/api/http.ts` 不变
- 所有页面通过 `http.get/post` 调用后端

### 数据流
- `onMounted` 调用 `load()` 获取数据
- `ref` 存储响应数据
- 表单提交后重新 `load()` 刷新

### 错误处理
- `a-message` 全局提示
- `http.ts` 拦截器统一处理网络错误
- `a-form` 内置规则校验

### Loading 状态
- 页面加载：`a-spin` 包裹内容区
- 按钮提交：`loading` 属性
- 表格加载：`a-table` 的 `loading` 属性

### 空状态
- 列表为空时使用 `a-empty` 组件

## 依赖安装

```bash
npm install ant-design-vue @ant-design/icons-vue dayjs
```
