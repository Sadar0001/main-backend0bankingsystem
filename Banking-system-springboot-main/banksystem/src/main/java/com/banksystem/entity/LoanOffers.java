package com.banksystem.entity;


import com.banksystem.enums.LoanType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "loan_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanOffers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_bank_id", nullable = false)
    @JsonIgnore
    private HeadBank headBank;

    @Column(name = "offer_name", nullable = false)
    private String offerName;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal interestRate;

    @Column(name = "min_amount", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal maxAmount;

    @Column(name = "min_tenure_months", nullable = false)
    private Integer minTenureMonths;

    @Column(name = "max_tenure_months", nullable = false)
    private Integer maxTenureMonths;

    @Column(name = "eligibility_criteria")
    private String eligibilityCriteria;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();


    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false, length = 50)
    private LoanType loanType;


    @OneToMany(mappedBy = "loanOffer", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LoanAccount> loanAccounts;

    @OneToMany(mappedBy = "loanOffer", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LoanApplication> loanApplications;
}