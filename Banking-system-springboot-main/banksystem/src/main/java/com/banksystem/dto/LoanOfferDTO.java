package com.banksystem.dto;

import com.banksystem.enums.LoanType;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanOfferDTO {
    private String name;
    private String description;
    private LoanType loanType;
    private BigDecimal interestRate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minTenure;
    private Integer maxTenure;
    private BigDecimal processingFee;
    private Long headBankId;
    private String eligibilityCriteria;
}