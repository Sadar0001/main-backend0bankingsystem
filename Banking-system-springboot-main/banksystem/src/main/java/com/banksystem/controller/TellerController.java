package com.banksystem.controller;

import com.banksystem.dto.ApiResponse;
import com.banksystem.dto.PendingSummaryDTO;
import com.banksystem.dto.RejectionDTO;
import com.banksystem.entity.*;
import com.banksystem.security.JwtHelperService;
import com.banksystem.services.TellerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/teller")
@Slf4j
public class TellerController {

    private final TellerService tellerService;
    private final JwtHelperService jwtHelper;

    public TellerController(TellerService tellerService, JwtHelperService jwtHelper) {
        this.tellerService = tellerService;
        this.jwtHelper = jwtHelper;
    }

    // ==================== ACCOUNT REQUESTS ====================

    @GetMapping("/accounts/requests/pending")
    public ResponseEntity<ApiResponse<List<AccountRequest>>> getPendingAccountRequests(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.TellerDetails details = jwtHelper.getTellerDetails(token);

        log.info("Teller {} fetching pending account requests for branch {}",
                details.getTellerId(), details.getBranchId());

        List<AccountRequest> requests = tellerService.getPendingAccountRequests(details.getBranchId());
        log.info("Found {} pending account requests", requests.size());

        return ResponseEntity.ok(ApiResponse.success("Pending account requests retrieved successfully", requests));
    }

    @PostMapping("/accounts/requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<Account>> approveAccountRequest(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} approving account request: {}", tellerId, requestId);
        Account account = tellerService.approveAccountRequest(tellerId, requestId);
        log.info("Account request {} approved successfully. New account number: {}", requestId, account.getAccountNumber());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account request approved successfully", account));
    }

    @PostMapping("/accounts/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectAccountRequest(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId,
            @Valid @RequestBody RejectionDTO rejectionDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} rejecting account request: {}", tellerId, requestId);
        tellerService.rejectAccountRequest(tellerId, requestId, rejectionDTO.getReason());
        log.info("Account request {} rejected successfully", requestId);

        return ResponseEntity.ok(ApiResponse.success("Account request rejected successfully", null));
    }

    // ==================== DEBIT CARD REQUESTS ====================

    @GetMapping("/cards/requests/pending")
    public ResponseEntity<ApiResponse<List<CardRequest>>> getPendingCardRequests(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} fetching pending card requests", tellerId);
        List<CardRequest> requests = tellerService.getPendingCardRequests(tellerId);
        log.info("Found {} pending card requests", requests.size());

        return ResponseEntity.ok(ApiResponse.success("Pending card requests retrieved successfully", requests));
    }

    @PostMapping("/cards/requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<DebitCard>> approveCardRequest(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} approving card request: {}", tellerId, requestId);
        DebitCard debitCard = tellerService.approveCardRequest(tellerId, requestId);
        log.info("Card request {} approved successfully. Card number: {}", requestId, debitCard.getCardNumber());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Card request approved and card issued successfully", debitCard));
    }

    @PostMapping("/cards/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectCardRequest(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId,
            @Valid @RequestBody RejectionDTO rejectionDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} rejecting card request: {}", tellerId, requestId);
        tellerService.rejectCardRequest(tellerId, requestId, rejectionDTO.getReason());
        log.info("Card request {} rejected successfully", requestId);

        return ResponseEntity.ok(ApiResponse.success("Card request rejected successfully", null));
    }

    // ==================== CHEQUE BOOK REQUESTS ====================

    @GetMapping("/chequebooks/requests/pending")
    public ResponseEntity<ApiResponse<List<ChequeBookRequest>>> getPendingChequeBookRequests(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} fetching pending cheque book requests", tellerId);
        List<ChequeBookRequest> requests = tellerService.getPendingChequeBookRequests(tellerId);
        log.info("Found {} pending cheque book requests", requests.size());

        return ResponseEntity.ok(ApiResponse.success("Pending cheque book requests retrieved successfully", requests));
    }

    @PostMapping("/chequebooks/requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<ChequeBook>> approveChequeBookRequest(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} approving cheque book request: {}", tellerId, requestId);
        ChequeBook chequeBook = tellerService.approveChequeBookRequest(tellerId, requestId);
        log.info("Cheque book request {} approved successfully. Cheque book number: {}",
                requestId, chequeBook.getChequeBookNumber());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cheque book request approved and issued successfully", chequeBook));
    }

    @PostMapping("/chequebooks/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectChequeBookRequest(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId,
            @Valid @RequestBody RejectionDTO rejectionDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} rejecting cheque book request: {}", tellerId, requestId);
        tellerService.rejectChequeBookRequest(tellerId, requestId, rejectionDTO.getReason());
        log.info("Cheque book request {} rejected successfully", requestId);

        return ResponseEntity.ok(ApiResponse.success("Cheque book request rejected successfully", null));
    }

    // ==================== CUSTOMER MANAGEMENT ====================

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<Customer>>> getBranchCustomers(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} fetching branch customers", tellerId);
        List<Customer> customers = tellerService.getBranchCustomers(tellerId);
        log.info("Found {} customers in the branch", customers.size());

        return ResponseEntity.ok(ApiResponse.success("Branch customers retrieved successfully", customers));
    }

    @PutMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomerDetails(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long customerId,
            @RequestBody CustomerUpdateDTO updatedDetails) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} updating details for customer: {}", tellerId, customerId);
        Customer customer = tellerService.updateCustomerDetails(tellerId, customerId, updatedDetails);
        log.info("Customer {} details updated successfully", customerId);

        return ResponseEntity.ok(ApiResponse.success("Customer details updated successfully", customer));
    }

    // ==================== ACCOUNT VALIDATION ====================

    @GetMapping("/accounts/{accountId}/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateAccountForTransaction(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long accountId) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} validating account: {} for transaction", tellerId, accountId);
        Account account = new Account();
        account.setId(accountId);
        boolean isValid = tellerService.validateAccountForTransaction(account);
        log.info("Account {} validation result: {}", accountId, isValid);

        return ResponseEntity.ok(ApiResponse.success(
                isValid ? "Account is active and valid" : "Account is not active",
                isValid));
    }

    // ==================== DASHBOARD & SUMMARY ====================

    @GetMapping("/dashboard/summary")
    public ResponseEntity<ApiResponse<PendingSummaryDTO>> getPendingRequestsSummary(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long tellerId = jwtHelper.getTellerIdFromToken(token);

        log.info("Teller {} fetching pending requests summary", tellerId);

        List<AccountRequest> accountRequests = tellerService.getPendingAccountRequests(tellerId);
        List<CardRequest> cardRequests = tellerService.getPendingCardRequests(tellerId);
        List<ChequeBookRequest> chequeBookRequests = tellerService.getPendingChequeBookRequests(tellerId);

        PendingSummaryDTO summary = new PendingSummaryDTO(
                accountRequests.size(),
                cardRequests.size(),
                chequeBookRequests.size()
        );

        log.info("Summary: {} account requests, {} card requests, {} cheque book requests",
                summary.getPendingAccountRequests(),
                summary.getPendingCardRequests(),
                summary.getPendingChequeBookRequests());

        return ResponseEntity.ok(ApiResponse.success("Pending requests summary retrieved successfully", summary));
    }
}