package com.banksystem.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "debit_card_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "card_type", nullable = false, length = 100)
    private String cardType;

    @Column(name = "daily_withdrawal_limit", precision = 15, scale = 2)
    private java.math.BigDecimal dailyWithdrawalLimit;

    @Column(name = "daily_transaction_limit", precision = 15, scale = 2)
    private java.math.BigDecimal dailyTransactionLimit;

    @Column(name = "international_usage")
    private Boolean internationalUsage = false;

    @Column(name = "annual_fee", precision = 10, scale = 2)
    private java.math.BigDecimal annualFee = java.math.BigDecimal.ZERO;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_bank_id", nullable = false)
    @JsonIgnore
    private HeadBank headBank;

}