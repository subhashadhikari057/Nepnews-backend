package com.nepnews.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private String name;
    private String userId;
    @JsonProperty("isSubscribed") // ✅ Fixes naming issue
    private boolean isSubscribed;
}
