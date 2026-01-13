package com.banksystem.controller;

import com.banksystem.dto.ApiResponse;
import com.banksystem.dto.HeadBankDTO;
import com.banksystem.entity.HeadBank;
import com.banksystem.enums.BankType;
import com.banksystem.repository.CentralBankRepository;
import com.banksystem.repository.HeadBankRepository;
import com.banksystem.security.JwtHelperService;
import com.banksystem.services.CentralBankAdminServices;
import com.banksystem.services.ChargesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/central-bank")
@Slf4j
public class CentralBankAdminController {

    private final CentralBankRepository centralBankRepository;
    private final CentralBankAdminServices centralBankAdminServices;
    private final HeadBankRepository headBankRepository;
    private final ChargesService chargesService;
    private final JwtHelperService jwtHelper;

    public CentralBankAdminController(CentralBankRepository centralBankRepository,
                                      CentralBankAdminServices centralBankAdminServices,
                                      HeadBankRepository headBankRepository,
                                      ChargesService chargesService,
                                      JwtHelperService jwtHelper) {
        this.centralBankRepository = centralBankRepository;
        this.centralBankAdminServices = centralBankAdminServices;
        this.headBankRepository = headBankRepository;
        this.chargesService = chargesService;
        this.jwtHelper = jwtHelper;
    }

    // ==================== HEAD BANK MANAGEMENT ====================

    @PostMapping("/add/head-bank")
    public ResponseEntity<ApiResponse<HeadBank>> addHeadBank(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody HeadBankDTO headBankDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getSpecificIdFromJwtToken(token);

        log.info("Central Bank Admin {} adding head bank with name: {}", adminId, headBankDTO.getName());
        HeadBank saved_headBank = centralBankAdminServices.addHeadBank(headBankDTO);
        log.info("Successfully added head bank with name: {}", saved_headBank.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Head bank Successfully created", saved_headBank));
    }

    @DeleteMapping("/deactive/{headBankId}")
    public ResponseEntity<ApiResponse<String>> deleteHeadBank(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("headBankId") Long headBankId) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getSpecificIdFromJwtToken(token);

        log.info("Central Bank Admin {} deleting head bank with id {}", adminId, headBankId);
        centralBankAdminServices.deActivateHeadBank(headBankId);
        log.info("Deleted successfully head bank with id {}", headBankId);

        return ResponseEntity.ok()
                .body(ApiResponse.success("Successfully deactivated the headbank", "deactivated account"));
    }

    @GetMapping("/headBanks")
    public ResponseEntity<ApiResponse<List<HeadBank>>> getHeadBanks(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getSpecificIdFromJwtToken(token);

        log.info("Central Bank Admin {} fetching all head banks", adminId);
        List<HeadBank> headBanks = centralBankAdminServices.getAllBanks();

        return ResponseEntity.ok(ApiResponse.success("Head banks retrieved successfully", headBanks));
    }

    // ==================== CHARGES ENDPOINTS ====================

    @GetMapping("/charges/date-range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCentralBankChargesByDateRange(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Long centralBankId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getSpecificIdFromJwtToken(token);

        log.info("Central Bank Admin {} fetching charges for ID: {}, date range: {} to {}",
                adminId, centralBankId, startDate, endDate);

        Map<String, Object> charges = chargesService.getChargesByDateRange(
                centralBankId, BankType.CENTRAL_BANK, startDate, endDate);

        log.info("Successfully retrieved charges for Central Bank ID: {}", centralBankId);
        return ResponseEntity.ok(ApiResponse.success("Charges retrieved successfully", charges));
    }

    @GetMapping("/charges/last-month")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCentralBankChargesLastMonth(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getSpecificIdFromJwtToken(token);

        // Hardcoded centralBankId = 1 (as per your original code)
        log.info("Central Bank Admin {} fetching charges for last month, ID: {}", adminId, 1);

        Map<String, Object> charges = chargesService.getChargesLastMonth(Long.valueOf(1), BankType.CENTRAL_BANK);

        log.info("Successfully retrieved last month charges for Central Bank ID: {}", 1);
        return ResponseEntity.ok(ApiResponse.success("Last month charges retrieved successfully", charges));
    }

    @GetMapping("/charges/last-year")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCentralBankChargesLastYear(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Long centralBankId) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getSpecificIdFromJwtToken(token);

        log.info("Central Bank Admin {} fetching charges for last year, ID: {}", adminId, centralBankId);

        Map<String, Object> charges = chargesService.getChargesLastYear(
                centralBankId, BankType.CENTRAL_BANK);

        log.info("Successfully retrieved last year charges for Central Bank ID: {}", centralBankId);
        return ResponseEntity.ok(ApiResponse.success("Last year charges retrieved successfully", charges));
    }

    @GetMapping("/charges/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTransactionChargesDetail(
            @PathVariable Long transactionId) {

        log.info("Fetching charge details for transaction ID: {}", transactionId);
        Map<String, Object> charges = chargesService.getTransactionChargesDetail(transactionId);
        log.info("Successfully retrieved charge details for transaction ID: {}", transactionId);

        return ResponseEntity.ok(ApiResponse.success("Transaction charges retrieved successfully", charges));
    }

    @GetMapping("/transactions/date-range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllTransactionsWithChargesByDateRange(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Long centralBankId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getSpecificIdFromJwtToken(token);

        log.info("Central Bank Admin {} fetching all transactions with charges for ID: {}, date range: {} to {}",
                adminId, centralBankId, startDate, endDate);

        Map<String, Object> transactions = chargesService.getAllTransactionsWithChargesByDateRange(
                centralBankId, BankType.CENTRAL_BANK, startDate, endDate);

        log.info("Successfully retrieved transactions with charges for Central Bank ID: {}", centralBankId);
        return ResponseEntity.ok(ApiResponse.success("Transactions with charges retrieved successfully", transactions));
    }
}