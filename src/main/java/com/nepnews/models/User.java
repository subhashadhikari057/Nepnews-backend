package com.nepnews.models;

import com.nepnews.models.enums.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    private String name;

    private String email;

    private String password;

    private Role role; // 👈 New: Use the Role enum (READER, AUTHOR, etc.)
    private boolean isSubscribed; // ✅ New field for subscription status
    

}
