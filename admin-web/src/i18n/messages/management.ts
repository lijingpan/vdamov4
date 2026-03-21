const sharedManagementMessages = {
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
      businessStatus: 'Business Status',
      businessTypes: 'Business Types',
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

export const managementMessages = {
  'zh-CN': sharedManagementMessages,
  'en-US': sharedManagementMessages,
  'th-TH': sharedManagementMessages,
} as const;
