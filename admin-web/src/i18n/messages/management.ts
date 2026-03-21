const enUSManagementMessages = {
  dict: {
    orderStatus: {
      PENDING: 'Pending',
      CONFIRMED: 'Confirmed',
    },
    paymentStatus: {
      UNPAID: 'Unpaid',
      PARTIAL: 'Partial',
      PAID: 'Paid',
      REFUNDED: 'Refunded',
    },
  },
  common: {
    add: 'Add',
    edit: 'Edit',
    save: 'Save',
    cancel: 'Cancel',
    export: 'Export',
    actions: 'Actions',
    enable: 'Enable',
    disable: 'Disable',
  },
  order: {
    filter: {
      paymentStatus: 'Payment Status',
      paymentStatusPlaceholder: 'All payment statuses',
    },
    manage: {
      title: 'Order Operations',
      orderStatus: 'Order Status',
      paymentStatus: 'Payment Status',
      saveOrderStatus: 'Update Order Status',
      savePaymentStatus: 'Update Payment Status',
      complete: 'Complete Order',
      updateSuccess: 'Order updated successfully',
    },
    batch: {
      selectedCount: '{count} selected',
      orderStatusPlaceholder: 'Select order status',
      paymentStatusPlaceholder: 'Select payment status',
      updateOrderStatus: 'Batch Update Order Status',
      updatePaymentStatus: 'Batch Update Payment Status',
      complete: 'Batch Complete',
      clearSelection: 'Clear Selection',
      emptySelection: 'Please select orders first',
    },
    actions: {
      manage: 'Edit',
      complete: 'Complete',
    },
  },
  store: {
    summary: {
      total: 'Total Stores',
      open: 'Open',
      rest: 'Rest',
      disabled: 'Disabled',
    },
    toolbar: {
      add: 'Add Store',
    },
    columns: {
      address: 'Address',
      latitude: 'Latitude',
      longitude: 'Longitude',
    },
    actions: {
      open: 'Resume',
      rest: 'Set Rest',
      disable: 'Disable Store',
    },
    form: {
      name: 'Store Name',
      namePlaceholder: 'Enter store name',
      countryCode: 'Country Code',
      countryCodePlaceholder: 'Example: CN / TH',
      address: 'Address',
      addressPlaceholder: 'Enter store address',
      latitude: 'Latitude',
      latitudePlaceholder: 'Enter latitude',
      longitude: 'Longitude',
      longitudePlaceholder: 'Enter longitude',
      pickOnMap: 'Pick on Map',
      businessStatus: 'Business Status',
      businessTypes: 'Business Types',
    },
    map: {
      dialogTitle: 'Select Store Location',
      searchPlaceholder: 'Search address',
      loadFailed: 'Failed to load Google Maps. Please retry later.',
      selectTip: 'Please choose a location on the map first.',
    },
  },
  tableArea: {
    filter: {
      enabled: 'Enabled',
      enabledPlaceholder: 'All statuses',
    },
    toolbar: {
      add: 'Add Area',
    },
    actions: {
      enable: 'Enable Area',
      disable: 'Disable Area',
    },
    form: {
      store: 'Store',
      storePlaceholder: 'Select store',
      areaName: 'Area Name',
      areaNamePlaceholder: 'Example: Hall A',
      areaCode: 'Area Code',
      areaCodePlaceholder: 'Example: HALL_A',
      sortOrder: 'Sort Order',
      enabled: 'Enabled',
    },
  },
  table: {
    summary: {
      total: 'Total Tables',
      activeOrders: 'Occupied',
      waitingCheckout: 'Waiting Checkout',
      disabled: 'Disabled',
    },
    toolbar: {
      add: 'Add Table',
    },
    actions: {
      markIdle: 'Mark Idle',
      disable: 'Disable Table',
      enable: 'Enable Table',
    },
    form: {
      store: 'Store',
      storePlaceholder: 'Select store',
      areaName: 'Area',
      areaNamePlaceholder: 'Select area',
      tableName: 'Table Name',
      tableNamePlaceholder: 'Example: A01 / VIP-1',
      capacity: 'Capacity',
      status: 'Table Status',
    },
    helper: {
      emptyAreas: 'There is no enabled area in this store yet. Create an area first.',
    },
  },
  product: {
    toolbar: {
      add: 'Add Product',
    },
    actions: {
      enable: 'Enable Product',
      disable: 'Disable Product',
    },
    form: {
      store: 'Store',
      storePlaceholder: 'Select store',
      name: 'Product Name',
      namePlaceholder: 'Enter product name',
      code: 'Product Code',
      codePlaceholder: 'Enter product code',
      categoryCode: 'Category',
      categoryCodePlaceholder: 'Select category',
      priceInCent: 'Price (Cent)',
      priceInCentPlaceholder: 'Enter price in cent',
      active: 'Active',
      emptyCategories: 'No category available for this store. Create a category first.',
    },
  },
  productCategory: {
    toolbar: {
      add: 'Add Category',
    },
    actions: {
      enable: 'Enable Category',
      disable: 'Disable Category',
    },
    form: {
      store: 'Store',
      storePlaceholder: 'Select store',
      categoryName: 'Category Name',
      categoryNamePlaceholder: 'Enter category name',
      categoryCode: 'Category Code',
      categoryCodePlaceholder: 'Enter category code',
      sortOrder: 'Sort Order',
      enabled: 'Enabled',
    },
  },
  device: {
    toolbar: {
      add: 'Add Device',
    },
    actions: {
      enable: 'Enable Device',
      disable: 'Disable Device',
    },
    form: {
      store: 'Store',
      storePlaceholder: 'Select store',
      name: 'Device Name',
      namePlaceholder: 'Enter device name',
      type: 'Device Type',
      typePlaceholder: 'Select device type',
      purpose: 'Device Purpose',
      purposePlaceholder: 'Select purpose',
      brand: 'Brand',
      brandPlaceholder: 'Enter brand',
      sn: 'SN',
      snPlaceholder: 'Enter serial number',
      size: 'Size',
      sizePlaceholder: 'Enter size',
      onlineStatus: 'Online Status',
      onlineStatusPlaceholder: 'Select online status',
      enabled: 'Enabled',
    },
  },
  member: {
    toolbar: {
      add: 'Add Member',
    },
    form: {
      store: 'Store',
      storePlaceholder: 'Select store',
      levelCode: 'Member Level',
      levelCodePlaceholder: 'Enter member level',
      displayName: 'Member Name',
      displayNamePlaceholder: 'Enter member name',
      countryCode: 'Country Code',
      countryCodePlaceholder: 'Enter country code',
      phoneNational: 'National Phone',
      phoneNationalPlaceholder: 'Enter national phone',
      phoneE164: 'International Phone',
      phoneE164Placeholder: 'Enter international phone',
    },
  },
  menuManage: {
    toolbar: {
      add: 'Add Menu',
    },
    summary: {
      total: 'Total Menus',
      topLevel: 'Top Level Menus',
      permissionCount: 'Permission Codes',
    },
    columns: {
      name: 'Menu Name',
      parentName: 'Parent Menu',
      route: 'Route',
      permissionCode: 'Permission Code',
      sortOrder: 'Sort Order',
    },
    form: {
      parent: 'Parent Menu',
      parentPlaceholder: 'Select parent menu',
      parentRoot: 'Root',
      name: 'Menu Name',
      namePlaceholder: 'Enter menu name',
      route: 'Route',
      routePlaceholder: 'Example: /menus',
      permissionCode: 'Permission Code',
      permissionCodePlaceholder: 'Example: menu:view',
      sortOrder: 'Sort Order',
      sortOrderPlaceholder: 'Enter sort order',
    },
  },
  role: {
    toolbar: {
      add: 'Add Role',
    },
    summary: {
      total: 'Total Roles',
      totalUsers: 'Total Users',
      totalPermissions: 'Permission Codes',
    },
    form: {
      code: 'Role Code',
      codePlaceholder: 'Enter role code',
      name: 'Role Name',
      namePlaceholder: 'Enter role name',
      menuIds: 'Menus',
      menuIdsPlaceholder: 'Select menus',
      menuTreeHint: 'Check menus and action permissions in the tree',
      expandAll: 'Expand All',
      collapseAll: 'Collapse All',
      checkAll: 'Check All',
      clearAll: 'Clear All',
    },
  },
  user: {
    toolbar: {
      add: 'Add User',
    },
    summary: {
      total: 'Total Users',
      enabled: 'Enabled Users',
      disabled: 'Disabled Users',
    },
    form: {
      username: 'Username',
      usernamePlaceholder: 'Enter username',
      password: 'Password',
      passwordPlaceholder: 'Enter password',
      passwordPlaceholderOptional: 'Leave blank to keep current password',
      displayName: 'Display Name',
      displayNamePlaceholder: 'Enter display name',
      enabled: 'Enabled',
      roleIds: 'Roles',
      roleIdsPlaceholder: 'Select roles',
      storeIds: 'Stores',
      storeIdsPlaceholder: 'Select stores',
    },
  },
} as const;

const zhCNManagementMessages = {
  dict: {
    orderStatus: {
      PENDING: '待处理',
      CONFIRMED: '已确认',
    },
    paymentStatus: {
      UNPAID: '未支付',
      PARTIAL: '部分支付',
      PAID: '已支付',
      REFUNDED: '已退款',
    },
  },
  common: {
    add: '新增',
    edit: '编辑',
    save: '保存',
    cancel: '取消',
    export: '导出',
    actions: '操作',
    enable: '启用',
    disable: '禁用',
  },
  order: {
    filter: {
      paymentStatus: '支付状态',
      paymentStatusPlaceholder: '全部支付状态',
    },
    manage: {
      title: '订单操作',
      orderStatus: '订单状态',
      paymentStatus: '支付状态',
      saveOrderStatus: '更新订单状态',
      savePaymentStatus: '更新支付状态',
      complete: '完成订单',
      updateSuccess: '订单更新成功',
    },
    batch: {
      selectedCount: '已选 {count} 项',
      orderStatusPlaceholder: '选择订单状态',
      paymentStatusPlaceholder: '选择支付状态',
      updateOrderStatus: '批量更新订单状态',
      updatePaymentStatus: '批量更新支付状态',
      complete: '批量完成',
      clearSelection: '清空选择',
      emptySelection: '请先选择订单',
    },
    actions: {
      manage: '编辑',
      complete: '完成',
    },
  },
  store: {
    summary: {
      total: '门店总数',
      open: '营业中',
      rest: '休息中',
      disabled: '已停用',
    },
    toolbar: {
      add: '新增门店',
    },
    actions: {
      open: '恢复营业',
      rest: '设为休息',
      disable: '停用门店',
    },
    form: {
      name: '门店名称',
      namePlaceholder: '请输入门店名称',
      countryCode: '国家/地区',
      countryCodePlaceholder: '例如：CN / TH',
      businessStatus: '营业状态',
      businessTypes: '业务模式',
    },
  },
  tableArea: {
    filter: {
      enabled: '启用状态',
      enabledPlaceholder: '全部状态',
    },
    toolbar: {
      add: '新增区域',
    },
    actions: {
      enable: '启用区域',
      disable: '停用区域',
    },
    form: {
      store: '门店',
      storePlaceholder: '选择门店',
      areaName: '区域名称',
      areaNamePlaceholder: '例如：大厅A区',
      areaCode: '区域编码',
      areaCodePlaceholder: '例如：HALL_A',
      sortOrder: '排序',
      enabled: '启用',
    },
  },
  table: {
    summary: {
      total: '桌台总数',
      activeOrders: '占用中',
      waitingCheckout: '待结账',
      disabled: '已停用',
    },
    toolbar: {
      add: '新增桌台',
    },
    actions: {
      markIdle: '设为空闲',
      disable: '停用桌台',
      enable: '启用桌台',
    },
    form: {
      store: '门店',
      storePlaceholder: '选择门店',
      areaName: '区域',
      areaNamePlaceholder: '选择区域',
      tableName: '桌台名称',
      tableNamePlaceholder: '例如：A01 / VIP-1',
      capacity: '容量',
      status: '桌台状态',
    },
    helper: {
      emptyAreas: '当前门店还没有启用的区域，请先创建区域。',
    },
  },
  product: {
    toolbar: {
      add: '新增商品',
    },
    actions: {
      enable: '启用商品',
      disable: '停用商品',
    },
    form: {
      store: '门店',
      storePlaceholder: '选择门店',
      name: '商品名称',
      namePlaceholder: '请输入商品名称',
      code: '商品编码',
      codePlaceholder: '请输入商品编码',
      categoryCode: '分类',
      categoryCodePlaceholder: '选择分类',
      priceInCent: '价格（分）',
      priceInCentPlaceholder: '请输入价格（分）',
      active: '启用',
      emptyCategories: '当前门店没有可用分类，请先创建分类。',
    },
  },
  productCategory: {
    toolbar: {
      add: '新增分类',
    },
    actions: {
      enable: '启用分类',
      disable: '停用分类',
    },
    form: {
      store: '门店',
      storePlaceholder: '选择门店',
      categoryName: '分类名称',
      categoryNamePlaceholder: '请输入分类名称',
      categoryCode: '分类编码',
      categoryCodePlaceholder: '请输入分类编码',
      sortOrder: '排序',
      enabled: '启用',
    },
  },
  device: {
    toolbar: {
      add: '新增设备',
    },
    actions: {
      enable: '启用设备',
      disable: '停用设备',
    },
    form: {
      store: '门店',
      storePlaceholder: '选择门店',
      name: '设备名称',
      namePlaceholder: '请输入设备名称',
      type: '设备类型',
      typePlaceholder: '选择设备类型',
      purpose: '设备用途',
      purposePlaceholder: '选择设备用途',
      brand: '品牌',
      brandPlaceholder: '请输入品牌',
      sn: 'SN',
      snPlaceholder: '请输入序列号',
      size: '规格',
      sizePlaceholder: '请输入规格',
      onlineStatus: '在线状态',
      onlineStatusPlaceholder: '选择在线状态',
      enabled: '启用',
    },
  },
  member: {
    toolbar: {
      add: '新增会员',
    },
    form: {
      store: '门店',
      storePlaceholder: '选择门店',
      levelCode: '会员等级',
      levelCodePlaceholder: '请输入会员等级',
      displayName: '会员名称',
      displayNamePlaceholder: '请输入会员名称',
      countryCode: '国家区号',
      countryCodePlaceholder: '请输入国家区号',
      phoneNational: '本地手机号',
      phoneNationalPlaceholder: '请输入本地手机号',
      phoneE164: '国际手机号',
      phoneE164Placeholder: '请输入国际手机号',
    },
  },
  menuManage: {
    toolbar: {
      add: '新增菜单',
    },
    summary: {
      total: '菜单总数',
      topLevel: '顶级菜单',
      permissionCount: '权限码数',
    },
    columns: {
      name: '菜单名称',
      parentName: '上级菜单',
      route: '路由',
      permissionCode: '权限码',
      sortOrder: '排序',
    },
    form: {
      parent: '上级菜单',
      parentPlaceholder: '选择上级菜单',
      parentRoot: '根节点',
      name: '菜单名称',
      namePlaceholder: '请输入菜单名称',
      route: '路由',
      routePlaceholder: '例如：/menus',
      permissionCode: '权限码',
      permissionCodePlaceholder: '例如：menu:view',
      sortOrder: '排序',
      sortOrderPlaceholder: '请输入排序',
    },
  },
  role: {
    toolbar: {
      add: '新增角色',
    },
    summary: {
      total: '角色总数',
      totalUsers: '用户总数',
      totalPermissions: '权限码数',
    },
    form: {
      code: '角色编码',
      codePlaceholder: '请输入角色编码',
      name: '角色名称',
      namePlaceholder: '请输入角色名称',
      menuIds: '菜单权限',
      menuIdsPlaceholder: '请选择菜单权限',
      menuTreeHint: '请在树中勾选菜单和动作权限',
      expandAll: '全部展开',
      collapseAll: '全部收起',
      checkAll: '全部勾选',
      clearAll: '全部清空',
    },
  },
  user: {
    toolbar: {
      add: '新增用户',
    },
    summary: {
      total: '用户总数',
      enabled: '启用用户',
      disabled: '停用用户',
    },
    form: {
      username: '用户名',
      usernamePlaceholder: '请输入用户名',
      password: '密码',
      passwordPlaceholder: '请输入密码',
      passwordPlaceholderOptional: '留空则保持当前密码',
      displayName: '显示名称',
      displayNamePlaceholder: '请输入显示名称',
      enabled: '启用',
      roleIds: '角色',
      roleIdsPlaceholder: '请选择角色',
      storeIds: '门店',
      storeIdsPlaceholder: '请选择门店',
    },
  },
} as const;

const thTHManagementMessages = {
  dict: {
    orderStatus: {
      PENDING: 'รอดำเนินการ',
      CONFIRMED: 'ยืนยันแล้ว',
    },
    paymentStatus: {
      UNPAID: 'ยังไม่ชำระ',
      PARTIAL: 'ชำระบางส่วน',
      PAID: 'ชำระแล้ว',
      REFUNDED: 'คืนเงินแล้ว',
    },
  },
  common: {
    add: 'เพิ่ม',
    edit: 'แก้ไข',
    save: 'บันทึก',
    cancel: 'ยกเลิก',
    export: 'ส่งออก',
    actions: 'การดำเนินการ',
    enable: 'เปิดใช้งาน',
    disable: 'ปิดใช้งาน',
  },
  order: {
    filter: {
      paymentStatus: 'สถานะการชำระเงิน',
      paymentStatusPlaceholder: 'ทุกสถานะการชำระเงิน',
    },
    manage: {
      title: 'การจัดการออเดอร์',
      orderStatus: 'สถานะออเดอร์',
      paymentStatus: 'สถานะการชำระเงิน',
      saveOrderStatus: 'อัปเดตสถานะออเดอร์',
      savePaymentStatus: 'อัปเดตสถานะการชำระเงิน',
      complete: 'ปิดออเดอร์',
      updateSuccess: 'อัปเดตออเดอร์สำเร็จ',
    },
    batch: {
      selectedCount: 'เลือกแล้ว {count} รายการ',
      orderStatusPlaceholder: 'เลือกสถานะออเดอร์',
      paymentStatusPlaceholder: 'เลือกสถานะการชำระเงิน',
      updateOrderStatus: 'อัปเดตสถานะออเดอร์แบบกลุ่ม',
      updatePaymentStatus: 'อัปเดตสถานะการชำระเงินแบบกลุ่ม',
      complete: 'ปิดออเดอร์แบบกลุ่ม',
      clearSelection: 'ล้างรายการที่เลือก',
      emptySelection: 'กรุณาเลือกออเดอร์ก่อน',
    },
    actions: {
      manage: 'แก้ไข',
      complete: 'ปิดออเดอร์',
    },
  },
  store: {
    summary: {
      total: 'จำนวนสาขาทั้งหมด',
      open: 'เปิดให้บริการ',
      rest: 'พักร้าน',
      disabled: 'ปิดใช้งาน',
    },
    toolbar: {
      add: 'เพิ่มสาขา',
    },
    actions: {
      open: 'กลับมาเปิด',
      rest: 'ตั้งเป็นพักร้าน',
      disable: 'ปิดใช้งานสาขา',
    },
    form: {
      name: 'ชื่อสาขา',
      namePlaceholder: 'กรอกชื่อสาขา',
      countryCode: 'ประเทศ/ภูมิภาค',
      countryCodePlaceholder: 'ตัวอย่าง: CN / TH',
      businessStatus: 'สถานะการให้บริการ',
      businessTypes: 'ประเภทธุรกิจ',
    },
  },
  tableArea: {
    filter: {
      enabled: 'สถานะการใช้งาน',
      enabledPlaceholder: 'ทุกสถานะ',
    },
    toolbar: {
      add: 'เพิ่มโซน',
    },
    actions: {
      enable: 'เปิดใช้งานโซน',
      disable: 'ปิดใช้งานโซน',
    },
    form: {
      store: 'สาขา',
      storePlaceholder: 'เลือกสาขา',
      areaName: 'ชื่อโซน',
      areaNamePlaceholder: 'ตัวอย่าง: Hall A',
      areaCode: 'รหัสโซน',
      areaCodePlaceholder: 'ตัวอย่าง: HALL_A',
      sortOrder: 'ลำดับ',
      enabled: 'เปิดใช้งาน',
    },
  },
  table: {
    summary: {
      total: 'จำนวนโต๊ะทั้งหมด',
      activeOrders: 'กำลังใช้งาน',
      waitingCheckout: 'รอชำระเงิน',
      disabled: 'ปิดใช้งาน',
    },
    toolbar: {
      add: 'เพิ่มโต๊ะ',
    },
    actions: {
      markIdle: 'ตั้งเป็นว่าง',
      disable: 'ปิดใช้งานโต๊ะ',
      enable: 'เปิดใช้งานโต๊ะ',
    },
    form: {
      store: 'สาขา',
      storePlaceholder: 'เลือกสาขา',
      areaName: 'โซน',
      areaNamePlaceholder: 'เลือกโซน',
      tableName: 'ชื่อโต๊ะ',
      tableNamePlaceholder: 'ตัวอย่าง: A01 / VIP-1',
      capacity: 'จำนวนที่นั่ง',
      status: 'สถานะโต๊ะ',
    },
    helper: {
      emptyAreas: 'สาขานี้ยังไม่มีโซนที่เปิดใช้งาน กรุณาสร้างโซนก่อน',
    },
  },
  product: {
    toolbar: {
      add: 'เพิ่มสินค้า',
    },
    actions: {
      enable: 'เปิดใช้งานสินค้า',
      disable: 'ปิดใช้งานสินค้า',
    },
    form: {
      store: 'สาขา',
      storePlaceholder: 'เลือกสาขา',
      name: 'ชื่อสินค้า',
      namePlaceholder: 'กรอกชื่อสินค้า',
      code: 'รหัสสินค้า',
      codePlaceholder: 'กรอกรหัสสินค้า',
      categoryCode: 'หมวดหมู่',
      categoryCodePlaceholder: 'เลือกหมวดหมู่',
      priceInCent: 'ราคา (เซ็นต์)',
      priceInCentPlaceholder: 'กรอกราคาเป็นเซ็นต์',
      active: 'เปิดใช้งาน',
      emptyCategories: 'สาขานี้ยังไม่มีหมวดหมู่ที่ใช้งานได้ กรุณาสร้างหมวดหมู่ก่อน',
    },
  },
  productCategory: {
    toolbar: {
      add: 'เพิ่มหมวดหมู่',
    },
    actions: {
      enable: 'เปิดใช้งานหมวดหมู่',
      disable: 'ปิดใช้งานหมวดหมู่',
    },
    form: {
      store: 'สาขา',
      storePlaceholder: 'เลือกสาขา',
      categoryName: 'ชื่อหมวดหมู่',
      categoryNamePlaceholder: 'กรอกชื่อหมวดหมู่',
      categoryCode: 'รหัสหมวดหมู่',
      categoryCodePlaceholder: 'กรอกรหัสหมวดหมู่',
      sortOrder: 'ลำดับ',
      enabled: 'เปิดใช้งาน',
    },
  },
  device: {
    toolbar: {
      add: 'เพิ่มอุปกรณ์',
    },
    actions: {
      enable: 'เปิดใช้งานอุปกรณ์',
      disable: 'ปิดใช้งานอุปกรณ์',
    },
    form: {
      store: 'สาขา',
      storePlaceholder: 'เลือกสาขา',
      name: 'ชื่ออุปกรณ์',
      namePlaceholder: 'กรอกชื่ออุปกรณ์',
      type: 'ประเภทอุปกรณ์',
      typePlaceholder: 'เลือกประเภทอุปกรณ์',
      purpose: 'วัตถุประสงค์การใช้งาน',
      purposePlaceholder: 'เลือกวัตถุประสงค์',
      brand: 'ยี่ห้อ',
      brandPlaceholder: 'กรอกยี่ห้อ',
      sn: 'SN',
      snPlaceholder: 'กรอกหมายเลขซีเรียล',
      size: 'ขนาด',
      sizePlaceholder: 'กรอกขนาด',
      onlineStatus: 'สถานะออนไลน์',
      onlineStatusPlaceholder: 'เลือกสถานะออนไลน์',
      enabled: 'เปิดใช้งาน',
    },
  },
  member: {
    toolbar: {
      add: 'เพิ่มสมาชิก',
    },
    form: {
      store: 'สาขา',
      storePlaceholder: 'เลือกสาขา',
      levelCode: 'ระดับสมาชิก',
      levelCodePlaceholder: 'กรอกระดับสมาชิก',
      displayName: 'ชื่อสมาชิก',
      displayNamePlaceholder: 'กรอกชื่อสมาชิก',
      countryCode: 'รหัสประเทศ',
      countryCodePlaceholder: 'กรอกรหัสประเทศ',
      phoneNational: 'เบอร์โทรในประเทศ',
      phoneNationalPlaceholder: 'กรอกเบอร์โทรในประเทศ',
      phoneE164: 'เบอร์โทรสากล',
      phoneE164Placeholder: 'กรอกเบอร์โทรสากล',
    },
  },
  menuManage: {
    toolbar: {
      add: 'เพิ่มเมนู',
    },
    summary: {
      total: 'จำนวนเมนูทั้งหมด',
      topLevel: 'เมนูระดับบนสุด',
      permissionCount: 'จำนวนรหัสสิทธิ์',
    },
    columns: {
      name: 'ชื่อเมนู',
      parentName: 'เมนูแม่',
      route: 'เส้นทาง',
      permissionCode: 'รหัสสิทธิ์',
      sortOrder: 'ลำดับ',
    },
    form: {
      parent: 'เมนูแม่',
      parentPlaceholder: 'เลือกเมนูแม่',
      parentRoot: 'ราก',
      name: 'ชื่อเมนู',
      namePlaceholder: 'กรอกชื่อเมนู',
      route: 'เส้นทาง',
      routePlaceholder: 'ตัวอย่าง: /menus',
      permissionCode: 'รหัสสิทธิ์',
      permissionCodePlaceholder: 'ตัวอย่าง: menu:view',
      sortOrder: 'ลำดับ',
      sortOrderPlaceholder: 'กรอกลำดับ',
    },
  },
  role: {
    toolbar: {
      add: 'เพิ่มบทบาท',
    },
    summary: {
      total: 'จำนวนบทบาททั้งหมด',
      totalUsers: 'จำนวนผู้ใช้ทั้งหมด',
      totalPermissions: 'จำนวนรหัสสิทธิ์',
    },
    form: {
      code: 'รหัสบทบาท',
      codePlaceholder: 'กรอกรหัสบทบาท',
      name: 'ชื่อบทบาท',
      namePlaceholder: 'กรอกชื่อบทบาท',
      menuIds: 'สิทธิ์เมนู',
      menuIdsPlaceholder: 'เลือกสิทธิ์เมนู',
      menuTreeHint: 'กรุณาเลือกเมนูและสิทธิ์การทำงานในต้นไม้',
      expandAll: 'ขยายทั้งหมด',
      collapseAll: 'ย่อทั้งหมด',
      checkAll: 'เลือกทั้งหมด',
      clearAll: 'ล้างทั้งหมด',
    },
  },
  user: {
    toolbar: {
      add: 'เพิ่มผู้ใช้',
    },
    summary: {
      total: 'จำนวนผู้ใช้ทั้งหมด',
      enabled: 'ผู้ใช้ที่เปิดใช้งาน',
      disabled: 'ผู้ใช้ที่ปิดใช้งาน',
    },
    form: {
      username: 'ชื่อผู้ใช้',
      usernamePlaceholder: 'กรอกชื่อผู้ใช้',
      password: 'รหัสผ่าน',
      passwordPlaceholder: 'กรอกรหัสผ่าน',
      passwordPlaceholderOptional: 'เว้นว่างไว้เพื่อใช้รหัสผ่านเดิม',
      displayName: 'ชื่อที่แสดง',
      displayNamePlaceholder: 'กรอกชื่อที่แสดง',
      enabled: 'เปิดใช้งาน',
      roleIds: 'บทบาท',
      roleIdsPlaceholder: 'เลือกบทบาท',
      storeIds: 'สาขา',
      storeIdsPlaceholder: 'เลือกสาขา',
    },
  },
} as const;

const storeLocationOverrides = {
  'zh-CN': {
    store: {
      columns: {
        address: '门店地址',
        latitude: '纬度',
        longitude: '经度',
      },
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
        loadFailed: 'Google 地图加载失败，请稍后重试',
        selectTip: '请先在地图上选择位置',
      },
    },
  },
  'en-US': {},
  'th-TH': {
    store: {
      columns: {
        address: 'ที่อยู่ร้าน',
        latitude: 'ละติจูด',
        longitude: 'ลองจิจูด',
      },
      form: {
        address: 'ที่อยู่ร้าน',
        addressPlaceholder: 'กรอกที่อยู่ร้าน',
        latitude: 'ละติจูด',
        latitudePlaceholder: 'กรอกละติจูด',
        longitude: 'ลองจิจูด',
        longitudePlaceholder: 'กรอกลองจิจูด',
        pickOnMap: 'เลือกจากแผนที่',
      },
      map: {
        dialogTitle: 'เลือกตำแหน่งร้าน',
        searchPlaceholder: 'ค้นหาที่อยู่',
        loadFailed: 'โหลด Google Maps ไม่สำเร็จ กรุณาลองใหม่อีกครั้ง',
        selectTip: 'กรุณาเลือกตำแหน่งบนแผนที่ก่อน',
      },
    },
  },
} as const;

function mergeMessages(base: Record<string, any>, patch: Record<string, any>): Record<string, any> {
  const result: Record<string, any> = { ...base };
  Object.entries(patch).forEach(([key, value]) => {
    const current = result[key];
    if (
      current &&
      value &&
      typeof current === 'object' &&
      typeof value === 'object' &&
      !Array.isArray(current) &&
      !Array.isArray(value)
    ) {
      result[key] = mergeMessages(current as Record<string, any>, value as Record<string, any>);
      return;
    }
    result[key] = value;
  });
  return result;
}

export const managementMessages = {
  'zh-CN': mergeMessages(zhCNManagementMessages as Record<string, any>, storeLocationOverrides['zh-CN']),
  'en-US': mergeMessages(enUSManagementMessages as Record<string, any>, storeLocationOverrides['en-US']),
  'th-TH': mergeMessages(thTHManagementMessages as Record<string, any>, storeLocationOverrides['th-TH']),
} as const;
