package com.banksystem.controller;

import com.banksystem.dto.ApiResponse;
import com.banksystem.dto.TellerDTO;
import com.banksystem.entity.*;
import com.banksystem.enums.BankType;
import com.banksystem.security.JwtHelperService;
import com.banksystem.services.ChargesService;
import com.banksystem.services.branchManagerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/branch-manager")
@Slf4j
public class BranchManagerController {

    private final branchManagerService branchManagerService;
    private final ChargesService chargesService;
    private final JwtHelperService jwtHelper;

    public BranchManagerController(branchManagerService branchManagerService,
                                   ChargesService chargesService,
                                   JwtHelperService jwtHelper) {
        this.branchManagerService = branchManagerService;
        this.chargesService = chargesService;
        this.jwtHelper = jwtHelper;
    }

    // ==================== TELLER MANAGEMENT ====================

    @PostMapping("/tellers/add")
    public ResponseEntity<ApiResponse<Teller>> addTeller(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TellerDTO tellerDTO) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.ManagerDetails details = jwtHelper.getManagerDetails(token);

        // Verify teller is being added to manager's branch
        jwtHelper.verifyManagerBranch(token, tellerDTO.getBranchId());

        log.info("Branch Manager {} adding new teller: {}", details.getManagerId(), tellerDTO.getUsername());
        Teller teller = branchManagerService.addTeller(tellerDTO);
        log.info("Successfully added new teller: {}", tellerDTO.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Teller created successfully", teller));
    }

    @DeleteMapping("/tellers/deactivate/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateTeller(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        String token = jwtHelper.extractToken(authHeader);
        Long managerId = jwtHelper.getBranchManagerIdFromToken(token);

        log.info("Branch Manager {} deactivating teller with ID: {}", managerId, id);
        branchManagerService.deactivateTeller(id);
        log.info("Successfully deactivated teller with ID: {}", id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Teller deactivated successfully", null));
    }

    @GetMapping("/tellers/getAllByBranch")
    public ResponseEntity<ApiResponse<List<Teller>>> getTellersByBranch(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.ManagerDetails details = jwtHelper.getManagerDetails(token);

        log.info("Branch Manager {} fetching tellers for branch {}", details.getManagerId(), details.getBranchId());
        List<Teller> tellers = branchManagerService.getTellersByBranch(details.getBranchId());
        log.info("Successfully got {} tellers", tellers.size());

        return ResponseEntity.ok(ApiResponse.success("Tellers retrieved successfully", tellers));
    }

    // ==================== LOAN MANAGEMENT ====================

    @GetMapping("/loans/pending")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getPendingLoanApplications(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long managerId = jwtHelper.getBranchManagerIdFromToken(token);

        log.info("Branch Manager {} fetching pending loan applications", managerId);
        List<LoanApplication> applications = branchManagerService.getPendingLoanApplications(managerId);
        log.info("Found {} pending loan applications", applications.size());

        return ResponseEntity.ok(ApiResponse.success("Pending loan applications retrieved successfully", applications));
    }

    @PostMapping("/loans/{loanApplicationId}/approve")
    public ResponseEntity<ApiResponse<LoanApplication>> approveLoanApplication(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long loanApplicationId,
            @RequestParam BigDecimal approvedAmount,
            @RequestParam Integer approvedTenure) {

        String token = jwtHelper.extractToken(authHeader);
        Long managerId = jwtHelper.getBranchManagerIdFromToken(token);

        log.info("Branch Manager {} approving loan application: {}", managerId, loanApplicationId);
        LoanApplication application = branchManagerService.approveLoanApplication(
                managerId, loanApplicationId, approvedAmount, approvedTenure);
        log.info("Loan application {} approved successfully", loanApplicationId);

        return ResponseEntity.ok(ApiResponse.success("Loan application approved successfully", application));
    }

    @PostMapping("/loans/{loanApplicationId}/reject")
    public ResponseEntity<ApiResponse<LoanApplication>> rejectLoanApplication(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long loanApplicationId,
            @RequestParam String rejectionReason) {

        String token = jwtHelper.extractToken(authHeader);
        Long managerId = jwtHelper.getBranchManagerIdFromToken(token);

        log.info("Branch Manager {} rejecting loan application: {}", managerId, loanApplicationId);
        LoanApplication application = branchManagerService.rejectLoanApplication(
                managerId, loanApplicationId, rejectionReason);
        log.info("Loan application {} rejected successfully", loanApplicationId);

        return ResponseEntity.ok(ApiResponse.success("Loan application rejected successfully", application));
    }

    // ==================== ACCOUNT MANAGEMENT ====================

    @PutMapping("/accounts/{accountId}/freeze")
    public ResponseEntity<ApiResponse<Void>> freezeAccount(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long accountId) {

        String token = jwtHelper.extractToken(authHeader);
        Long managerId = jwtHelper.getBranchManagerIdFromToken(token);

        log.info("Branch Manager {} freezing account: {}", managerId, accountId);
        branchManagerService.freezeAccount(managerId, accountId);
        log.info("Account {} frozen successfully", accountId);

        return ResponseEntity.ok(ApiResponse.success("Account frozen successfully", null));
    }

    @PutMapping("/customers/{customerId}/freeze-all")
    public ResponseEntity<ApiResponse<Void>> freezeAllCustomerAccounts(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long customerId) {

        String token = jwtHelper.extractToken(authHeader);
        Long managerId = jwtHelper.getBranchManagerIdFromToken(token);

        log.info("Branch Manager {} freezing all accounts for customer: {}", managerId, customerId);
        branchManagerService.freezeAllCustomerAccounts(managerId, customerId);
        log.info("All accounts for customer {} frozen successfully", customerId);

        return ResponseEntity.ok(ApiResponse.success("All customer accounts frozen successfully", null));
    }

    @PutMapping("/accounts/{accountId}/unfreeze")
    public ResponseEntity<ApiResponse<Void>> unfreezeAccount(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long accountId) {

        String token = jwtHelper.extractToken(authHeader);
        Long managerId = jwtHelper.getBranchManagerIdFromToken(token);

        log.info("Branch Manager {} unfreezing account: {}", managerId, accountId);
        branchManagerService.unfreezeAccount(managerId, accountId);
        log.info("Account {} unfrozen successfully", accountId);

        return ResponseEntity.ok(ApiResponse.success("Account unfrozen successfully", null));
    }

    @DeleteMapping("/accounts/{accountId}/close")
    public ResponseEntity<ApiResponse<Void>> closeAccount(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long accountId) {

        String token = jwtHelper.extractToken(authHeader);
        Long managerId = jwtHelper.getBranchManagerIdFromToken(token);

        log.info("Branch Manager {} closing account: {}", managerId, accountId);
        branchManagerService.closeAccount(managerId, accountId);
        log.info("Account {} closed successfully", accountId);

        return ResponseEntity.ok(ApiResponse.success("Account closed successfully", null));
    }

    // ==================== REPORTS & ANALYTICS ====================

    @GetMapping("/earnings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBranchEarningDetails(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.ManagerDetails details = jwtHelper.getManagerDetails(token);

        log.info("Branch Manager {} fetching earnings from {} to {}",
                details.getManagerId(), startDate, endDate);

        Map<String, Object> earnings = branchManagerService.getBranchEarningDetails(
                details.getBranchId(), startDate, endDate);

        log.info("Successfully retrieved earnings for branch {}", details.getBranchId());
        return ResponseEntity.ok(ApiResponse.success("Branch earnings retrieved successfully", earnings));
    }

    @GetMapping("/loans/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBranchLoanStatistics(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.ManagerDetails details = jwtHelper.getManagerDetails(token);

        log.info("Branch Manager {} fetching loan statistics", details.getManagerId());

        Map<String, Object> statistics = branchManagerService.getBranchLoanStatistics(details.getBranchId());

        log.info("Successfully retrieved loan statistics for branch {}", details.getBranchId());
        return ResponseEntity.ok(ApiResponse.success("Loan statistics retrieved successfully", statistics));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactionListByDateRange(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.ManagerDetails details = jwtHelper.getManagerDetails(token);

        log.info("Branch Manager {} fetching transactions from {} to {}",
                details.getManagerId(), startDate, endDate);

        List<Transaction> transactions = branchManagerService.getTransactionListByDateRange(
                details.getBranchId(), startDate, endDate);

        log.info("Found {} transactions", transactions.size());
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", transactions));
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomersByBranch(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.ManagerDetails details = jwtHelper.getManagerDetails(token);

        log.info("Branch Manager {} fetching all customers", details.getManagerId());

        List<Customer> customers = branchManagerService.getAllCustomersByBranch(details.getBranchId());

        log.info("Found {} customers", customers.size());
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
    }

    @GetMapping("/customers/{customerId}/details")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomerDetailsWithAccounts(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long customerId) {

        String token = jwtHelper.extractToken(authHeader);
        Long managerId = jwtHelper.getBranchManagerIdFromToken(token);

        log.info("Branch Manager {} fetching detailed customer info for: {}", managerId, customerId);

        Map<String, Object> customerDetails = branchManagerService.getCustomerDetailsWithAccounts(
                managerId, customerId);

        log.info("Successfully retrieved customer details for: {}", customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer details retrieved successfully", customerDetails));
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<Account>>> getAllAccountsByBranch(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.ManagerDetails details = jwtHelper.getManagerDetails(token);

        log.info("Branch Manager {} fetching all accounts", details.getManagerId());

        List<Account> accounts = branchManagerService.getAllAccountsByBranch(details.getBranchId());

        log.info("Found {} accounts", accounts.size());
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", accounts));
    }

    // ==================== CHARGES ENDPOINTS ====================

    @GetMapping("/charges/date-range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBranchChargesByDateRange(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.ManagerDetails details = jwtHelper.getManagerDetails(token);

        log.info("Fetching Branch charges for ID: {}, date range: {} to {}",
                details.getBranchId(), startDate, endDate);

        Map<String, Object> charges = chargesService.getChargesByDateRange(
                details.getBranchId(), BankType.BANK_BRANCH, startDate, endDate);

        log.info("Successfully retrieved charges for Branch ID: {}", details.getBranchId());
        return ResponseEntity.ok(ApiResponse.success("Charges retrieved successfully", charges));
    }

    @GetMapping("/charges/last-month")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBranchChargesLastMonth(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.ManagerDetails details = jwtHelper.getManagerDetails(token);

        log.info("Fetching Branch charges for last month, ID: {}", details.getBranchId());

        Map<String, Object> charges = chargesService.getChargesLastMonth(
                details.getBranchId(), BankType.BANK_BRANCH);

        log.info("Successfully retrieved last month charges for Branch ID: {}", details.getBranchId());
        return ResponseEntity.ok(ApiResponse.success("Last month charges retrieved successfully", charges));
    }

    @GetMapping("/charges/last-year")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBranchChargesLastYear(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.ManagerDetails details = jwtHelper.getManagerDetails(token);

        log.info("Fetching Branch charges for last year, ID: {}", details.getBranchId());

        Map<String, Object> charges = chargesService.getChargesLastYear(
                details.getBranchId(), BankType.BANK_BRANCH);

        log.info("Successfully retrieved last year charges for Branch ID: {}", details.getBranchId());
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
}