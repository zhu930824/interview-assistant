import { createApp } from "vue";
import { createRouter, createWebHistory } from "vue-router";
import App from "./App.vue";
import "./styles.css";
import HomePage from "./pages/HomePage.vue";
import ResumePage from "./pages/ResumePage.vue";
import InterviewPage from "./pages/InterviewPage.vue";
import SchedulePage from "./pages/SchedulePage.vue";
import KnowledgePage from "./pages/KnowledgePage.vue";
import VoiceInterviewPage from "./pages/VoiceInterviewPage.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      component: HomePage,
      meta: {
        section: "全局总览",
        title: "面试中心",
        description: "集中查看候选人流程、训练进度、关键指标和系统入口。"
      }
    },
    {
      path: "/resumes",
      component: ResumePage,
      meta: {
        section: "候选人管理",
        title: "简历管理",
        description: "上传简历、触发异步分析、查看状态流转并导出分析报告。"
      }
    },
    {
      path: "/interviews",
      component: InterviewPage,
      meta: {
        section: "模拟训练",
        title: "模拟面试",
        description: "配置面试方向、阶段时长和追问策略，统一输出评估结果。"
      }
    },
    {
      path: "/voice-interviews",
      component: VoiceInterviewPage,
      meta: {
        section: "实时语音",
        title: "语音面试",
        description: "基于实时语音对话完成问答、字幕同步、暂停恢复与会话分析。"
      }
    },
    {
      path: "/schedules",
      component: SchedulePage,
      meta: {
        section: "流程协同",
        title: "面试安排",
        description: "管理邀请解析、日历视图、状态流转和提醒配置。"
      }
    },
    {
      path: "/knowledge",
      component: KnowledgePage,
      meta: {
        section: "知识中台",
        title: "知识库",
        description: "上传资料、完成向量化处理，并通过流式问答快速获取答案。"
      }
    }
  ]
});

createApp(App).use(router).mount("#app");
