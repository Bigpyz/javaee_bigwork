<template>
  <div class="page">
    <div class="header">
      <h2>商品列表</h2>
      <div class="hint">点击商品可进入详情（若商品绑定了活动，会进入秒杀详情页）</div>
    </div>

    <div v-if="loading" class="state">加载中...</div>
    <div v-else-if="error" class="state error">{{ error }}</div>
    <div v-else-if="products.length === 0" class="state">暂无商品</div>

    <div v-else class="grid">
      <div class="card" v-for="p in products" :key="p.id" @click="goDetail(p)">
        <div class="image">
          <img v-if="p.imageUrl" :src="getImageUrl(p.imageUrl)" :alt="p.name" class="img">
          <div v-else class="no-image">暂无图片</div>
        </div>
        <div class="title">{{ p.name }}</div>
        <div class="desc">{{ p.description || '暂无描述' }}</div>
        <div class="meta">
          <div>原价：¥{{ p.originalPrice }}</div>
          <div>秒杀价：¥{{ p.seckillPrice }}</div>
          <div>秒杀库存：{{ p.seckillStock }}</div>
        </div>
        <div class="actions">
          <button class="btn" :disabled="!p.activityId">查看详情</button>
          <span v-if="!p.activityId" class="tag">未绑定活动</span>
          <span v-else class="tag active">活动ID: {{ p.activityId }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getAllProducts } from '../api/product'
import apiConfig from '../config/api'

export default {
  name: 'ProductsPage',
  data() {
    return {
      loading: false,
      error: '',
      products: []
    }
  },
  mounted() {
    this.load()
  },
  methods: {
    getImageUrl(imagePath) {
      if (!imagePath) return ''
      if (typeof imagePath === 'string' && imagePath.startsWith('http')) {
        return imagePath
      }
      if (typeof imagePath === 'string' && imagePath.startsWith('/')) {
        return apiConfig.IMAGE_BASE_URL + imagePath
      }
      return apiConfig.IMAGE_BASE_URL + '/' + imagePath
    },
    async load() {
      this.loading = true
      this.error = ''
      try {
        const res = await getAllProducts()
        this.products = res.data || []
      } catch (e) {
        this.error = e.message || '加载失败'
      } finally {
        this.loading = false
      }
    },
    goDetail(p) {
      if (!p.activityId) {
        return
      }
      this.$router.push({
        path: `/product/${p.id}`,
        query: { activityId: p.activityId }
      })
    }
  }
}
</script>

<style scoped>
.page {
  background: rgba(255, 255, 255, 0.80);
  border-radius: var(--radius);
  padding: 18px;
  border: 1px solid rgba(17, 24, 39, 0.10);
  box-shadow: 0 16px 40px rgba(17, 24, 39, 0.08);
  backdrop-filter: blur(10px);
}

.header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.hint {
  color: var(--muted);
  font-size: 12px;
}

.state {
  padding: 24px;
  text-align: center;
  color: var(--muted);
}

.state.error {
  color: #e74c3c;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}

.card {
  border: 1px solid rgba(17, 24, 39, 0.10);
  border-radius: var(--radius);
  padding: 14px;
  background: rgba(255, 255, 255, 0.92);
  cursor: pointer;
  transition: all .2s ease;
}

.image {
  height: 160px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(17, 24, 39, 0.08);
  background: rgba(17, 24, 39, 0.03);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  color: var(--muted);
  font-size: 12px;
}

.card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 45px rgba(17, 24, 39, 0.10);
  border-color: rgba(255, 68, 0, 0.25);
}

.title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 6px;
}

.desc {
  color: var(--muted);
  font-size: 13px;
  min-height: 32px;
}

.meta {
  margin-top: 10px;
  display: grid;
  gap: 4px;
  font-size: 13px;
  color: rgba(31, 41, 55, 0.92);
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

.btn {
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid var(--primary);
  background: var(--primary);
  color: #fff;
  cursor: pointer;
  font-weight: 700;
}

.btn:disabled {
  opacity: .5;
  cursor: not-allowed;
}

.tag {
  font-size: 12px;
  color: var(--muted);
}

.tag.active {
  color: var(--primary);
}
</style>
