import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import {
  fetchCurrentMenus,
  fetchCurrentUser,
  login as loginApi,
  logout as logoutApi,
  type AuthenticatedUser,
  type LoginRequest,
  type MenuSummary,
} from '@/api/auth';
import { clearToken, getToken } from '@/api/http';

let bootstrapTask: Promise<void> | null = null;

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken());
  const user = ref<AuthenticatedUser | null>(null);
  const menus = ref<MenuSummary[]>([]);
  const initialized = ref(false);

  const isAuthenticated = computed(() => Boolean(token.value && user.value));
  const allowedRoutes = computed(() => menus.value.map((item) => item.route).filter((item) => Boolean(item)));
  const permissionCodes = computed(() => {
    const fromUser = user.value?.permissionCodes ?? [];
    if (fromUser.length) {
      return fromUser;
    }
    return menus.value
      .map((item) => item.permissionCode)
      .filter((item) => Boolean(item));
  });
  const firstMenuPath = computed(() => menus.value[0]?.route ?? '/dashboard');

  function applySession(nextToken: string | null, nextUser: AuthenticatedUser | null, nextMenus: MenuSummary[]) {
    token.value = nextToken;
    user.value = nextUser;
    menus.value = nextMenus;
    initialized.value = true;
  }

  function clearSession() {
    clearToken();
    applySession(null, null, []);
  }

  async function bootstrap(force = false): Promise<void> {
    token.value = getToken();
    if (!token.value) {
      initialized.value = true;
      return;
    }
    if (initialized.value && !force) {
      return;
    }
    if (!bootstrapTask || force) {
      bootstrapTask = Promise.all([fetchCurrentUser(), fetchCurrentMenus()])
        .then(([currentUser, currentMenus]) => {
          applySession(token.value, currentUser, currentMenus);
        })
        .catch((error) => {
          clearSession();
          throw error;
        })
        .finally(() => {
          bootstrapTask = null;
        });
    }
    await bootstrapTask;
  }

  async function login(payload: LoginRequest): Promise<void> {
    const response = await loginApi(payload);
    applySession(response.accessToken, response.user, response.menus);
  }

  async function logout(): Promise<void> {
    try {
      if (token.value) {
        await logoutApi();
      }
    } finally {
      clearSession();
    }
  }

  function canAccess(path: string): boolean {
    return allowedRoutes.value.includes(path);
  }

  function canAccessRoute(path: string): boolean {
    return canAccess(path);
  }

  function hasPermission(permissionCode: string): boolean {
    if (!permissionCode) {
      return true;
    }
    return permissionCodes.value.includes(permissionCode);
  }

  return {
    token,
    user,
    menus,
    initialized,
    isAuthenticated,
    allowedRoutes,
    permissionCodes,
    firstMenuPath,
    bootstrap,
    login,
    logout,
    clearSession,
    canAccess,
    canAccessRoute,
    hasPermission,
  };
});
