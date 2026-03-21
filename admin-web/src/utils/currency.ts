const DEFAULT_CURRENCY = 'THB';

const formatterCache = new Map<string, Intl.NumberFormat>();

function resolveLocale(locale?: string): string {
  return locale || 'zh-CN';
}

export function formatMoneyFromCent(valueInCent: number | undefined | null, locale?: string): string {
  const normalizedLocale = resolveLocale(locale);
  const cacheKey = `${normalizedLocale}:${DEFAULT_CURRENCY}`;
  let formatter = formatterCache.get(cacheKey);
  if (!formatter) {
    formatter = new Intl.NumberFormat(normalizedLocale, {
      style: 'currency',
      currency: DEFAULT_CURRENCY,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
    formatterCache.set(cacheKey, formatter);
  }

  const numericValue = typeof valueInCent === 'number' && Number.isFinite(valueInCent) ? valueInCent : 0;
  return formatter.format(numericValue / 100);
}
