package com.nepnews.dto;

import lombok.Data;

@Data
public class KhaltiPaymentRequestDto {
    private String returnUrl;
    private String websiteUrl;
    private int amount;  // In paisa (e.g., Rs. 100 = 10000)
    private String purchaseOrderId;
    private String purchaseOrderName;

    // Customer Info
    private String name;
    private String email;
    private String phone;
}
