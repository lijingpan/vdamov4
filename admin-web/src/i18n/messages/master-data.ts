export const masterDataMessages = {
  'zh-CN': {
    menu: {
      tableAreas: '桌区管理',
      productCategories: '商品分类',
    },
    tableArea: {
      filter: {
        store: '门店',
        storePlaceholder: '全部门店',
        keyword: '关键词',
        keywordPlaceholder: '桌区名称 / 编码',
      },
      summary: {
        total: '桌区总数',
        enabled: '启用桌区',
        tableCount: '关联桌位数',
      },
      columns: {
        storeName: '门店',
        areaName: '桌区名称',
        areaCode: '桌区编码',
        sortOrder: '排序',
        enabled: '状态',
        tableCount: '桌位数',
      },
    },
    productCategory: {
      filter: {
        store: '门店',
        storePlaceholder: '全部门店',
        enabled: '状态',
        enabledPlaceholder: '全部状态',
        keyword: '关键词',
        keywordPlaceholder: '分类名称 / 编码',
      },
      summary: {
        total: '分类总数',
        enabled: '启用分类',
        productCount: '关联商品数',
      },
      columns: {
        storeName: '门店',
        categoryName: '分类名称',
        categoryCode: '分类编码',
        sortOrder: '排序',
        enabled: '状态',
        productCount: '商品数',
      },
    },
    page: {
      tableAreas: {
        title: '桌区管理',
        description: '查看门店桌区、编码、启用状态和关联桌位数量。',
      },
      productCategories: {
        title: '商品分类',
        description: '查看门店商品分类、编码、启用状态和关联商品数量。',
      },
    },
  },
  'en-US': {
    menu: {
      tableAreas: 'Table Area',
      productCategories: 'Product Category',
    },
    tableArea: {
      filter: {
        store: 'Store',
        storePlaceholder: 'All stores',
        keyword: 'Keyword',
        keywordPlaceholder: 'Area name / code',
      },
      summary: {
        total: 'Total Areas',
        enabled: 'Enabled Areas',
        tableCount: 'Linked Tables',
      },
      columns: {
        storeName: 'Store',
        areaName: 'Area Name',
        areaCode: 'Area Code',
        sortOrder: 'Sort',
        enabled: 'Status',
        tableCount: 'Table Count',
      },
    },
    productCategory: {
      filter: {
        store: 'Store',
        storePlaceholder: 'All stores',
        enabled: 'Status',
        enabledPlaceholder: 'All statuses',
        keyword: 'Keyword',
        keywordPlaceholder: 'Category name / code',
      },
      summary: {
        total: 'Total Categories',
        enabled: 'Enabled Categories',
        productCount: 'Linked Products',
      },
      columns: {
        storeName: 'Store',
        categoryName: 'Category Name',
        categoryCode: 'Category Code',
        sortOrder: 'Sort',
        enabled: 'Status',
        productCount: 'Product Count',
      },
    },
    page: {
      tableAreas: {
        title: 'Table Area Management',
        description: 'Review store table areas, codes, statuses, and linked table counts.',
      },
      productCategories: {
        title: 'Product Category Management',
        description: 'Review store product categories, codes, statuses, and linked product counts.',
      },
    },
  },
  'th-TH': {
    menu: {
      tableAreas: 'จัดการโซนโต๊ะ',
      productCategories: 'หมวดหมู่สินค้า',
    },
    tableArea: {
      filter: {
        store: 'สาขา',
        storePlaceholder: 'ทุกสาขา',
        keyword: 'คำค้น',
        keywordPlaceholder: 'ชื่อโซน / รหัส',
      },
      summary: {
        total: 'จำนวนโซนทั้งหมด',
        enabled: 'โซนที่เปิดใช้งาน',
        tableCount: 'จำนวนโต๊ะที่เชื่อมโยง',
      },
      columns: {
        storeName: 'สาขา',
        areaName: 'ชื่อโซน',
        areaCode: 'รหัสโซน',
        sortOrder: 'ลำดับ',
        enabled: 'สถานะ',
        tableCount: 'จำนวนโต๊ะ',
      },
    },
    productCategory: {
      filter: {
        store: 'สาขา',
        storePlaceholder: 'ทุกสาขา',
        enabled: 'สถานะ',
        enabledPlaceholder: 'ทุกสถานะ',
        keyword: 'คำค้น',
        keywordPlaceholder: 'ชื่อหมวดหมู่ / รหัส',
      },
      summary: {
        total: 'จำนวนหมวดหมู่ทั้งหมด',
        enabled: 'หมวดหมู่ที่เปิดใช้งาน',
        productCount: 'จำนวนสินค้าที่เชื่อมโยง',
      },
      columns: {
        storeName: 'สาขา',
        categoryName: 'ชื่อหมวดหมู่',
        categoryCode: 'รหัสหมวดหมู่',
        sortOrder: 'ลำดับ',
        enabled: 'สถานะ',
        productCount: 'จำนวนสินค้า',
      },
    },
    page: {
      tableAreas: {
        title: 'จัดการโซนโต๊ะ',
        description: 'ตรวจสอบโซนโต๊ะ รหัส สถานะ และจำนวนโต๊ะที่เชื่อมโยงของแต่ละสาขา',
      },
      productCategories: {
        title: 'จัดการหมวดหมู่สินค้า',
        description: 'ตรวจสอบหมวดหมู่สินค้า รหัส สถานะ และจำนวนสินค้าที่เชื่อมโยงของแต่ละสาขา',
      },
    },
  },
} as const;
