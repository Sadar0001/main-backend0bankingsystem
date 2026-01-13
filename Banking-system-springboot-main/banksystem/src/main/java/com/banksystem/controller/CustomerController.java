package com.banksystem.controller;

import com.banksystem.dto.*;
import com.banksystem.entity.*;
import com.banksystem.enums.AccountHolderType;
import com.banksystem.security.JwtHelperService;
import com.banksystem.services.CustomerService;
import com.banksystem.services.TransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@Slf4j
public class CustomerController {

    private final CustomerService customerService;
    private final TransactionService transactionService;
    private final JwtHelperService jwtHelper;

    public CustomerController(CustomerService customerService,
                              TransactionService transactionService,
                              JwtHelperService jwtHelper) {
        this.customerService = customerService;
        this.transactionService = transactionService;
        this.jwtHelper = jwtHelper;
    }

    // ==================== TRANSACTIONS ====================

    @PostMapping("/transaction")
    public ResponseEntity<ApiResponse<Transaction>> makeTransaction(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TransactionDto transactionDto) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        transactionDto.setAccountHolderType(AccountHolderType.CUSTOMER);

        log.info("Customer {} making transaction", customerId);
        Transaction transaction = transactionService.makeTransaction(transactionDto);
        log.info("Transaction successful {}", transaction);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Successfully implemented transaction", transaction));
    }

    // ==================== ACCOUNT REQUESTS ====================

    @PostMapping("/accounts/request")
    public ResponseEntity<ApiResponse<AccountRequest>> requestNewAccount(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AccountRequestDTO requestDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        // Verify customer ID matches
        jwtHelper.verifyCustomerOwnership(token, requestDTO.getCustomerId());

        log.warn("Customer {} requesting new {} account", customerId, requestDTO.getAccountType());
        AccountRequest accountRequest = customerService.createAccountRequest(customerId, requestDTO);
        log.warn("Account request created successfully with ID: {}", accountRequest.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account request submitted successfully", accountRequest));
    }

    @GetMapping("/accounts/requests")
    public ResponseEntity<ApiResponse<List<AccountRequest>>> getMyAccountRequests(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Fetching account requests for customer: {}", customerId);
        List<AccountRequest> requests = customerService.getMyAccountRequests(customerId);

        return ResponseEntity.ok(ApiResponse.success("Account requests retrieved successfully", requests));
    }

    // ==================== LOAN APPLICATIONS ====================

    @PostMapping("/loans/apply")
    public ResponseEntity<ApiResponse<LoanApplication>> applyForLoan(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody LoanApplicationDTO loanDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Customer {} applying for loan of amount: {}", customerId, loanDTO.getRequestedAmount());
        LoanApplication application = customerService.applyForLoan(customerId, loanDTO);
        log.info("Loan application submitted successfully with ID: {}", application.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan application submitted successfully", application));
    }

    @GetMapping("/loans")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getMyLoanApplications(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Fetching loan applications for customer: {}", customerId);
        List<LoanApplication> applications = customerService.getMyLoanApplications(customerId);

        return ResponseEntity.ok(ApiResponse.success("Loan applications retrieved successfully", applications));
    }

    @GetMapping("/loans/offers")
    public ResponseEntity<ApiResponse<List<LoanOffers>>> getAvailableLoanOffers(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Fetching available loan offers for customer: {}", customerId);
        List<LoanOffers> offers = customerService.getAvailableLoanOffers(customerId);

        return ResponseEntity.ok(ApiResponse.success("Loan offers retrieved successfully", offers));
    }

    // ==================== DEBIT CARD REQUESTS ====================

    @PostMapping("/cards/request")
    public ResponseEntity<ApiResponse<CardRequest>> requestDebitCard(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CardRequestDTO cardDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Customer {} requesting debit card for account: {}", customerId, cardDTO.getAccountId());
        CardRequest cardRequest = customerService.requestDebitCard(customerId, cardDTO);
        log.info("Card request submitted successfully with ID: {}", cardRequest.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Card request submitted successfully", cardRequest));
    }

    @GetMapping("/cards/requests")
    public ResponseEntity<ApiResponse<List<CardRequest>>> getMyCardRequests(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Fetching card requests for customer: {}", customerId);
        List<CardRequest> requests = customerService.getMyCardRequests(customerId);

        return ResponseEntity.ok(ApiResponse.success("Card requests retrieved successfully", requests));
    }

    @GetMapping("/cards")
    public ResponseEntity<ApiResponse<List<DebitCard>>> getMyCards(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Fetching all cards for customer: {}", customerId);
        List<DebitCard> cards = customerService.getMyCards(customerId);

        return ResponseEntity.ok(ApiResponse.success("Cards retrieved successfully", cards));
    }

    @PutMapping("/cards/{cardId}/block")
    public ResponseEntity<ApiResponse<Void>> blockCard(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long cardId) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Customer {} blocking card: {}", customerId, cardId);
        customerService.blockCard(customerId, cardId);
        log.info("Card {} blocked successfully", cardId);

        return ResponseEntity.ok(ApiResponse.success("Card blocked successfully", null));
    }

    @PutMapping("/cards/{cardId}/unblock")
    public ResponseEntity<ApiResponse<Void>> unblockCard(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long cardId,
            @RequestParam String transactionPin) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Customer {} unblocking card: {}", customerId, cardId);
        customerService.unblockCard(customerId, cardId, transactionPin);
        log.info("Card {} unblocked successfully", cardId);

        return ResponseEntity.ok(ApiResponse.success("Card unblocked successfully", null));
    }

    // ==================== CHEQUE BOOK REQUESTS ====================

    @PostMapping("/chequebooks/request")
    public ResponseEntity<ApiResponse<ChequeBookRequest>> requestChequeBook(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChequeBookRequestDTO chequeDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Customer {} requesting cheque book for account: {}", customerId, chequeDTO.getAccountId());
        ChequeBookRequest request = customerService.requestChequeBook(customerId, chequeDTO);
        log.info("Cheque book request submitted successfully with ID: {}", request.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cheque book request submitted successfully", request));
    }

    @GetMapping("/chequebooks/requests")
    public ResponseEntity<ApiResponse<List<ChequeBookRequest>>> getMyChequeBookRequests(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Fetching cheque book requests for customer: {}", customerId);
        List<ChequeBookRequest> requests = customerService.getMyChequeBookRequests(customerId);

        return ResponseEntity.ok(ApiResponse.success("Cheque book requests retrieved successfully", requests));
    }

    // ==================== ACCOUNT DETAILS & TRANSACTIONS ====================

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<Account>>> getMyAccounts(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Fetching all accounts for customer: {}", customerId);
        List<Account> accounts = customerService.getMyAccounts(customerId);

        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", accounts));
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<Account>> getAccountDetails(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long accountId) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Fetching account details for customer: {} and account: {}", customerId, accountId);
        Account account = customerService.getAccountDetails(customerId, accountId);

        return ResponseEntity.ok(ApiResponse.success("Account details retrieved successfully", account));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactionHistory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Fetching transaction history for customer: {}, account: {} from {} to {}",
                customerId, accountId, startDate, endDate);

        List<Transaction> transactions = customerService.getTransactionHistory(
                customerId, accountId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success("Transaction history retrieved successfully", transactions));
    }

    @PutMapping("/pin/update")
    public ResponseEntity<ApiResponse<Void>> updateTransactionPin(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdatePinDTO updatePinDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long customerId = jwtHelper.getCustomerIdFromToken(token);

        log.info("Customer {} updating transaction PIN", customerId);
        customerService.updateTransactionPin(customerId, updatePinDTO.getOldPin(), updatePinDTO.getNewPin());
        log.info("Transaction PIN updated successfully for customer: {}", customerId);

        return ResponseEntity.ok(ApiResponse.success("Transaction PIN updated successfully", null));
    }


}