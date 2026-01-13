package com.banksystem.repository;

import com.banksystem.entity.ChargesBook;
import com.banksystem.enums.BankType;
import com.banksystem.enums.TransactionType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ChargesBookRepository extends CrudRepository<ChargesBook, Long> {

    // Bank-specific searches
    List<ChargesBook> findByBankIdAndBankTypeAndIsActiveTrue(Long bankId, BankType bankType);
    List<ChargesBook> findByBankIdAndBankType(Long bankId, BankType bankType);

    // Duplicate prevention per bank
    boolean existsByBankIdAndBankTypeAndFeeNameAndIsActiveTrue(
            Long bankId, BankType bankType, String feeName);

    @Query("SELECT cb FROM ChargesBook cb WHERE " +
            "cb.bankId = :bankId AND " +              // ← ADD THIS FILTER
            "cb.bankType = :bankType AND " +
            "cb.transactionType = :transactionType AND " +
            ":amount BETWEEN cb.minValue AND cb.maxValue AND " +
            "cb.isActive = true")
    List<ChargesBook> getAllValidCharges(
            @Param("bankId") Long bankId,             // ← ADD THIS PARAM
            @Param("bankType") BankType bankType,
            @Param("transactionType") TransactionType transactionType,
            @Param("amount") BigDecimal amount
    );
}