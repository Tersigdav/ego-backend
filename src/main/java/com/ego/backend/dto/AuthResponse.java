package com.ego.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@Data @AllArgsConstructor
public class AuthResponse {
    private String token;
}
