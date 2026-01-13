
package com.banksystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "cheque_book")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChequeBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private Account account;

    @Column(name = "cheque_book_number", nullable = false, unique = true)
    private String chequeBookNumber;

    @Column(name = "starting_cheque_number", nullable = false)
    private Long startingChequeNumber;

    @Column(name = "number_of_leaves", nullable = false)
    private Integer numberOfLeaves;

    @Column(name = "leaves_used")
    private Integer leavesUsed = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "issued_date")
    private LocalDateTime issuedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}