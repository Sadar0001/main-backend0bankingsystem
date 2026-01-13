package com.banksystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HeadBankDTO {
    private Long id;
    private String name;
    private String code;
    private String routingNumber;
    private String address;
    private String contactNumber;
    private String email;
    private Boolean isActive;
    private Long centralBankId;
    private LocalDateTime createdAt;
}