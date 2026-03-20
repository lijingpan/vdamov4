export const orderDetailMessages = {
  'zh-CN': {
    order: {
      columns: {
        actions: '操作',
      },
      actions: {
        viewDetail: '详情',
      },
      detail: {
        sections: {
          basic: '基本信息',
          amount: '金额摘要',
          items: '商品明细',
          appendRecords: '加菜记录',
          paymentRecords: '支付记录',
        },
        fields: {
          orderNo: '订单号',
          createdAt: '下单时间',
          storeName: '门店',
          tableName: '桌位',
          memberName: '会员',
          orderStatus: '订单状态',
          paymentStatus: '支付状态',
          appendOrder: '是否加菜单',
          remark: '备注',
        },
        amounts: {
          originalAmount: '原价金额',
          discountAmount: '优惠金额',
          payableAmount: '应付金额',
          paidAmount: '实付金额',
        },
        items: {
          columns: {
            productName: '商品',
            skuName: '规格',
            quantity: '数量',
            unitPrice: '单价',
            totalPrice: '小计',
            remark: '备注',
          },
        },
        append: {
          columns: {
            appendNo: '加菜号',
            operatorName: '操作人',
            quantity: '加菜数量',
            amount: '加菜金额',
            remark: '备注',
            createdAt: '创建时间',
          },
        },
        payment: {
          columns: {
            paymentNo: '支付单号',
            paymentMethod: '支付方式',
            paymentStatus: '支付状态',
            amount: '支付金额',
            operatorName: '操作人',
            paidAt: '支付时间',
          },
        },
        actions: {
          backToList: '返回列表',
        },
        invalidOrderId: '订单 ID 无效。',
        empty: '暂无订单详情数据。',
      },
    },
    page: {
      orderDetail: {
        title: '订单详情',
        description: '查看订单基本信息、金额摘要、商品明细、加菜记录和支付记录。',
      },
    },
  },
  'en-US': {
    order: {
      columns: {
        actions: 'Actions',
      },
      actions: {
        viewDetail: 'Detail',
      },
      detail: {
        sections: {
          basic: 'Basic Information',
          amount: 'Amount Summary',
          items: 'Order Items',
          appendRecords: 'Append Records',
          paymentRecords: 'Payment Records',
        },
        fields: {
          orderNo: 'Order No',
          createdAt: 'Created At',
          storeName: 'Store',
          tableName: 'Table',
          memberName: 'Member',
          orderStatus: 'Order Status',
          paymentStatus: 'Payment Status',
          appendOrder: 'Append Order',
          remark: 'Remark',
        },
        amounts: {
          originalAmount: 'Original Amount',
          discountAmount: 'Discount Amount',
          payableAmount: 'Payable Amount',
          paidAmount: 'Paid Amount',
        },
        items: {
          columns: {
            productName: 'Product',
            skuName: 'SKU',
            quantity: 'Quantity',
            unitPrice: 'Unit Price',
            totalPrice: 'Amount',
            remark: 'Remark',
          },
        },
        append: {
          columns: {
            appendNo: 'Append No',
            operatorName: 'Operator',
            quantity: 'Quantity',
            amount: 'Amount',
            remark: 'Remark',
            createdAt: 'Created At',
          },
        },
        payment: {
          columns: {
            paymentNo: 'Payment No',
            paymentMethod: 'Method',
            paymentStatus: 'Status',
            amount: 'Amount',
            operatorName: 'Operator',
            paidAt: 'Paid At',
          },
        },
        actions: {
          backToList: 'Back to List',
        },
        invalidOrderId: 'Invalid order id.',
        empty: 'No order detail data.',
      },
    },
    page: {
      orderDetail: {
        title: 'Order Detail',
        description: 'Review order overview, amount summary, order items, append records, and payments.',
      },
    },
  },
  'th-TH': {
    order: {
      columns: {
        actions: 'การดำเนินการ',
      },
      actions: {
        viewDetail: 'รายละเอียด',
      },
      detail: {
        sections: {
          basic: 'ข้อมูลพื้นฐาน',
          amount: 'สรุปยอดเงิน',
          items: 'รายการสินค้า',
          appendRecords: 'บันทึกการเพิ่มรายการ',
          paymentRecords: 'บันทึกการชำระเงิน',
        },
        fields: {
          orderNo: 'เลขที่คำสั่งซื้อ',
          createdAt: 'เวลาที่สร้าง',
          storeName: 'ร้าน',
          tableName: 'โต๊ะ',
          memberName: 'สมาชิก',
          orderStatus: 'สถานะคำสั่งซื้อ',
          paymentStatus: 'สถานะการชำระเงิน',
          appendOrder: 'ออเดอร์เพิ่มรายการ',
          remark: 'หมายเหตุ',
        },
        amounts: {
          originalAmount: 'ยอดเดิม',
          discountAmount: 'ส่วนลด',
          payableAmount: 'ยอดที่ต้องชำระ',
          paidAmount: 'ยอดที่ชำระแล้ว',
        },
        items: {
          columns: {
            productName: 'สินค้า',
            skuName: 'ตัวเลือก',
            quantity: 'จำนวน',
            unitPrice: 'ราคาต่อหน่วย',
            totalPrice: 'ยอดรวม',
            remark: 'หมายเหตุ',
          },
        },
        append: {
          columns: {
            appendNo: 'เลขที่เพิ่มรายการ',
            operatorName: 'ผู้ดำเนินการ',
            quantity: 'จำนวนที่เพิ่ม',
            amount: 'ยอดเพิ่ม',
            remark: 'หมายเหตุ',
            createdAt: 'เวลาที่สร้าง',
          },
        },
        payment: {
          columns: {
            paymentNo: 'เลขที่ชำระเงิน',
            paymentMethod: 'วิธีชำระเงิน',
            paymentStatus: 'สถานะ',
            amount: 'ยอดชำระ',
            operatorName: 'ผู้ดำเนินการ',
            paidAt: 'เวลาชำระเงิน',
          },
        },
        actions: {
          backToList: 'กลับไปหน้ารายการ',
        },
        invalidOrderId: 'รหัสคำสั่งซื้อไม่ถูกต้อง',
        empty: 'ไม่มีข้อมูลรายละเอียดคำสั่งซื้อ',
      },
    },
    page: {
      orderDetail: {
        title: 'รายละเอียดคำสั่งซื้อ',
        description: 'ดูข้อมูลคำสั่งซื้อ สรุปยอด รายการสินค้า ประวัติเพิ่มรายการ และประวัติการชำระเงิน',
      },
    },
  },
} as const;
