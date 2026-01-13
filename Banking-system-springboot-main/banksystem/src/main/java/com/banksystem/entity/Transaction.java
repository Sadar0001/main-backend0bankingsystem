package com.banksystem.entity;

import com.banksystem.enums.TransactionStatus;
import com.banksystem.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    @JsonIgnore
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    @JsonIgnore
    private Account toAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "INR";

    private String description="simple transaction";

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 100)
    private String transactionReference;

    @Column(name="total_charges")
    private BigDecimal totalCharges;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "fee_amount", precision = 10, scale = 2)
    private java.math.BigDecimal feeAmount = java.math.BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 15, scale = 2)
    private java.math.BigDecimal netAmount;

    @OneToMany(mappedBy = "transaction")
    @JsonIgnore
    private List<Charges> charges=new ArrayList<>();

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate = LocalDateTime.now();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}