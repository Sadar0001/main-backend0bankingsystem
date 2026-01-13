package com.banksystem.services;

import com.banksystem.dto.ApiResponse;
import com.banksystem.dto.TransactionDto;
import com.banksystem.entity.Charges;
import com.banksystem.entity.ChargesBook;
import com.banksystem.enums.BankType;
import com.banksystem.exception.BusinessRuleException;
import com.banksystem.repository.ChargesBookRepository;
import com.banksystem.repository.ChargesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChargesService {

    private final ChargesBookRepository chargesBookRepository;
    private final ChargesRepository chargesRepository;

    public ChargesService(ChargesBookRepository chargesBookRepository, ChargesRepository chargesRepository) {
        this.chargesBookRepository = chargesBookRepository;
        this.chargesRepository = chargesRepository;
    }

    // Only CREATE charges, don't save them yet
    public List<Charges> getChargesList(TransactionDto transactionDto) {
        List<ChargesBook> chargesBooks = chargesBookRepository.getAllValidCharges(
                transactionDto.getBankId(),
                transactionDto.getBankType(),
                transactionDto.getTransactionType(),
                transactionDto.getAmount()
        );

        List<Charges> chargesList = new ArrayList<>();
        for (ChargesBook cb : chargesBooks) {
            Charges c = new Charges();
            c.setBankType(cb.getBankType());
            c.setBankId(cb.getBankId());
            c.setFeeName(cb.getFeeName());
            c.setChargedAmount(cb.getFeeAmount());
            chargesList.add(c);
        }
        return chargesList;
    }

    @Transactional(readOnly = true)
    public List<Charges> getChargesByTransactionId(Long transactionId) {
        log.info("Fetching charges for transaction ID: {}", transactionId);
        return chargesRepository.findByTransactionId(transactionId);
    }

    /**
     * Get transaction charges summary for a custom date range
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getChargesByDateRange(Long bankId, BankType bankType,
                                                     LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching charges for bank ID: {}, type: {}, from {} to {}",
                bankId, bankType, startDate, endDate);

        List<Object[]> results = chargesRepository.findTransactionChargesSummaryByDateRange(
                bankId, bankType.name(), startDate, endDate);

        return buildChargesResponse(results);
    }

    /**
     * Get transaction charges summary for last month
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getChargesLastMonth(Long bankId, BankType bankType) {
        log.info("Fetching last month's charges for bank ID: {}, type: {}", bankId, bankType);

        List<Object[]> results = chargesRepository.findTransactionChargesSummaryLastMonth(
                bankId, bankType.name());

        return buildChargesResponse(results);
    }

    /**
     * Get transaction charges summary for last year (grouped by month)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getChargesLastYear(Long bankId, BankType bankType) {
        log.info("Fetching last year's charges for bank ID: {}, type: {}", bankId, bankType);

        List<Object[]> results = chargesRepository.findTransactionChargesSummaryLastYear(
                bankId, bankType.name());

        return buildYearlyChargesResponse(results);
    }

    /**
     * Get detailed charges for a specific transaction with breakdown
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTransactionChargesDetail(Long transactionId) {
        log.info("Fetching detailed charges for transaction ID: {}", transactionId);

        List<Charges> charges = chargesRepository.findByTransactionId(transactionId);

        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", transactionId);
        response.put("totalCharges", charges.stream()
                .mapToDouble(Charges::getChargedAmount)
                .sum());
        response.put("chargesBreakdown", charges.stream()
                .map(charge -> {
                    Map<String, Object> chargeDetail = new HashMap<>();
                    chargeDetail.put("chargeId", charge.getId());
                    chargeDetail.put("feeName", charge.getFeeName());
                    chargeDetail.put("bankType", charge.getBankType());
                    chargeDetail.put("chargedAmount", charge.getChargedAmount());
                    chargeDetail.put("bankId", charge.getBankId());
                    chargeDetail.put("createdAt", charge.getCreatedAt());
                    return chargeDetail;
                })
                .collect(Collectors.toList()));

        return response;
    }

    /**
     * Helper method to build response for transaction list queries
     */
    private Map<String, Object> buildChargesResponse(List<Object[]> results) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        double totalCharges = 0.0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Object[] row : results) {
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("transactionId", row[0]);
            transaction.put("amount", row[1]);
            transaction.put("netAmount", row[2]);
            transaction.put("transactionDate", row[3]);
            transaction.put("transactionReference", row[4]);
            transaction.put("description", row[5]);
            transaction.put("status", row[6]);
            transaction.put("transactionType", row[7]);

            Double charged = row[8] != null ? ((Number) row[8]).doubleValue() : 0.0;
            transaction.put("totalCharged", charged);

            transactions.add(transaction);
            totalCharges += charged;

            if (row[1] != null) {
                totalAmount = totalAmount.add((BigDecimal) row[1]);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions);
        response.put("totalTransactions", transactions.size());
        response.put("totalCharges", totalCharges);
        response.put("totalAmount", totalAmount);

        return response;
    }

    /**
     * Helper method to build response for yearly summary
     */
    private Map<String, Object> buildYearlyChargesResponse(List<Object[]> results) {
        List<Map<String, Object>> monthlySummary = new ArrayList<>();
        double totalCharges = 0.0;
        long totalTransactions = 0;

        for (Object[] row : results) {
            Map<String, Object> month = new HashMap<>();
            month.put("month", row[0]);
            month.put("transactionCount", row[1]);
            month.put("totalAmount", row[2]);
            month.put("totalNetAmount", row[3]);

            Double charged = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            month.put("totalCharged", charged);

            monthlySummary.add(month);
            totalCharges += charged;
            totalTransactions += row[1] != null ? ((Number) row[1]).longValue() : 0;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("monthlySummary", monthlySummary);
        response.put("totalTransactions", totalTransactions);
        response.put("totalCharges", totalCharges);

        return response;
    }

    @PostMapping("charges/{transactionId}")
    public Optional<Charges> getAllChargesByTransactionId(@RequestParam Long transactionId) {
        Optional<Charges> charges=chargesRepository.findAllByTransactionId(transactionId);
        if (!charges.isPresent()) {
           throw new BusinessRuleException("nothing found");
        }
        return charges;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAllTransactionsWithChargesByDateRange(Long bankId, BankType bankType,
                                                                        LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching all transactions with charges for bank ID: {}, type: {}, from {} to {}",
                bankId, bankType, startDate, endDate);

        List<Object[]> results = chargesRepository.findTransactionChargesSummaryByDateRange(
                bankId, bankType.name(), startDate, endDate);

        return buildTransactionsWithChargesResponse(results);
    }

    /**
     * Helper method to build response for transactions with charges
     */
    private Map<String, Object> buildTransactionsWithChargesResponse(List<Object[]> results) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        double totalAllCharges = 0.0;
        BigDecimal totalAllAmount = BigDecimal.ZERO;

        for (Object[] row : results) {
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("transactionId", row[0]);
            transaction.put("amount", row[1]);
            transaction.put("netAmount", row[2]);
            transaction.put("transactionDate", row[3]);
            transaction.put("transactionReference", row[4]);
            transaction.put("description", row[5]);
            transaction.put("status", row[6]);
            transaction.put("transactionType", row[7]);

            Double totalCharged = row[8] != null ? ((Number) row[8]).doubleValue() : 0.0;
            transaction.put("totalCharges", totalCharged);

            transactions.add(transaction);
            totalAllCharges += totalCharged;

            if (row[1] != null) {
                totalAllAmount = totalAllAmount.add((BigDecimal) row[1]);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions);
        response.put("totalTransactions", transactions.size());
        response.put("totalCharges", totalAllCharges);
        response.put("totalAmount", totalAllAmount);
        response.put("averageChargesPerTransaction", transactions.isEmpty() ? 0 : totalAllCharges / transactions.size());

        return response;
    }
}