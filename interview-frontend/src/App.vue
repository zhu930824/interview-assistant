<script setup lang="ts">
import { computed } from "vue";
import { RouterLink, RouterView, useRoute } from "vue-router";

type NavigationItem = {
  to: string;
  kicker: string;
  label: string;
  description: string;
};

const route = useRoute();

const navigation: NavigationItem[] = [
  {
    to: "/",
    kicker: "01",
    label: "面试中心",
    description: "查看全局数据、快捷入口与关键进度"
  },
  {
    to: "/resumes",
    kicker: "02",
    label: "简历管理",
    description: "上传解析简历、查看异步进度和分析报告"
  },
  {
    to: "/interviews",
    kicker: "03",
    label: "模拟面试",
    description: "发起文字面试、配置阶段和智能追问"
  },
  {
    to: "/voice-interviews",
    kicker: "04",
    label: "语音面试",
    description: "实时语音对话、字幕追踪与会话控制"
  },
  {
    to: "/schedules",
    kicker: "05",
    label: "面试安排",
    description: "统一管理邀请解析、日程状态与提醒"
  },
  {
    to: "/knowledge",
    kicker: "06",
    label: "知识库",
    description: "文档入库、向量检索与流式问答"
  }
];

const currentPage = computed(() => {
  const meta = route.meta as {
    section?: string;
    title?: string;
    description?: string;
  };

  return {
    section: meta.section ?? "管理后台",
    title: meta.title ?? "面试与简历管理系统",
    description:
      meta.description ??
      "把简历、面试、安排、知识库和语音能力统一在一个后台里协同管理。"
  };
});

const todayLabel = new Intl.DateTimeFormat("zh-CN", {
  month: "long",
  day: "numeric",
  weekday: "long"
}).format(new Date());
</script>

<template>
  <div class="shell admin-shell">
    <aside class="sidebar">
      <div class="brand">
        <p class="eyebrow">Interview Ops</p>
        <h1>面试与简历管理系统</h1>
        <p class="muted">
          用一套后台工作台串起候选人筛选、模拟训练、语音面试、日程安排和知识沉淀。
        </p>
      </div>

      <div class="sidebar-panel">
        <p class="panel-label">今日焦点</p>
        <strong>优先推进正在进行中的候选人流程</strong>
        <p class="muted">
          从简历解析到模拟面试复盘，再到面试安排和知识库准备，保持同一套操作节奏。
        </p>
      </div>

      <div class="sidebar-section">
        <p class="panel-label">功能导航</p>
        <nav class="nav">
          <RouterLink
            v-for="item in navigation"
            :key="item.to"
            :to="item.to"
            class="nav-link"
          >
            <span class="nav-kicker">{{ item.kicker }}</span>
            <strong>{{ item.label }}</strong>
            <small>{{ item.description }}</small>
          </RouterLink>
        </nav>
      </div>

      <div class="sidebar-footer-card">
        <span class="pill">系统在线</span>
        <h3>统一面试运营看板</h3>
        <p class="muted">
          当前布局已切换为后台管理样式，适合持续扩展列表、表单、统计和审批流。
        </p>
      </div>
    </aside>

    <section class="content-shell">
      <header class="topbar">
        <div>
          <p class="eyebrow">{{ currentPage.section }}</p>
          <h2>{{ currentPage.title }}</h2>
          <p class="muted">{{ currentPage.description }}</p>
        </div>

        <div class="topbar-tools">
          <label class="search-field">
            <span>全局搜索</span>
            <input class="input" type="text" placeholder="搜索候选人、岗位、面试方向" />
          </label>

          <div class="topbar-actions">
            <span class="status-chip">实时同步中</span>
            <span class="status-chip ghost">{{ todayLabel }}</span>
            <RouterLink class="button secondary" to="/interviews">新建模拟面试</RouterLink>
          </div>
        </div>
      </header>

      <main class="content">
        <div class="workspace-panel">
          <RouterView />
        </div>
      </main>
    </section>
  </div>
</template>
