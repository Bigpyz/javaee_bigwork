<!-- src/views/Register.vue -->
<template>
  <div class="register-container">
    <div class="register-card">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">用户名：</label>
          <input type="text" id="username" v-model="username" />
        </div>
        <div class="form-group">
          <label for="password">密码：</label>
          <input type="password" id="password" v-model="password" />
        </div>
        <div v-if="error" class="error">{{ error }}</div>
        <button type="submit" class="register-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
        <div class="login-link">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { register } from "@/api/user";

export default {
  data() {
    return {
      username: "",
      password: "",
      loading: false,
      error: ""
    };
  },
  methods: {
    async handleRegister() {
      // 前端验证用户名和密码是否为空
      if (!this.username.trim()) {
        this.error = "用户名不能为空";
        return;
      }
      
      if (!this.password.trim()) {
        this.error = "密码不能为空";
        return;
      }
      
      this.loading = true;
      this.error = "";
      try {
        const response = await register(this.username, this.password);
        alert("注册成功！");
        this.$router.push("/login");
      } catch (error) {
        console.error("注册失败:", error);
        // 根据不同的错误类型显示不同的错误信息
        if (error.response) {
          // 服务器返回的错误
          if (error.response.status === 409) {
            this.error = "用户名已存在，请选择其他用户名";
          } else if (error.response.status === 400) {
            this.error = "输入信息不符合要求：" + (error.response.data.message || "请检查输入信息");
          } else {
            this.error = "注册失败：" + (error.response.data.message || "服务器错误");
          }
        } else if (error.request) {
          // 请求发送了但没有收到响应
          this.error = "网络连接失败，请检查网络后重试";
        } else {
          // 其他错误
          this.error = "注册失败：" + (error.message || "未知错误");
        }
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  background: radial-gradient(900px 600px at 20% 0%, rgba(255, 68, 0, 0.14), transparent 55%),
    radial-gradient(900px 600px at 90% 10%, rgba(99, 102, 241, 0.14), transparent 55%),
    var(--bg);
}

.register-card {
  background-color: rgba(255, 255, 255, 0.85);
  padding: 26px;
  border-radius: 14px;
  border: 1px solid rgba(17, 24, 39, 0.10);
  box-shadow: 0 16px 40px rgba(17, 24, 39, 0.12);
  backdrop-filter: blur(10px);
  width: 100%;
  max-width: 400px;
}

.register-card h2 {
  margin-top: 0;
  color: var(--text);
  text-align: center;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--muted);
  font-weight: 600;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid rgba(17, 24, 39, 0.12);
  border-radius: 10px;
  box-sizing: border-box;
  transition: border-color .15s ease, box-shadow .15s ease;
}

.form-group input:focus {
  outline: none;
  border-color: rgba(255, 68, 0, 0.55);
  box-shadow: 0 0 0 4px rgba(255, 68, 0, 0.12);
}

.register-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: var(--primary);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 1rem;
  margin-top: 1rem;
  font-weight: 700;
  box-shadow: 0 10px 22px rgba(255, 68, 0, 0.18);
  transition: transform .15s ease, background-color .15s ease;
}

.register-btn:hover:not(:disabled) {
  background-color: var(--primary-700);
  transform: translateY(-1px);
}

.register-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error {
  color: #e74c3c;
  margin: 0.5rem 0;
}

.login-link {
  text-align: center;
  margin-top: 1rem;
  color: var(--muted);
}

.login-link a {
  color: #ff4400;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
