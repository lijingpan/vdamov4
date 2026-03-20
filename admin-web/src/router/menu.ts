import type { Component } from 'vue';
import {
  DataLine,
  Document,
  HomeFilled,
  OfficeBuilding,
  Operation,
  Platform,
  UserFilled,
} from '@element-plus/icons-vue';

export interface MenuItem {
  key: string;
  path: string;
  icon: Component;
  i18nKey: string;
}

export const menuItems: MenuItem[] = [
  {
    key: 'dashboard',
    path: '/dashboard',
    icon: DataLine,
    i18nKey: 'menu.dashboard',
  },
  {
    key: 'stores',
    path: '/stores',
    icon: OfficeBuilding,
    i18nKey: 'menu.stores',
  },
  {
    key: 'tables',
    path: '/tables',
    icon: HomeFilled,
    i18nKey: 'menu.tables',
  },
  {
    key: 'orders',
    path: '/orders',
    icon: Document,
    i18nKey: 'menu.orders',
  },
  {
    key: 'devices',
    path: '/devices',
    icon: Platform,
    i18nKey: 'menu.devices',
  },
  {
    key: 'products',
    path: '/products',
    icon: Operation,
    i18nKey: 'menu.products',
  },
  {
    key: 'members',
    path: '/members',
    icon: UserFilled,
    i18nKey: 'menu.members',
  },
];

export const menuItemMap = menuItems.reduce<Record<string, MenuItem>>((result, item) => {
  result[item.path] = item;
  return result;
}, {});
