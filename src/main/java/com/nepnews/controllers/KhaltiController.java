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
import java.util.concurrent.ConcurrentHashMap;

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

    // ✅ TEMPORARY IN-MEMORY STORAGE: Map pidx → userId
    private final Map<String, String> pidxUserMap = new ConcurrentHashMap<>();

    // ✅ INITIATE PAYMENT
    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@RequestBody KhaltiPaymentRequestDto dto) {
        String khaltiApi = khaltiBaseUrl + "/epayment/initiate/";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("return_url", dto.getReturnUrl());
        requestBody.put("website_url", dto.getWebsiteUrl());
        requestBody.put("amount", dto.getAmount());
        requestBody.put("purchase_order_id", dto.getPurchaseOrderId());
        requestBody.put("purchase_order_name", dto.getPurchaseOrderName());

        // 💡 We skip merchant_extra — we'll handle mapping internally

        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("name", dto.getName());
        customerInfo.put("email", dto.getEmail());
        customerInfo.put("phone", dto.getPhone());
        requestBody.put("customer_info", customerInfo);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(khaltiApi, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.get("pidx") != null) {
                String pidx = responseBody.get("pidx").toString();
                String userId = dto.getPurchaseOrderId();
                pidxUserMap.put(pidx, userId); // ✅ Save mapping
                System.out.println("💾 Saved pidx mapping: " + pidx + " → " + userId);
            }

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error initiating Khalti payment: " + e.getMessage());
        }
    }

    // ✅ VERIFY PAYMENT AND UPDATE DB
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody KhaltiVerificationDto dto) {
        String khaltiLookupUrl = khaltiBaseUrl + "/epayment/lookup/";

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

            System.out.println("👉 Full Khalti Response: " + responseBody);

            String status = responseBody != null ? (String) responseBody.get("status") : null;
            String userId = pidxUserMap.get(dto.getPidx());

            System.out.println("🔍 Retrieved userId from pidx map: " + userId);

            if ("Completed".equals(status) && userId != null) {
                userService.subscribeUser(userId);
                System.out.println("✅ User subscribed in DB for ID: " + userId);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Payment successful. Subscription activated."
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "success", false,
                        "message", "Payment not completed or userId missing"
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
