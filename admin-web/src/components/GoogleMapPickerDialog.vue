<template>
  <el-dialog
    v-model="visible"
    :title="t('store.map.dialogTitle')"
    width="860px"
    destroy-on-close
    @opened="onOpened"
    @closed="onClosed"
  >
    <div class="map-picker">
      <div class="map-picker__search">
        <el-input
          ref="searchInputRef"
          v-model="addressInput"
          clearable
          :placeholder="t('store.map.searchPlaceholder')"
        />
      </div>
      <div ref="mapContainerRef" class="map-picker__canvas" />
    </div>

    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="confirmSelection">{{ t('common.save') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import { loadGoogleMapsApi } from '@/utils/googleMaps';

interface MapSelection {
  address: string;
  latitude: number;
  longitude: number;
}

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    address?: string;
    countryCode?: string;
    latitude?: number;
    longitude?: number;
  }>(),
  {
    address: '',
    countryCode: '',
    latitude: undefined,
    longitude: undefined,
  },
);

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void;
  (event: 'select', payload: MapSelection): void;
}>();

const { t } = useI18n();

const defaultCenter = { lat: 13.7601366, lng: 100.5659859 };

const searchInputRef = ref<any>(null);
const mapContainerRef = ref<HTMLElement | null>(null);
const addressInput = ref(props.address || '');
const pickedPoint = ref<{ lat: number; lng: number } | null>(null);

let mapInstance: any = null;
let markerInstance: any = null;
let geocoderInstance: any = null;
let autocompleteInstance: any = null;
let mapClickListener: any = null;
let placeChangedListener: any = null;

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
});

function normalizePoint(latitude?: number, longitude?: number): { lat: number; lng: number } | null {
  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
    return null;
  }
  return { lat: Number(latitude), lng: Number(longitude) };
}

function readSearchInputElement(): HTMLInputElement | null {
  const host = searchInputRef.value?.$el as HTMLElement | undefined;
  if (!host) {
    return null;
  }
  return host.querySelector('input');
}

function updateMarkerPosition(lat: number, lng: number, shouldGeocode = true) {
  pickedPoint.value = { lat, lng };
  if (markerInstance) {
    markerInstance.setPosition(pickedPoint.value);
  }
  if (mapInstance) {
    mapInstance.panTo(pickedPoint.value);
  }
  if (!shouldGeocode || !geocoderInstance) {
    return;
  }
  geocoderInstance.geocode({ location: pickedPoint.value }, (results: any, status: string) => {
    if (status === 'OK' && Array.isArray(results) && results[0]?.formatted_address) {
      addressInput.value = results[0].formatted_address;
    }
  });
}

async function initMap() {
  const google = await loadGoogleMapsApi();
  await nextTick();

  if (!mapContainerRef.value) {
    return;
  }

  const initialPoint = normalizePoint(props.latitude, props.longitude) ?? defaultCenter;
  const initialAddress = props.address?.trim() ?? '';
  const countryCode = props.countryCode.trim().toLowerCase();
  const hasCountryRestriction = countryCode.length === 2;
  pickedPoint.value = initialPoint;
  addressInput.value = initialAddress;

  mapInstance = new google.maps.Map(mapContainerRef.value, {
    center: initialPoint,
    zoom: 15,
    mapTypeControl: false,
    streetViewControl: false,
    fullscreenControl: false,
  });
  markerInstance = new google.maps.Marker({
    map: mapInstance,
    position: initialPoint,
    draggable: false,
  });
  geocoderInstance = new google.maps.Geocoder();

  mapClickListener = mapInstance.addListener('click', (event: any) => {
    if (!event?.latLng) {
      return;
    }
    updateMarkerPosition(event.latLng.lat(), event.latLng.lng(), true);
  });

  const inputElement = readSearchInputElement();
  if (inputElement) {
    autocompleteInstance = new google.maps.places.Autocomplete(inputElement, {
      fields: ['formatted_address', 'geometry', 'name'],
      types: ['address'],
      componentRestrictions: hasCountryRestriction ? { country: countryCode } : undefined,
    });
    placeChangedListener = autocompleteInstance.addListener('place_changed', () => {
      const place = autocompleteInstance.getPlace();
      const point = place?.geometry?.location;
      if (!point) {
        return;
      }
      const lat = point.lat();
      const lng = point.lng();
      if (place.formatted_address) {
        addressInput.value = place.formatted_address;
      }
      updateMarkerPosition(lat, lng, false);
    });
  }
}

function destroyMap() {
  if (mapClickListener?.remove) {
    mapClickListener.remove();
  }
  if (placeChangedListener?.remove) {
    placeChangedListener.remove();
  }
  mapClickListener = null;
  placeChangedListener = null;
  mapInstance = null;
  markerInstance = null;
  geocoderInstance = null;
  autocompleteInstance = null;
}

async function onOpened() {
  try {
    await initMap();
  } catch {
    ElMessage.error(t('store.map.loadFailed'));
  }
}

function onClosed() {
  destroyMap();
}

function confirmSelection() {
  if (!pickedPoint.value) {
    ElMessage.warning(t('store.map.selectTip'));
    return;
  }
  if (!addressInput.value.trim()) {
    ElMessage.warning(t('store.form.addressPlaceholder'));
    return;
  }
  emit('select', {
    address: addressInput.value.trim(),
    latitude: Number(pickedPoint.value.lat.toFixed(6)),
    longitude: Number(pickedPoint.value.lng.toFixed(6)),
  });
  visible.value = false;
}
</script>

<style scoped>
.map-picker {
  position: relative;
}

.map-picker__search {
  position: absolute;
  top: 12px;
  left: 12px;
  width: min(560px, calc(100% - 24px));
  z-index: 5;
}

.map-picker__canvas {
  height: 520px;
  border-radius: 12px;
  border: 1px solid #d9e2ef;
}

:global(.pac-container) {
  z-index: 4000 !important;
}
</style>
