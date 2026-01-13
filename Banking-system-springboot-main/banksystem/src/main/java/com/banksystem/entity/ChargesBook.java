package com.banksystem.entity;

import com.banksystem.enums.BankType;
import com.banksystem.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="charges_book")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class ChargesBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="fee_name", nullable = false)
    private String feeName;

    @Column(name="fee_amount",nullable = false)
    private Double feeAmount;

    @Column(name="bank_id", nullable = false)
    private Long bankId;

    @Enumerated(EnumType.STRING)
    @Column(name="bank_type",nullable = false)
    private BankType bankType;

    @Enumerated(EnumType.STRING)
    @Column(name="transaction_type")
    private TransactionType  transactionType;

    @Column(name="max_value")
    private Double maxValue=Double.MAX_VALUE;

    @Column(name="min_value")
    private Double minValue=Double.MIN_VALUE;

    @Column(name="is_active")
    private boolean isActive=true;


    @Column(name="created_at",updatable = false)
    private LocalDateTime createdAt=LocalDateTime.now();

    @Column(name="updated_at")
    private LocalDateTime updatedAt=LocalDateTime.now();
}
