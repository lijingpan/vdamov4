import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import AdminLayout from '@/layouts/AdminLayout.vue';
import DashboardView from '@/views/DashboardView.vue';
import DeviceView from '@/views/DeviceView.vue';
import MemberView from '@/views/MemberView.vue';
import OrderView from '@/views/OrderView.vue';
import ProductView from '@/views/ProductView.vue';
import StoreView from '@/views/StoreView.vue';
import TableView from '@/views/TableView.vue';

const routes: RouteRecordRaw[] = [
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
        path: 'orders',
        name: 'orders',
        component: OrderView,
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
        path: 'members',
        name: 'members',
        component: MemberView,
      },
    ],
  },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});
