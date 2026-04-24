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
  confirmPassword: '',
})

const loading = ref(false)

const handleSubmit = async () => {
  if (!form.value.username || !form.value.password) {
    message.warning('请填写所有字段')
    return
  }

  if (form.value.username.length < 3) {
    message.warning('用户名至少需要3个字符')
    return
  }

  if (form.value.password.length < 6) {
    message.warning('密码至少需要6个字符')
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    message.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    await auth.register({
      username: form.value.username,
      password: form.value.password,
    })
    message.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    message.error(error.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-container">
    <div class="register-background">
      <div class="mesh-gradient"></div>
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
    </div>

    <div class="register-card glass-card">
      <div class="register-header">
        <div class="brand-icon">
          <span>IM</span>
        </div>
        <h1 class="register-title">创建账户</h1>
        <p class="register-subtitle">注册以使用面试管理系统</p>
      </div>

      <form class="register-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <div class="input-wrapper">
            <UserOutlined class="input-icon" />
            <input
              v-model="form.username"
              type="text"
              class="form-input"
              placeholder="请输入用户名 (至少3个字符)"
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
              placeholder="请输入密码 (至少6个字符)"
              autocomplete="new-password"
            />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">确认密码</label>
          <div class="input-wrapper">
            <LockOutlined class="input-icon" />
            <input
              v-model="form.confirmPassword"
              type="password"
              class="form-input"
              placeholder="请再次输入密码"
              autocomplete="new-password"
            />
          </div>
        </div>

        <button
          type="submit"
          class="submit-btn"
          :disabled="loading"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="register-footer">
        <RouterLink to="/login" class="login-link">
          已有账户？立即登录
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/dashboard.css';

.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.register-background {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.register-background .mesh-gradient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(at 80% 20%, rgba(139, 92, 246, 0.15) 0%, transparent 50%),
    radial-gradient(at 20% 80%, rgba(99, 102, 241, 0.12) 0%, transparent 50%),
    radial-gradient(at 50% 50%, rgba(236, 72, 153, 0.08) 0%, transparent 50%);
}

.floating-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: floatOrb 12s ease-in-out infinite;
}

.orb-1 {
  width: 350px;
  height: 350px;
  background: rgba(139, 92, 246, 0.2);
  top: 10%;
  right: -10%;
}

.orb-2 {
  width: 280px;
  height: 280px;
  background: rgba(99, 102, 241, 0.15);
  bottom: 10%;
  left: -10%;
  animation-delay: -5s;
}

@keyframes floatOrb {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-30px, 20px) scale(1.05); }
}

.register-card {
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
  position: relative;
  z-index: 1;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.brand-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #8b5cf6 0%, #ec4899 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 20px;
  font-weight: 700;
  color: white;
}

.register-title {
  font-family: 'Geist', 'SF Pro Display', -apple-system, sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary, #1a1c2e);
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 15px;
  color: var(--text-secondary, #64748b);
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
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
  border-color: #8b5cf6;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.15);
}

.submit-btn {
  width: 100%;
  padding: 16px 24px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #8b5cf6 0%, #ec4899 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(139, 92, 246, 0.35);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.register-footer {
  text-align: center;
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid var(--glass-border, rgba(0, 0, 0, 0.1));
}

.login-link {
  font-size: 14px;
  color: #8b5cf6;
  text-decoration: none;
  transition: color 0.2s ease;
}

.login-link:hover {
  color: #6366f1;
}

@media (max-width: 480px) {
  .register-card {
    padding: 32px 24px;
  }
}
</style>
