<template>
  <div class="admin-login">
    <div class="login-container">
      <div class="login-header">
        <h1>秒杀系统管理后台</h1>
        <p>请使用管理员账号登录</p>
      </div>
      
      <div class="login-form">
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            type="text" 
            id="username" 
            v-model="username" 
            placeholder="请输入管理员用户名"
            @keyup.enter="handleLogin"
          >
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          >
        </div>
        
        <div class="form-actions">
          <button 
            class="login-btn" 
            @click="handleLogin"
            :disabled="loading"
          >
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </div>
        
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
        
       
      </div>
      
      <div class="login-footer">
        <router-link to="/" class="back-link">返回用户端</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { adminLogin } from '../api/user';

// 管理员账号配置
const ADMIN_CREDENTIALS = {
  username: 'admin',
  password: 'seckill'
};

export default {
  name: 'AdminLogin',
  data() {
    return {
      username: '',
      password: '',
      loading: false,
      error: '',
      ADMIN_CREDENTIALS: ADMIN_CREDENTIALS
    };
  },
  methods: {
    async handleLogin() {
      if (!this.username || !this.password) {
        this.error = '请输入用户名和密码';
        return;
      }
      
      this.loading = true;
      this.error = '';
      
      try {
        const response = await adminLogin(this.username, this.password)
        const ok = response && response.data === true
        if (!ok) {
          this.error = '登录失败，请检查用户名和密码';
          return;
        }

        localStorage.setItem('adminInfo', JSON.stringify({
          username: this.username,
          token: 'admin-token-' + Date.now(),
          isAdmin: true
        }));

        this.$router.push('/admin/activity-management');
      } catch (err) {
        this.error = '登录失败，请检查用户名和密码';
        console.error('管理员登录失败:', err);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(900px 600px at 20% 0%, rgba(255, 68, 0, 0.14), transparent 55%),
    radial-gradient(900px 600px at 90% 10%, rgba(99, 102, 241, 0.14), transparent 55%),
    var(--bg);
  padding: 24px;
}

.login-container {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.88);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.login-header {
  background: rgba(255, 68, 0, 0.08);
  color: var(--text);
  padding: 22px 20px;
  text-align: center;
  border-bottom: 1px solid rgba(17, 24, 39, 0.08);
}

.login-header h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 600;
}

.login-header p {
  margin: 0;
  font-size: 16px;
  color: var(--muted);
}

.login-form {
  padding: 30px 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 700;
  color: var(--muted);
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color .15s ease, box-shadow .15s ease;
}

.form-group input:focus {
  outline: none;
  border-color: rgba(255, 68, 0, 0.55);
  box-shadow: 0 0 0 4px rgba(255, 68, 0, 0.12);
}

.form-actions {
  margin-top: 30px;
}

.login-btn {
  width: 100%;
  padding: 12px 16px;
  background: var(--primary);
  color: #fff;
  border: 1px solid var(--primary);
  border-radius: 10px;
  font-size: 16px;
  font-weight: 800;
  cursor: pointer;
  transition: transform .15s ease, background-color .15s ease, border-color .15s ease, box-shadow .15s ease;
  box-shadow: 0 10px 22px rgba(255, 68, 0, 0.18);
}

.login-btn:hover {
  transform: translateY(-1px);
  background: var(--primary-700);
  border-color: var(--primary-700);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error-message {
  margin-top: 15px;
  padding: 10px;
  background-color: rgba(231, 76, 60, 0.08);
  border: 1px solid rgba(231, 76, 60, 0.22);
  border-radius: 10px;
  color: #e74c3c;
  font-size: 14px;
}

.admin-hint {
  margin-top: 20px;
  padding: 15px;
}

.login-footer {
  padding: 20px;
  text-align: center;
  border-top: 1px solid rgba(17, 24, 39, 0.08);
}

.back-link {
  color: var(--primary);
  text-decoration: none;
  font-size: 14px;
}

.back-link:hover {
  text-decoration: underline;
}
</style>