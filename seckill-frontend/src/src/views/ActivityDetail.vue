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
            <div v-for="product in activity.activityProducts" :key="product.id" class="product-card">
              <div class="product-image">
              <img :src="product.imageDisplayUrl || 'https://via.placeholder.com/200x200?text=暂无图片'" :alt="product.name" @error="handleImageError">
              <div class="product-id">ID: {{ product.id }}</div>
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
          <button v-if="activity.status === 0" @click="editActivity" class="btn btn-warning">编辑活动</button>
          <button v-if="activity.status === 1" @click="closeActivity" class="btn btn-danger">关闭活动</button>
          <button @click="viewDashboard" class="btn btn-success">查看数据</button>
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
import { getActivityById, closeActivity, updateActivity, getProductsByActivityId, getProductImage, getProductById } from '../api/activity'
import axios from 'axios'
import apiConfig from '../config/api'

export default {
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
                // 获取完整的商品信息
                const productDetailResponse = await getProductById(product.id)
                const fullProduct = productDetailResponse.data
                
                // 合并原有信息和详细信息
                const mergedProduct = { ...product, ...fullProduct }
                
                // 获取商品图片
                mergedProduct.imageDisplayUrl = await this.getProductImage(mergedProduct)
                console.log(`商品${product.id}的完整信息:`, mergedProduct)
                console.log(`商品${product.id}的图片URL:`, mergedProduct.imageDisplayUrl)
                
                return mergedProduct
              } catch (error) {
                console.error(`获取商品${product.id}详细信息失败:`, error)
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
      this.$router.push(`/activity-dashboard/${this.activity.id}`)
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
    testApi() {
      const id = this.$route.params.id
      console.log('测试API，活动ID:', id)
      
      // 测试活动详情API
      getActivityById(id)
        .then(response => {
          console.log('活动详情API测试成功:', response.data)
          console.log('活动详情的activityProducts字段:', response.data.activityProducts)
        })
        .catch(error => {
          console.error('活动详情API测试失败:', error)
        })
      
      // 测试商品信息API
      getProductsByActivityId(id)
        .then(response => {
          console.log('商品信息API测试成功:', response.data)
          if (response.data && response.data.length > 0) {
            console.log('第一个商品的完整数据结构:', response.data[0])
            console.log('第一个商品的所有字段:', Object.keys(response.data[0]))
            console.log('第一个商品的图片相关字段:', {
              imageUrl: response.data[0].imageUrl,
              image: response.data[0].image,
              picture: response.data[0].picture,
              pic: response.data[0].pic
            })
          }
        })
        .catch(error => {
          console.error('商品信息API测试失败:', error)
        })
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
    
    // 如果没有图片URL，尝试通过商品ID获取图片
    if (product.id) {
      try {
        const response = await getProductImage(product.id)
        if (response.data && response.data.imageUrl) {
          return response.data.imageUrl
        }
      } catch (error) {
        console.error(`获取商品${product.id}的图片失败:`, error)
      }
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
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.activity-detail h2 {
  color: #333;
  margin-bottom: 25px;
  font-size: 1.8rem;
  font-weight: 600;
  padding-bottom: 10px;
  border-bottom: 2px solid #ff4400;
}

.loading, .error {
  text-align: center;
  padding: 50px;
  font-size: 18px;
}

.error {
  color: #dc3545;
}

.activity-info {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 25px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.activity-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.6rem;
}

.status-pending {
  background-color: #ffc107;
  color: #212529;
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
}

.status-active {
  background-color: #28a745;
  color: white;
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
}

.status-ended {
  background-color: #6c757d;
  color: white;
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
}

.activity-timeline {
  display: flex;
  gap: 40px;
  margin-bottom: 30px;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.timeline-label {
  font-size: 14px;
  color: #6c757d;
  font-weight: 500;
}

.timeline-value {
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.activity-rule {
  margin-bottom: 30px;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.activity-rule h4 {
  margin-bottom: 10px;
  color: #333;
  font-size: 1.2rem;
}

.activity-rule p {
  color: #6c757d;
  line-height: 1.6;
  margin: 0;
}

.activity-products h4 {
  margin-bottom: 20px;
  color: #333;
  font-size: 1.2rem;
}

.no-products {
  text-align: center;
  padding: 30px;
  color: #6c757d;
  font-size: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.product-card {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
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
  border-radius: 4px;
  transition: transform 0.3s ease;
}

.product-image:hover img {
  transform: scale(1.05);
}

.product-id {
  position: absolute;
  top: 5px;
  left: 5px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.product-info h5 {
  margin-bottom: 5px;
  color: #333;
  font-size: 1.1rem;
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.product-category {
  color: #6c757d;
  font-size: 12px;
  margin-bottom: 8px;
}

.product-description {
  color: #6c757d;
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
  color: #6c757d;
  font-size: 14px;
  text-decoration: line-through;
}

.seckill-price {
  color: #dc3545;
  font-size: 20px;
  font-weight: 700;
}

.discount {
  background-color: #28a745;
  color: white;
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
  color: #6c757d;
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
  color: #6c757d;
}

.progress-bar {
  height: 8px;
  background-color: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 4px;
}

.progress-fill {
  height: 100%;
  background-color: #28a745;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  text-align: right;
  color: #6c757d;
}

.product-status-draft {
  background-color: #6c757d;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.product-status-active {
  background-color: #28a745;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.product-status-inactive {
  background-color: #ffc107;
  color: #212529;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.product-status-deleted {
  background-color: #dc3545;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.btn-warning {
  background-color: #ffc107;
  color: #212529;
}

.btn-warning:hover {
  background-color: #e0a800;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-danger:hover {
  background-color: #c82333;
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover {
  background-color: #218838;
}

.btn-info {
  background-color: #17a2b8;
  color: white;
}

.btn-info:hover {
  background-color: #138496;
}
</style>