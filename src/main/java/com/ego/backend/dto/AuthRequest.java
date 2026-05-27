package com.ego.backend.dto;

import lombok.Data;

@Data
public class AuthRequest {
    String username;
    String password;
}
