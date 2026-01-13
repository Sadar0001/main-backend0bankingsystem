package com.banksystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="central_bank")
@AllArgsConstructor
@Getter  @Setter
@Builder
@NoArgsConstructor
public class CentralBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    private String address;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name="total_earning")
    private BigDecimal totalEarning=BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "centralBank", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CentralBankAdmin> admins=  new ArrayList<>();


    @OneToMany(mappedBy = "centralBank", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<HeadBank> headBanks=new ArrayList<>();

}
