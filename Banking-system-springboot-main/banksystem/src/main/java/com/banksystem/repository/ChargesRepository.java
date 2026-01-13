package com.banksystem.repository;

import com.banksystem.entity.Charges;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChargesRepository extends CrudRepository<Charges, Long> {

    // Get all charges for a specific transaction
    @Query("SELECT c FROM Charges c WHERE c.transaction.id = :transactionId")
    List<Charges> findByTransactionId(@Param("transactionId") Long transactionId);

    // Get transaction summary with total charges for a bank within date range
    @Query(value = """
        SELECT 
            t.id AS transactionId,
            t.amount AS amount,
            t.net_amount AS netAmount,
            t.transaction_date AS transactionDate,
            t.transaction_reference AS transactionReference,
            t.description AS description,
            t.status AS status,
            t.transaction_type AS transactionType,
            SUM(c.charged_amount) AS totalCharged
        FROM transactions t
        JOIN charges c ON t.id = c.transaction_id
        WHERE c.bank_id = :bankId 
          AND c.bank_type = :bankType
          AND c.created_at BETWEEN :startDate AND :endDate
        GROUP BY t.id, t.amount, t.net_amount, t.transaction_date, 
                 t.transaction_reference, t.description, t.status, t.transaction_type
        ORDER BY t.transaction_date DESC
        """, nativeQuery = true)
    List<Object[]> findTransactionChargesSummaryByDateRange(
            @Param("bankId") Long bankId,
            @Param("bankType") String bankType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Get transaction summary with total charges for last month
    @Query(value = """
        SELECT 
            t.id AS transactionId,
            t.amount AS amount,
            t.net_amount AS netAmount,
            t.transaction_date AS transactionDate,
            t.transaction_reference AS transactionReference,
            t.description AS description,
            t.status AS status,
            t.transaction_type AS transactionType,
            DATE_FORMAT(t.transaction_date, '%Y-%m-%d') AS transactionDay,
            SUM(c.charged_amount) AS totalCharged
        FROM transactions t
        JOIN charges c ON t.id = c.transaction_id
        WHERE c.bank_id = :bankId 
          AND c.bank_type = :bankType
          AND t.transaction_date >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)
        GROUP BY t.id, t.amount, t.net_amount, t.transaction_date, 
                 t.transaction_reference, t.description, t.status, t.transaction_type,
                 DATE_FORMAT(t.transaction_date, '%Y-%m-%d')
        ORDER BY t.transaction_date DESC
        """, nativeQuery = true)
    List<Object[]> findTransactionChargesSummaryLastMonth(
            @Param("bankId") Long bankId,
            @Param("bankType") String bankType
    );

    // Get transaction summary with total charges for last year (grouped by month)
    @Query(value = """
        SELECT 
            DATE_FORMAT(t.transaction_date, '%Y-%m') AS month,
            COUNT(DISTINCT t.id) AS transactionCount,
            SUM(t.amount) AS totalAmount,
            SUM(t.net_amount) AS totalNetAmount,
            SUM(c.charged_amount) AS totalCharged
        FROM transactions t
        JOIN charges c ON t.id = c.transaction_id
        WHERE c.bank_id = :bankId 
          AND c.bank_type = :bankType
          AND t.transaction_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
        GROUP BY DATE_FORMAT(t.transaction_date, '%Y-%m')
        ORDER BY month DESC
        """, nativeQuery = true)
    List<Object[]> findTransactionChargesSummaryLastYear(
            @Param("bankId") Long bankId,
            @Param("bankType") String bankType
    );

    Optional<Charges> findAllByTransactionId(Long transactionId);
}
