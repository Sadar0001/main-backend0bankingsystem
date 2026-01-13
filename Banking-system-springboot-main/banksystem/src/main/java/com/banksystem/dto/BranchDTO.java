package com.banksystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BranchDTO {
    private Long id;
    private String name;
    private String branchCode;
    private String ifscCode;
    private String address;
    private String contactNumber;
    private String email;
    private Boolean isActive;
    private Long headBankId;
    private LocalDateTime createdAt;
}