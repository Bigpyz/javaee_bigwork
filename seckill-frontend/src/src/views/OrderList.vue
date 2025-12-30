<template>
  <div class="order-list">
    <header class="header">
      <h1>在线秒杀系统</h1>
      <div class="user-info">
        <span v-if="!isLoggedIn">
          <router-link to="/register">注册</router-link> |
          <router-link to="/login">登录</router-link>
        </span>
        <span v-else>
          欢迎，{{ username }} | <button @click="logout">退出登录</button>
        </span>
      </div>
    </header>

    <main class="main">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else class="order-list-container">
        <h2>我的订单</h2>
        
        <div v-if="orders.length === 0" class="no-orders">
          暂无订单
        </div>
        
        <div v-else class="orders-table-container">
          <table class="orders-table">
            <thead>
              <tr>
                <th>订单编号</th>
                <th>秒杀价格</th>
                <th>购买数量</th>
                <th>订单金额</th>
                <th>订单状态</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in orders" :key="order.id">
                <td>{{ order.orderNo }}</td>
                <td>¥{{ order.seckillPrice }}</td>
                <td>{{ order.quantity }}</td>
                <td>¥{{ order.totalAmount }}</td>
                <td>
                  <span class="status" :class="getOrderStatusClass(order.status)">
                    {{ getOrderStatusText(order.status) }}
                  </span>
                </td>
                <td>{{ formatDate(order.createTime) }}</td>
                <td>
                  <button class="btn btn-view" @click="viewOrderDetail(order.id)">
                    查看详情
                  </button>
                  <button 
                    class="btn btn-pay" 
                    v-if="order.status === 0" 
                    @click="payOrder(order.id)"
                  >
                    立即支付
                  </button>
                  <button 
                    class="btn btn-cancel" 
                    v-if="order.status === 0" 
                    @click="cancelOrder(order.id)"
                  >
                    取消订单
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { getOrdersByUserId, payOrder, cancelOrder } from '../api/order';
import { me, logout as userLogout } from '../api/user';

export default {
  name: "OrderList",
  data() {
    return {
      orders: [],
      loading: false,
      error: '',
      isLoggedIn: false,
      username: '',
      userId: null
    };
  },
  mounted() {
    this.checkLoginStatus().then(() => {
      if (this.isLoggedIn && this.userId) {
        this.loadOrders();
      }
    })
  },
  methods: {
    async loadOrders() {
      this.loading = true;
      this.error = '';
      try {
        // 获取用户的订单列表
        const res = await getOrdersByUserId(this.userId);
        this.orders = res.data;
      } catch (err) {
        this.error = '加载订单列表失败，请稍后重试';
        console.error('加载订单列表失败:', err);
      } finally {
        this.loading = false;
      }
    },
    async payOrder(orderId) {
      try {
        await payOrder(orderId);
        alert('支付成功！');
        // 重新加载订单列表
        this.loadOrders();
      } catch (err) {
        alert('支付失败：' + (err.response?.data?.message || err.message || '未知错误'));
        console.error('支付失败:', err);
      }
    },
    async cancelOrder(orderId) {
      try {
        if (confirm('确定要取消订单吗？')) {
          await cancelOrder(orderId);
          alert('订单已取消！');
          // 重新加载订单列表
          this.loadOrders();
        }
      } catch (err) {
        alert('取消订单失败：' + (err.response?.data?.message || err.message || '未知错误'));
        console.error('取消订单失败:', err);
      }
    },
    viewOrderDetail(orderId) {
      this.$router.push(`/order/${orderId}`);
    },
    getOrderStatusText(status) {
      const statusMap = {
        0: '待支付',
        1: '已支付',
        2: '已发货',
        3: '已完成',
        4: '已取消',
        5: '超时未支付'
      };
      return statusMap[status] || '未知状态';
    },
    getOrderStatusClass(status) {
      const classMap = {
        0: 'pending',
        1: 'paid',
        2: 'shipped',
        3: 'completed',
        4: 'canceled',
        5: 'expired'
      };
      return classMap[status] || '';
    },
    async checkLoginStatus() {
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        try {
          const parsed = JSON.parse(userInfo);
          const { id, username } = parsed || {};
          if (id != null) {
            this.isLoggedIn = true;
            this.username = username;
            this.userId = id;
            return;
          }
        } catch (e) {
          localStorage.removeItem('userInfo');
        }
      }

      try {
        const res = await me();
        const u = res.data;
        if (u && u.id != null) {
          localStorage.setItem('userInfo', JSON.stringify(u));
          this.isLoggedIn = true;
          this.username = u.username;
          this.userId = u.id;
          return;
        }
      } catch (e) {
        // ignore
      }

      this.isLoggedIn = false;
      this.username = '';
      this.userId = null;
    },
    async logout() {
      try {
        await userLogout();
      } catch (e) {
        // ignore
      }
      localStorage.removeItem('userInfo');
      localStorage.removeItem('adminInfo');
      this.isLoggedIn = false;
      this.username = '';
      this.userId = null;
      this.$router.push('/login');
    },
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    }
  }
};
</script>

<style scoped>
.order-list {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
}

.header {
  background: linear-gradient(135deg, var(--primary), #ff6633);
  color: #fff;
  padding: 14px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 10px 30px rgba(17, 24, 39, 0.10);
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(10px);
}

.header h1 {
  margin: 0;
  font-size: 1.5rem;
  letter-spacing: 0.3px;
}

.user-info {
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info a {
  color: #fff;
  text-decoration: none;
  padding: 6px 10px;
  border-radius: 999px;
  transition: background-color .15s ease, transform .15s ease;
}

.user-info a:hover {
  background-color: rgba(255, 255, 255, 0.16);
  transform: translateY(-1px);
}

.user-info button {
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.45);
  color: #fff;
  padding: 6px 10px;
  border-radius: 10px;
  cursor: pointer;
  margin-left: 0.5rem;
  transition: transform .15s ease, background-color .15s ease;
}

.user-info button:hover {
  background-color: rgba(255, 255, 255, 0.24);
  transform: translateY(-1px);
}

.main {
  flex: 1;
  padding: 24px 16px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.loading, .error, .no-orders {
  text-align: center;
  padding: 60px 16px;
  color: var(--muted);
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
}

.error {
  color: var(--primary);
}

.order-list-container {
  background: rgba(255, 255, 255, 0.80);
  padding: 18px;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  backdrop-filter: blur(10px);
}

.order-list-container h2 {
  margin-top: 0;
  color: var(--text);
  padding-bottom: 10px;
  margin-bottom: 16px;
  border-bottom: 1px solid rgba(255, 68, 0, 0.22);
}

.orders-table-container {
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid rgba(17, 24, 39, 0.08);
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
  background: rgba(255, 255, 255, 0.92);
}

.orders-table th,
.orders-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid rgba(17, 24, 39, 0.08);
  vertical-align: middle;
}

.orders-table th {
  background-color: rgba(17, 24, 39, 0.03);
  font-weight: 800;
  color: var(--muted);
  font-size: 12px;
  letter-spacing: 0.3px;
}

.orders-table tbody tr:hover {
  background-color: rgba(255, 68, 0, 0.03);
}

.status {
  padding: 3px 10px;
  border-radius: 999px;
  font-weight: 800;
  font-size: 12px;
  border: 1px solid rgba(17, 24, 39, 0.10);
  background: rgba(17, 24, 39, 0.02);
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

.status.shipped {
  border-color: rgba(52, 152, 219, 0.35);
  background: rgba(52, 152, 219, 0.12);
  color: #3498db;
}

.status.completed {
  border-color: rgba(26, 188, 156, 0.35);
  background: rgba(26, 188, 156, 0.12);
  color: #1abc9c;
}

.status.canceled, .status.expired {
  border-color: rgba(231, 76, 60, 0.35);
  background: rgba(231, 76, 60, 0.10);
  color: #e74c3c;
}

.btn {
  padding: 8px 12px;
  border: 1px solid transparent;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  margin-right: 8px;
  margin-bottom: 8px;
  font-weight: 700;
  transition: transform .15s ease, background-color .15s ease, border-color .15s ease, box-shadow .15s ease;
}

.btn:hover {
  transform: translateY(-1px);
}

.btn-view {
  background-color: rgba(52, 152, 219, 0.12);
  color: #3498db;
  border-color: rgba(52, 152, 219, 0.25);
}

.btn-view:hover {
  background-color: rgba(52, 152, 219, 0.18);
}

.btn-pay {
  background-color: var(--primary);
  color: #fff;
  box-shadow: 0 10px 22px rgba(255, 68, 0, 0.18);
}

.btn-pay:hover {
  background-color: var(--primary-700);
}

.btn-cancel {
  background-color: rgba(231, 76, 60, 0.12);
  color: #e74c3c;
  border-color: rgba(231, 76, 60, 0.25);
  box-shadow: none;
}

.btn-cancel:hover {
  background-color: rgba(231, 76, 60, 0.18);
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
    padding: 14px 16px;
  }
  
  .main {
    padding: 1rem;
  }
  
  .order-list-container {
    padding: 1rem;
  }
  
  .orders-table {
    font-size: 0.9rem;
  }
  
  .orders-table th,
  .orders-table td {
    padding: 0.5rem;
  }
  
  .btn {
    padding: 0.3rem 0.6rem;
    font-size: 0.8rem;
  }
}
</style>