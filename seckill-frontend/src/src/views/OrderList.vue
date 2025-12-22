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
    this.checkLoginStatus();
    if (this.isLoggedIn && this.userId) {
      this.loadOrders();
    }
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
    checkLoginStatus() {
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        const { id, username } = JSON.parse(userInfo);
        this.isLoggedIn = true;
        this.username = username;
        this.userId = id;
      }
    },
    logout() {
      localStorage.removeItem('userInfo');
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
  background-color: #f5f5f5;
}

.header {
  background-color: #ff4400;
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header h1 {
  margin: 0;
  font-size: 1.5rem;
}

.user-info {
  font-size: 1rem;
}

.user-info button {
  background: none;
  border: 1px solid white;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 0.5rem;
}

.user-info button:hover {
  background-color: white;
  color: #ff4400;
}

.main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.loading, .error, .no-orders {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.error {
  color: #ff4400;
}

.order-list-container {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.order-list-container h2 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #ff4400;
  padding-bottom: 0.5rem;
  margin-bottom: 1.5rem;
}

.orders-table-container {
  overflow-x: auto;
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th,
.orders-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.orders-table th {
  background-color: #f9f9f9;
  font-weight: bold;
  color: #666;
}

.status {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-weight: bold;
  font-size: 0.9rem;
}

.status.pending {
  background-color: #f0ad4e;
  color: white;
}

.status.paid {
  background-color: #5cb85c;
  color: white;
}

.status.shipped {
  background-color: #337ab7;
  color: white;
}

.status.completed {
  background-color: #5bc0de;
  color: white;
}

.status.canceled, .status.expired {
  background-color: #d9534f;
  color: white;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  margin-right: 0.5rem;
  margin-bottom: 0.5rem;
}

.btn-view {
  background-color: #5bc0de;
  color: white;
}

.btn-view:hover {
  background-color: #31b0d5;
}

.btn-pay {
  background-color: #ff4400;
  color: white;
}

.btn-pay:hover {
  background-color: #ff5511;
}

.btn-cancel {
  background-color: #d9534f;
  color: white;
}

.btn-cancel:hover {
  background-color: #c9302c;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
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