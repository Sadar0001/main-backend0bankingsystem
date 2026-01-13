package com.banksystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DebitCardRulesDTO {
    private String cardType;
    private BigDecimal issuanceFee;
    private BigDecimal annualFee;
    private BigDecimal dailyWithdrawalLimit;
    private BigDecimal dailyPurchaseLimit;
    private Long headBankId;
}