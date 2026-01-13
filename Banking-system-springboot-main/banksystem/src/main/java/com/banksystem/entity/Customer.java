package com.banksystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Branch branch;

    @Column(name = "customer_id", nullable = false, unique = true, length = 50)
    private String customerId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    private String email;
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String address;

    @Column(name = "aadhar_number", unique = true, length = 20)
    private String aadharNumber;

    @Column(name = "pan_number", unique = true, length = 20)
    private String panNumber;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "transaction_pin_hash")
    private String transactionPinHash;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "pin_locked_until")
    private LocalDateTime pinLockedUntil;

    // === RELATIONSHIPS (All Excluded from ToString) ===

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Account> accounts;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<LoanApplication> loanApplications;

    @OneToMany(mappedBy = "requestedBy")
    @JsonIgnore
    @ToString.Exclude
    private List<CardRequest> cardRequests;

    @OneToMany(mappedBy = "requestedBy")
    @JsonIgnore
    @ToString.Exclude
    private List<ChequeBookRequest> chequeBookRequests;

    // LINK TO USER
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private User user;
}