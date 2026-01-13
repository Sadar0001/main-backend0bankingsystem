package com.banksystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class HeadBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "central_bank_id", nullable = false)
    @JsonIgnore
    private CentralBank centralBank;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    private String address;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "routing_number", unique = true, length = 20)
    private String routingNumber;

    @Column(name="total_earning")
    private BigDecimal totalEarning=BigDecimal.ZERO;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "headBank", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<HeadBankAdmin> admins;

    @OneToMany(mappedBy = "headBank", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LoanOffers> loanOffers;


    @OneToMany(mappedBy = "headBank", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DebitCardRules> debitCardRules;


    @OneToMany(mappedBy = "headBank", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Branch> branches;

}
