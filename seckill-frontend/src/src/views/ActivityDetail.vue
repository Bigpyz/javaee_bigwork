<template>
  <div class="activity-detail">
    <h2>活动详情</h2>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="activity" class="activity-info">
      <!-- 查看模式 -->
      <div v-if="!showEditForm">
        <div class="activity-header">
          <h3>{{ activity.name }}</h3>
          <span :class="getStatusClass(activity.status)">
            {{ getStatusText(activity.status) }}
          </span>
        </div>
        
        <div class="activity-timeline">
          <div class="timeline-item">
            <div class="timeline-label">开始时间</div>
            <div class="timeline-value">{{ formatDate(activity.startTime) }}</div>
          </div>
          <div class="timeline-item">
            <div class="timeline-label">结束时间</div>
            <div class="timeline-value">{{ formatDate(activity.endTime) }}</div>
          </div>
        </div>
        
        <div class="activity-rule">
          <h4>活动规则</h4>
          <p>{{ activity.rule || '暂无规则说明' }}</p>
        </div>
        
        <div class="activity-products">
          <h4>秒杀商品</h4>
          <div v-if="!activity.activityProducts || activity.activityProducts.length === 0" class="no-products">
            暂无关联商品
          </div>
          <div v-else class="products-grid">
            <div v-for="product in activity.activityProducts" :key="product.activityProductId || product.productId || product.id" class="product-card" @click="goToProduct(product)">
              <div class="product-image">
              <img :src="product.imageDisplayUrl || 'https://via.placeholder.com/200x200?text=暂无图片'" :alt="product.name" @error="handleImageError">
              <div class="product-id">商品ID: {{ product.productId || product.id }}</div>
            </div>
              <div class="product-info">
                <div class="product-header">
                  <h5>{{ product.name }}</h5>
                  <span :class="getProductStatusClass(product.status)">{{ getProductStatusText(product.status) }}</span>
                </div>
                <p class="product-description">{{ product.description || '暂无描述' }}</p>
                <div class="product-prices">
                  <span class="original-price">¥{{ (product.originalPrice || 0).toFixed(2) }}</span>
                  <span class="seckill-price">¥{{ (product.seckillPrice || 0).toFixed(2) }}</span>
                  <span class="discount" v-if="product.originalPrice && product.seckillPrice">节省{{ ((1 - product.seckillPrice / product.originalPrice) * 100).toFixed(1) }}%</span>
                </div>
                <div class="product-stock">
                  <div class="stock-info">
                    <span>秒杀库存: {{ product.seckillStock || 0 }}</span>
                    <span>每人限购: {{ product.limitPerUser || 0 }}</span>
                  </div>
                  
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="action-buttons">
          <button @click="goBack" class="btn btn-secondary">返回</button>
          <template v-if="isAdminRoute">
            <button v-if="activity.status === 0" @click="editActivity" class="btn btn-warning">编辑活动</button>
            <button v-if="activity.status === 1" @click="closeActivity" class="btn btn-danger">关闭活动</button>
            <button @click="viewDashboard" class="btn btn-success">查看数据</button>
          </template>
        </div>
      </div>
      
      <!-- 编辑模式 -->
      <div v-else class="activity-edit">
        <div class="activity-header">
          <h3>编辑活动</h3>
        </div>
        
        <form @submit.prevent="saveActivity">
          <div class="form-group">
            <label>活动名称</label>
            <input type="text" v-model="formData.name" required class="form-control" :class="{ 'is-invalid': errors.name }">
            <div v-if="errors.name" class="invalid-feedback">{{ errors.name }}</div>
          </div>
          
          <div class="form-row">
            <div class="form-group col-md-6">
              <label>开始时间</label>
              <input type="datetime-local" v-model="formData.startTime" required class="form-control" :class="{ 'is-invalid': errors.startTime }">
              <div v-if="errors.startTime" class="invalid-feedback">{{ errors.startTime }}</div>
            </div>
            
            <div class="form-group col-md-6">
              <label>结束时间</label>
              <input type="datetime-local" v-model="formData.endTime" required class="form-control" :class="{ 'is-invalid': errors.endTime }">
              <div v-if="errors.endTime" class="invalid-feedback">{{ errors.endTime }}</div>
            </div>
          </div>
          
          <div class="form-group">
            <label>活动规则</label>
            <textarea v-model="formData.rule" class="form-control" rows="3"></textarea>
          </div>
          
          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="editLoading">
              <span v-if="editLoading" class="spinner-border spinner-border-sm mr-2"></span>
              保存修改
            </button>
            <button type="button" @click="cancelEdit" class="btn btn-secondary" :disabled="editLoading">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { getActivityById, closeActivity, updateActivity, getProductsByActivityId, getProductById } from '../api/activity'
import apiConfig from '../config/api'

export default {
  computed: {
    isAdminRoute() {
      return this.$route && typeof this.$route.path === 'string' && this.$route.path.startsWith('/admin')
    }
  },
  data() {
    return {
      activity: null,
      loading: true,
      error: '',
      showEditForm: false,
      formData: {
        id: null,
        name: '',
        startTime: '',
        endTime: '',
        rule: '',
        activityProducts: []
      },
      editLoading: false,
      errors: {
        name: '',
        startTime: '',
        endTime: '',
        activityProducts: []
      }
    }
  },
  mounted() {
    this.loadActivity()
  },
  methods: {
    goToProduct(product) {
      const productId = product && (product.productId != null ? product.productId : product.id)
      if (!productId) {
        return
      }
      this.$router.push({
        path: `/product/${productId}`,
        query: { activityId: this.activity?.id }
      })
    },
    loadActivity() {
      const id = this.$route.params.id
      console.log('加载活动详情，ID:', id)
      this.loading = true
      
      // 设置超时处理
      const timeout = setTimeout(() => {
        if (this.loading) {
          console.log('请求超时，停止加载状态')
          this.loading = false
          this.error = '请求超时，请检查网络连接或稍后重试'
        }
      }, 10000) // 10秒超时
      
      // 先加载活动详情
      getActivityById(id)
        .then(activityResponse => {
          console.log('活动详情加载成功:', activityResponse.data)
          this.activity = activityResponse.data
          
          // 然后加载关联的商品信息
          console.log('开始加载商品信息...')
          return getProductsByActivityId(id)
        })
        .then(async productsResponse => {
          console.log('商品信息加载成功:', productsResponse.data)
          // 检查第一个商品的数据结构
          if (productsResponse.data && productsResponse.data.length > 0) {
            console.log('第一个商品的完整数据结构:', productsResponse.data[0])
            console.log('第一个商品的originalPrice:', productsResponse.data[0].originalPrice)
            console.log('第一个商品的seckillPrice:', productsResponse.data[0].seckillPrice)
          }
          
          // 为每个商品获取完整信息和图片URL
          const productsWithDetails = await Promise.all(
            productsResponse.data.map(async product => {
              try {
                const productId = product.productId != null ? product.productId : product.id
                // 获取完整的商品信息
                const productDetailResponse = await getProductById(productId)
                const fullProduct = productDetailResponse.data
                
                // 合并原有信息和详细信息
                const mergedProduct = {
                  ...product,
                  activityProductId: product.id,
                  ...fullProduct,
                  productId: productId
                }
                
                // 获取商品图片
                mergedProduct.imageDisplayUrl = await this.getProductImage(mergedProduct)
                console.log(`商品${productId}的完整信息:`, mergedProduct)
                console.log(`商品${productId}的图片URL:`, mergedProduct.imageDisplayUrl)
                
                return mergedProduct
              } catch (error) {
                const fallbackProductId = product.productId != null ? product.productId : product.id
                console.error(`获取商品${fallbackProductId}详细信息失败:`, error)
                product.imageDisplayUrl = 'https://via.placeholder.com/200x200?text=商品信息加载失败'
                return product
              }
            })
          )
          
          // 将处理后的商品信息添加到活动对象中
          this.activity.activityProducts = productsWithDetails
          clearTimeout(timeout)
          this.loading = false
          console.log('数据加载完成，loading状态已设置为false')
        })
        .catch(error => {
          console.error('加载活动详情失败:', error)
          console.error('错误详情:', error.response)
          clearTimeout(timeout)
          this.error = '加载活动详情失败: ' + (error.response?.data?.message || error.message)
          this.loading = false
          console.log('发生错误，loading状态已设置为false')
        })
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    getStatusClass(status) {
      switch (status) {
        case 0: return 'status-pending'
        case 1: return 'status-active'
        case 2: return 'status-ended'
        default: return ''
      }
    },
    getStatusText(status) {
      switch (status) {
        case 0: return '未开始'
        case 1: return '进行中'
        case 2: return '已结束'
        default: return '未知状态'
      }
    },
    goBack() {
      this.$router.go(-1)
    },
    editActivity() {
      // 填充表单数据
      this.formData = {
        ...this.activity,
        startTime: this.formatDateTimeLocal(new Date(this.activity.startTime)),
        endTime: this.formatDateTimeLocal(new Date(this.activity.endTime))
      }
      this.showEditForm = true
    },
    cancelEdit() {
      this.showEditForm = false
      this.resetForm()
    },
    resetForm() {
      this.errors = {
        name: '',
        startTime: '',
        endTime: '',
        activityProducts: []
      }
    },
    closeActivity() {
      if (confirm('确定要关闭此活动吗？')) {
        closeActivity(this.activity.id)
          .then(() => {
            alert('活动已关闭')
            this.loadActivity()
          })
          .catch(error => {
            console.error('关闭活动失败:', error)
            alert('关闭活动失败')
          })
      }
    },
    viewDashboard() {
      this.$router.push(`/admin/activity-dashboard/${this.activity.id}`)
    },
    validateForm() {
      let isValid = true
      this.errors = {
        name: '',
        startTime: '',
        endTime: '',
        activityProducts: []
      }

      if (!this.formData.name.trim()) {
        this.errors.name = '活动名称不能为空'
        isValid = false
      }

      if (!this.formData.startTime) {
        this.errors.startTime = '开始时间不能为空'
        isValid = false
      }

      if (!this.formData.endTime) {
        this.errors.endTime = '结束时间不能为空'
        isValid = false
      }

      const start = new Date(this.formData.startTime)
      const end = new Date(this.formData.endTime)
      const now = new Date()

      if (start >= end) {
        this.errors.endTime = '结束时间必须晚于开始时间'
        isValid = false
      }

      if (start < now) {
        this.errors.startTime = '开始时间不能早于当前时间'
        isValid = false
      }

      return isValid
    },
    saveActivity() {
      if (!this.validateForm()) {
        return
      }

      this.editLoading = true

      // 转换日期格式
      const updatedActivity = {
        ...this.formData,
        startTime: new Date(this.formData.startTime),
        endTime: new Date(this.formData.endTime)
      }

      updateActivity(this.activity.id, updatedActivity)
        .then(response => {
          this.activity = response.data
          this.showEditForm = false
          this.resetForm()
          alert('活动信息更新成功')
        })
        .catch(error => {
          console.error('更新活动失败:', error)
          alert('更新活动失败，请重试')
        })
        .finally(() => {
          this.editLoading = false
        })
    },
    formatDateTimeLocal(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day}T${hours}:${minutes}`
    },
    getProductStatusClass(status) {
      switch (status) {
        case 0: return 'product-status-draft'
        case 1: return 'product-status-active'
        case 2: return 'product-status-inactive'
        case 3: return 'product-status-deleted'
        default: return ''
      }
    },
    getProductStatusText(status) {
      switch (status) {
        case 0: return '草稿'
        case 1: return '上架'
        case 2: return '下架'
        case 3: return '已删除'
        default: return '未知状态'
      }
    },
    getSalesProgress(product) {
      const sold = product.soldCount || 0
      const total = product.seckillStock || 1
      return total > 0 ? Math.min((sold / total) * 100, 100) : 0
    },
    async getProductImage(product) {
  try {
    // 如果商品有图片URL，直接使用
    const imageUrl = product.imageUrl || product.image || product.picture || product.pic || ''
    if (imageUrl) {
      // 如果是相对路径，添加基础URL
      if (imageUrl.startsWith('/')) {
        return apiConfig.IMAGE_BASE_URL + imageUrl
      }
      // 如果是完整URL，直接返回
      if (imageUrl.startsWith('http')) {
        return imageUrl
      }
      // 其他情况，尝试作为相对路径处理
      return apiConfig.IMAGE_BASE_URL + '/' + imageUrl
    }
    
    // 如果都没有，返回占位图片
    return 'https://via.placeholder.com/200x200?text=暂无图片'
  } catch (error) {
    console.error('获取商品图片时出错:', error)
    return 'https://via.placeholder.com/200x200?text=图片加载失败'
  }
},
    handleImageError(event) {
      // 图片加载失败时，使用占位图片
      event.target.src = 'https://via.placeholder.com/200x200?text=图片加载失败'
    }
  }
}
</script>

<style scoped>
.activity-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.activity-detail h2 {
  color: var(--text);
  margin-bottom: 25px;
  font-size: 1.8rem;
  font-weight: 600;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 68, 0, 0.22);
}

.loading, .error {
  text-align: center;
  padding: 50px;
  font-size: 18px;
  color: var(--muted);
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
}

.error {
  color: #e74c3c;
}

.activity-info {
  background-color: rgba(255, 255, 255, 0.90);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 18px;
  border: 1px solid var(--border);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.activity-header h3 {
  margin: 0;
  color: var(--text);
  font-size: 1.6rem;
}

.status-pending {
  background-color: rgba(241, 196, 15, 0.12);
  color: #b58900;
  border: 1px solid rgba(241, 196, 15, 0.30);
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 800;
}

.status-active {
  background-color: rgba(46, 204, 113, 0.12);
  color: #2ecc71;
  border: 1px solid rgba(46, 204, 113, 0.25);
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 800;
}

.status-ended {
  background-color: rgba(231, 76, 60, 0.10);
  color: #e74c3c;
  border: 1px solid rgba(231, 76, 60, 0.25);
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 800;
}

.activity-timeline {
  display: flex;
  gap: 40px;
  margin-bottom: 30px;
  padding: 20px 0;
  border-bottom: 1px solid rgba(17, 24, 39, 0.08);
}

.timeline-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.timeline-label {
  font-size: 14px;
  color: var(--muted);
  font-weight: 700;
}

.timeline-value {
  font-size: 16px;
  color: var(--text);
  font-weight: 600;
}

.activity-rule {
  margin-bottom: 30px;
  padding: 20px 0;
  border-bottom: 1px solid rgba(17, 24, 39, 0.08);
}

.activity-rule h4 {
  margin-bottom: 10px;
  color: var(--text);
  font-size: 1.2rem;
}

.activity-rule p {
  color: var(--muted);
  line-height: 1.6;
  margin: 0;
}

.activity-products h4 {
  margin-bottom: 20px;
  color: var(--text);
  font-size: 1.2rem;
}

.no-products {
  text-align: center;
  padding: 30px;
  color: var(--muted);
  font-size: 16px;
  background-color: rgba(17, 24, 39, 0.02);
  border-radius: var(--radius);
  border: 1px solid rgba(17, 24, 39, 0.08);
  margin-bottom: 20px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.product-card {
  background-color: #fff;
  border-radius: var(--radius);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  transition: transform .15s ease, box-shadow .15s ease, border-color .15s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
  border-color: rgba(255, 68, 0, 0.20);
}

.product-image {
  text-align: center;
  margin-bottom: 15px;
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
  transition: transform .15s ease;
}

.product-image:hover img {
  transform: scale(1.05);
}

.product-info h5 {
  margin-bottom: 5px;
  color: var(--text);
  font-size: 1.1rem;
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.product-category {
  color: var(--muted);
  font-size: 12px;
  margin-bottom: 8px;
}

.product-description {
  color: var(--muted);
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 15px;
  height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.product-prices {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-bottom: 15px;
}

.original-price {
  color: var(--muted);
  font-size: 14px;
  text-decoration: line-through;
}

.seckill-price {
  color: var(--primary);
  font-size: 20px;
  font-weight: 700;
}

.discount {
  background-color: rgba(46, 204, 113, 0.12);
  color: #2ecc71;
  border: 1px solid rgba(46, 204, 113, 0.25);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.product-stock {
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 14px;
  color: var(--muted);
}

.stock-info {
  display: flex;
  justify-content: space-between;
}

.progress-container {
  margin-top: 5px;
}

.progress-label {
  font-size: 12px;
  margin-bottom: 4px;
  color: var(--muted);
}

.progress-bar {
  height: 8px;
  background-color: var(--border);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 4px;
}

.progress-fill {
  height: 100%;
  background-color: #2ecc71;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  text-align: right;
  color: var(--muted);
}

.product-status-draft {
  background-color: var(--border);
  color: var(--muted);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.product-status-active {
  background-color: rgba(46, 204, 113, 0.12);
  color: #2ecc71;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.product-status-inactive {
  background-color: rgba(241, 196, 15, 0.12);
  color: #b58900;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.product-status-deleted {
  background-color: rgba(231, 76, 60, 0.10);
  color: #e74c3c;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid rgba(17, 24, 39, 0.08);
}

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

.btn-secondary {
  background-color: rgba(17, 24, 39, 0.06);
  color: var(--text);
  border-color: rgba(17, 24, 39, 0.14);
}

.btn-secondary:hover {
  background-color: rgba(17, 24, 39, 0.10);
}

.btn-warning {
  background-color: rgba(241, 196, 15, 0.12);
  color: #b58900;
  border-color: rgba(241, 196, 15, 0.30);
}

.btn-warning:hover {
  background-color: rgba(241, 196, 15, 0.18);
}

.btn-danger {
  background-color: rgba(231, 76, 60, 0.12);
  color: #e74c3c;
  border-color: rgba(231, 76, 60, 0.25);
}

.btn-danger:hover {
  background-color: rgba(231, 76, 60, 0.18);
}

.btn-success {
  background-color: rgba(46, 204, 113, 0.12);
  color: #2ecc71;
  border-color: rgba(46, 204, 113, 0.25);
}

.btn-success:hover {
  background-color: rgba(46, 204, 113, 0.18);
}

.btn-info {
  background-color: rgba(52, 152, 219, 0.12);
  color: #3498db;
  border-color: rgba(52, 152, 219, 0.25);
}

.btn-info:hover {
  background-color: rgba(52, 152, 219, 0.18);
}

@media (max-width: 768px) {
  .activity-detail {
    padding: 16px;
  }

  .activity-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .activity-timeline {
    flex-direction: column;
    gap: 12px;
  }

  .action-buttons {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
}
</style>