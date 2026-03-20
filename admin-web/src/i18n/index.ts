import { createI18n } from 'vue-i18n';
import enUS from './messages/en-US';
import { orderDetailMessages } from './messages/order-detail';
import thTH from './messages/th-TH';
import zhCN from './messages/zh-CN';

type LocaleMessageObject = Record<string, unknown>;

function mergeLocaleMessages(base: LocaleMessageObject, patch: LocaleMessageObject): LocaleMessageObject {
  if (!patch || typeof patch !== 'object') {
    return base;
  }
  if (!base || typeof base !== 'object' || Array.isArray(base)) {
    return { ...patch };
  }

  const target: LocaleMessageObject = { ...base };
  Object.entries(patch).forEach(([key, value]) => {
    const current = target[key];
    if (
      current &&
      value &&
      typeof current === 'object' &&
      typeof value === 'object' &&
      !Array.isArray(current) &&
      !Array.isArray(value)
    ) {
      target[key] = mergeLocaleMessages(current as LocaleMessageObject, value as LocaleMessageObject);
      return;
    }
    target[key] = value;
  });
  return target;
}

const messages = {
  'zh-CN': mergeLocaleMessages(zhCN as LocaleMessageObject, orderDetailMessages['zh-CN'] as LocaleMessageObject),
  'en-US': mergeLocaleMessages(enUS as LocaleMessageObject, orderDetailMessages['en-US'] as LocaleMessageObject),
  'th-TH': mergeLocaleMessages(thTH as LocaleMessageObject, orderDetailMessages['th-TH'] as LocaleMessageObject),
} as Record<string, any>;

export const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages,
});
