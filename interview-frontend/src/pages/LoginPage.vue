<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
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
  <div class="login-container">
    <div class="login-background">
      <div class="mesh-gradient"></div>
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
    </div>

    <div class="login-card glass-card">
      <div class="login-header">
        <div class="brand-icon">
          <span>IM</span>
        </div>
        <h1 class="login-title">面试管理系统</h1>
        <p class="login-subtitle">登录您的账户以继续</p>
      </div>

      <form class="login-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <div class="input-wrapper">
            <UserOutlined class="input-icon" />
            <input
              v-model="form.username"
              type="text"
              class="form-input"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <div class="input-wrapper">
            <LockOutlined class="input-icon" />
            <input
              v-model="form.password"
              type="password"
              class="form-input"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </div>
        </div>

        <button
          type="submit"
          class="submit-btn"
          :disabled="loading"
        >
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="login-footer">
        <RouterLink to="/register" class="register-link">
          还没有账户？立即注册
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/dashboard.css';

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.login-background .mesh-gradient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(at 20% 30%, rgba(99, 102, 241, 0.15) 0%, transparent 50%),
    radial-gradient(at 80% 70%, rgba(139, 92, 246, 0.12) 0%, transparent 50%),
    radial-gradient(at 50% 50%, rgba(236, 72, 153, 0.08) 0%, transparent 50%);
}

.floating-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: floatOrb 12s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: rgba(99, 102, 241, 0.2);
  top: -10%;
  left: -10%;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: rgba(139, 92, 246, 0.15);
  bottom: -10%;
  right: -10%;
  animation-delay: -5s;
}

@keyframes floatOrb {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.1); }
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.brand-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 20px;
  font-weight: 700;
  color: white;
}

.login-title {
  font-family: 'Geist', 'SF Pro Display', -apple-system, sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary, #1a1c2e);
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 15px;
  color: var(--text-secondary, #64748b);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #1a1c2e);
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: var(--text-secondary, #64748b);
}

.form-input {
  width: 100%;
  padding: 14px 16px 14px 48px;
  font-size: 15px;
  border: 1px solid var(--glass-border, rgba(0, 0, 0, 0.1));
  border-radius: 12px;
  background: var(--glass-bg, rgba(255, 255, 255, 0.7));
  color: var(--text-primary, #1a1c2e);
  transition: all 0.3s ease;
}

.form-input::placeholder {
  color: var(--text-secondary, #64748b);
}

.form-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.submit-btn {
  width: 100%;
  padding: 16px 24px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.35);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--glass-border, rgba(0, 0, 0, 0.1));
}

.register-link {
  font-size: 14px;
  color: #6366f1;
  text-decoration: none;
  transition: color 0.2s ease;
}

.register-link:hover {
  color: #8b5cf6;
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px;
  }
}
</style>
