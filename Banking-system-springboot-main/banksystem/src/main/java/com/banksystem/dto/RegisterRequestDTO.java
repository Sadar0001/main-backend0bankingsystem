package com.banksystem.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String aadharNumber;
    private String panNumber;
    private Long branchId; // Customer needs a home branch
}