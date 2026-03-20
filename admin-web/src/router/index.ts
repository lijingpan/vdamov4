import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import AdminLayout from '@/layouts/AdminLayout.vue';
import DashboardView from '@/views/DashboardView.vue';
import DeviceView from '@/views/DeviceView.vue';
import LoginView from '@/views/LoginView.vue';
import MenuView from '@/views/MenuView.vue';
import MemberView from '@/views/MemberView.vue';
import OrderDetailView from '@/views/OrderDetailView.vue';
import OrderView from '@/views/OrderView.vue';
import ProductCategoryView from '@/views/ProductCategoryView.vue';
import ProductView from '@/views/ProductView.vue';
import RoleView from '@/views/RoleView.vue';
import SalesReportView from '@/views/SalesReportView.vue';
import StoreView from '@/views/StoreView.vue';
import TableAreaView from '@/views/TableAreaView.vue';
import TableView from '@/views/TableView.vue';
import UserView from '@/views/UserView.vue';
import { ApiError } from '@/api/http';
import { useAuthStore } from '@/stores/auth';
import { pinia } from '@/stores';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      public: true,
    },
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: DashboardView,
      },
      {
        path: 'stores',
        name: 'stores',
        component: StoreView,
      },
      {
        path: 'tables',
        name: 'tables',
        component: TableView,
      },
      {
        path: 'table-areas',
        name: 'table-areas',
        component: TableAreaView,
      },
      {
        path: 'orders',
        name: 'orders',
        component: OrderView,
      },
      {
        path: 'sales-reports',
        name: 'sales-reports',
        component: SalesReportView,
      },
      {
        path: 'orders/:id',
        name: 'order-detail',
        component: OrderDetailView,
        meta: {
          accessPath: '/orders',
        },
      },
      {
        path: 'devices',
        name: 'devices',
        component: DeviceView,
      },
      {
        path: 'products',
        name: 'products',
        component: ProductView,
      },
      {
        path: 'product-categories',
        name: 'product-categories',
        component: ProductCategoryView,
      },
      {
        path: 'members',
        name: 'members',
        component: MemberView,
      },
      {
        path: 'users',
        name: 'users',
        component: UserView,
      },
      {
        path: 'roles',
        name: 'roles',
        component: RoleView,
      },
      {
        path: 'menus',
        name: 'menus',
        component: MenuView,
      },
    ],
  },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia);
  const isPublic = Boolean(to.meta.public);

  if (isPublic) {
    if (to.path === '/login' && authStore.token) {
      try {
        await authStore.bootstrap();
        if (authStore.isAuthenticated) {
          return authStore.firstMenuPath;
        }
      } catch {
        authStore.clearSession();
      }
    }
    return true;
  }

  try {
    await authStore.bootstrap();
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      authStore.clearSession();
    }
  }

  if (!authStore.isAuthenticated) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath,
      },
    };
  }

  const accessPath = typeof to.meta.accessPath === 'string' ? to.meta.accessPath : to.path;
  if (!authStore.canAccess(accessPath)) {
    return authStore.firstMenuPath;
  }

  return true;
});
