import Vue from 'vue'
import VueRouter from 'vue-router'
import Register from "@/views/Register.vue";
import Login from "@/views/Login.vue";
import ProductDetail from "@/views/ProductDetail.vue";
import OrderDetail from "@/views/OrderDetail.vue";
import OrderList from "@/views/OrderList.vue";
import ActivityManagement from "@/views/ActivityManagement.vue";
import ActivityDashboard from "@/views/ActivityDashboard.vue";
import ActivityDetail from "@/views/ActivityDetail.vue";
import AdminLogin from "@/views/AdminLogin.vue";
import MainLayout from "@/layouts/MainLayout.vue";
import ProductsPage from "@/views/ProductsPage.vue";
import Home from "@/views/Home.vue";
import ProfilePage from "@/views/ProfilePage.vue";
import { me } from "@/api/user";

Vue.use(VueRouter)

const routes = [
  {
    path: "/",
    component: MainLayout,
    children: [
      {
        path: "",
        name: "Home",
        component: ProductsPage,
      },
      {
        path: "activities",
        name: "Activities",
        component: Home,
        props: { showHeader: false },
      },
      {
        path: "activity/:id",
        name: "ActivityDetailPublic",
        component: ActivityDetail,
      },
      {
        path: "profile",
        name: "Profile",
        component: ProfilePage,
      },
    ]
  },
  {
    path: "/register",
    name: "Register",
    component: Register,
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
  },
  {
    path: "/product/:id",
    name: "ProductDetail",
    component: ProductDetail,
  },
  {
    path: "/order/:id",
    name: "OrderDetail",
    component: OrderDetail,
  },
  {
    path: "/orders",
    name: "OrderList",
    component: OrderList,
  },
  {
    path: "/admin/activity/:id",
    name: "ActivityDetail",
    component: ActivityDetail,
  },
  
  // 管理端路由
  {
    path: "/admin/login",
    name: "AdminLogin",
    component: AdminLogin,
  },
  {
    path: "/admin/activity-management",
    name: "AdminActivityManagement",
    component: ActivityManagement,
  },
  {
    path: "/admin/activity-dashboard/:id",
    name: "AdminActivityDashboard",
    component: ActivityDashboard,
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫，检查用户登录状态
router.beforeEach((to, from, next) => {
  // 需要用户登录才能访问的页面
  const requiresUserAuth = ['ProductDetail', 'OrderDetail', 'OrderList'];
  
  // 需要管理员登录才能访问的页面
  const requiresAdminAuth = ['AdminActivityManagement', 'AdminActivityDashboard', 'ActivityDetail'];
  
  // 检查用户是否已登录
  const isLoggedIn = localStorage.getItem('userInfo');
  
  // 检查管理员是否已登录
  const isAdminLoggedIn = localStorage.getItem('adminInfo');
  
  if (requiresUserAuth.includes(to.name) && !isLoggedIn) {
    me().then(res => {
      if (res && res.data) {
        localStorage.setItem('userInfo', JSON.stringify(res.data));
        next();
        return;
      }
      next({ name: 'Login' });
    }).catch(() => {
      next({ name: 'Login' });
    });
    return;
  } else if (requiresAdminAuth.includes(to.name) && !isAdminLoggedIn) {
    // 管理员未登录且尝试访问需要管理员权限的页面，重定向到管理员登录页
    next({ name: 'AdminLogin' });
  } else {
    // 用户已登录或访问的页面不需要登录，正常跳转
    next();
  }
});

export default router
