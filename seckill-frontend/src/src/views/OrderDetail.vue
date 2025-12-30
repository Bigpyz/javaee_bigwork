<template>
  <div class="order-detail">
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
      <div v-else class="order-container">
        <h2>订单详情</h2>
        <div class="order-info">
          <div class="order-item">
            <span class="label">订单编号：</span>
            <span class="value">{{ order.orderNo }}</span>
          </div>
          <div class="order-item">
            <span class="label">订单状态：</span>
            <span class="value status" :class="getOrderStatusClass(order.status)">
              {{ getOrderStatusText(order.status) }}
            </span>
          </div>
          <div class="order-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ formatDate(order.createTime) }}</span>
          </div>
          <div class="order-item" v-if="order.paymentTime">
            <span class="label">支付时间：</span>
            <span class="value">{{ formatDate(order.paymentTime) }}</span>
          </div>
        </div>

        <div class="product-info">
          <h3>商品信息</h3>
          <div class="product-item">
            <span class="label">商品名称：</span>
            <span class="value">{{ product.name }}</span>
          </div>
          <div class="product-item">
            <span class="label">秒杀价格：</span>
            <span class="value seckill-price">¥{{ order.seckillPrice }}</span>
          </div>
          <div class="product-item">
            <span class="label">购买数量：</span>
            <span class="value">{{ order.quantity }}</span>
          </div>
        </div>

        <div class="payment-info">
          <h3>支付信息</h3>
          <div class="payment-item">
            <span class="label">订单金额：</span>
            <span class="value total-amount">¥{{ order.totalAmount }}</span>
          </div>
        </div>

        <div class="action-buttons">
          <button 
            class="action-btn pay-btn" 
            v-if="order.status === 0" 
            @click="payOrder"
          >
            立即支付
          </button>
          <button 
            class="action-btn cancel-btn" 
            v-if="order.status === 0" 
            @click="cancelOrder"
          >
            取消订单
          </button>
          <button 
            class="action-btn back-btn" 
            @click="goBack"
          >
            返回首页
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { getOrderById } from '../api/order';
import { getProductById } from '../api/product';
import { payOrder, cancelOrder } from '../api/order';

export default {
  name: "OrderDetail",
  data() {
    return {
      order: {},
      product: {},
      loading: false,
      error: '',
      isLoggedIn: false,
      username: ''
    };
  },
  mounted() {
    this.checkLoginStatus();
    this.loadOrderDetail();
  },
  methods: {
    async loadOrderDetail() {
      this.loading = true;
      this.error = '';
      try {
        const orderId = this.$route.params.id;
        if (!orderId) {
          this.error = '参数错误';
          return;
        }

        // 加载订单信息
        const orderRes = await getOrderById(orderId);
        this.order = orderRes.data;

        // 加载商品信息
        const productRes = await getProductById(this.order.productId);
        this.product = productRes.data;
      } catch (err) {
        this.error = '加载订单详情失败，请稍后重试';
        console.error('加载订单详情失败:', err);
      } finally {
        this.loading = false;
      }
    },
    async payOrder() {
      try {
        await payOrder(this.order.id);
        alert('支付成功！');
        // 重新加载订单信息
        this.loadOrderDetail();
      } catch (err) {
        alert('支付失败：' + (err.response?.data?.message || err.message || '未知错误'));
        console.error('支付失败:', err);
      }
    },
    async cancelOrder() {
      try {
        if (confirm('确定要取消订单吗？')) {
          await cancelOrder(this.order.id);
          alert('订单已取消！');
          // 重新加载订单信息
          this.loadOrderDetail();
        }
      } catch (err) {
        alert('取消订单失败：' + (err.response?.data?.message || err.message || '未知错误'));
        console.error('取消订单失败:', err);
      }
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
    goBack() {
      this.$router.push('/');
    },
    checkLoginStatus() {
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        const { username } = JSON.parse(userInfo);
        this.isLoggedIn = true;
        this.username = username;
      }
    },
    logout() {
      localStorage.removeItem('userInfo');
      this.isLoggedIn = false;
      this.username = '';
      this.$router.push('/login');
    },
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    }
  }
}
</script>

<style scoped>
.order-detail {
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
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.loading, .error {
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

.order-container {
  background: rgba(255, 255, 255, 0.80);
  padding: 18px;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  backdrop-filter: blur(10px);
}

.order-container h2 {
  margin-top: 0;
  color: var(--text);
  padding-bottom: 10px;
  margin-bottom: 16px;
  border-bottom: 1px solid rgba(255, 68, 0, 0.22);
}

.order-info, .product-info, .payment-info {
  margin-bottom: 2rem;
}

.order-item, .product-item, .payment-item {
  display: flex;
  margin-bottom: 0.75rem;
  gap: 12px;
}

.label {
  width: 120px;
  font-weight: bold;
  color: var(--muted);
}

.value {
  color: var(--text);
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

.seckill-price {
  color: var(--primary);
  font-weight: 800;
}

.total-amount {
  color: var(--primary);
  font-weight: 900;
  font-size: 20px;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
  flex-wrap: wrap;
}

.action-btn {
  padding: 10px 12px;
  border: 1px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 800;
  transition: transform .15s ease, background-color .15s ease, border-color .15s ease, box-shadow .15s ease;
}

.action-btn:hover {
  transform: translateY(-1px);
}

.pay-btn {
  background-color: var(--primary);
  color: #fff;
  box-shadow: 0 10px 22px rgba(255, 68, 0, 0.18);
}

.pay-btn:hover {
  background-color: var(--primary-700);
}

.cancel-btn {
  background-color: rgba(231, 76, 60, 0.12);
  color: #e74c3c;
  border-color: rgba(231, 76, 60, 0.25);
}

.cancel-btn:hover {
  background-color: rgba(231, 76, 60, 0.18);
}

.back-btn {
  background-color: rgba(52, 152, 219, 0.12);
  color: #3498db;
  border-color: rgba(52, 152, 219, 0.25);
}

.back-btn:hover {
  background-color: rgba(52, 152, 219, 0.18);
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
    padding: 14px 16px;
  }

  .main {
    padding: 16px;
  }

  .order-container {
    padding: 14px;
  }

  .order-item, .product-item, .payment-item {
    flex-direction: column;
    gap: 6px;
  }

  .label {
    width: auto;
  }
}
</style>