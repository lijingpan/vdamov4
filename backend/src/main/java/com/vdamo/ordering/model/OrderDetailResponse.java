package com.vdamo.ordering.model;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Header header,
        List<Item> items,
        List<AppendLog> appendLogs,
        List<PaymentRecord> paymentRecords,
        AmountSummary amountSummary
) {

    public record Header(
            long id,
            String orderNo,
            long storeId,
            String storeName,
            long tableId,
            String tableName,
            Long memberId,
            String memberDisplayName,
            String orderStatus,
            String paymentStatus,
            int appendCount,
            LocalDateTime createTime
    ) {
    }

    public record Item(
            long id,
            Long productId,
            String itemName,
            String itemCode,
            int unitPriceInCent,
            int quantity,
            int originalAmountInCent,
            int discountAmountInCent,
            int payableAmountInCent,
            String itemStatus,
            int appendRound,
            String remark
    ) {
    }

    public record AppendLog(
            long id,
            int appendRound,
            String actionType,
            int appendItemCount,
            int appendAmountInCent,
            LocalDateTime operateTime,
            String operatorName,
            String note
    ) {
    }

    public record PaymentRecord(
            long id,
            String paymentNo,
            String paymentMethod,
            String paymentChannel,
            int paidAmountInCent,
            int changeAmountInCent,
            String paymentStatus,
            LocalDateTime paidTime,
            String cashierName,
            String remark
    ) {
    }

    public record AmountSummary(
            int originalAmountInCent,
            int discountAmountInCent,
            int payableAmountInCent,
            int paidAmountInCent,
            int outstandingAmountInCent,
            int itemPayableAmountInCent,
            int appendAmountInCent,
            int paymentRecordAmountInCent
    ) {
    }
}
