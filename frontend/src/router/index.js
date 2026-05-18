import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    meta: { noAuth: true },
    component: () => import('../views/login/Login.vue')
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { title: '首页仪表盘' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('../views/category/CategoryList.vue'),
        meta: { title: '家畜种类管理' }
      },
      {
        path: 'site',
        name: 'Site',
        component: () => import('../views/site/SiteList.vue'),
        meta: { title: '养殖场地管理' }
      },
      {
        path: 'batch',
        name: 'Batch',
        component: () => import('../views/batch/BatchList.vue'),
        meta: { title: '存栏列表' }
      },
      {
        path: 'batch/add',
        name: 'BatchAdd',
        component: () => import('../views/batch/BatchForm.vue'),
        meta: { title: '登记存栏' }
      },
      {
        path: 'batch/:id',
        name: 'BatchDetail',
        component: () => import('../views/batch/BatchDetail.vue'),
        meta: { title: '批次详情' }
      },
      {
        path: 'cost',
        name: 'Cost',
        component: () => import('../views/cost/CostList.vue'),
        meta: { title: '成本管理' }
      },
      {
        path: 'rule',
        name: 'PriceRule',
        component: () => import('../views/rule/PriceRuleList.vue'),
        meta: { title: '价格规则管理' }
      },
      {
        path: 'revenue',
        name: 'Revenue',
        component: () => import('../views/revenue/RevenueEstimate.vue'),
        meta: { title: '收益预估' }
      },
      {
        path: 'advice',
        name: 'Advice',
        component: () => import('../views/advice/AdviceList.vue'),
        meta: { title: '养殖建议' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/Profile.vue'),
        meta: { title: '个人资料' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/settings/Settings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  if (to.meta.noAuth) {
    next()
  } else {
    const token = localStorage.getItem('token')
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
