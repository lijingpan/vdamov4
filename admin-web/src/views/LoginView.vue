<template>
  <div class="login-page">
    <div class="login-page__hero">
      <div class="login-page__copy">
        <span class="login-page__eyebrow">{{ t('login.eyebrow') }}</span>
        <h1>{{ t('app.title') }}</h1>
        <p>{{ t('login.description') }}</p>
      </div>
    </div>
    <div class="login-page__panel">
      <el-card class="login-card" shadow="never">
        <template #header>
          <div class="login-card__header">
            <div>
              <h2>{{ t('login.title') }}</h2>
              <p>{{ t('login.subtitle') }}</p>
            </div>
            <el-select v-model="selectedLocale" class="lang-select" @change="changeLocale">
              <el-option :label="t('locale.zhCN')" value="zh-CN" />
              <el-option :label="t('locale.enUS')" value="en-US" />
              <el-option :label="t('locale.thTH')" value="th-TH" />
            </el-select>
          </div>
        </template>

        <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

        <el-form class="login-form" label-position="top" @submit.prevent="submit">
          <el-form-item :label="t('login.username')">
            <el-input v-model="form.username" :placeholder="t('login.usernamePlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('login.password')">
            <el-input
              v-model="form.password"
              :placeholder="t('login.passwordPlaceholder')"
              show-password
              type="password"
              @keyup.enter="submit"
            />
          </el-form-item>
          <el-button class="login-form__submit" :loading="loading" type="primary" @click="submit">
            {{ t('login.submit') }}
          </el-button>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { ApiError } from '@/api/http';
import { useAuthStore } from '@/stores/auth';
import { useLocaleStore, type AppLocale } from '@/stores/locale';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const localeStore = useLocaleStore();
const { locale, t } = useI18n();

locale.value = localeStore.locale;

const loading = ref(false);
const errorMessage = ref('');
const selectedLocale = ref<AppLocale>(localeStore.locale);
const form = reactive({
  username: 'admin',
  password: 'admin123',
});

watch(
  () => localeStore.locale,
  (value) => {
    locale.value = value;
    selectedLocale.value = value;
  },
);

function changeLocale(value: AppLocale) {
  localeStore.setLocale(value);
}

async function submit() {
  loading.value = true;
  errorMessage.value = '';
  try {
    await authStore.login(form);
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : authStore.firstMenuPath;
    await router.replace(redirect);
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : t('login.failed');
  } finally {
    loading.value = false;
  }
}
</script>
