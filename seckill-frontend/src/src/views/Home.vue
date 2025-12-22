<template>
  <div class="home">
    <header class="header">
      <h1>在线秒杀系统</h1>
      <div class="header-actions">
        <div class="user-info">
          <span v-if="!isLoggedIn">
            <router-link to="/register">用户注册</router-link> |
            <router-link to="/login">用户登录</router-link> |
            <router-link to="/admin/login" class="admin-link">管理员登录</router-link>
          </span>
          <span v-else>
            欢迎，{{ username }} | <router-link to="/orders" class="order-link">我的订单</router-link> | <button @click="logout">退出登录</button>
          </span>
        </div>
      </div>
    </header>

    <main class="main">
      <!-- 活动时间轴 -->
      <section class="seckill-section">
        <div class="section-header">
          <h2 class="section-title">秒杀活动时间轴</h2>
          
        </div>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else-if="allActivities.length === 0" class="empty">
          暂无秒杀活动
        </div>
        <div v-else class="activity-timeline">
          <!-- 时间轴中心线 -->
          <div class="timeline-line"></div>
          
          <!-- 活动节点 -->
          <div 
            v-for="activity in allActivities" 
            :key="activity.id" 
            class="timeline-item"
            :class="{ 'active': isActiveActivity(activity) }"
          >
            <!-- 时间节点 -->
            <div class="timeline-node">
              <div class="node-dot"></div>
              <div class="node-time">{{ formatTime(activity.startTime) }}</div>
            </div>
            
            <!-- 活动内容 -->
            <div class="timeline-content">
              <div class="activity-header">
                <h3>{{ activity.name }}</h3>
                <div class="activity-badge" :class="{ 'upcoming': !isActiveActivity(activity) }">
                  {{ isActiveActivity(activity) ? '进行中' : '即将开始' }}
                </div>
              </div>
              <p class="activity-time">
                {{ isActiveActivity(activity) ? 
                  `开始时间：${formatDate(activity.startTime)} | 结束时间：${formatDate(activity.endTime)}` : 
                  `开始时间：${formatDate(activity.startTime)}` 
                }}
              </p>
              
              <!-- 倒计时 -->
              <div class="countdown-section">
                <div class="countdown-label">
                  {{ isActiveActivity(activity) ? '距离结束还有' : '距离开始还有' }}
                </div>
                <div class="countdown" v-html="formatCountdown(isActiveActivity(activity) ? activity.endTime : activity.startTime)"></div>
              </div>
              
              <!-- 活动产品列表 - 支持水平滑动 -->
              <div v-if="isActiveActivity(activity) && activity.products" class="products-container">
                <div class="products-scroll">
                  <div 
                    v-for="product in activity.products" 
                    :key="product.productId" 
                    class="product-card"
                  >
                    <div class="product-image">
                      <img v-if="product.imageUrl" :src="'http://8.130.65.25:28080' + product.imageUrl" :alt="product.productName" class="product-img">
                      <div v-else class="no-image">暂无图片</div>
                    </div>
                    <div class="product-info">
                      <h4>{{ product.productName }}</h4>
                      <p class="price">
                        <span class="original-price">¥{{ product.originalPrice }}</span>
                        <span class="seckill-price">¥{{ product.seckillPrice }}</span>
                      </p>
                      <p class="stock">库存：{{ product.seckillStock }}</p>
                    </div>
                    <div class="product-actions">
                      <button 
                        class="seckill-btn" 
                        :disabled="!isLoggedIn || product.seckillStock <= 0"
                        @click="goToProductDetail(product.productId, activity.id)"
                      >
                        立即秒杀
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import { getActiveActivities, getUpcomingActivities, getProductsByActivityId } from '../api/activity';
import { getProductById } from '../api/product';

export default {
  name: "Home",
  data() {
    return {
      activeActivities: [],
      upcomingActivities: [],
      loading: false,
      error: '',
      isLoggedIn: false,
      username: '',
      countdownTimer: null,
      activityRefreshTimer: null,
      // 排序相关数据
      currentSort: 'startTime', // 默认按开始时间排序
      sortOptions: [
        { value: 'startTime', label: '按开始时间排序' },
        { value: 'endTime', label: '按结束时间排序' },
        { value: 'name', label: '按活动名称排序' }
      ]
    };
  },
  computed: {
    // 合并所有活动并按用户选择的方式排序
    allActivities() {
      // 合并活动并创建副本
      const activities = [...this.activeActivities, ...this.upcomingActivities];
      
      // 创建排序后的新数组
      const sortedActivities = [...activities];
      
      // 根据当前排序方式排序
      if (this.currentSort === 'startTime') {
        sortedActivities.sort((a, b) => {
          try {
            const timeA = new Date(a.startTime).getTime();
            const timeB = new Date(b.startTime).getTime();
            return timeA - timeB;
          } catch (e) {
            console.error('时间排序错误:', e);
            return 0;
          }
        });
      } else if (this.currentSort === 'endTime') {
        sortedActivities.sort((a, b) => {
          try {
            const timeA = new Date(a.endTime).getTime();
            const timeB = new Date(b.endTime).getTime();
            return timeA - timeB;
          } catch (e) {
            console.error('时间排序错误:', e);
            return 0;
          }
        });
      } else if (this.currentSort === 'name') {
        sortedActivities.sort((a, b) => {
          try {
            return (a.name || '').localeCompare(b.name || '', 'zh-CN');
          } catch (e) {
            console.error('名称排序错误:', e);
            return 0;
          }
        });
      }
      
      return sortedActivities;
    }
  },
  mounted() {
    this.checkLoginStatus();
    this.loadActivities();
    
    // 每秒更新倒计时
    this.countdownTimer = setInterval(() => {
      this.$forceUpdate();
    }, 1000);
    
    // 每30秒自动刷新活动状态，确保状态实时更新
    this.activityRefreshTimer = setInterval(() => {
      this.loadActivities(false);
    }, 30000);
  },
  beforeDestroy() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
    }
    if (this.activityRefreshTimer) {
      clearInterval(this.activityRefreshTimer);
    }
  },
  methods: {
    async loadActivities(showLoading = true) {
      this.error = '';
      if (showLoading) {
        this.loading = true;
      }
      try {
        // 加载正在进行的活动
        const activeRes = await getActiveActivities();
        if (activeRes.data) {
          const newActiveActivities = [];
          
          // 遍历新获取的活动，只更新或添加状态变化的活动
          for (const newActivity of activeRes.data) {
            // 查找是否已存在该活动
            const existingActivityIndex = this.activeActivities.findIndex(act => act.id === newActivity.id);
            
            if (existingActivityIndex >= 0) {
              // 活动已存在，只更新必要信息
              const existingActivity = this.activeActivities[existingActivityIndex];
              
              // 检查产品是否有变化
              let needUpdateProducts = false;
              if (existingActivity.products) {
                const productsRes = await getProductsByActivityId(newActivity.id);
                if (productsRes.data) {
                  // 比较产品数量或库存是否有变化
                  if (productsRes.data.length !== existingActivity.products.length) {
                    needUpdateProducts = true;
                  } else {
                    // 检查库存是否有变化
                    for (let i = 0; i < productsRes.data.length; i++) {
                      const newProduct = productsRes.data[i];
                      const existingProduct = existingActivity.products.find(p => p.productId === newProduct.productId);
                      if (!existingProduct || existingProduct.seckillStock !== newProduct.seckillStock) {
                        needUpdateProducts = true;
                        break;
                      }
                    }
                  }
                  
                  if (needUpdateProducts) {
                    // 只更新有变化的产品详情
                    const productsWithDetails = await Promise.all(
                      productsRes.data.map(async product => {
                        // 查找是否已存在该产品的详情
                        const existingProduct = existingActivity.products.find(p => p.productId === product.productId);
                        if (existingProduct) {
                          // 只更新库存
                          return {
                            ...existingProduct,
                            seckillStock: product.seckillStock
                          };
                        } else {
                          // 新产品，获取完整详情
                          const productRes = await getProductById(product.productId);
                          return {
                            ...product,
                            productName: productRes.data.name,
                            originalPrice: productRes.data.originalPrice,
                            imageUrl: productRes.data.imageUrl
                          };
                        }
                      })
                    );
                    
                    newActiveActivities.push({
                      ...existingActivity,
                      ...newActivity,
                      products: productsWithDetails
                    });
                  } else {
                    // 产品无变化，直接使用现有产品数据
                    newActiveActivities.push({
                      ...existingActivity,
                      ...newActivity
                    });
                  }
                } else {
                  newActiveActivities.push({
                    ...existingActivity,
                    ...newActivity
                  });
                }
              } else {
                // 首次加载产品详情
                const productsRes = await getProductsByActivityId(newActivity.id);
                if (productsRes.data) {
                  const productsWithDetails = await Promise.all(
                    productsRes.data.map(async product => {
                      const productRes = await getProductById(product.productId);
                      return {
                        ...product,
                        productName: productRes.data.name,
                        originalPrice: productRes.data.originalPrice,
                        imageUrl: productRes.data.imageUrl
                      };
                    })
                  );
                  newActiveActivities.push({
                    ...newActivity,
                    products: productsWithDetails
                  });
                } else {
                  newActiveActivities.push(newActivity);
                }
              }
            } else {
              // 新活动，需要获取完整信息
              const productsRes = await getProductsByActivityId(newActivity.id);
              if (productsRes.data) {
                const productsWithDetails = await Promise.all(
                  productsRes.data.map(async product => {
                    const productRes = await getProductById(product.productId);
                    return {
                      ...product,
                      productName: productRes.data.name,
                      originalPrice: productRes.data.originalPrice,
                      imageUrl: productRes.data.imageUrl
                    };
                  })
                );
                newActiveActivities.push({
                  ...newActivity,
                  products: productsWithDetails
                });
              } else {
                newActiveActivities.push(newActivity);
              }
            }
          }
          
          // 更新活动数据，但保持引用不变（避免Vue重新渲染整个列表）
          this.activeActivities.splice(0, this.activeActivities.length, ...newActiveActivities);
        }

        // 加载即将开始的活动
        const upcomingRes = await getUpcomingActivities();
        if (upcomingRes.data) {
          const newUpcomingActivities = [];
          
          // 调试：输出活动数据结构
          console.log('活动数据结构:', { activeActivities: this.activeActivities, upcomingActivities: upcomingRes.data });
          
          // 遍历新获取的即将开始活动
          for (const newActivity of upcomingRes.data) {
            // 查找是否已存在该活动
            const existingActivityIndex = this.upcomingActivities.findIndex(act => act.id === newActivity.id);
            
            if (existingActivityIndex >= 0) {
              // 活动已存在，直接使用现有数据
              newUpcomingActivities.push(this.upcomingActivities[existingActivityIndex]);
            } else {
              // 新活动，添加到列表
              newUpcomingActivities.push(newActivity);
            }
          }
          
          // 更新活动数据，但保持引用不变
          this.upcomingActivities.splice(0, this.upcomingActivities.length, ...newUpcomingActivities);
        }
      } catch (err) {
        this.error = '加载活动失败，请稍后重试';
        console.error('加载活动失败:', err);
      } finally {
        if (showLoading) {
          this.loading = false;
        }
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
    },
    getCountdown(endTimeString) {
      const endTime = new Date(endTimeString).getTime();
      const now = new Date().getTime();
      const diff = endTime - now;

      if (diff <= 0) return '活动已结束';

      const hours = Math.floor(diff / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((diff % (1000 * 60)) / 1000);

      return `${hours}时${minutes}分${seconds}秒`;
    },
    formatCountdown(endTimeString) {
      const endTime = new Date(endTimeString).getTime();
      const now = new Date().getTime();
      const diff = endTime - now;

      if (diff <= 0) {
        return '<div class="countdown-item">00</div><div class="countdown-separator">:</div><div class="countdown-item">00</div><div class="countdown-separator">:</div><div class="countdown-item">00</div>';
      }

      const hours = Math.floor(diff / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((diff % (1000 * 60)) / 1000);

      // 格式化数字，确保两位数
      const formattedHours = hours.toString().padStart(2, '0');
      const formattedMinutes = minutes.toString().padStart(2, '0');
      const formattedSeconds = seconds.toString().padStart(2, '0');

      // 返回HTML结构
      return `
        <div class="countdown-item">
          <div>${formattedHours}</div>
          <div class="countdown-unit">时</div>
        </div>
        <div class="countdown-separator">:</div>
        <div class="countdown-item">
          <div>${formattedMinutes}</div>
          <div class="countdown-unit">分</div>
        </div>
        <div class="countdown-separator">:</div>
        <div class="countdown-item">
          <div>${formattedSeconds}</div>
          <div class="countdown-unit">秒</div>
        </div>
      `;
    },
    goToProductDetail(productId, activityId) {
      this.$router.push({
        path: '/product/' + productId,
        query: { activityId }
      });
    },

    isActiveActivity(activity) {
      // 判断活动是否处于进行中状态
      const now = new Date();
      const startTime = new Date(activity.startTime);
      const endTime = new Date(activity.endTime);
      return now >= startTime && now <= endTime;
    },
    formatTime(dateString) {
      // 格式化时间为 HH:MM 格式
      const date = new Date(dateString);
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    },
    handleSortChange() {
      // 调试排序功能
      console.log('排序方式已改变:', this.currentSort);
      console.log('排序后的活动列表:', this.allActivities);
    }
  }
};
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #ff4400, #ff6633);
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 16px rgba(255, 68, 0, 0.3);
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(10px);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  letter-spacing: 1px;
  background: linear-gradient(90deg, #ffffff, #ffe0d5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-info {
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-info a, .user-info .order-link {
  color: white;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 0.3rem 0.6rem;
  border-radius: 6px;
}

.user-info a:hover, .user-info .order-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.user-info button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 0.3rem 0.6rem;
  border-radius: 6px;
  cursor: pointer;
  margin-left: 0.5rem;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  font-weight: 500;
}

.user-info button:hover {
  background-color: white;
  color: #ff4400;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.admin-link {
  color: #ff9800;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.admin-link:hover {
  color: #f57c00;
}

.main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.seckill-section {
  margin-bottom: 2rem;
  background-color: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.section-title {
  margin-top: 0;
  color: #333;
  font-size: 1.8rem;
  font-weight: 700;
  position: relative;
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  font-size: 1rem;
}

.sort-select {
  padding: 0.5rem 1rem;
  border: 2px solid #ff4400;
  border-radius: 8px;
  background-color: white;
  color: #333;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.sort-select:hover {
  border-color: #ff5511;
  box-shadow: 0 2px 8px rgba(255, 68, 0, 0.15);
}

.sort-select:focus {
  outline: none;
  border-color: #ff5511;
  box-shadow: 0 0 0 3px rgba(255, 68, 0, 0.1);
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #ff4400, #ff6633);
  border-radius: 2px;
}

.loading, .error, .empty {
  text-align: center;
  padding: 3rem;
  color: #666;
  font-size: 1.1rem;
}

.error {
  color: #ff4400;
}

/* 垂直时间轴样式 */
.activity-timeline {
  position: relative;
  padding: 2rem 0 2rem 7rem;
  overflow: visible;
}

/* 时间轴中心线 */
.timeline-line {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 60px;
  width: 4px;
  background: linear-gradient(to bottom, #ff4400, #ff6633, #ccc);
  border-radius: 2px;
}

/* 时间轴节点 */
.timeline-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 2rem;
  margin-bottom: 2.5rem;
  transition: all 0.3s ease;
  width: 100%;
  box-sizing: border-box;
}

/* 时间节点 */
.timeline-node {
  position: absolute;
  top: 0;
  left: -5rem;
  display: flex;
  align-items: center;
  gap: 0.8rem;
  z-index: 2;
  overflow: visible;
  white-space: nowrap;
  width: auto;
}

.node-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #ff4400;
  border: 4px solid white;
  box-shadow: 0 0 0 2px #ff4400;
  transition: all 0.3s ease;
  z-index: 3;
}

.node-time {
  font-size: 0.85rem;
  font-weight: 600;
  color: #333;
  background: white;
  padding: 0.3rem 0.6rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
  position: relative;
  z-index: 3;
  transform: translateX(0);
}

/* 活动内容 */
.timeline-content {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
  flex: 1;
  min-height: 280px;
  width: 100%;
  box-sizing: border-box;
}

.timeline-item:hover {
  transform: translateY(-10px);
}

.timeline-item:hover .timeline-content {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.timeline-item:hover .node-dot {
  transform: scale(1.3);
  box-shadow: 0 0 0 4px rgba(255, 68, 0, 0.2);
}

/* 即将开始的活动样式 */
.timeline-item:not(.active) .node-dot {
  background: #1890ff;
  box-shadow: 0 0 0 2px #1890ff;
}

.timeline-item:not(.active) .timeline-content {
  border-color: #e6f7ff;
  background-color: #f0f9ff;
}

.timeline-item:not(.active) .activity-badge {
  background-color: #1890ff;
  border-color: #1890ff;
}

/* 活动头部 */
.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.activity-header h3 {
  margin: 0;
  color: #ff4400;
  font-size: 1.3rem;
  font-weight: 700;
  flex: 1;
}

.timeline-item:not(.active) .activity-header h3 {
  color: #1890ff;
}

.activity-badge {
  display: inline-block;
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  background-color: #ff4400;
  color: white;
  font-size: 0.85rem;
  font-weight: 600;
  border: 2px solid #ff4400;
  white-space: nowrap;
}

.activity-badge.upcoming {
  background-color: #1890ff;
  border-color: #1890ff;
}

/* 活动时间 */
.activity-time {
  color: #666;
  font-size: 0.95rem;
  margin-bottom: 1rem;
  font-weight: 500;
  line-height: 1.6;
  white-space: nowrap;
}

/* 倒计时部分 */
.countdown-section {
  margin: 1rem 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.5rem;
}

.countdown-label {
  font-size: 0.95rem;
  font-weight: 600;
  color: #ff4400;
  margin: 0;
}

.timeline-item:not(.active) .countdown-label {
  color: #1890ff;
}

.countdown {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.countdown-item {
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  color: #ff4400;
  padding: 0.6rem;
  border-radius: 8px;
  font-weight: 700;
  font-size: 1.1rem;
  min-width: 60px;
  width: auto;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 2px solid #ff4400;
  position: relative;
  overflow: hidden;
  animation: pulse 2s infinite;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.2rem;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4px 12px rgba(255, 68, 0, 0.1);
  }
  50% {
    box-shadow: 0 6px 18px rgba(255, 68, 0, 0.2);
  }
}

.countdown-separator {
  color: #ff4400;
  font-weight: 700;
  font-size: 1.3rem;
  line-height: 1.4;
  margin-bottom: 0.3rem;
}

.countdown-unit {
  font-size: 0.75rem;
  text-transform: uppercase;
  font-weight: 500;
  margin-top: 0.1rem;
}

/* 即将开始的活动倒计时样式 */
.timeline-item:not(.active) .countdown-item {
  color: #1890ff;
  border-color: #1890ff;
  animation: pulse-blue 2s infinite;
}

@keyframes pulse-blue {
  0%, 100% {
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.1);
  }
  50% {
    box-shadow: 0 6px 18px rgba(24, 144, 255, 0.2);
  }
}

.timeline-item:not(.active) .countdown-separator {
  color: #1890ff;
}

/* 产品列表 - 支持水平滑动 */
.products-container {
  overflow: hidden;
  margin-top: 1.5rem;
  position: relative;
  width: 100%;
}

.products-scroll {
  display: flex;
  gap: 1rem;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 1rem;
  scrollbar-width: thin;
  scrollbar-color: #ff4400 #f5f5f5;
  -webkit-overflow-scrolling: touch;
  width: 100%;
  min-width: 100%;
}

.products-scroll::-webkit-scrollbar {
  height: 8px;
}

.products-scroll::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 4px;
}

.products-scroll::-webkit-scrollbar-thumb {
  background: #ff4400;
  border-radius: 4px;
}

.products-scroll::-webkit-scrollbar-thumb:hover {
  background: #ff5511;
}

/* 产品卡片 */
.product-card {
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 1.5rem;
  background: white;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
  flex: 0 0 auto;
  width: 280px;
}

.product-card::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 40px 40px 0;
  border-color: transparent #ff4400 transparent transparent;
  opacity: 0.8;
  transform: scale(0.9);
  transition: all 0.4s ease;
}

.product-card:hover {
  transform: translateY(-10px) scale(1.02);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
  border-color: #ff4400;
}

.product-card:hover::after {
  opacity: 1;
  transform: scale(1);
}

/* 产品图片 */
.product-image {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f5f5f5;
}

.product-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.product-card:hover .product-img {
  transform: scale(1.05);
}

.no-image {
  color: #999;
  font-size: 14px;
  text-align: center;
  padding: 20px;
}

/* 产品信息 */
.product-info {
  margin-bottom: 1rem;
}

.product-info h4 {
  margin-top: 0;
  color: #333;
  font-size: 1.1rem;
  font-weight: 600;
  height: 3rem;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 1rem;
}

/* 价格样式 */
.price {
  margin: 1rem 0;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.original-price {
  color: #999;
  text-decoration: line-through;
  font-size: 0.9rem;
}

.seckill-price {
  color: #ff4400;
  font-size: 1.4rem;
  font-weight: bold;
  display: flex;
  align-items: baseline;
}

.seckill-price::before {
  content: '¥';
  font-size: 0.9rem;
  margin-right: 2px;
}

/* 库存 */
.stock {
  color: #666;
  font-size: 0.95rem;
  margin: 1rem 0;
  font-weight: 500;
}

.stock::before {
  content: '📦 ';
  margin-right: 4px;
}

/* 秒杀按钮 */
.seckill-btn {
  background-color: #ff4400;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  cursor: pointer;
  width: 100%;
  font-size: 1rem;
  font-weight: 600;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
}

.seckill-btn:hover:not(:disabled) {
  background-color: #ff5511;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 68, 0, 0.3);
}

.seckill-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media (max-width: 1024px) {
  .main {
    padding: 1.5rem;
  }
  
  /* 垂直时间轴桌面样式 */
  .activity-timeline {
    padding-left: 5rem;
  }
  
  .timeline-line {
    left: 50px;
  }
  
  .timeline-node {
    left: -4rem;
  }
  
  .timeline-content {
    padding: 1.3rem;
  }
  
  .product-card {
    padding: 1.3rem;
    width: 260px;
  }
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
    padding: 1rem;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 0.8rem;
    width: 100%;
    align-items: center;
  }
  
  .user-info {
    flex-direction: column;
    gap: 0.5rem;
    width: 100%;
  }
}
  
  .main {
    padding: 1rem;
  }
  
  .seckill-section {
    padding: 1rem;
  }
  
  /* 垂直时间轴移动端样式 */
  .activity-timeline {
    padding-left: 4.5rem;
  }
  
  .timeline-line {
    left: 40px;
  }
  
  .timeline-node {
    left: -3.5rem;
  }
  
  .timeline-content {
    padding: 1.2rem;
    min-height: 250px;
  }
  
  .activity-header h3 {
    font-size: 1.1rem;
  }
  
  .activity-time {
    font-size: 0.9rem;
  }
  
  .product-card {
    padding: 1rem;
    width: 240px;
  }
  
  .seckill-price {
    font-size: 1.2rem;
  }


/* 活动卡片增强样式 */
.activity-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  height: 6px;
  width: 100%;
  background: linear-gradient(90deg, #ff4400, #ff6633);
  opacity: 1;
  transform: scaleX(1);
  transform-origin: left;
  transition: transform 0.4s ease;
}



/* 添加更多视觉效果 */
.seckill-section {
  position: relative;
  overflow: hidden;
}

.seckill-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(255, 68, 0, 0.05) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(255, 102, 51, 0.05) 0%, transparent 20%);
  z-index: -1;
}

/* 丰富产品卡片内容 */
.product-card::after {
  content: '🔥 限时秒杀';
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 80px 80px 0;
  border-color: transparent #ff4400 transparent transparent;
  opacity: 0.8;
  transform: scale(0.8);
  transition: all 0.4s ease;
  color: white;
  font-size: 0.8rem;
  font-weight: bold;
  text-align: center;
  line-height: 30px;
  padding-right: 10px;
  box-sizing: border-box;
}

/* 移动端适配 */


@media (max-width: 480px) {
  .header h1 {
    font-size: 1.5rem;
  }
  
  /* 垂直时间轴小屏样式 */
  .activity-timeline {
    padding-left: 4rem;
  }
  
  .timeline-line {
    left: 30px;
  }
  
  .timeline-node {
    left: -3rem;
  }
  
  .timeline-content {
    padding: 1rem;
    min-height: 230px;
  }
  
  .activity-header h3 {
    font-size: 1rem;
  }
  
  .product-card {
    width: 220px;
    padding: 0.8rem;
  }
  
  .product-card h4 {
    font-size: 1rem;
  }
  
  .countdown {
    font-size: 0.85rem;
  }
  
  .countdown-item {
    min-width: 50px;
    padding: 0.5rem;
    font-size: 1rem;
  }
  
  .seckill-btn {
    padding: 0.4rem 0.8rem;
    font-size: 0.9rem;
  }
  
  .node-time {
    font-size: 0.8rem;
  }
}


</style>
