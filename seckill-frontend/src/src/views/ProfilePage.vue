<template>
  <div class="page">
    <h2>用户信息</h2>

    <div class="panel">
      <div class="actions">
        <button class="btn ghost" @click="loadMe">刷新</button>
        <button class="btn danger" @click="doLogout">退出登录</button>
      </div>

      <div v-if="error" class="err">{{ error }}</div>

      <div v-else-if="loading" class="state">加载中...</div>

      <div v-else-if="!profile" class="state">未登录，请先登录</div>

      <div v-if="profile" class="profile">
        <div><b>ID：</b>{{ profile.id }}</div>
        <div><b>用户名：</b>{{ profile.username }}</div>
        <div><b>状态：</b>{{ profile.status }}</div>
        <div><b>注册时间：</b>{{ profile.registerTime }}</div>
      </div>
    </div>

    <div class="panel">
      <div class="orders-header">
        <h3>该用户订单</h3>
        <button class="btn ghost" @click="loadOrders">刷新</button>
      </div>

      <div v-if="ordersError" class="err">{{ ordersError }}</div>
      <div v-else-if="ordersLoading" class="state">加载中...</div>
      <div v-else-if="orders.length === 0" class="state">暂无订单</div>

      <div v-else class="orders-table-container">
        <table class="orders-table">
          <thead>
            <tr>
              <th>订单号</th>
              <th>金额</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in orders" :key="o.id">
              <td>{{ o.orderNo }}</td>
              <td>¥{{ o.totalAmount }}</td>
              <td>
                <span class="status" :class="getOrderStatusClass(o.status)">{{ getOrderStatusText(o.status) }}</span>
              </td>
              <td>{{ formatDate(o.createTime) }}</td>
              <td>
                <button class="btn" @click="viewOrderDetail(o.id)">查看详情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="tip">
      说明：该页面基于 Session/Cookie 登录态展示当前用户信息。
    </div>
  </div>
</template>

<script>
import { me, logout } from '../api/user'
import { getOrdersByUserId } from '../api/order'

export default {
  name: 'ProfilePage',
  data() {
    return {
      profile: null,
      error: '',
      loading: false,
      orders: [],
      ordersLoading: false,
      ordersError: ''
    }
  },
  mounted() {
    this.loadMe()
  },
  methods: {
    async loadMe() {
      this.error = ''
      this.loading = true
      try {
        const res = await me()
        this.profile = res.data || null

        if (this.profile) {
          localStorage.setItem('userInfo', JSON.stringify(this.profile))
          await this.loadOrders()
        } else {
          this.orders = []
        }
      } catch (e) {
        localStorage.removeItem('userInfo')
        this.profile = null
        this.orders = []
        this.error = e.message || '加载失败'
      } finally {
        this.loading = false
      }
    },
    async loadOrders() {
      this.ordersError = ''
      this.orders = []
      const userId = this.profile && this.profile.id != null ? this.profile.id : null
      if (!userId) {
        this.ordersError = '未登录'
        return
      }
      this.ordersLoading = true
      try {
        const res = await getOrdersByUserId(userId)
        this.orders = res.data || []
      } catch (e) {
        this.ordersError = e.message || '加载订单失败'
      } finally {
        this.ordersLoading = false
      }
    },
    async doLogout() {
      try {
        await logout()
      } catch (e) {
        // ignore
      }
      localStorage.removeItem('userInfo')
      localStorage.removeItem('adminInfo')
      this.profile = null
      this.error = ''
      this.orders = []
      this.ordersLoading = false
      this.ordersError = ''
      this.$router.push('/login')
    },
    viewOrderDetail(orderId) {
      this.$router.push(`/order/${orderId}`)
    },
    getOrderStatusText(status) {
      const statusMap = {
        0: '待支付',
        1: '已支付',
        2: '已发货',
        3: '已完成',
        4: '已取消',
        5: '超时未支付'
      }
      return statusMap[status] || '未知状态'
    },
    getOrderStatusClass(status) {
      const classMap = {
        0: 'pending',
        1: 'paid',
        2: 'shipped',
        3: 'completed',
        4: 'canceled',
        5: 'expired'
      }
      return classMap[status] || ''
    },
    formatDate(dateString) {
      try {
        const date = new Date(dateString)
        return date.toLocaleString('zh-CN')
      } catch (e) {
        return dateString
      }
    },
  }
}
</script>

<style scoped>
.page {
  background: rgba(255, 255, 255, 0.75);
  border-radius: var(--radius);
  padding: 18px;
  border: 1px solid rgba(17, 24, 39, 0.10);
  box-shadow: 0 16px 40px rgba(17, 24, 39, 0.08);
  backdrop-filter: blur(10px);
}

.panel {
  margin-top: 12px;
  border: 1px solid rgba(17, 24, 39, 0.10);
  border-radius: var(--radius);
  padding: 14px;
  background: rgba(255, 255, 255, 0.90);
}

.orders-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.state {
  margin-top: 10px;
  color: var(--muted);
}

.orders-table-container {
  margin-top: 10px;
  overflow: auto;
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th,
.orders-table td {
  border-bottom: 1px solid rgba(17, 24, 39, 0.10);
  padding: 10px 8px;
  text-align: left;
  font-size: 13px;
}

.orders-table th {
  color: var(--muted);
  font-weight: 800;
  background: rgba(17, 24, 39, 0.02);
}

.status {
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid #eee;
  color: #666;
  background: #fafafa;
}

.status.pending {
  border-color: rgba(241, 196, 15, 0.4);
  background: rgba(241, 196, 15, 0.12);
  color: #b58900;
}

.status.paid {
  border-color: rgba(46, 204, 113, 0.4);
  background: rgba(46, 204, 113, 0.12);
  color: #2ecc71;
}

.status.canceled,
.status.expired {
  border-color: rgba(231, 76, 60, 0.35);
  background: rgba(231, 76, 60, 0.10);
  color: #e74c3c;
}

.row {
  display: grid;
  grid-template-columns: 110px 1fr;
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;
}

.label {
  color: var(--muted);
  font-weight: 700;
}

.input {
  height: 36px;
  border: 1px solid rgba(17, 24, 39, 0.12);
  border-radius: 10px;
  padding: 0 12px;
  transition: border-color .15s ease, box-shadow .15s ease;
}

.input:focus {
  outline: none;
  border-color: rgba(255, 68, 0, 0.55);
  box-shadow: 0 0 0 4px rgba(255, 68, 0, 0.12);
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.btn {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid var(--primary);
  background: var(--primary);
  color: #fff;
  cursor: pointer;
  font-weight: 800;
  transition: transform .15s ease, background-color .15s ease, border-color .15s ease;
}

.btn:hover {
  transform: translateY(-1px);
  background: var(--primary-700);
  border-color: var(--primary-700);
}

.btn.ghost {
  background: #fff;
  color: var(--primary);
  border-color: rgba(255, 68, 0, 0.35);
}

.btn.danger {
  background: #fff;
  color: #e74c3c;
  border-color: rgba(231, 76, 60, 0.45);
}

.err {
  margin-top: 10px;
  color: #e74c3c;
}

.profile {
  margin-top: 12px;
  background: rgba(17, 24, 39, 0.02);
  border: 1px solid rgba(17, 24, 39, 0.10);
  border-radius: var(--radius);
  padding: 12px;
  display: grid;
  gap: 6px;
}

.tip {
  margin-top: 14px;
  color: var(--muted);
  font-size: 12px;
}
</style>
