import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from './layouts/AdminLayout.vue'
import HomePage from './pages/HomePage.vue'
import ResumePage from './pages/ResumePage.vue'
import ResumeDetailPage from './pages/ResumeDetailPage.vue'
import InterviewPage from './pages/InterviewPage.vue'
import VoiceInterviewPage from './pages/VoiceInterviewPage.vue'
import SchedulePage from './pages/SchedulePage.vue'
import KnowledgePage from './pages/KnowledgePage.vue'
import LoginPage from './pages/LoginPage.vue'
import RegisterPage from './pages/RegisterPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: LoginPage,
      meta: { public: true }
    },
    {
      path: '/register',
      component: RegisterPage,
      meta: { public: true }
    },
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
          path: 'resumes/:id',
          component: ResumeDetailPage,
          meta: { title: '简历详情', breadcrumb: ['首页', '简历管理', '详情'] }
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

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('auth_token')
  const isPublicRoute = to.matched.some(record => record.meta.public)

  if (!token && !isPublicRoute) {
    next('/login')
  } else if (token && isPublicRoute) {
    next('/')
  } else {
    next()
  }
})

const app = createApp(App)
app.use(Antd)
app.use(router)
app.mount('#app')

export { router }
