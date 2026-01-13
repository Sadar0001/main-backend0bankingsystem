package com.banksystem.entity;


import com.banksystem.enums.LoanStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_offer_id", nullable = false)
    @JsonIgnore
    private LoanOffers loanOffer;

    @Column(name = "loan_amount", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal loanAmount;

    @Column(name = "disbursed_amount", precision = 15, scale = 2)
    private java.math.BigDecimal disbursedAmount = java.math.BigDecimal.ZERO;

    @Column(name = "outstanding_balance", precision = 15, scale = 2)
    private java.math.BigDecimal outstandingBalance = java.math.BigDecimal.ZERO;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal interestRate;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LoanStatus status = LoanStatus.REQUESTED;

    @Column(name = "emi_amount", precision = 10, scale = 2)
    private java.math.BigDecimal emiAmount;

    @Column(name = "next_emi_date")
    private LocalDate nextEmiDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}