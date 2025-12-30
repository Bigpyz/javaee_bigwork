<template>
  <div class="page">
    <div class="header">
      <h2>活动列表</h2>
      <button class="btn" @click="load">刷新</button>
    </div>

    <div v-if="loading" class="state">加载中...</div>
    <div v-else-if="error" class="state error">{{ error }}</div>

    <div v-else>
      <div class="section">
        <h3>进行中</h3>
        <div v-if="active.length === 0" class="empty">暂无进行中活动</div>
        <div v-else class="list">
          <div class="item" v-for="a in active" :key="a.id">
            <div class="name">{{ a.name }}</div>
            <div class="time">{{ format(a.startTime) }} - {{ format(a.endTime) }}</div>
            <div class="badge active">进行中</div>
          </div>
        </div>
      </div>

      <div class="section">
        <h3>即将开始</h3>
        <div v-if="upcoming.length === 0" class="empty">暂无即将开始活动</div>
        <div v-else class="list">
          <div class="item" v-for="a in upcoming" :key="a.id">
            <div class="name">{{ a.name }}</div>
            <div class="time">{{ format(a.startTime) }} - {{ format(a.endTime) }}</div>
            <div class="badge upcoming">即将开始</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getActiveActivities, getUpcomingActivities } from '../api/activity'

export default {
  name: 'ActivitiesPage',
  data() {
    return {
      loading: false,
      error: '',
      active: [],
      upcoming: []
    }
  },
  mounted() {
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      this.error = ''
      try {
        const [a, u] = await Promise.all([getActiveActivities(), getUpcomingActivities()])
        this.active = a.data || []
        this.upcoming = u.data || []
      } catch (e) {
        this.error = e.message || '加载失败'
      } finally {
        this.loading = false
      }
    },
    format(v) {
      try {
        return new Date(v).toLocaleString('zh-CN')
      } catch (e) {
        return v
      }
    }
  }
}
</script>

<style scoped>
.page {
  background: rgba(255, 255, 255, 0.80);
  border-radius: var(--radius);
  padding: 18px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  backdrop-filter: blur(10px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section {
  margin-top: 14px;
}

.section h3 {
  margin: 14px 0 10px;
  color: var(--text);
}

.list {
  display: grid;
  gap: 10px;
}

.item {
  border: 1px solid rgba(17, 24, 39, 0.10);
  border-radius: 14px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.92);
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  transition: transform .15s ease, box-shadow .15s ease, border-color .15s ease;
}

.item:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 40px rgba(17, 24, 39, 0.08);
  border-color: rgba(255, 68, 0, 0.20);
}

.name {
  font-weight: 700;
}

.time {
  color: var(--muted);
  font-size: 13px;
}

.badge {
  justify-self: end;
  align-self: start;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 999px;
  border: 1px solid rgba(17, 24, 39, 0.10);
}

.badge.active {
  border-color: rgba(46, 204, 113, 0.35);
  background: rgba(46, 204, 113, 0.12);
  color: #2ecc71;
}

.badge.upcoming {
  border-color: rgba(52, 152, 219, 0.35);
  background: rgba(52, 152, 219, 0.12);
  color: #3498db;
}

.state {
  padding: 24px;
  text-align: center;
  color: var(--muted);
}

.state.error {
  color: #ff4400;
}

.empty {
  color: var(--muted);
  padding: 10px 0;
}

.btn {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid var(--primary);
  background: var(--primary);
  color: #fff;
  cursor: pointer;
  font-weight: 700;
  transition: transform .15s ease, background-color .15s ease, border-color .15s ease;
}

.btn:hover {
  transform: translateY(-1px);
  background: var(--primary-700);
  border-color: var(--primary-700);
}

@media (max-width: 768px) {
  .page {
    padding: 14px;
  }

  .header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
}
</style>
