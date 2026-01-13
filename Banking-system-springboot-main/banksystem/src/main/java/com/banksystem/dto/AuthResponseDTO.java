package com.banksystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String username;
    private String role;
    private Long specificId; // Returns CustomerId, TellerId, or ManagerId based on role
}