package com.banksystem.services;

import com.banksystem.dto.TellerDTO;
import com.banksystem.entity.*;
import com.banksystem.enums.*;
import com.banksystem.exception.BusinessRuleException;
import com.banksystem.exception.ResourceNotFoundException;
import com.banksystem.repository.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class branchManagerService {

    private final BranchRepository branchRepository;
    private final TellerRepository tellerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final ChargesBookRepository chargesBookRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final BranchManagerRepository branchManagerRepository;
    private final TellerService tellerService;

    public branchManagerService(BranchRepository branchRepository,
                                TellerRepository tellerRepository,
                                LoanApplicationRepository loanApplicationRepository,
                                ChargesBookRepository chargesBookRepository,
                                AccountRepository accountRepository,
                                CustomerRepository customerRepository,
                                TransactionRepository transactionRepository,
                                BranchManagerRepository branchManagerRepository,
                                TellerService tellerService) {
        this.branchRepository = branchRepository;
        this.tellerRepository = tellerRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.chargesBookRepository = chargesBookRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.branchManagerRepository = branchManagerRepository;
        this.tellerService = tellerService;
    }

    // ==================== HELPER METHOD ====================

    private BranchManager getBranchManager(Long managerId) {
        return branchManagerRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("BranchManager", "id", managerId));
    }

    // ==================== TELLER MANAGEMENT ====================

    @Transactional
    public Teller addTeller(@Valid TellerDTO tellerDTO) {
        try {
            log.info("Finding branch: {}", tellerDTO.getBranchId());
            Branch branch = branchRepository.findById(tellerDTO.getBranchId())
                    .orElseThrow(() -> new BusinessRuleException("Branch not found"));

            log.info("Finding account: {}", tellerDTO.getAccountId());
            Account account = accountRepository.findById(tellerDTO.getAccountId())
                    .orElseThrow(() -> new BusinessRuleException("Account not found"));

            // Validate branch matches
            if (!account.getBranch().getId().equals(branch.getId())) {
                throw new BusinessRuleException("Teller branch and account branch doesn't match");
            }

            // Check if account is already assigned to a teller
            if (account.getAccountHolderType() == AccountHolderType.TELLER) {
                throw new BusinessRuleException("Account is already assigned to a teller");
            }

            // Check if username already exists
            if (tellerRepository.existsByUsername(tellerDTO.getUsername())) {
                throw new BusinessRuleException("Username already exists: " + tellerDTO.getUsername());
            }

            log.info("Creating teller entity");

            Teller teller = new Teller();
            teller.setFullName(tellerDTO.getFirstName() + " " + tellerDTO.getLastName());
            teller.setUsername(tellerDTO.getUsername());
            teller.setEmail(tellerDTO.getEmail());
            teller.setPasswordHash(tellerDTO.getPassword());
            teller.setAccountId(account.getId());
            teller.setAccountNumber(account.getAccountNumber());
            teller.setBranch(branch);

            account.setAccountHolderType(AccountHolderType.TELLER);
            accountRepository.save(account);

            log.info("Saving teller");
            Teller savedTeller = tellerRepository.save(teller);

            log.info("Successfully saved teller with ID: {}", savedTeller.getId());
            return savedTeller;

        } catch (BusinessRuleException e) {
            log.error("Business rule violation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error adding teller: ", e);
            throw new BusinessRuleException("Failed to add teller: " + e.getMessage());
        }
    }

    public void deactivateTeller(Long tellerId) {
        Teller teller = tellerRepository.findById(tellerId)
                .orElseThrow(() -> new BusinessRuleException("Teller id " + tellerId));
        teller.setIsActive(false);
        tellerRepository.save(teller);
    }

    public List<Teller> getTellersByBranch(Long branchId) {
        if (!branchRepository.existsById(branchId)) {
            throw new BusinessRuleException("Branch id " + branchId);
        }
        return tellerRepository.findByBranchIdAndIsActive(branchId, true);
    }

    // ==================== LOAN MANAGEMENT ====================

    public List<LoanApplication> getPendingLoanApplications(Long branchManagerId) {
        BranchManager branchManager = getBranchManager(branchManagerId);
        return loanApplicationRepository.findPendingApplicationsByBranch(branchManager.getBranch().getId());
    }

    @Transactional
    public LoanApplication approveLoanApplication(Long branchManagerId, Long loanApplicationId,
                                                  BigDecimal approvedAmount, Integer approvedTenure) {
        BranchManager branchManager = getBranchManager(branchManagerId);

        LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", loanApplicationId));

        // VERIFY: Loan application belongs to branch manager's branch
        if (!loanApplication.getCustomer().getBranch().getId().equals(branchManager.getBranch().getId())) {
            throw new BusinessRuleException("Access denied: Loan application does not belong to your branch");
        }

        if (loanApplication.getStatus() != RequestStatus.PENDING) {
            throw new BusinessRuleException("Loan application is not in pending status");
        }

        loanApplication.setStatus(RequestStatus.APPROVED);
        loanApplication.setApprovedBy(branchManager);
        loanApplication.setApprovedAmount(approvedAmount);
        loanApplication.setApprovedTenureMonths(approvedTenure);
        loanApplication.setApprovedAt(LocalDateTime.now());

        LoanOffers loanOffers = loanApplication.getLoanOffer();

        Account account = new Account();
        account.setCustomer(loanApplication.getCustomer());
        account.setBranch(loanApplication.getCustomer().getBranch());
        account.setAccountType(AccountType.LOAN);
        account.setAccountNumber(tellerService.generateAccountNumber());
        account.setCurrentBalance(approvedAmount);
        account.setAvailableBalance(approvedAmount);
        Account savedAccount = accountRepository.save(account);

        BigDecimal emiAmount = calculateEMI(approvedAmount, loanOffers.getInterestRate(), approvedTenure);

        LoanAccount loanAccount = new LoanAccount();
        loanAccount.setAccount(savedAccount);
        loanAccount.setLoanOffer(loanApplication.getLoanOffer());
        loanAccount.setDisbursedAmount(approvedAmount);
        loanAccount.setOutstandingBalance(approvedAmount);
        loanAccount.setInterestRate(loanOffers.getInterestRate());
        loanAccount.setStartDate(LocalDate.now());
        loanAccount.setEndDate(LocalDate.now().plusMonths(approvedTenure));
        loanAccount.setEmiAmount(emiAmount);
        loanAccount.setNextEmiDate(LocalDate.now().plusMonths(1));
        loanAccount.setTenureMonths(approvedTenure);
        loanAccount.setStatus(LoanStatus.APPROVED);

        return loanApplicationRepository.save(loanApplication);
    }

    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualInterestRate, Integer tenureMonths) {
        if (tenureMonths <= 0) {
            return principal;
        }

        BigDecimal monthlyRate = annualInterestRate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusR.pow(tenureMonths);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public LoanApplication rejectLoanApplication(Long branchManagerId, Long loanApplicationId, String rejectionReason) {
        BranchManager branchManager = getBranchManager(branchManagerId);

        LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", loanApplicationId));

        // VERIFY: Loan application belongs to branch manager's branch
        if (!loanApplication.getCustomer().getBranch().getId().equals(branchManager.getBranch().getId())) {
            throw new BusinessRuleException("Access denied: Loan application does not belong to your branch");
        }

        if (loanApplication.getStatus() != RequestStatus.PENDING) {
            throw new BusinessRuleException("Loan application is not in pending status");
        }

        loanApplication.setStatus(RequestStatus.REJECTED);
        loanApplication.setRejectionReason(rejectionReason);
        loanApplication.setApprovedAt(LocalDateTime.now());

        return loanApplicationRepository.save(loanApplication);
    }

    // ==================== ACCOUNT MANAGEMENT ====================

    @Transactional
    public void freezeAccount(Long branchManagerId, Long accountId) {
        BranchManager branchManager = getBranchManager(branchManagerId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // VERIFY: Account belongs to branch manager's branch
        if (!account.getBranch().getId().equals(branchManager.getBranch().getId())) {
            throw new BusinessRuleException("Access denied: Account does not belong to your branch");
        }

        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
    }

    @Transactional
    public void freezeAllCustomerAccounts(Long branchManagerId, Long customerId) {
        BranchManager branchManager = getBranchManager(branchManagerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        // VERIFY: Customer belongs to branch manager's branch
        if (!customer.getBranch().getId().equals(branchManager.getBranch().getId())) {
            throw new BusinessRuleException("Access denied: Customer does not belong to your branch");
        }

        List<Account> accounts = accountRepository.findByCustomer(customer);
        for (Account account : accounts) {
            account.setStatus(AccountStatus.FROZEN);
        }
        accountRepository.saveAll(accounts);
    }

    @Transactional
    public void unfreezeAccount(Long branchManagerId, Long accountId) {
        BranchManager branchManager = getBranchManager(branchManagerId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // VERIFY: Account belongs to branch manager's branch
        if (!account.getBranch().getId().equals(branchManager.getBranch().getId())) {
            throw new BusinessRuleException("Access denied: Account does not belong to your branch");
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    @Transactional
    public void closeAccount(Long branchManagerId, Long accountId) {
        BranchManager branchManager = getBranchManager(branchManagerId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // VERIFY: Account belongs to branch manager's branch
        if (!account.getBranch().getId().equals(branchManager.getBranch().getId())) {
            throw new BusinessRuleException("Access denied: Account does not belong to your branch");
        }

        if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException("Cannot close account with balance. Please withdraw all funds first.");
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosedDate(LocalDateTime.now());
        accountRepository.save(account);
    }

    // ==================== REPORTS & ANALYTICS ====================

    public Map<String, Object> getBranchEarningDetails(Long branchId, LocalDateTime startDate, LocalDateTime endDate) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        return Map.of(
                "branchId", branchId,
                "branchName", branch.getName(),
                "startDate", startDate,
                "endDate", endDate,
                "totalEarnings", branch.getTotalEarning(),
                "periodEarnings", BigDecimal.ZERO,
                "transactionCount", 0
        );
    }

    public Map<String, Object> getBranchLoanStatistics(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        List<LoanApplication> allLoanApplications = loanApplicationRepository.findAll().stream()
                .filter(la -> la.getCustomer().getBranch().getId().equals(branchId))
                .collect(Collectors.toList());

        long pendingLoans = allLoanApplications.stream()
                .filter(la -> la.getStatus() == RequestStatus.PENDING)
                .count();

        long approvedLoans = allLoanApplications.stream()
                .filter(la -> la.getStatus() == RequestStatus.APPROVED)
                .count();

        long rejectedLoans = allLoanApplications.stream()
                .filter(la -> la.getStatus() == RequestStatus.REJECTED)
                .count();

        BigDecimal totalLoanAmount = allLoanApplications.stream()
                .map(LoanApplication::getRequestedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "branchId", branchId,
                "totalLoanApplications", allLoanApplications.size(),
                "pendingLoans", pendingLoans,
                "approvedLoans", approvedLoans,
                "rejectedLoans", rejectedLoans,
                "totalLoanAmount", totalLoanAmount
        );
    }

    public List<Transaction> getTransactionListByDateRange(Long branchId, LocalDateTime startDate, LocalDateTime endDate) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        List<Account> branchAccounts = branch.getAccounts();

        return branchAccounts.stream()
                .flatMap(account -> transactionRepository.findByAccountAndDateRange(account, startDate, endDate).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Customer> getAllCustomersByBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        return customerRepository.findByBranch(branch);
    }

    public Map<String, Object> getCustomerDetailsWithAccounts(Long branchManagerId, Long customerId) {
        BranchManager branchManager = getBranchManager(branchManagerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        // VERIFY: Customer belongs to branch manager's branch
        if (!customer.getBranch().getId().equals(branchManager.getBranch().getId())) {
            throw new BusinessRuleException("Access denied: Customer does not belong to your branch");
        }

        List<Account> accounts = accountRepository.findByCustomer(customer);
        List<LoanApplication> loanApplications = loanApplicationRepository.findByCustomer(customer);

        return Map.of(
                "customer", customer,
                "accounts", accounts,
                "loanApplications", loanApplications,
                "totalAccounts", accounts.size(),
                "activeAccounts", accounts.stream().filter(acc -> acc.getStatus() == AccountStatus.ACTIVE).count()
        );
    }

    public List<Account> getAllAccountsByBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        return branch.getAccounts();
    }

    public BranchManager getBranchManagerById(Long branchManagerId) {
        return getBranchManager(branchManagerId);
    }
}