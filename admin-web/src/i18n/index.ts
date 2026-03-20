import { createI18n } from 'vue-i18n';
import enUS from './messages/en-US';
import thTH from './messages/th-TH';
import zhCN from './messages/zh-CN';

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS,
  'th-TH': thTH,
};

export const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages,
});
