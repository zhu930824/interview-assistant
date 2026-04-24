<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, RobotOutlined, MessageOutlined, PhoneOutlined, MailOutlined } from '@ant-design/icons-vue'
import { useAuth } from '../stores/auth'

const router = useRouter()
const auth = useAuth()

const form = ref({
  username: '',
  password: '',
})

const loading = ref(false)

const handleSubmit = async () => {
  if (!form.value.username || !form.value.password) {
    message.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    await auth.login(form.value)
    message.success('登录成功')
    router.push('/')
  } catch (error: any) {
    message.error(error.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <!-- 左侧品牌展示 -->
    <div class="auth-showcase">
      <!-- 背景层 -->
      <div class="showcase-bg">
        <div class="bg-pattern"></div>
        <div class="bg-gradient"></div>
        <div class="bg-mesh"></div>
      </div>

      <div class="showcase-content">
        <!-- Logo -->
        <div class="showcase-logo">
          <div class="logo-icon">
            <RobotOutlined />
          </div>
          <span class="logo-text">面试助手</span>
        </div>

        <h1 class="showcase-title">
          AI 驱动的<br/>
          <span class="title-highlight">智能面试平台</span>
        </h1>
        <p class="showcase-desc">
          集成智能简历解析、模拟面试、实时反馈于一体的面试准备平台
        </p>

        <!-- AI 聊天机器人预览 -->
        <div class="chatbot-preview">
          <div class="chatbot-header">
            <div class="bot-avatar">
              <RobotOutlined />
            </div>
            <div class="bot-info">
              <span class="bot-name">AI 面试官</span>
              <span class="bot-status">
                <span class="status-dot"></span>
                在线
              </span>
            </div>
          </div>
          <div class="chatbot-messages">
            <div class="message bot">
              <p>您好！我是 AI 面试官，很高兴为您服务 😊</p>
            </div>
            <div class="message bot">
              <p>请问您想进行哪种类型的模拟面试？</p>
            </div>
            <div class="message user">
              <p>我想练习前端开发的面试</p>
            </div>
            <div class="message bot typing">
              <span></span><span></span><span></span>
            </div>
          </div>
          <div class="chatbot-input">
            <input type="text" placeholder="输入您的问题..." disabled />
            <button class="send-btn">
              <MessageOutlined />
            </button>
          </div>
        </div>

        <!-- 全渠道特性 -->
        <div class="channels">
          <div class="channel-item">
            <div class="channel-icon">
              <MessageOutlined />
            </div>
            <span>网页对话</span>
          </div>
          <div class="channel-item">
            <div class="channel-icon">
              <PhoneOutlined />
            </div>
            <span>语音面试</span>
          </div>
          <div class="channel-item">
            <div class="channel-icon">
              <MailOutlined />
            </div>
            <span>邮件通知</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="auth-form-panel">
      <!-- 背景装饰 -->
      <div class="panel-bg">
        <div class="panel-circle c1"></div>
        <div class="panel-circle c2"></div>
        <div class="panel-circle c3"></div>
      </div>

      <div class="form-container">
        <div class="form-header">
          <div class="form-logo">
            <RobotOutlined />
          </div>
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">登录您的账户继续使用</p>
        </div>

        <form class="login-form" @submit.prevent="handleSubmit">
          <div class="field-group">
            <label class="field-label">用户名</label>
            <div class="field-wrap">
              <UserOutlined class="field-icon" />
              <input
                v-model="form.username"
                type="text"
                placeholder="请输入用户名"
                autocomplete="username"
              />
            </div>
          </div>

          <div class="field-group">
            <label class="field-label">密码</label>
            <div class="field-wrap">
              <LockOutlined class="field-icon" />
              <input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                autocomplete="current-password"
              />
            </div>
          </div>

          <div class="field-row">
            <label class="checkbox-wrap">
              <input type="checkbox" />
              <span>记住我</span>
            </label>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>

          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="loading" class="loading-spinner"></span>
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>

        <div class="form-divider">
          <span>或</span>
        </div>

        <div class="social-row">
          <button type="button" class="social-btn wechat">
            <span class="social-icon">微</span>
            微信登录
          </button>
          <button type="button" class="social-btn alipay">
            <span class="social-icon">支</span>
            支付宝
          </button>
        </div>

        <div class="form-footer">
          <RouterLink to="/register" class="register-link">
            还没有账户？<span>立即注册</span>
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ── 基础变量 ── */
.auth-page {
  --primary: #6b8e7a;
  --primary-light: #8fb8a3;
  --primary-dark: #4a6b5a;
  --accent: #d4a574;
  --accent-light: #e8c9a8;
  --secondary: #7eb8c4;
  --success: #7cb798;
  --text-dark: #3a3a3a;
  --text-muted: #6a6a6a;
  --text-light: #9a9a9a;
  --card-bg: rgba(255, 255, 255, 0.95);
  --shadow-soft: rgba(80, 60, 40, 0.08);
  --shadow-hover: rgba(80, 60, 40, 0.15);
}

.auth-page {
  min-height: 100vh;
  display: flex;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
}

/* ── 左侧品牌展示 ── */
.auth-showcase {
  flex: 1.4;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.showcase-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.bg-pattern {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(107, 142, 122, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(126, 184, 196, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(212, 165, 116, 0.08) 0%, transparent 50%);
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg,
    #f5f0ea 0%,
    #e8e2da 25%,
    #f0ebe5 50%,
    #e5dfd8 75%,
    #f2ede7 100%
  );
}

.bg-mesh {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%239C9080' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.showcase-content {
  max-width: 440px;
  width: 100%;
  position: relative;
  z-index: 2;
}

.showcase-logo {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 40px;
}

.logo-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  box-shadow: 0 8px 24px rgba(107, 142, 122, 0.25);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-dark);
  letter-spacing: -0.5px;
}

.showcase-title {
  font-size: 44px;
  font-weight: 800;
  line-height: 1.18;
  color: var(--text-dark);
  margin-bottom: 18px;
  letter-spacing: -1px;
}

.title-highlight {
  background: linear-gradient(135deg, var(--primary-light), var(--secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.showcase-desc {
  font-size: 16px;
  line-height: 1.7;
  color: var(--text-muted);
  margin-bottom: 32px;
}

/* ── 聊天机器人预览 ── */
.chatbot-preview {
  background: var(--card-bg);
  border-radius: 24px;
  padding: 22px;
  margin-bottom: 26px;
  box-shadow:
    0 10px 40px rgba(0, 0, 0, 0.06),
    0 0 0 1px rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
}

.chatbot-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.bot-avatar {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.bot-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.bot-name {
  font-weight: 600;
  color: var(--text-dark);
  font-size: 15px;
}

.bot-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--success);
}

.status-dot {
  width: 7px;
  height: 7px;
  background: var(--success);
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.chatbot-messages {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.message {
  max-width: 85%;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
}

.message.bot {
  background: #f5f2ee;
  color: var(--text-dark);
  align-self: flex-start;
  border-bottom-left-radius: 4px;
}

.message.user {
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: white;
  align-self: flex-end;
  border-bottom-right-radius: 4px;
}

.message.typing {
  display: flex;
  gap: 4px;
  padding: 12px 18px;
}

.message.typing span {
  width: 7px;
  height: 7px;
  background: var(--text-light);
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.message.typing span:nth-child(2) { animation-delay: 0.2s; }
.message.typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 100% { transform: translateY(0); opacity: 0.5; }
  50% { transform: translateY(-4px); opacity: 1; }
}

.chatbot-input {
  display: flex;
  gap: 10px;
}

.chatbot-input input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: #faf8f5;
  border-radius: 12px;
  font-size: 13px;
  color: var(--text-muted);
}

.send-btn {
  width: 44px;
  height: 44px;
  border: none;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  border-radius: 12px;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.send-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(107, 142, 122, 0.3);
}

/* ── 全渠道特性 ── */
.channels {
  display: flex;
  gap: 12px;
}

.channel-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 12px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  color: var(--text-dark);
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
}

.channel-item:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.05);
}

.channel-icon {
  width: 38px;
  height: 38px;
  background: linear-gradient(135deg, var(--accent-light), var(--accent));
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: white;
}

/* ── 右侧表单面板 ── */
.auth-form-panel {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 50px;
  overflow: hidden;
  background: linear-gradient(135deg, #2d3a3a 0%, #1e2828 50%, #2a3535 100%);
}

.panel-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.panel-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}

.panel-circle.c1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #d4a574, #e8c9a8);
  top: -15%;
  right: -10%;
  animation: circleFloat 20s ease-in-out infinite;
}

.panel-circle.c2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #8fb8a3, #7eb8c4);
  bottom: -10%;
  left: -5%;
  animation: circleFloat 25s ease-in-out infinite reverse;
}

.panel-circle.c3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #7cb798, #a8d4b8);
  top: 40%;
  left: 20%;
  animation: circleFloat 18s ease-in-out infinite;
}

@keyframes circleFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(20px, -15px) scale(1.05); }
  50% { transform: translate(-10px, 20px) scale(0.95); }
  75% { transform: translate(-15px, -10px) scale(1.02); }
}

.form-container {
  width: 100%;
  max-width: 420px;
  position: relative;
  z-index: 2;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-logo {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: white;
  margin: 0 auto 20px;
  box-shadow: 0 8px 25px rgba(107, 142, 122, 0.25);
}

.form-title {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-dark);
  margin-bottom: 10px;
}

.form-subtitle {
  font-size: 15px;
  color: var(--text-muted);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-label {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.85);
}

.field-wrap {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.field-wrap:focus-within {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.field-icon {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.6);
}

.field-wrap input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 15px;
  color: white;
  outline: none;
}

.field-wrap input::placeholder {
  color: rgba(255, 255, 255, 0.45);
}

.field-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkbox-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
}

.checkbox-wrap input {
  width: 18px;
  height: 18px;
  accent-color: var(--primary-light);
}

.forgot-link {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: white;
}

/* ── 提交按钮 ── */
.submit-btn {
  width: 100%;
  padding: 18px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  border: none;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 30px rgba(107, 142, 122, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(107, 142, 122, 0.45);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ── 分隔线 ── */
.form-divider {
  display: flex;
  align-items: center;
  gap: 18px;
  margin: 28px 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.form-divider::before,
.form-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
}

/* ── 社交按钮 ── */
.social-row {
  display: flex;
  gap: 14px;
}

.social-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 15px 18px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.08);
  border-radius: 14px;
  font-size: 14px;
  font-weight: 500;
  color: white;
  cursor: pointer;
  transition: all 0.25s ease;
  backdrop-filter: blur(5px);
}

.social-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.25);
}

.social-btn.wechat:hover {
  background: rgba(7, 193, 96, 0.2);
  border-color: #07c160;
}

.social-btn.alipay:hover {
  background: rgba(22, 119, 255, 0.2);
  border-color: #1677ff;
}

.social-icon {
  font-weight: 700;
}

/* ── 页脚 ── */
.form-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 26px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.register-link {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
}

.register-link span {
  color: white;
  font-weight: 600;
}

.register-link:hover span {
  text-decoration: underline;
}

/* ── 响应式 ── */
@media (max-width: 1024px) {
  .auth-showcase {
    display: none;
  }

  .auth-form-panel {
    flex: 1;
  }
}

@media (max-width: 640px) {
  .auth-form-panel {
    padding: 24px;
  }

  .social-row {
    flex-direction: column;
  }
}
</style>
