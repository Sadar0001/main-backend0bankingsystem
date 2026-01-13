package com.banksystem.entity;

import com.banksystem.enums.BankType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Charges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name="transaction_id")
    @ManyToOne(fetch=FetchType.LAZY)
    private Transaction transaction;


    @Column(name="fee_name", nullable = false)
    private String feeName;

    @Enumerated(EnumType.STRING)
    @Column(name="bank_type")
    private BankType  bankType;

    @Column(name="charged_amount")
    private Double chargedAmount;

    @Column(name="bank_id")
    private Long bankId;

    @Column(name="created_at",updatable = false)
    private LocalDateTime createdAt=LocalDateTime.now();

}