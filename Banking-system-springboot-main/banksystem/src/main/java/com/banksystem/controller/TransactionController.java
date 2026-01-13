package com.banksystem.controller;

import com.banksystem.dto.ApiResponse;
import com.banksystem.dto.TransactionDto;
import com.banksystem.entity.Transaction;
import com.banksystem.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionDto>> makeTransaction(@RequestBody TransactionDto transactionDto) {
        try {
            Transaction transaction = transactionService.makeTransaction(transactionDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transaction completed successfully");
            response.put("transactionReference", transaction.getTransactionReference());
            response.put("amount", transaction.getAmount());
            response.put("totalCharges", transaction.getTotalCharges());
            response.put("netAmount", transaction.getNetAmount());
            response.put("status", transaction.getStatus());
            response.put("transactionDate", transaction.getTransactionDate());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success("ok done ",transactionDto));
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("this is the error {}",e);
            log.warn("this is the error.maassage {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.success("not able to implement",transactionDto));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        try {
            // You'll need to add this method to TransactionService
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transaction retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}