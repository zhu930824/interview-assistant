<script setup lang="ts">
const overviewStats = [
  { label: "本周新增简历", value: "128", note: "待分析 19 份" },
  { label: "进行中模拟面试", value: "32", note: "语音场次占比 41%" },
  { label: "待处理面试安排", value: "14", note: "今日提醒 6 条" },
  { label: "知识库问答命中率", value: "92%", note: "近 7 日稳定提升" }
];

const pipelineCards = [
  {
    title: "简历分析队列",
    desc: "Redis Stream 异步处理正在稳定运行，支持失败重试和重复检测。",
    status: "分析中 7 份"
  },
  {
    title: "统一评估引擎",
    desc: "文字面试和语音面试统一进入结构化评估流程，便于横向对比。",
    status: "报告待导出 5 份"
  },
  {
    title: "语音会话监控",
    desc: "实时记录字幕、延迟与会话时长，为体验调优提供数据支持。",
    status: "平均首包 230ms"
  }
];

const todoList = [
  "完成字节专项模拟面试的二轮追问配置",
  "确认今晚 19:30 的 Zoom 技术面链接有效",
  "补充系统设计方向知识库文档并重新向量化",
  "导出候选人王晨的最新模拟面试评估报告"
];

const moduleCards = [
  {
    title: "简历管理",
    text: "处理简历上传、内容解析、去重校验、异步分析与 PDF 报告导出。"
  },
  {
    title: "模拟面试",
    text: "支持 Skill 驱动出题、阶段时长联动、追问策略和统一评估。"
  },
  {
    title: "语音面试",
    text: "提供实时语音对话、字幕回显、暂停恢复与手动提交能力。"
  },
  {
    title: "面试安排",
    text: "解析邀请内容、展示日程视图、管理状态流转和面试提醒。"
  },
  {
    title: "知识库",
    text: "完成文档上传、分块向量化、RAG 检索增强和流式问答。"
  },
  {
    title: "运营视图",
    text: "沉淀简历、面试、安排和知识使用情况，形成统一后台数据面板。"
  }
];
</script>

<template>
  <section class="page">
    <div class="page-head">
      <p class="eyebrow">运营总览</p>
      <h2>面试流程一站式管理面板</h2>
      <p class="muted">
        这个首页现在作为后台总览使用，重点展示候选人进度、系统状态、模块入口和今日待办。
      </p>
    </div>

    <div class="grid four">
      <article v-for="stat in overviewStats" :key="stat.label" class="stat">
        <small>{{ stat.label }}</small>
        <strong>{{ stat.value }}</strong>
        <p class="muted">{{ stat.note }}</p>
      </article>
    </div>

    <div class="grid two">
      <div class="card">
        <div class="section-head">
          <div>
            <p class="eyebrow">关键流程</p>
            <h3>系统当前的重点推进事项</h3>
          </div>
          <span class="pill">实时更新</span>
        </div>

        <div class="list">
          <article v-for="card in pipelineCards" :key="card.title" class="item">
            <div class="item-head">
              <strong>{{ card.title }}</strong>
              <span class="pill">{{ card.status }}</span>
            </div>
            <p class="muted">{{ card.desc }}</p>
          </article>
        </div>
      </div>

      <div class="card">
        <div class="section-head">
          <div>
            <p class="eyebrow">今日待办</p>
            <h3>建议优先完成的动作</h3>
          </div>
          <span class="pill">4 项</span>
        </div>

        <div class="list dense">
          <article v-for="task in todoList" :key="task" class="item">
            {{ task }}
          </article>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="section-head">
        <div>
          <p class="eyebrow">模块入口</p>
          <h3>按业务动作进入不同工作区</h3>
        </div>
      </div>

      <div class="grid three">
        <article v-for="card in moduleCards" :key="card.title" class="feature-card">
          <div class="icon-chip">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M5 7h14M5 12h14M5 17h14" />
            </svg>
          </div>
          <h3>{{ card.title }}</h3>
          <p>{{ card.text }}</p>
        </article>
      </div>
    </div>

    <div class="grid two">
      <div class="card">
        <div class="section-head">
          <div>
            <p class="eyebrow">平台能力</p>
            <h3>当前管理系统已覆盖的关键特性</h3>
          </div>
        </div>

        <div class="badge-grid">
          <div class="badge-item">多格式简历解析</div>
          <div class="badge-item">Redis Stream 异步流</div>
          <div class="badge-item">Skill 驱动出题</div>
          <div class="badge-item">统一评估引擎</div>
          <div class="badge-item">实时语音问答</div>
          <div class="badge-item">RAG 检索问答</div>
        </div>
      </div>

      <div class="card">
        <div class="section-head">
          <div>
            <p class="eyebrow">使用建议</p>
            <h3>推荐的后台操作顺序</h3>
          </div>
        </div>

        <div class="list">
          <article class="item">先在简历管理完成上传与解析，再根据分析结果决定是否进入模拟面试。</article>
          <article class="item">对于重点候选人，优先结合知识库补充岗位材料，再进入文字或语音面试训练。</article>
          <article class="item">最后通过面试安排页统一管理邀请、提醒和状态流转，避免流程散落。</article>
        </div>
      </div>
    </div>
  </section>
</template>
