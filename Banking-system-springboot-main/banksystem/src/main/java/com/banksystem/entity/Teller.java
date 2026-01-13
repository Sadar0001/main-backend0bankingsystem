package com.banksystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "teller")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Branch branch;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    @JsonIgnore
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(length = 50)
    private String role = "teller";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name="teller_account_id", nullable = false)
    private Long accountId;

    @Column(name="teller_account_number", nullable = false)
    private String accountNumber;

    @OneToMany(mappedBy = "processedBy")
    @JsonIgnore
    @ToString.Exclude
    private List<CardRequest> processedCardRequests;

    @OneToMany(mappedBy = "processedBy")
    @JsonIgnore
    @ToString.Exclude
    private List<ChequeBookRequest> processedChequeBookRequests;

    // LINK TO USER
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private User user;
}