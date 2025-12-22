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
import { login } from '../api/user';

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
        // 检查是否为预设的管理员账号
        if (this.username === ADMIN_CREDENTIALS.username && this.password === ADMIN_CREDENTIALS.password) {
          // 保存管理员登录信息
          localStorage.setItem('adminInfo', JSON.stringify({
            username: this.username,
            token: 'admin-token-' + Date.now(),
            isAdmin: true
          }));
          
          // 跳转到活动管理页面
          this.$router.push('/admin/activity-management');
          return;
        }
        
        // 如果不是预设账号，尝试调用API
        const response = await login({
          username: this.username,
          password: this.password
        });
        
        if (response.data && response.data.token) {
          // 保存管理员登录信息
          localStorage.setItem('adminInfo', JSON.stringify({
            username: this.username,
            token: response.data.token,
            isAdmin: true
          }));
          
          // 跳转到活动管理页面
          this.$router.push('/admin/activity-management');
        } else {
          this.error = '登录失败，请检查用户名和密码';
        }
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.login-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px 20px;
  text-align: center;
}

.login-header h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 600;
}

.login-header p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
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
  font-weight: 500;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-group input:focus {
  border-color: #667eea;
  outline: none;
}

.form-actions {
  margin-top: 30px;
}

.login-btn {
  width: 100%;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.login-btn:hover {
  opacity: 0.9;
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error-message {
  margin-top: 15px;
  padding: 10px;
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  color: #ff4d4f;
  font-size: 14px;
}

.admin-hint {
  margin-top: 20px;
  padding: 15px;
  background-color: #f6f8fa;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
  color: #656d76;
}

.admin-hint p {
  margin: 5px 0;
}

.admin-hint p:first-child {
  font-weight: 500;
  margin-bottom: 10px;
  color: #24292f;
}

.login-footer {
  padding: 20px;
  text-align: center;
  border-top: 1px solid #eee;
}

.back-link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
}

.back-link:hover {
  text-decoration: underline;
}
</style>