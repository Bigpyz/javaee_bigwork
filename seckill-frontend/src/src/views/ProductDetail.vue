<template>
  <div class="product-detail">
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
      <div v-else class="product-container">
        <div class="product-info">
          <div class="product-image">
            <img v-if="product.imageUrl" :src="getImageUrl(product.imageUrl)" :alt="product.name" class="product-img">
            <div v-else class="no-image">暂无图片</div>
          </div>
          <h2>{{ product.name }}</h2>
          <p class="description">{{ product.description }}</p>
          <div class="price-section">
            <p class="original-price">原价：¥{{ product.originalPrice }}</p>
            <p class="seckill-price">秒杀价：¥{{ activityProduct.seckillPrice }}</p>
          </div>
          <div class="stock-section">
            <p>库存：{{ activityProduct.seckillStock }}</p>
            <div class="quantity-control">
              <button @click="decreaseQuantity" :disabled="quantity <= 1">-</button>
              <span>{{ quantity }}</span>
              <button @click="increaseQuantity" :disabled="quantity >= activityProduct.seckillStock">+</button>
            </div>
          </div>
        </div>

        <div class="activity-info">
          <h3>秒杀活动信息</h3>
          <p class="activity-name">{{ activity.name }}</p>
          <p class="activity-time">
            活动时间：{{ formatDate(activity.startTime) }} - {{ formatDate(activity.endTime) }}
          </p>
          <div class="countdown-section">
            <div class="countdown-title">
              {{ countdownStatus === 'upcoming' ? '距离开始' : countdownStatus === 'active' ? '距离结束' : '活动已结束' }}
            </div>
            <div class="countdown" v-if="countdownStatus !== 'ended'">
              <span class="countdown-item">{{ countdown.hours }}<span class="unit">时</span></span>
              <span class="countdown-separator">:</span>
              <span class="countdown-item">{{ countdown.minutes }}<span class="unit">分</span></span>
              <span class="countdown-separator">:</span>
              <span class="countdown-item">{{ countdown.seconds }}<span class="unit">秒</span></span>
            </div>
            <div v-else class="countdown-ended">活动已结束</div>
          </div>

          <div class="captcha-section" v-if="showCaptcha">
            <h4>验证码</h4>
            <div class="captcha-container">
              <div class="captcha-display" :class="{ 'captcha-empty': !captchaCode }" @click="generateCaptcha">
                {{ captchaCode || '点击生成验证码' }}
              </div>
              <button @click="generateCaptcha" class="refresh-captcha">刷新</button>
            </div>
            <input 
              type="text" 
              v-model="userCaptcha" 
              placeholder="请输入验证码" 
              class="captcha-input"
              maxlength="4"
              style="letter-spacing: 5px; text-align: center;"
            >
          </div>

          <div class="seckill-action">
            <button 
              class="seckill-btn" 
              :disabled="!isLoggedIn || activityProduct.seckillStock <= 0 || countdownStatus !== 'active'"
              @click="startSeckill"
            >
              {{ !isLoggedIn ? '请先登录' : activityProduct.seckillStock <= 0 ? '库存不足' : countdownStatus !== 'active' ? '活动未开始' : '立即抢购' }}
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { getProductById, checkStock } from '../api/product';
import { getActivityById, recordVisit } from '../api/activity';
import { createSeckillOrder } from '../api/order';
import { generateCaptcha } from '../api/captcha';
import apiConfig from '../config/api';

export default {
  name: "ProductDetail",
  data() {
    return {
      product: {},
      activity: {},
      activityProduct: {},
      loading: false,
      error: '',
      isLoggedIn: false,
      username: '',
      quantity: 1,
      countdown: {
        hours: 0,
        minutes: 0,
        seconds: 0
      },
      countdownStatus: 'upcoming', // 'upcoming', 'active', 'ended'
      timer: null,
      showCaptcha: true,
      captchaId: '',
      captchaCode: '',
      userCaptcha: ''
    };
  },
  mounted() {
    this.checkLoginStatus();
    this.loadProductDetail();
    // 每秒更新倒计时
    this.timer = setInterval(() => {
      this.updateCountdown();
    }, 1000);
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    // 获取完整的图片URL
    getImageUrl(imagePath) {
      if (!imagePath) return '';
      // 如果已经是完整URL，直接返回
      if (imagePath.startsWith('http')) {
        return imagePath;
      }
      // 否则拼接基础URL
      return apiConfig.IMAGE_BASE_URL + imagePath;
    },
    async loadProductDetail() {
      this.loading = true;
      this.error = '';
      try {
        const productId = this.$route.params.id;
        const activityId = this.$route.query.activityId;

        if (!productId || !activityId) {
          this.error = '参数错误';
          return;
        }

        // 加载商品信息
        const productRes = await getProductById(productId);
        this.product = productRes.data;

        // 加载活动信息
        const activityRes = await getActivityById(activityId);
        this.activity = activityRes.data;

        // 获取活动商品信息
        const productsRes = await this.getActivityProducts(activityId);
        const activityProduct = productsRes.find(p => p.productId === parseInt(productId));
        if (!activityProduct) {
          this.error = '该商品不在此活动中';
          return;
        }
        this.activityProduct = activityProduct;

        // 初始化倒计时
        this.updateCountdown();
        
        // 记录访问日志
        try {
          const userInfo = JSON.parse(localStorage.getItem('userInfo'));
          await recordVisit(this.activity.id, userInfo?.id || null);
        } catch (err) {
          console.error('记录访问日志失败:', err);
          // 记录失败不影响用户体验，继续执行
        }
        
        // 页面加载完成后自动生成验证码
        if (this.isLoggedIn) {
          this.generateCaptcha();
        }
      } catch (err) {
        this.error = '加载商品详情失败，请稍后重试';
        console.error('加载商品详情失败:', err);
      } finally {
        this.loading = false;
      }
    },
    async getActivityProducts(activityId) {
      // 这里需要实现获取活动商品的API调用
      const { getProductsByActivityId } = await import('../api/activity');
      const res = await getProductsByActivityId(activityId);
      return res.data;
    },
    async generateCaptcha() {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo'));
        const res = await generateCaptcha(userInfo.id, this.product.id);
        // 获取后端返回的验证码ID和值
        this.captchaId = res.data.captchaId;
        this.captchaCode = res.data.captchaValue;
      } catch (err) {
        console.error('生成验证码失败:', err);
        this.captchaId = '';
        this.captchaCode = '';
        alert('生成验证码失败，请稍后重试');
      }
    },
    updateCountdown() {
      const now = new Date().getTime();
      const startTime = new Date(this.activity.startTime).getTime();
      const endTime = new Date(this.activity.endTime).getTime();

      if (now < startTime) {
        // 活动即将开始
        this.countdownStatus = 'upcoming';
        const diff = startTime - now;
        this.calculateCountdown(diff);
      } else if (now >= startTime && now <= endTime) {
        // 活动进行中
        this.countdownStatus = 'active';
        const diff = endTime - now;
        this.calculateCountdown(diff);
      } else {
        // 活动已结束
        this.countdownStatus = 'ended';
      }
    },
    calculateCountdown(milliseconds) {
      this.countdown.hours = Math.floor(milliseconds / (1000 * 60 * 60));
      this.countdown.minutes = Math.floor((milliseconds % (1000 * 60 * 60)) / (1000 * 60));
      this.countdown.seconds = Math.floor((milliseconds % (1000 * 60)) / 1000);
    },
    increaseQuantity() {
      if (this.quantity < this.activityProduct.seckillStock) {
        this.quantity++;
      }
    },
    decreaseQuantity() {
      if (this.quantity > 1) {
        this.quantity--;
      }
    },
    async startSeckill() {
      if (!this.isLoggedIn) {
        this.$router.push('/login');
        return;
      }

      if (this.activityProduct.seckillStock <= 0) {
        alert('库存不足');
        return;
      }

      if (this.countdownStatus !== 'active') {
        alert('活动未开始或已结束');
        return;
      }

      // 检查是否有验证码
      if (!this.captchaId) {
        try {
          await this.generateCaptcha();
        } catch (err) {
          alert('生成验证码失败，请稍后重试');
          return;
        }
      }
      
      if (!this.userCaptcha) {
        alert('请先填写验证码');
        return;
      }

      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo'));
        const order = await createSeckillOrder(
          userInfo.id,
          this.activity.id,
          this.product.id,
          this.quantity,
          this.captchaId,
          this.userCaptcha
        );
        alert('秒杀成功！');
        this.$router.push({ path: '/order/' + order.data.id });
      } catch (err) {
        console.error('秒杀失败:', err);
        alert('秒杀失败：' + (err.response?.data?.message || err.message || '未知错误'));
        // 重新生成验证码
        this.generateCaptcha();
        this.userCaptcha = '';
        // 重新加载商品信息以获取最新库存
        this.loadProductDetail();
      }
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
.product-detail {
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

.loading, .error {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.error {
  color: #ff4400;
}

.product-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.product-image {
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  background-color: #f9f9f9;
  border-radius: 8px;
  overflow: hidden;
}

.product-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.no-image {
  color: #999;
  font-size: 1.2rem;
}

.product-info h2 {
  margin-top: 0;
  color: #333;
}

.description {
  color: #666;
  line-height: 1.5;
  margin-bottom: 2rem;
}

.price-section {
  margin-bottom: 2rem;
}

.original-price {
  color: #999;
  text-decoration: line-through;
  font-size: 1.2rem;
  margin: 0.5rem 0;
}

.seckill-price {
  color: #ff4400;
  font-size: 2rem;
  font-weight: bold;
  margin: 0.5rem 0;
}

.stock-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.quantity-control button {
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  background-color: white;
  cursor: pointer;
  border-radius: 4px;
}

.quantity-control button:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.quantity-control span {
  width: 30px;
  text-align: center;
}

.activity-info {
  border-left: 2px solid #ff4400;
  padding-left: 2rem;
}

.activity-info h3 {
  margin-top: 0;
  color: #ff4400;
}

.activity-name {
  font-weight: bold;
  margin: 0.5rem 0;
}

.activity-time {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.countdown-section {
  margin-bottom: 2rem;
}

.countdown-title {
  font-weight: bold;
  margin-bottom: 0.5rem;
  color: #333;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.countdown-item {
  background-color: #ff4400;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  font-weight: bold;
  font-size: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.countdown-item .unit {
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.countdown-separator {
  font-size: 1.5rem;
  color: #ff4400;
}

.countdown-ended {
  color: #999;
  font-size: 1.2rem;
}

.seckill-action {
  text-align: center;
}

.seckill-btn {
  background-color: #ff4400;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.2rem;
  width: 100%;
  max-width: 300px;
}

.seckill-btn:hover:not(:disabled) {
  background-color: #ff5511;
}

.seckill-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.captcha-section {
  margin-bottom: 2rem;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.captcha-display {
  background-color: #f9f9f9;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
  cursor: pointer;
  border: 1px solid #ddd;
  min-width: 100px;
  text-align: center;
}

.captcha-display.captcha-empty {
  color: #999;
  font-weight: normal;
}

.captcha-display:hover {
  background-color: #f0f0f0;
}

.refresh-captcha {
  padding: 0.75rem 1rem;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #333;
}

.refresh-captcha:hover {
  background-color: #f5f5f5;
}

.captcha-input {
  width: 100%;
  max-width: 200px;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.captcha-input:focus {
  outline: none;
  border-color: #ff4400;
  box-shadow: 0 0 0 2px rgba(255, 68, 0, 0.1);
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .product-container {
    grid-template-columns: 1fr;
    padding: 1rem;
  }

  .activity-info {
    border-left: none;
    border-top: 2px solid #ff4400;
    padding-left: 0;
    padding-top: 2rem;
  }
  
  .captcha-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .captcha-input {
    max-width: none;
  }
}
</style>