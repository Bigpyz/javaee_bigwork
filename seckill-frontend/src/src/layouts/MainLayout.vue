<template>
  <div class="layout">
    <header class="nav">
      <div class="brand" @click="$router.push('/')">秒杀系统</div>
      <nav class="links">
        <router-link class="link" exact to="/">首页(商品)</router-link>
        <router-link class="link" to="/activities">活动页</router-link>
        <router-link class="link" to="/profile">用户信息</router-link>
      </nav>
      <div class="right">
        <span v-if="!isLoggedIn">
          <router-link class="link" to="/login">登录</router-link>
          <router-link class="link" to="/register">注册</router-link>
        </span>
        <span v-else class="welcome">
          <span class="username">{{ username }}</span>
          <router-link class="link" to="/orders">我的订单</router-link>
          <button class="logout" @click="logout">退出</button>
        </span>
      </div>
    </header>

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script>
import { me, logout } from '@/api/user'

export default {
  name: 'MainLayout',
  data() {
    return {
      isLoggedIn: false,
      username: ''
    }
  },
  mounted() {
    this.refreshUser()
    window.addEventListener('storage', this.refreshUser)
  },
  beforeDestroy() {
    window.removeEventListener('storage', this.refreshUser)
  },
  methods: {
    async refreshUser() {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr)
          this.isLoggedIn = true
          this.username = userInfo.username || String(userInfo.id || '')
          return
        } catch (e) {
          localStorage.removeItem('userInfo')
        }
      }

      try {
        const res = await me()
        const userInfo = res.data
        if (userInfo) {
          localStorage.setItem('userInfo', JSON.stringify(userInfo))
          this.isLoggedIn = true
          this.username = userInfo.username || String(userInfo.id || '')
          return
        }
      } catch (e) {
        // ignore
      }

      localStorage.removeItem('userInfo')
      this.isLoggedIn = false
      this.username = ''
    },
    async logout() {
      try {
        await logout()
      } catch (e) {
        // ignore
      }
      localStorage.removeItem('userInfo')
      localStorage.removeItem('adminInfo')
      await this.refreshUser()
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: transparent;
}

.nav {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.75);
  border-bottom: 1px solid rgba(17, 24, 39, 0.08);
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 30px rgba(17, 24, 39, 0.06);
}

.brand {
  font-weight: 700;
  color: var(--primary);
  cursor: pointer;
  margin-right: 16px;
  letter-spacing: 0.3px;
}

.links {
  display: flex;
  gap: 10px;
}

.link {
  text-decoration: none;
  color: rgba(31, 41, 55, 0.90);
  padding: 7px 12px;
  border-radius: 999px;
  border: 1px solid transparent;
  transition: background-color .15s ease, color .15s ease, border-color .15s ease;
}

.link:hover {
  background: rgba(255, 68, 0, 0.06);
  border-color: rgba(255, 68, 0, 0.18);
}

.link.router-link-exact-active,
.link.router-link-active {
  background: rgba(255, 68, 0, 0.1);
  color: var(--primary);
  border-color: rgba(255, 68, 0, 0.22);
}

.right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.welcome {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  color: var(--muted);
}

.logout {
  border: 1px solid var(--primary);
  background: var(--primary);
  color: #fff;
  padding: 7px 12px;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 700;
  transition: transform .15s ease, background-color .15s ease, border-color .15s ease;
}

.logout:hover {
  transform: translateY(-1px);
  background: var(--primary-700);
  border-color: var(--primary-700);
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 18px 16px;
}
</style>
