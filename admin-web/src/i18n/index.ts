import { createI18n } from 'vue-i18n';
import enUS from './messages/en-US';
import { discountMessages } from './messages/discount';
import { managementMessages } from './messages/management';
import { masterDataMessages } from './messages/master-data';
import { menuTypeMessages } from './messages/menu-type';
import { orderDetailMessages } from './messages/order-detail';
import { productConfigMessages } from './messages/product-config';
import { productSimpleMessages } from './messages/product-simple';
import { salesReportMessages } from './messages/sales-report';
import { storeLocationMessages } from './messages/store-location';
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
  'zh-CN': mergeLocaleMessages(
    mergeLocaleMessages(
      mergeLocaleMessages(
        mergeLocaleMessages(
          mergeLocaleMessages(zhCN as LocaleMessageObject, orderDetailMessages['zh-CN'] as LocaleMessageObject),
          masterDataMessages['zh-CN'] as LocaleMessageObject,
        ),
        managementMessages['zh-CN'] as LocaleMessageObject,
      ),
      storeLocationMessages['zh-CN'] as LocaleMessageObject,
    ),
    mergeLocaleMessages(
      mergeLocaleMessages(
        salesReportMessages['zh-CN'] as LocaleMessageObject,
        menuTypeMessages['zh-CN'] as LocaleMessageObject,
      ),
      mergeLocaleMessages(
        mergeLocaleMessages(
          productConfigMessages['zh-CN'] as LocaleMessageObject,
          productSimpleMessages['zh-CN'] as LocaleMessageObject,
        ),
        discountMessages['zh-CN'] as LocaleMessageObject,
      ),
    ),
  ),
  'en-US': mergeLocaleMessages(
    mergeLocaleMessages(
      mergeLocaleMessages(
        mergeLocaleMessages(
          mergeLocaleMessages(enUS as LocaleMessageObject, orderDetailMessages['en-US'] as LocaleMessageObject),
          masterDataMessages['en-US'] as LocaleMessageObject,
        ),
        managementMessages['en-US'] as LocaleMessageObject,
      ),
      storeLocationMessages['en-US'] as LocaleMessageObject,
    ),
    mergeLocaleMessages(
      mergeLocaleMessages(
        salesReportMessages['en-US'] as LocaleMessageObject,
        menuTypeMessages['en-US'] as LocaleMessageObject,
      ),
      mergeLocaleMessages(
        mergeLocaleMessages(
          productConfigMessages['en-US'] as LocaleMessageObject,
          productSimpleMessages['en-US'] as LocaleMessageObject,
        ),
        discountMessages['en-US'] as LocaleMessageObject,
      ),
    ),
  ),
  'th-TH': mergeLocaleMessages(
    mergeLocaleMessages(
      mergeLocaleMessages(
        mergeLocaleMessages(
          mergeLocaleMessages(thTH as LocaleMessageObject, orderDetailMessages['th-TH'] as LocaleMessageObject),
          masterDataMessages['th-TH'] as LocaleMessageObject,
        ),
        managementMessages['th-TH'] as LocaleMessageObject,
      ),
      storeLocationMessages['th-TH'] as LocaleMessageObject,
    ),
    mergeLocaleMessages(
      mergeLocaleMessages(
        salesReportMessages['th-TH'] as LocaleMessageObject,
        menuTypeMessages['th-TH'] as LocaleMessageObject,
      ),
      mergeLocaleMessages(
        mergeLocaleMessages(
          productConfigMessages['th-TH'] as LocaleMessageObject,
          productSimpleMessages['th-TH'] as LocaleMessageObject,
        ),
        discountMessages['th-TH'] as LocaleMessageObject,
      ),
    ),
  ),
} as Record<string, any>;

export const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages,
});
