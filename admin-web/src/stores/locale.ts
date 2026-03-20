import { defineStore } from 'pinia';
import { ref } from 'vue';

export type AppLocale = 'zh-CN' | 'en-US' | 'th-TH';

const STORAGE_KEY = 'ordering-admin-locale';

function getInitialLocale(): AppLocale {
  const value = localStorage.getItem(STORAGE_KEY) as AppLocale | null;
  if (value === 'zh-CN' || value === 'en-US' || value === 'th-TH') {
    return value;
  }
  return 'zh-CN';
}

export const useLocaleStore = defineStore('locale', () => {
  const locale = ref<AppLocale>(getInitialLocale());

  function setLocale(next: AppLocale) {
    locale.value = next;
    localStorage.setItem(STORAGE_KEY, next);
  }

  return {
    locale,
    setLocale,
  };
});
