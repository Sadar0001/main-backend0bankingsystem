package com.banksystem.controller;

import com.banksystem.dto.*;
import com.banksystem.entity.*;
import com.banksystem.enums.BankType;
import com.banksystem.exception.ResourceNotFoundException;
import com.banksystem.repository.HeadBankRepository;
import com.banksystem.security.JwtHelperService;
import com.banksystem.services.ChargesService;
import com.banksystem.services.DebitCardRulesService;
import com.banksystem.services.HeadBankAdminSerivice;
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
@RequestMapping("/api/headBank")
@Slf4j
public class HeadBankAdminController {
    private final HeadBankAdminSerivice headBankAdminSerivice;
    private final ChargesService chargesService;
    private final HeadBankRepository headBankRepository;
    private final DebitCardRulesService debitCardRulesService;
    private final JwtHelperService jwtHelper;

    public HeadBankAdminController(HeadBankAdminSerivice headBankAdminSerivice,
                                   ChargesService chargesService,
                                   HeadBankRepository headBankRepository,
                                   DebitCardRulesService debitCardRulesService,
                                   JwtHelperService jwtHelper) {
        this.headBankAdminSerivice = headBankAdminSerivice;
        this.chargesService = chargesService;
        this.headBankRepository = headBankRepository;
        this.debitCardRulesService = debitCardRulesService;
        this.jwtHelper = jwtHelper;
    }

    // ==================== BRANCH MANAGEMENT ====================

    @PostMapping("/add_branch")
    public ResponseEntity<ApiResponse<Branch>> addBranch(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BranchDTO branchDTO) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.HeadAdminDetails details = jwtHelper.getHeadAdminDetails(token);

        // Verify branch is being added to admin's head bank
        if (!branchDTO.getHeadBankId().equals(details.getHeadBankId())) {
            throw new ResourceNotFoundException("Cannot add branch to different head bank");
        }

        log.info("Head Bank Admin {} adding branch: {}", details.getAdminId(), branchDTO.getBranchCode());
        Branch branch = headBankAdminSerivice.addBranch(branchDTO);
        log.info("Successfully created branch {}", branch.getBranchCode());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch created successfully", branch));
    }

    @DeleteMapping("/deactivate_branch/{branchId}")
    public ResponseEntity<ApiResponse<Branch>> deactivateBranch(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable long branchId) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getHeadBankAdminIdFromToken(token);

        log.info("Head Bank Admin {} deactivating branch: {}", adminId, branchId);
        headBankAdminSerivice.deactivateBranch(branchId);
        log.info("Successfully deactivated branch {}", branchId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Branch deactivated successfully", null));
    }

    // ==================== BRANCH MANAGER MANAGEMENT ====================

    @PostMapping("/branch_manager/add")
    public ResponseEntity<ApiResponse<BranchManager>> addBranchManager(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody BranchManagerDTO branchManagerDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getHeadBankAdminIdFromToken(token);

        log.info("Head Bank Admin {} adding branch manager: {}",
                adminId, branchManagerDTO.getFirstName() + " " + branchManagerDTO.getLastName());

        BranchManager branchManager = headBankAdminSerivice.addBranchManager(branchManagerDTO);
        log.info("Successfully added branch manager {}", branchManager.getFullName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch manager created successfully", branchManager));
    }

    @DeleteMapping("/branch_manager/deactive/{managerId}")
    public ResponseEntity<ApiResponse<BranchManager>> deactivateBranchManager(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long managerId) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getHeadBankAdminIdFromToken(token);

        log.info("Head Bank Admin {} deactivating branch manager: {}", adminId, managerId);
        BranchManager branchManager = headBankAdminSerivice.deactivateManager(managerId);
        log.info("Successfully deactivated branch manager {}", branchManager.getFullName());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Branch manager deactivated successfully", null));
    }

    // ==================== LOAN OFFERS MANAGEMENT ====================

    @GetMapping("/loan-offers/all")
    public ResponseEntity<ApiResponse<List<LoanOffers>>> getAllLoanOffers(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.HeadAdminDetails details = jwtHelper.getHeadAdminDetails(token);

        log.info("Head Bank Admin {} fetching loan offers", details.getAdminId());
        List<LoanOffers> ls = headBankAdminSerivice.getAllLoanOffers(details.getHeadBankId());
        log.info("Successfully fetched {} loan offers", ls.size());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("LoanOffers found successfully", ls));
    }

    @PostMapping("/create-loan-offers")
    public ResponseEntity<ApiResponse<LoanOffers>> addLoanOffers(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LoanOfferDTO loanOfferDTO) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.HeadAdminDetails details = jwtHelper.getHeadAdminDetails(token);

        // Verify loan offer is for admin's head bank
        if (!loanOfferDTO.getHeadBankId().equals(details.getHeadBankId())) {
            throw new ResourceNotFoundException("Cannot create loan offer for different head bank");
        }

        log.info("Head Bank Admin {} adding new loan offer {}", details.getAdminId(), loanOfferDTO.getName());
        headBankAdminSerivice.addLoanOffer(loanOfferDTO);
        log.info("Successfully added loan offer {}", loanOfferDTO.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Added new loan offer successfully", null));
    }

    @PutMapping("/loan-offers/{id}")
    public ResponseEntity<ApiResponse<LoanOffers>> updateLoanOffer(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody LoanOfferDTO offerDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getHeadBankAdminIdFromToken(token);

        log.info("Head Bank Admin {} updating loan offer with ID: {}", adminId, id);
        LoanOffers updatedOffer = headBankAdminSerivice.updateLoanOffer(id, offerDTO);
        log.info("Successfully updated loan offer with ID: {}", id);

        return ResponseEntity.ok(ApiResponse.success("Loan offer updated successfully", updatedOffer));
    }

    @DeleteMapping("/loan-offers/deactive/{id}")
    public ResponseEntity<ApiResponse<LoanOffers>> deactivateLoanOffer(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.HeadAdminDetails details = jwtHelper.getHeadAdminDetails(token);

        log.info("Head Bank Admin {} deactivating loan offer with ID: {}", details.getAdminId(), id);
        LoanOffers loanOffers = headBankAdminSerivice.deactivateLoanOffers(details.getHeadBankId(), id);
        log.info("Successfully deactivated loan offer with ID: {}", id);

        return ResponseEntity.ok(ApiResponse.success("Loan offer deactivated successfully", loanOffers));
    }

    // ==================== EARNINGS ====================

    @GetMapping("/headBank-earning")
    public ResponseEntity<ApiResponse<BigDecimal>> getHeadBankEarnings(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.HeadAdminDetails details = jwtHelper.getHeadAdminDetails(token);

        log.info("Head Bank Admin {} fetching earnings", details.getAdminId());
        BigDecimal earnings = headBankAdminSerivice.getHeadBankEarning(details.getHeadBankId());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Head bank earnings retrieved successfully", earnings));
    }

    @GetMapping("/bank-earning")
    public ResponseEntity<ApiResponse<BigDecimal>> getBankEarnings(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Long bankId) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getHeadBankAdminIdFromToken(token);

        log.info("Head Bank Admin {} fetching bank earnings for ID: {}", adminId, bankId);
        BigDecimal earnings = headBankAdminSerivice.getBankEarning(bankId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Bank earnings retrieved successfully", earnings));
    }

    // ==================== CHARGES ENDPOINTS ====================

    @GetMapping("/charges/date-range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHeadBankChargesByDateRange(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.HeadAdminDetails details = jwtHelper.getHeadAdminDetails(token);

        log.info("Fetching Head Bank charges for ID: {}, date range: {} to {}",
                details.getHeadBankId(), startDate, endDate);

        Map<String, Object> charges = chargesService.getChargesByDateRange(
                details.getHeadBankId(), BankType.HEAD_BANK, startDate, endDate);

        log.info("Successfully retrieved charges for Head Bank ID: {}", details.getHeadBankId());
        return ResponseEntity.ok(ApiResponse.success("Charges retrieved successfully", charges));
    }

    @GetMapping("/charges/last-month")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHeadBankChargesLastMonth(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.HeadAdminDetails details = jwtHelper.getHeadAdminDetails(token);

        log.info("Fetching Head Bank charges for last month, ID: {}", details.getHeadBankId());

        Map<String, Object> charges = chargesService.getChargesLastMonth(
                details.getHeadBankId(), BankType.HEAD_BANK);

        log.info("Successfully retrieved last month charges for Head Bank ID: {}", details.getHeadBankId());
        return ResponseEntity.ok(ApiResponse.success("Last month charges retrieved successfully", charges));
    }

    @GetMapping("/charges/last-year")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHeadBankChargesLastYear(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.HeadAdminDetails details = jwtHelper.getHeadAdminDetails(token);

        log.info("Fetching Head Bank charges for last year, ID: {}", details.getHeadBankId());

        Map<String, Object> charges = chargesService.getChargesLastYear(
                details.getHeadBankId(), BankType.HEAD_BANK);

        log.info("Successfully retrieved last year charges for Head Bank ID: {}", details.getHeadBankId());
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

    // ==================== DEBIT CARD RULES ====================

    @PostMapping("/debit-card-rules")
    public ResponseEntity<ApiResponse<DebitCardRules>> addDebitCardRules(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DebitCardRulesDTO rulesDTO) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.HeadAdminDetails details = jwtHelper.getHeadAdminDetails(token);

        // Verify rules are for admin's head bank
        if (!rulesDTO.getHeadBankId().equals(details.getHeadBankId())) {
            throw new ResourceNotFoundException("Cannot create rules for different head bank");
        }

        log.info("Head Bank Admin {} adding debit card rules for card type: {}",
                details.getAdminId(), rulesDTO.getCardType());

        DebitCardRules debitCardRules = debitCardRulesService.addDebitCardRules(rulesDTO);
        log.info("Successfully added debit card rules with ID: {}", debitCardRules.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Debit card rules created successfully", debitCardRules));
    }

    @PutMapping("/debit-card-rules/{rulesId}")
    public ResponseEntity<ApiResponse<DebitCardRules>> updateDebitCardRules(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long rulesId,
            @Valid @RequestBody DebitCardRulesDTO rulesDTO) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getHeadBankAdminIdFromToken(token);

        log.info("Head Bank Admin {} updating debit card rules with ID: {}", adminId, rulesId);
        DebitCardRules updatedRules = debitCardRulesService.updateDebitCardRules(rulesId, rulesDTO);
        log.info("Successfully updated debit card rules with ID: {}", rulesId);

        return ResponseEntity.ok(ApiResponse.success("Debit card rules updated successfully", updatedRules));
    }

    @DeleteMapping("/debit-card-rules/{rulesId}")
    public ResponseEntity<ApiResponse<Void>> deactivateDebitCardRules(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long rulesId) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getHeadBankAdminIdFromToken(token);

        log.info("Head Bank Admin {} deactivating debit card rules with ID: {}", adminId, rulesId);
        debitCardRulesService.deactivateDebitCardRules(rulesId);
        log.info("Successfully deactivated debit card rules with ID: {}", rulesId);

        return ResponseEntity.ok(ApiResponse.success("Debit card rules deactivated successfully", null));
    }

    @GetMapping("/debit-card-rules/head-bank")
    public ResponseEntity<ApiResponse<List<DebitCardRules>>> getAllDebitCardRulesByHeadBank(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.HeadAdminDetails details = jwtHelper.getHeadAdminDetails(token);

        log.info("Head Bank Admin {} fetching all debit card rules", details.getAdminId());
        List<DebitCardRules> rules = debitCardRulesService.getAllDebitCardRulesByHeadBank(details.getHeadBankId());
        log.info("Successfully retrieved {} debit card rules", rules.size());

        return ResponseEntity.ok(ApiResponse.success("Debit card rules retrieved successfully", rules));
    }

    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<Branch>>> getAllBranchesByHeadBank(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtHelper.extractToken(authHeader);
        JwtHelperService.HeadAdminDetails details = jwtHelper.getHeadAdminDetails(token);

        log.info("Head Bank Admin {} fetching all branches", details.getAdminId());

        HeadBank headBank = headBankRepository.findById(details.getHeadBankId())
                .orElseThrow(() -> new ResourceNotFoundException("HeadBank", "id", details.getHeadBankId()));

        List<Branch> branches = headBank.getBranches().stream()
                .filter(Branch::getIsActive)
                .toList();

        log.info("Successfully retrieved {} branches", branches.size());
        return ResponseEntity.ok(ApiResponse.success("Branches retrieved successfully", branches));
    }
}