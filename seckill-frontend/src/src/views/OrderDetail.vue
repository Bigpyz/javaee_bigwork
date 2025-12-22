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
};
</script>

<style scoped>
.order-detail {
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
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.loading, .error {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.error {
  color: #ff4400;
}

.order-container {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.order-container h2 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #ff4400;
  padding-bottom: 0.5rem;
  margin-bottom: 1.5rem;
}

.order-info, .product-info, .payment-info {
  margin-bottom: 2rem;
}

.order-item, .product-item, .payment-item {
  display: flex;
  margin-bottom: 0.75rem;
}

.label {
  width: 120px;
  font-weight: bold;
  color: #666;
}

.value {
  color: #333;
}

.status {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-weight: bold;
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

.seckill-price {
  color: #ff4400;
  font-weight: bold;
}

.total-amount {
  color: #ff4400;
  font-weight: bold;
  font-size: 1.2rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.action-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
}

.pay-btn {
  background-color: #ff4400;
  color: white;
}

.pay-btn:hover {
  background-color: #ff5511;
}

.cancel-btn {
  background-color: #d9534f;
  color: white;
}

.cancel-btn:hover {
  background-color: #c9302c;
}

.back-btn {
  background-color: #5bc0de;
  color: white;
}

.back-btn:hover {
  background-color: #31b0d5;
}
</style>