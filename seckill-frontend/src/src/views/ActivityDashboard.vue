<template>
  <div class="activity-dashboard">
    <h2>活动数据看板</h2>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="!stats" class="error">未找到活动数据</div>
    <div v-else>
      <div class="dashboard-header">
        <h3>{{ stats.activityName }}</h3>
        <button @click="goBack" class="btn btn-primary">返回活动列表</button>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-title">页面浏览量 (PV)</div>
          <div class="stat-value">{{ stats.pv }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">独立访客数 (UV)</div>
          <div class="stat-value">{{ stats.uv }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">参与人数</div>
          <div class="stat-value">{{ stats.participants }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">售出商品数</div>
          <div class="stat-value">{{ stats.soldQuantity }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">成交总额 (GMV)</div>
          <div class="stat-value">¥{{ formatNumber(stats.totalGmv) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">待支付订单</div>
          <div class="stat-value">{{ stats.pendingOrders }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">已完成订单</div>
          <div class="stat-value">{{ stats.completedOrders }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">已取消订单</div>
          <div class="stat-value">{{ stats.cancelledOrders }}</div>
        </div>
      </div>

      <div class="charts-container">
        <div class="chart-card">
          <h4>订单状态分布</h4>
          <div class="chart">
            <div class="chart-item">
              <div class="chart-label">待支付</div>
              <div class="chart-bar">
                <div class="chart-fill pending" :style="{ width: getStatusPercentage(stats.pendingOrders) + '%' }"></div>
              </div>
              <div class="chart-value">{{ stats.pendingOrders }}</div>
            </div>
            <div class="chart-item">
              <div class="chart-label">已完成</div>
              <div class="chart-bar">
                <div class="chart-fill completed" :style="{ width: getStatusPercentage(stats.completedOrders) + '%' }"></div>
              </div>
              <div class="chart-value">{{ stats.completedOrders }}</div>
            </div>
            <div class="chart-item">
              <div class="chart-label">已取消</div>
              <div class="chart-bar">
                <div class="chart-fill cancelled" :style="{ width: getStatusPercentage(stats.cancelledOrders) + '%' }"></div>
              </div>
              <div class="chart-value">{{ stats.cancelledOrders }}</div>
            </div>
          </div>
        </div>

        <div class="chart-card">
          <h4>销售数据概览</h4>
          <div class="sales-summary">
            <div class="sales-item">
              <div class="sales-label">总销售额</div>
              <div class="sales-value">¥{{ formatNumber(stats.totalGmv) }}</div>
            </div>
            <div class="sales-item">
              <div class="sales-label">平均订单金额</div>
              <div class="sales-value">¥{{ formatNumber(getAverageOrderValue()) }}</div>
            </div>
            <div class="sales-item">
              <div class="sales-label">订单转化率</div>
              <div class="sales-value">{{ getConversionRate() }}%</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getActivityStats } from '../api/activity'

export default {
  data() {
    return {
      stats: null,
      loading: true,
      error: null,
      refreshTimer: null
    }
  },
  mounted() {
    this.loadActivityStats()
    // 每30秒自动刷新数据
    this.refreshTimer = setInterval(() => {
      this.loadActivityStats()
    }, 30000)
  },
  beforeDestroy() {
    // 清理定时器
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
    }
  },
  methods: {
    loadActivityStats() {
      const activityId = this.$route.params.id
      this.loading = true
      this.error = null
      
      getActivityStats(activityId)
        .then(response => {
          this.stats = response.data
          this.loading = false
          
        })
        .catch(error => {
          console.error('加载活动统计数据失败:', error)
          this.error = '加载活动数据失败，请检查网络连接或联系管理员'
          this.loading = false
        })
    },
    formatNumber(number) {
      if (number === null || number === undefined) return '0'
      return number.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    getStatusPercentage(value) {
      const total = this.stats.pendingOrders + this.stats.completedOrders + this.stats.cancelledOrders
      if (total === 0) return 0
      return ((value / total) * 100).toFixed(0)
    },
    getAverageOrderValue() {
      const totalOrders = this.stats.pendingOrders + this.stats.completedOrders + this.stats.cancelledOrders
      if (totalOrders === 0) return 0
      return this.stats.totalGmv / totalOrders
    },
    getConversionRate() {
      if (this.stats.uv === 0) return 0
      const totalOrders = this.stats.pendingOrders + this.stats.completedOrders + this.stats.cancelledOrders
      return ((totalOrders / this.stats.uv) * 100).toFixed(2)
    },
    goBack() {
      this.$router.push('/admin/activity-management')
    }
  }
}
</script>

<style scoped>
.activity-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.activity-dashboard h2 {
  color: var(--text);
  margin-bottom: 25px;
  font-size: 1.8rem;
  font-weight: 600;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 68, 0, 0.22);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 16px;
  background-color: #fff;
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.dashboard-header h3 {
  color: var(--text);
  margin: 0;
  font-size: 1.4rem;
  font-weight: 600;
}

/* Button Styles */
.btn {
  padding: 10px 14px;
  border: 1px solid transparent;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  transition: transform .15s ease, background-color .15s ease, border-color .15s ease, box-shadow .15s ease;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}

.btn:hover {
  transform: translateY(-1px);
}

.btn-primary {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
  box-shadow: 0 10px 22px rgba(255, 68, 0, 0.14);
}

.btn-primary:hover {
  background: var(--primary-700);
  border-color: var(--primary-700);
  box-shadow: 0 14px 28px rgba(255, 68, 0, 0.18);
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background-color: #fff;
  border-radius: var(--radius);
  padding: 18px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  text-align: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: var(--primary);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.stat-title {
  font-size: 14px;
  color: var(--muted);
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--text);
  margin-bottom: 5px;
}

/* Charts Container */
.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 25px;
}

.chart-card {
  background-color: #fff;
  border-radius: var(--radius);
  padding: 18px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: var(--shadow);
}

.chart-card h4 {
  margin-bottom: 20px;
  color: var(--text);
  font-size: 1.2rem;
  font-weight: 600;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(17, 24, 39, 0.08);
}

/* Chart Styles */
.chart {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-item {
  display: flex;
  align-items: center;
  gap: 20px;
}

.chart-label {
  width: 90px;
  font-size: 14px;
  font-weight: 500;
  color: var(--muted);
}

.chart-bar {
  flex: 1;
  height: 25px;
  background-color: rgba(17, 24, 39, 0.06);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chart-fill {
  height: 100%;
  border-radius: 12px;
  transition: width 0.8s ease;
  position: relative;
  overflow: hidden;
}

.chart-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

.chart-fill.pending {
  background: linear-gradient(90deg, #f1c40f, #ffd54f);
}

.chart-fill.completed {
  background: linear-gradient(90deg, #2ecc71, #4caf50);
}

.chart-fill.cancelled {
  background: linear-gradient(90deg, #e74c3c, #f44336);
}

.chart-value {
  width: 60px;
  text-align: right;
  font-size: 14px;
  font-weight: bold;
  color: var(--text);
}

/* Sales Summary */
.sales-summary {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.sales-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px;
  background: rgba(17, 24, 39, 0.02);
  border-radius: 10px;
  transition: all 0.3s ease;
  border: 1px solid rgba(17, 24, 39, 0.08);
}

.sales-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.sales-label {
  font-size: 14px;
  color: var(--muted);
  font-weight: 500;
}

.sales-value {
  font-size: 20px;
  font-weight: bold;
  color: var(--primary);
}

/* Loading and Error States */
.loading, .error {
  text-align: center;
  padding: 80px 20px;
  font-size: 18px;
  color: var(--muted);
  background-color: #fff;
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
}

.loading::after {
  content: '';
  display: block;
  width: 40px;
  height: 40px;
  margin: 20px auto;
  border: 3px solid #f3f3f3;
  border-top: 3px solid var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive Design */
@media (max-width: 768px) {
  .activity-dashboard {
    padding: 10px;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 15px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .charts-container {
    grid-template-columns: 1fr;
  }
  
  .chart-item {
    gap: 10px;
  }
  
  .chart-label {
    width: 70px;
    font-size: 12px;
  }
  
  .chart-value {
    width: 50px;
    font-size: 12px;
  }
  
  .sales-value {
    font-size: 16px;
  }
}
</style>
