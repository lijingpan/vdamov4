import type { Component } from 'vue';
import {
  CollectionTag,
  DataLine,
  Document,
  Files,
  HomeFilled,
  OfficeBuilding,
  Operation,
  Platform,
  Setting,
  Tickets,
  UserFilled,
  User,
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
    key: 'tableAreas',
    path: '/table-areas',
    icon: Tickets,
    i18nKey: 'menu.tableAreas',
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
    key: 'productCategories',
    path: '/product-categories',
    icon: Files,
    i18nKey: 'menu.productCategories',
  },
  {
    key: 'members',
    path: '/members',
    icon: UserFilled,
    i18nKey: 'menu.members',
  },
  {
    key: 'users',
    path: '/users',
    icon: User,
    i18nKey: 'menu.users',
  },
  {
    key: 'roles',
    path: '/roles',
    icon: Setting,
    i18nKey: 'menu.roles',
  },
  {
    key: 'menus',
    path: '/menus',
    icon: CollectionTag,
    i18nKey: 'menu.menus',
  },
];

export const menuItemMap = menuItems.reduce<Record<string, MenuItem>>((result, item) => {
  result[item.path] = item;
  return result;
}, {});
