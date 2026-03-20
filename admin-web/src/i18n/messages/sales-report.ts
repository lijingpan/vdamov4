export const salesReportMessages = {
  'zh-CN': {
    menu: {
      salesReports: '销售报表',
    },
    salesReport: {
      filter: {
        store: '门店',
        storePlaceholder: '全部门店',
        dateRange: '日期范围',
        startDate: '开始日期',
        endDate: '结束日期',
      },
      summary: {
        totalOrders: '总订单数',
        completedOrders: '已完成订单',
        inProgressOrders: '进行中订单',
        revenue: '营业额',
        paid: '实收金额',
        discount: '优惠金额',
        appendOrders: '加菜订单数',
        avgOrderAmount: '客单价',
      },
      sections: {
        byStore: '按门店汇总',
        byDate: '按日期趋势',
      },
      byStore: {
        columns: {
          storeName: '门店',
          totalOrders: '总订单',
          completedOrders: '已完成',
          inProgressOrders: '进行中',
          revenue: '营业额',
          paid: '实收',
          discount: '优惠',
          appendOrders: '加菜单',
          avgOrderAmount: '客单价',
        },
      },
      byDate: {
        columns: {
          date: '日期',
          totalOrders: '总订单',
          completedOrders: '已完成',
          inProgressOrders: '进行中',
          revenue: '营业额',
          paid: '实收',
        },
      },
    },
    page: {
      salesReports: {
        title: '销售报表',
        description: '查看经营概览、门店汇总和按日销售趋势。',
      },
    },
  },
  'en-US': {
    menu: {
      salesReports: 'Sales Reports',
    },
    salesReport: {
      filter: {
        store: 'Store',
        storePlaceholder: 'All stores',
        dateRange: 'Date Range',
        startDate: 'Start Date',
        endDate: 'End Date',
      },
      summary: {
        totalOrders: 'Total Orders',
        completedOrders: 'Completed',
        inProgressOrders: 'In Progress',
        revenue: 'Revenue',
        paid: 'Paid',
        discount: 'Discount',
        appendOrders: 'Append Orders',
        avgOrderAmount: 'Avg Order Amount',
      },
      sections: {
        byStore: 'Summary By Store',
        byDate: 'Trend By Date',
      },
      byStore: {
        columns: {
          storeName: 'Store',
          totalOrders: 'Total Orders',
          completedOrders: 'Completed',
          inProgressOrders: 'In Progress',
          revenue: 'Revenue',
          paid: 'Paid',
          discount: 'Discount',
          appendOrders: 'Append Orders',
          avgOrderAmount: 'Avg Order Amount',
        },
      },
      byDate: {
        columns: {
          date: 'Date',
          totalOrders: 'Total Orders',
          completedOrders: 'Completed',
          inProgressOrders: 'In Progress',
          revenue: 'Revenue',
          paid: 'Paid',
          discount: 'Discount',
          appendOrders: 'Append Orders',
          avgOrderAmount: 'Avg Order Amount',
        },
      },
    },
    page: {
      salesReports: {
        title: 'Sales Reports',
        description: 'Review summary metrics, store-level aggregates, and daily trends.',
      },
    },
  },
  'th-TH': {
    menu: {
      salesReports: 'รายงานการขาย',
    },
    salesReport: {
      filter: {
        store: 'สาขา',
        storePlaceholder: 'ทุกสาขา',
        dateRange: 'ช่วงวันที่',
        startDate: 'วันที่เริ่มต้น',
        endDate: 'วันที่สิ้นสุด',
      },
      summary: {
        totalOrders: 'จำนวนออเดอร์ทั้งหมด',
        completedOrders: 'ออเดอร์ที่เสร็จสิ้น',
        inProgressOrders: 'ออเดอร์ที่กำลังดำเนินการ',
        revenue: 'ยอดขาย',
        paid: 'ยอดชำระแล้ว',
        discount: 'ส่วนลด',
        appendOrders: 'ออเดอร์เพิ่มรายการ',
        avgOrderAmount: 'ยอดเฉลี่ยต่อบิล',
      },
      sections: {
        byStore: 'สรุปตามสาขา',
        byDate: 'แนวโน้มรายวัน',
      },
      byStore: {
        columns: {
          storeName: 'สาขา',
          totalOrders: 'ออเดอร์ทั้งหมด',
          completedOrders: 'เสร็จสิ้น',
          inProgressOrders: 'กำลังดำเนินการ',
          revenue: 'ยอดขาย',
          paid: 'ยอดชำระ',
          discount: 'ส่วนลด',
          appendOrders: 'ออเดอร์เพิ่ม',
          avgOrderAmount: 'เฉลี่ยต่อบิล',
        },
      },
      byDate: {
        columns: {
          date: 'วันที่',
          totalOrders: 'ออเดอร์ทั้งหมด',
          completedOrders: 'เสร็จสิ้น',
          inProgressOrders: 'กำลังดำเนินการ',
          revenue: 'ยอดขาย',
          paid: 'ยอดชำระ',
        },
      },
    },
    page: {
      salesReports: {
        title: 'รายงานการขาย',
        description: 'ดูภาพรวมการดำเนินงาน สรุปตามสาขา และแนวโน้มยอดขายรายวัน',
      },
    },
  },
} as const;
