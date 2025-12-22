<template>
  <div class="activity-management">
    <h2>秒杀活动管理</h2>
    <div class="action-bar">
      <button @click="showCreateForm = true" class="btn btn-primary">创建新活动</button>
    </div>

    <div class="activity-list">
      <table class="table">
        <thead>
          <tr>
            <th>活动名称</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="activity in activities" :key="activity.id">
            <td>{{ activity.name }}</td>
            <td>{{ formatDate(activity.startTime) }}</td>
            <td>{{ formatDate(activity.endTime) }}</td>
            <td>
              <span :class="getStatusClass(activity.status)">
                {{ getStatusText(activity.status) }}
              </span>
            </td>
            <td>
              <button @click="viewActivity(activity.id)" class="btn btn-info btn-sm">查看</button>
              <button v-if="activity.status === 0" @click="editActivity(activity)" class="btn btn-warning btn-sm">编辑</button>
              <button v-if="activity.status === 1" @click="closeActivity(activity.id)" class="btn btn-danger btn-sm">关闭</button>
              <button @click="viewDashboard(activity.id)" class="btn btn-success btn-sm">数据</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 创建/编辑活动模态框 -->
    <div v-if="showCreateForm || showEditForm" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ showEditForm ? '编辑活动' : '创建活动' }}</h3>
          <button @click="closeModal" class="close">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveActivity">
            <div class="form-group">
              <label>活动名称</label>
              <input type="text" v-model="formData.name" required class="form-control" :class="{ 'is-invalid': errors.name }">
              <div v-if="errors.name" class="invalid-feedback">{{ errors.name }}</div>
            </div>
            <div class="form-group">
              <label>开始时间</label>
              <input type="datetime-local" v-model="formData.startTime" required class="form-control" :class="{ 'is-invalid': errors.startTime }">
              <div v-if="errors.startTime" class="invalid-feedback">{{ errors.startTime }}</div>
            </div>
            <div class="form-group">
              <label>结束时间</label>
              <input type="datetime-local" v-model="formData.endTime" required class="form-control" :class="{ 'is-invalid': errors.endTime }">
              <div v-if="errors.endTime" class="invalid-feedback">{{ errors.endTime }}</div>
            </div>
            <div class="form-group">
              <label>活动规则</label>
              <textarea v-model="formData.rule" class="form-control" rows="3"></textarea>
            </div>
            
            <!-- 商品设置部分 -->
            <div class="form-group">
              <label>秒杀商品设置</label>
              <div v-if="errors.activityProducts && errors.activityProducts.length === 1 && !errors.activityProducts[0].name" class="invalid-feedback mb-2">
                {{ errors.activityProducts[0] }}
              </div>
              <div class="activity-products">
                <div v-for="(product, index) in formData.activityProducts" :key="index" class="activity-product-item">
                  <h4>商品 {{ index + 1 }}</h4>
                  <div class="row">
                    <div class="col-md-6">
                      <label>商品名称</label>
                      <input type="text" v-model="product.name" required class="form-control" :class="{ 'is-invalid': errors.activityProducts[index]?.name }">
                      <div v-if="errors.activityProducts[index]?.name" class="invalid-feedback">{{ errors.activityProducts[index].name }}</div>
                    </div>
                    <div class="col-md-6">
                      <label>商品图片</label>
                      <input type="file" accept="image/*" class="form-control" @change="handleFileSelect(index, $event)">
                      <div v-if="product.imagePreview" class="image-preview mt-2">
                        <img :src="product.imagePreview" :alt="product.name" class="img-thumbnail">
                      </div>
                    </div>
                  </div>
                  <div class="row mt-3">
                    <div class="col-md-12">
                      <label>商品描述</label>
                      <textarea v-model="product.description" class="form-control" rows="2"></textarea>
                    </div>
                  </div>
                  <div class="row mt-3">
                    <div class="col-md-3">
                      <label>原价</label>
                      <input type="number" v-model="product.originalPrice" step="0.01" required class="form-control" :class="{ 'is-invalid': errors.activityProducts[index]?.originalPrice }">
                      <div v-if="errors.activityProducts[index]?.originalPrice" class="invalid-feedback">{{ errors.activityProducts[index].originalPrice }}</div>
                    </div>
                    <div class="col-md-3">
                      <label>秒杀价格</label>
                      <input type="number" v-model="product.seckillPrice" step="0.01" required class="form-control" :class="{ 'is-invalid': errors.activityProducts[index]?.seckillPrice }">
                      <div v-if="errors.activityProducts[index]?.seckillPrice" class="invalid-feedback">{{ errors.activityProducts[index].seckillPrice }}</div>
                    </div>
                    <div class="col-md-3">
                      <label>总库存</label>
                      <input type="number" v-model="product.totalStock" required class="form-control" :class="{ 'is-invalid': errors.activityProducts[index]?.totalStock }">
                      <div v-if="errors.activityProducts[index]?.totalStock" class="invalid-feedback">{{ errors.activityProducts[index].totalStock }}</div>
                    </div>
                    <div class="col-md-3">
                      <label>秒杀库存</label>
                      <input type="number" v-model="product.seckillStock" required class="form-control" :class="{ 'is-invalid': errors.activityProducts[index]?.seckillStock }">
                      <div v-if="errors.activityProducts[index]?.seckillStock" class="invalid-feedback">{{ errors.activityProducts[index].seckillStock }}</div>
                    </div>
                  </div>
                  <div class="row mt-3">
                    <div class="col-md-3">
                      <label>每人限购</label>
                      <input type="number" v-model="product.limitPerUser" required class="form-control" :class="{ 'is-invalid': errors.activityProducts[index]?.limitPerUser }">
                      <div v-if="errors.activityProducts[index]?.limitPerUser" class="invalid-feedback">{{ errors.activityProducts[index].limitPerUser }}</div>
                    </div>
                  </div>
                  <button type="button" @click="removeProduct(index)" class="btn btn-danger btn-sm mt-2">删除</button>
                </div>
                <button type="button" @click="addProduct" class="btn btn-success btn-sm mt-3">添加商品</button>
              </div>
            </div>
            
            <div class="form-actions">
              <button type="submit" class="btn btn-primary" :disabled="loading">
                <span v-if="loading" class="spinner-border spinner-border-sm mr-2"></span>
                {{ showEditForm ? '保存修改' : '创建活动' }}
              </button>
              <button type="button" @click="closeModal" class="btn btn-secondary" :disabled="loading">取消</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { getAllActivities, updateActivity, createActivityWithProducts } from '../api/activity'
import { uploadImage } from '../api/file'

export default {
  data() {
    return {
      activities: [],
      showCreateForm: false,
      showEditForm: false,
      loading: false,
      formData: {
        id: null,
        name: '',
        startTime: '',
        endTime: '',
        rule: '',
        activityProducts: []
      },
      errors: {
        name: '',
        startTime: '',
        endTime: '',
        activityProducts: []
      },
      refreshTimer: null
    }
  },
  mounted() {
    this.loadActivities()
    // 每30秒自动刷新活动列表
    this.refreshTimer = setInterval(() => {
      this.loadActivities()
    }, 30000)
  },
  beforeDestroy() {
    // 清理定时器
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
    }
  },
  methods: {
    loadActivities() {
      getAllActivities().then(response => {
        this.activities = response.data
      }).catch(error => {
        console.error('加载活动列表失败:', error)
        alert('加载活动列表失败')
      })
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
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
    viewActivity(id) {
      this.$router.push(`/admin/activity/${id}`)
    },
    editActivity(activity) {
      this.formData = {
        ...activity,
        startTime: this.formatDateTimeLocal(new Date(activity.startTime)),
        endTime: this.formatDateTimeLocal(new Date(activity.endTime))
      }
      this.showEditForm = true
    },
    closeActivity(id) {
      if (confirm('确定要关闭此活动吗？')) {
        axios.post(`http://localhost:28080/api/activities/${id}/end`)
          .then(() => {
            alert('活动已关闭')
            this.loadActivities()
          })
          .catch(error => {
            console.error('关闭活动失败:', error)
            alert('关闭活动失败')
          })
      }
    },
    viewDashboard(id) {
      this.$router.push(`/admin/activity-dashboard/${id}`)
    },
    async saveActivity() {
      // 表单验证
      if (!this.validateForm()) {
        return
      }
      
      this.loading = true
      const activityData = {
        ...this.formData,
        startTime: new Date(this.formData.startTime),
        endTime: new Date(this.formData.endTime)
      }

      try {
        if (this.showEditForm) {
          // 更新活动
          await updateActivity(activityData.id, activityData)
          alert('活动更新成功')
        } else {
          // 上传所有商品图片
          const productsWithImages = this.formData.activityProducts.map(product => {
            // 深拷贝商品对象，但保留File对象
            const newProduct = {...product}
            // 确保imageFile是正确的File对象类型
            if (product.imageFile instanceof File) {
              newProduct.imageFile = product.imageFile
            } else {
              newProduct.imageFile = null
              newProduct.imagePreview = ''
            }
            return newProduct
          })
          
          for (let i = 0; i < productsWithImages.length; i++) {
            const product = productsWithImages[i]
            // 只有当有有效的imageFile时才上传图片
            if (product.imageFile && product.imageFile instanceof File) {
              try {
                const imageUrl = await uploadImage(product.imageFile)
                product.imageUrl = imageUrl
                product.imageFile = null
                product.imagePreview = ''
              } catch (error) {
                console.error('上传图片失败:', error)
                alert(`商品 ${i + 1} 图片上传失败: ${error.response?.data || '未知错误'}`)
                this.loading = false
                return
              }
            } else {
              // 没有选择图片文件时，清除相关字段
              product.imageFile = null
              product.imagePreview = ''
            }
          }
          
          // 创建活动（包含商品），转换价格为数字类型
          const requestData = {
            activity: activityData,
            seckillProducts: productsWithImages.map(product => ({
              ...product,
              originalPrice: parseFloat(product.originalPrice),
              seckillPrice: parseFloat(product.seckillPrice),
              totalStock: parseInt(product.totalStock),
              seckillStock: parseInt(product.seckillStock),
              limitPerUser: parseInt(product.limitPerUser)
            }))
          }
          
          await createActivityWithProducts(requestData)
          alert('活动创建成功')
        }
        
        this.closeModal()
        this.loadActivities()
      } catch (error) {
        console.error('操作失败:', error)
        alert(this.showEditForm ? '更新活动失败' : '创建活动失败')
        // 打印详细错误信息
        if (error.response) {
          console.error('错误响应:', error.response.data)
          console.error('错误状态:', error.response.status)
        } else if (error.request) {
          console.error('请求错误:', error.request)
        } else {
          console.error('错误消息:', error.message)
        }
      } finally {
        this.loading = false
      }
    },
    closeModal() {
      this.showCreateForm = false
      this.showEditForm = false
      this.resetForm()
    },
    resetForm() {
      this.formData = {
        id: null,
        name: '',
        startTime: '',
        endTime: '',
        rule: '',
        activityProducts: []
      }
    },
    addProduct() {
      this.formData.activityProducts.push({
        name: '',
        description: '',
        imageUrl: '',
        imagePreview: '',
        imageFile: null,
        originalPrice: '',
        totalStock: '',
        seckillPrice: '',
        seckillStock: '',
        limitPerUser: ''
      })
    },
    removeProduct(index) {
      this.formData.activityProducts.splice(index, 1)
    },
    formatDateTimeLocal(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day}T${hours}:${minutes}`
    },
    
    // 表单验证方法
    validateForm() {
      let isValid = true
      this.errors = {
        name: '',
        startTime: '',
        endTime: '',
        activityProducts: []
      }
      
      // 验证活动基本信息
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
      
      if (this.formData.startTime && this.formData.endTime) {
        const start = new Date(this.formData.startTime)
        const end = new Date(this.formData.endTime)
        if (start >= end) {
          this.errors.endTime = '结束时间必须晚于开始时间'
          isValid = false
        }
        if (start <= new Date()) {
          this.errors.startTime = '开始时间必须晚于当前时间'
          isValid = false
        }
      }
      
      // 验证商品信息
      if (this.formData.activityProducts.length === 0) {
        this.errors.activityProducts.push('至少需要添加一个商品')
        isValid = false
      } else {
        this.formData.activityProducts.forEach((product, index) => {
          const productError = {}
          
          if (!product.name.trim()) {
            productError.name = '商品名称不能为空'
            isValid = false
          }
          
          if (!product.originalPrice || parseFloat(product.originalPrice) <= 0) {
            productError.originalPrice = '原价必须大于0'
            isValid = false
          }
          
          if (!product.seckillPrice || parseFloat(product.seckillPrice) <= 0) {
            productError.seckillPrice = '秒杀价格必须大于0'
            isValid = false
          }
          
          if (product.seckillPrice && product.originalPrice) {
            if (parseFloat(product.seckillPrice) >= parseFloat(product.originalPrice)) {
              productError.seckillPrice = '秒杀价格必须小于原价'
              isValid = false
            }
          }
          
          if (!product.totalStock || parseInt(product.totalStock) <= 0) {
            productError.totalStock = '总库存必须大于0'
            isValid = false
          }
          
          if (!product.seckillStock || parseInt(product.seckillStock) <= 0) {
            productError.seckillStock = '秒杀库存必须大于0'
            isValid = false
          }
          
          if (product.seckillStock && product.totalStock) {
            if (parseInt(product.seckillStock) > parseInt(product.totalStock)) {
              productError.seckillStock = '秒杀库存不能大于总库存'
              isValid = false
            }
          }
          
          if (!product.limitPerUser || parseInt(product.limitPerUser) <= 0) {
            productError.limitPerUser = '每人限购数量必须大于0'
            isValid = false
          }
          
          this.errors.activityProducts[index] = productError
        })
      }
      
      return isValid
    },
    
    // 清除表单错误
    clearErrors() {
      this.errors = {
        name: '',
        startTime: '',
        endTime: '',
        activityProducts: []
      }
    },
    
    // 处理文件选择
    handleFileSelect(index, event) {
      const file = event.target.files[0]
      if (file) {
        // 验证文件类型
        if (!file.type.startsWith('image/')) {
          alert('请选择图片文件')
          return
        }
        
        // 验证文件大小（限制为2MB）
        if (file.size > 2 * 1024 * 1024) {
          alert('图片大小不能超过2MB')
          return
        }
        
        // 读取文件并显示预览
        const reader = new FileReader()
        reader.onload = (e) => {
          this.formData.activityProducts[index].imagePreview = e.target.result
          this.formData.activityProducts[index].imageFile = file
        }
        reader.readAsDataURL(file)
      }
    }
  }
}
</script>

<style scoped>
.activity-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.activity-management h2 {
  color: #333;
  margin-bottom: 25px;
  font-size: 1.8rem;
  font-weight: 600;
  padding-bottom: 10px;
  border-bottom: 2px solid #ff4400;
}

.action-bar {
  margin-bottom: 25px;
}

/* Button Styles */
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

.btn-primary {
  background: linear-gradient(90deg, #ff4400, #ff6633);
  color: white;
  box-shadow: 0 2px 8px rgba(255, 68, 0, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 68, 0, 0.4);
}

.btn-info {
  background-color: #17a2b8;
  color: white;
}

.btn-info:hover {
  background-color: #138496;
  transform: translateY(-1px);
}

.btn-warning {
  background-color: #ffc107;
  color: #212529;
}

.btn-warning:hover {
  background-color: #e0a800;
  transform: translateY(-1px);
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-danger:hover {
  background-color: #c82333;
  transform: translateY(-1px);
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover {
  background-color: #218838;
  transform: translateY(-1px);
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
  margin-right: 5px;
  margin-bottom: 5px;
}

/* Table Styles */
.table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.table th,
.table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.table th {
  background: linear-gradient(90deg, #f8f9fa, #e9ecef);
  color: #495057;
  font-weight: 600;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.table tbody tr {
  transition: all 0.3s ease;
}

.table tbody tr:hover {
  background-color: #f8f9fa;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* Status Styles */
.status-pending {
  color: #ffc107;
  font-weight: bold;
  background-color: rgba(255, 193, 7, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.status-active {
  color: #28a745;
  font-weight: bold;
  background-color: rgba(40, 167, 69, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.status-ended {
  color: #dc3545;
  font-weight: bold;
  background-color: rgba(220, 53, 69, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

/* Modal Styles */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background-color: white;
  padding: 30px;
  border-radius: 12px;
  width: 700px;
  max-width: 95%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  animation: slideIn 0.3s ease;
}

.modal-body {
  padding: 0;
}

.modal-body form {
  padding: 0;
}

.modal-content::-webkit-scrollbar {
  width: 8px;
}

.modal-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.modal-content::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.modal-content::-webkit-scrollbar-thumb:hover {
  background: #555;
}

@keyframes slideIn {
  from { transform: translateY(-50px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h3 {
  color: #333;
  margin: 0;
  font-size: 1.4rem;
  font-weight: 600;
}

.close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  transition: color 0.3s ease;
}

.close:hover {
  color: #333;
}

/* Form Styles */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 500;
  font-size: 14px;
}

.form-control {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
}

.form-control:focus {
  outline: none;
  border-color: #ff4400;
  box-shadow: 0 0 0 3px rgba(255, 68, 0, 0.1);
}

.form-control.is-invalid {
  border-color: #dc3545;
}

textarea.form-control {
  resize: vertical;
  min-height: 80px;
}

.invalid-feedback {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

/* Image Preview */
.image-preview {
  max-width: 200px;
  max-height: 150px;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.image-preview img {
  width: 100%;
  height: auto;
  display: block;
}

/* Activity Products */
.activity-product-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  background-color: #fafafa;
  transition: all 0.3s ease;
}

.activity-product-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.activity-product-item h4 {
  color: #333;
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 10px;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

/* Responsive Design */
@media (max-width: 768px) {
  .activity-management {
    padding: 10px;
  }
  
  .table {
    font-size: 12px;
  }
  
  .table th,
  .table td {
    padding: 8px;
  }
  
  .btn-sm {
    padding: 4px 8px;
    font-size: 10px;
  }
  
  .modal-content {
    padding: 20px;
  }
}
</style>
