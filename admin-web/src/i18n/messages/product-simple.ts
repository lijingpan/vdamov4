export const productSimpleMessages = {
  'zh-CN': {
    product: {
      sections: {
        rules: '规则',
      },
      rules: {
        groups: '规则组',
        addGroup: '新增规则组',
        removeGroup: '删除规则组',
        groupNamePlaceholder: '规则名称，例如：规格/口味',
        addValue: '新增规则值',
        removeValue: '删除规则值',
        valuePlaceholder: '规则值，例如：大杯/少冰',
        empty: '未配置规则时将按基础价格创建单规格商品。',
      },
      form: {
        basePriceInCent: '基础价格(分)',
        basePricePlaceholder: '请输入基础价格',
        basePriceHint: '用于自动生成规则组合价格。',
      },
      validation: {
        ruleGroupIncomplete: '规则组需要同时填写规则名称和至少一个规则值。',
        ruleGroupDuplicate: '规则组名称不能重复。',
        ruleValueDuplicate: '同一规则组内的规则值不能重复。',
      },
    },
  },
  'en-US': {
    product: {
      sections: {
        rules: 'Rules',
      },
      rules: {
        groups: 'Rule Groups',
        addGroup: 'Add Rule Group',
        removeGroup: 'Remove Rule Group',
        groupNamePlaceholder: 'Rule name, e.g. Size / Taste',
        addValue: 'Add Rule Value',
        removeValue: 'Remove Rule Value',
        valuePlaceholder: 'Rule value, e.g. Large / Less Ice',
        empty: 'If no rules are configured, a single-spec product will be created from base price.',
      },
      form: {
        basePriceInCent: 'Base Price (Cent)',
        basePricePlaceholder: 'Enter base price',
        basePriceHint: 'Used to auto-generate prices for rule combinations.',
      },
      validation: {
        ruleGroupIncomplete: 'Each rule group needs a name and at least one value.',
        ruleGroupDuplicate: 'Rule group names must be unique.',
        ruleValueDuplicate: 'Rule values in the same group must be unique.',
      },
    },
  },
  'th-TH': {
    product: {
      sections: {
        rules: 'กฎ',
      },
      rules: {
        groups: 'กลุ่มกฎ',
        addGroup: 'เพิ่มกลุ่มกฎ',
        removeGroup: 'ลบกลุ่มกฎ',
        groupNamePlaceholder: 'ชื่อกฎ เช่น ขนาด / รสชาติ',
        addValue: 'เพิ่มค่ากฎ',
        removeValue: 'ลบค่ากฎ',
        valuePlaceholder: 'ค่ากฎ เช่น ใหญ่ / น้ำแข็งน้อย',
        empty: 'หากไม่ตั้งค่ากฎ ระบบจะสร้างสินค้าแบบสเปกเดียวจากราคาพื้นฐาน',
      },
      form: {
        basePriceInCent: 'ราคาพื้นฐาน (สตางค์)',
        basePricePlaceholder: 'กรอกราคาพื้นฐาน',
        basePriceHint: 'ใช้สำหรับสร้างราคารายการกฎแบบอัตโนมัติ',
      },
      validation: {
        ruleGroupIncomplete: 'กลุ่มกฎต้องมีชื่อกฎและมีค่ากฎอย่างน้อยหนึ่งค่า',
        ruleGroupDuplicate: 'ชื่อกลุ่มกฎห้ามซ้ำ',
        ruleValueDuplicate: 'ค่ากฎภายในกลุ่มเดียวกันห้ามซ้ำ',
      },
    },
  },
} as const;
