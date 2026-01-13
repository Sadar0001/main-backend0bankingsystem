package com.banksystem.entity;


import com.banksystem.enums.AccountHolderType;
import com.banksystem.enums.AccountStatus;
import com.banksystem.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    @JsonIgnore
    private Branch branch;

    @Column(name = "account_number", nullable = false, unique = true, length = 50)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "current_balance", precision = 15, scale = 2)
    private java.math.BigDecimal currentBalance = java.math.BigDecimal.ZERO;

    @Column(name = "available_balance", precision = 15, scale = 2)
    private java.math.BigDecimal availableBalance = java.math.BigDecimal.ZERO;

    @Column(length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name="account_holder_type")
    private AccountHolderType accountHolderType=AccountHolderType.CUSTOMER;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "opened_date")
    private LocalDateTime openedDate = LocalDateTime.now();

    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();


    @OneToMany(mappedBy = "fromAccount")
    @JsonIgnore
    private List<Transaction> outgoingTransactions;

    @OneToMany(mappedBy = "toAccount")
    @JsonIgnore
    private List<Transaction> incomingTransactions;


    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DebitCard> debitCards;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CardRequest> cardRequests;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ChequeBookRequest> chequeBookRequests;
}