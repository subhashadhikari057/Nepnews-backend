package com.nepnews.controllers;

import com.nepnews.dto.KhaltiPaymentRequestDto;
import com.nepnews.dto.KhaltiVerificationDto;
import com.nepnews.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/khalti")
@RequiredArgsConstructor
public class KhaltiController {

    @Value("${KHALTI_SECRET_KEY}")
    private String secretKey;
    @Value("${KHALTI_BASE_URL}")
    private String khaltiBaseUrl;

    @Autowired
    private UserService userService;


    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@RequestBody KhaltiPaymentRequestDto dto) {
        String khaltiApi = khaltiBaseUrl + "/epayment/initiate/";
        String khaltiLookupUrl = khaltiBaseUrl + "/epayment/lookup/";


        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("return_url", dto.getReturnUrl());
        requestBody.put("website_url", dto.getWebsiteUrl());
        requestBody.put("amount", dto.getAmount());
        requestBody.put("purchase_order_id", dto.getPurchaseOrderId());
        requestBody.put("purchase_order_name", dto.getPurchaseOrderName());

        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("name", dto.getName());
        customerInfo.put("email", dto.getEmail());
        customerInfo.put("phone", dto.getPhone());
        requestBody.put("customer_info", customerInfo);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(khaltiApi, request, Map.class);
            return ResponseEntity.ok(response.getBody()); // includes payment_url
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error initiating Khalti payment: " + e.getMessage());
        }
    }
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody KhaltiVerificationDto dto) {
        String khaltiLookupUrl = "https://dev.khalti.com/api/v2/epayment/lookup/";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("pidx", dto.getPidx());

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(khaltiLookupUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && "Completed".equals(responseBody.get("status"))) {
                // Optional: Get userId from metadata if passed via purchaseOrderId
                String userId = (String) responseBody.get("purchase_order_id");

                // ✅ Subscribe the user
                if (userId != null) {
                    userService.subscribeUser(userId); // uses your existing method
                }

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Payment successful. Subscription activated."
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "success", false,
                        "message", "Payment not completed"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Verification error: " + e.getMessage()
            ));
        }
    }
}
