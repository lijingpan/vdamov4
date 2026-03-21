export const storeLocationMessages = {
  'zh-CN': {
    store: {
      form: {
        address: '门店地址',
        addressPlaceholder: '请输入门店地址',
        latitude: '纬度',
        latitudePlaceholder: '请输入纬度',
        longitude: '经度',
        longitudePlaceholder: '请输入经度',
        pickOnMap: '地图选点',
      },
      map: {
        dialogTitle: '选择门店位置',
        searchPlaceholder: '搜索地址',
        loadFailed: '谷歌地图加载失败，请稍后重试',
        selectTip: '请先在地图上选择位置',
      },
    },
  },
  'en-US': {
    store: {
      form: {
        address: 'Address',
        addressPlaceholder: 'Enter store address',
        latitude: 'Latitude',
        latitudePlaceholder: 'Enter latitude',
        longitude: 'Longitude',
        longitudePlaceholder: 'Enter longitude',
        pickOnMap: 'Pick on Map',
      },
      map: {
        dialogTitle: 'Select Store Location',
        searchPlaceholder: 'Search address',
        loadFailed: 'Failed to load Google Maps. Please retry later.',
        selectTip: 'Please choose a location on the map first.',
      },
    },
  },
  'th-TH': {
    store: {
      form: {
        address: 'ที่อยู่สาขา',
        addressPlaceholder: 'กรอกที่อยู่สาขา',
        latitude: 'ละติจูด',
        latitudePlaceholder: 'กรอกละติจูด',
        longitude: 'ลองจิจูด',
        longitudePlaceholder: 'กรอกลองจิจูด',
        pickOnMap: 'เลือกจากแผนที่',
      },
      map: {
        dialogTitle: 'เลือกตำแหน่งสาขา',
        searchPlaceholder: 'ค้นหาที่อยู่',
        loadFailed: 'โหลด Google Maps ไม่สำเร็จ กรุณาลองใหม่อีกครั้ง',
        selectTip: 'กรุณาเลือกตำแหน่งบนแผนที่ก่อน',
      },
    },
  },
} as const;
