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
        // 获取后端返回的验证码ID与题目（不返回明文答案）
        this.captchaId = res.data.captchaId;
        this.captchaCode = res.data.question;
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

.product-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  background: rgba(255, 255, 255, 0.80);
  padding: 18px;
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  backdrop-filter: blur(10px);
}

.product-image {
  margin-bottom: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  background: rgba(17, 24, 39, 0.03);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border);
}

.product-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.no-image {
  color: var(--muted);
  font-size: 14px;
}

.product-info h2 {
  margin-top: 0;
  color: var(--text);
  margin-bottom: 8px;
}

.description {
  color: var(--muted);
  line-height: 1.65;
  margin: 0 0 18px;
}

.price-section {
  margin-bottom: 18px;
  display: grid;
  gap: 6px;
}

.original-price {
  color: rgba(107, 114, 128, 0.95);
  text-decoration: line-through;
  font-size: 14px;
  margin: 0;
}

.seckill-price {
  color: var(--primary);
  font-size: 26px;
  font-weight: 800;
  margin: 0;
}

.stock-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  padding-top: 10px;
  border-top: 1px solid rgba(17, 24, 39, 0.08);
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.quantity-control button {
  width: 34px;
  height: 34px;
  border: 1px solid var(--border);
  background-color: #fff;
  cursor: pointer;
  border-radius: 10px;
  transition: transform .15s ease, background-color .15s ease, border-color .15s ease;
}

.quantity-control button:hover:not(:disabled) {
  transform: translateY(-1px);
  border-color: rgba(255, 68, 0, 0.35);
  background-color: rgba(255, 68, 0, 0.04);
}

.quantity-control button:disabled {
  background-color: rgba(17, 24, 39, 0.03);
  cursor: not-allowed;
}

.quantity-control span {
  width: 30px;
  text-align: center;
  font-weight: 700;
}

.activity-info {
  border-left: 1px solid rgba(255, 68, 0, 0.25);
  padding-left: 18px;
}

.activity-info h3 {
  margin-top: 0;
  color: var(--primary);
}

.activity-name {
  font-weight: bold;
  margin: 0.5rem 0;
}

.activity-time {
  color: var(--muted);
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.countdown-section {
  margin-bottom: 2rem;
}

.countdown-title {
  font-weight: bold;
  margin-bottom: 0.5rem;
  color: var(--text);
}

.countdown {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.countdown-item {
  background-color: var(--primary);
  color: #fff;
  padding: 0.5rem 1rem;
  border-radius: 10px;
  font-weight: bold;
  font-size: 1.25rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.countdown-item .unit {
  font-size: 0.8rem;
  margin-top: 0.25rem;
  opacity: 0.9;
}

.countdown-separator {
  font-size: 1.5rem;
  color: var(--primary);
}

.countdown-ended {
  color: var(--muted);
  font-size: 14px;
}

.seckill-action {
  text-align: center;
}

.seckill-btn {
  background-color: var(--primary);
  color: #fff;
  border: none;
  padding: 12px 16px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 16px;
  width: 100%;
  max-width: 300px;
  font-weight: 800;
  box-shadow: 0 12px 26px rgba(255, 68, 0, 0.18);
  transition: transform .15s ease, background-color .15s ease, box-shadow .15s ease;
}

.seckill-btn:hover:not(:disabled) {
  background-color: var(--primary-700);
  transform: translateY(-1px);
  box-shadow: 0 16px 34px rgba(255, 68, 0, 0.22);
}

.seckill-btn:disabled {
  background-color: rgba(17, 24, 39, 0.25);
  cursor: not-allowed;
  box-shadow: none;
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
  background-color: rgba(17, 24, 39, 0.03);
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 800;
  color: var(--text);
  cursor: pointer;
  border: 1px solid var(--border);
  min-width: 100px;
  text-align: center;
}

.captcha-display.captcha-empty {
  color: #999;
  font-weight: normal;
}

.captcha-display:hover {
  background-color: rgba(255, 68, 0, 0.06);
}

.refresh-captcha {
  padding: 10px 12px;
  background-color: #fff;
  border: 1px solid var(--border);
  border-radius: 12px;
  cursor: pointer;
  color: var(--text);
  font-weight: 700;
  transition: transform .15s ease, background-color .15s ease;
}

.refresh-captcha:hover {
  background-color: rgba(255, 68, 0, 0.06);
  transform: translateY(-1px);
}

.captcha-input {
  width: 100%;
  max-width: 200px;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 12px;
  font-size: 1rem;
}

.captcha-input:focus {
  outline: none;
  border-color: rgba(255, 68, 0, 0.55);
  box-shadow: 0 0 0 4px rgba(255, 68, 0, 0.12);
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
    padding: 14px 16px;
  }

  .product-container {
    grid-template-columns: 1fr;
    padding: 14px;
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