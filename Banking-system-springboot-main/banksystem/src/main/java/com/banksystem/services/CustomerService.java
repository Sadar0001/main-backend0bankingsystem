package com.banksystem.services;

import com.banksystem.dto.*;
import com.banksystem.entity.*;
import com.banksystem.enums.AccountStatus;
import com.banksystem.enums.AccountType;
import com.banksystem.enums.RequestStatus;
import com.banksystem.exception.BusinessRuleException;
import com.banksystem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AccountRequestRepository accountRequestRepository;
    private final BranchRepository branchRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanOffersRepository loanOffersRepository;
    private final CardRequestRepository cardRequestRepository;
    private final ChequeBookRequestRepository chequeBookRequestRepository;
    private final DebitCardRepository debitCardRepository;
    private final TransactionRepository transactionRepository;

    public CustomerService(CustomerRepository customerRepository,
                           AccountRepository accountRepository,
                           BranchRepository branchRepository,
                           LoanApplicationRepository loanApplicationRepository,
                           LoanOffersRepository loanOffersRepository,
                           CardRequestRepository cardRequestRepository,
                           ChequeBookRequestRepository chequeBookRequestRepository,
                           DebitCardRepository debitCardRepository,
                           TransactionRepository transactionRepository,
                           AccountRequestRepository accountRequestRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanOffersRepository = loanOffersRepository;
        this.cardRequestRepository = cardRequestRepository;
        this.chequeBookRequestRepository = chequeBookRequestRepository;
        this.debitCardRepository = debitCardRepository;
        this.transactionRepository = transactionRepository;
        this.accountRequestRepository = accountRequestRepository;
    }

    // ==================== HELPER METHOD ====================

    private Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessRuleException("Customer not found with id: " + customerId));
    }

    // ==================== ACCOUNT REQUESTS ====================

    @Transactional
    public AccountRequest createAccountRequest(Long customerId, AccountRequestDTO requestDTO) {
        Customer customer = getCustomer(customerId);

        // VERIFY: DTO customerId matches JWT customerId
        if (!requestDTO.getCustomerId().equals(customerId)) {
            throw new BusinessRuleException("Access denied: Cannot create request for another customer");
        }

        // Check if customer already has an account of this type in same branch
        if (accountRepository.existsByCustomerAndAccountTypeAndBranchId(
                requestDTO.getCustomerId(),
                requestDTO.getAccountType(),
                requestDTO.getBranchId())) {
            throw new BusinessRuleException("Customer already has a " + requestDTO.getAccountType() +
                    " account in same branch " + requestDTO.getBranchId());
        }

        // Check if request pending
        if (accountRequestRepository.existsByCustomer_IdAndAccountTypeAndBranchIdAndStatus(
                customerId,
                requestDTO.getAccountType(),
                requestDTO.getBranchId(),
                RequestStatus.PENDING)) {
            throw new BusinessRuleException("You already have a pending request for " +
                    requestDTO.getAccountType() + " account in this branch");
        }

        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setCustomer(customer);
        accountRequest.setAccountType(requestDTO.getAccountType());
        accountRequest.setBranchId(requestDTO.getBranchId());
        accountRequest.setHeadBankId(requestDTO.getHeadBankId());
        accountRequest.setStatus(RequestStatus.PENDING);
        accountRequest.setCreatedAt(LocalDateTime.now());

        return accountRequestRepository.save(accountRequest);
    }

    public List<AccountRequest> getMyAccountRequests(Long customerId) {
        Customer customer = getCustomer(customerId);
        return accountRequestRepository.findByCustomer(customer);
    }

    // ==================== LOAN APPLICATIONS ====================

    @Transactional
    public LoanApplication applyForLoan(Long customerId, LoanApplicationDTO loanDTO) {
        Customer customer = getCustomer(customerId);

        LoanOffers loanOffer = loanOffersRepository.findById(loanDTO.getLoanOfferId())
                .orElseThrow(() -> new BusinessRuleException("Loan offer not found"));

        // Validate loan amount
        if (loanDTO.getRequestedAmount().compareTo(loanOffer.getMinAmount()) < 0 ||
                loanDTO.getRequestedAmount().compareTo(loanOffer.getMaxAmount()) > 0) {
            throw new BusinessRuleException("Loan amount must be between " +
                    loanOffer.getMinAmount() + " and " + loanOffer.getMaxAmount());
        }

        // Validate tenure
        if (loanDTO.getRequestedTenure() < loanOffer.getMinTenureMonths() ||
                loanDTO.getRequestedTenure() > loanOffer.getMaxTenureMonths()) {
            throw new BusinessRuleException("Loan tenure must be between " +
                    loanOffer.getMinTenureMonths() + " and " + loanOffer.getMaxTenureMonths() + " months");
        }

        LoanApplication application = new LoanApplication();
        application.setCustomer(customer);
        application.setLoanOffer(loanOffer);
        application.setRequestedAmount(loanDTO.getRequestedAmount());
        application.setRequestedTenureMonths(loanDTO.getRequestedTenure());
        application.setPurpose(loanDTO.getPurpose());
        application.setStatus(RequestStatus.PENDING);
        application.setCreatedAt(LocalDateTime.now());

        return loanApplicationRepository.save(application);
    }

    public List<LoanApplication> getMyLoanApplications(Long customerId) {
        Customer customer = getCustomer(customerId);
        return loanApplicationRepository.findByCustomer(customer);
    }

    public List<LoanOffers> getAvailableLoanOffers(Long customerId) {
        Customer customer = getCustomer(customerId);
        Long headBankId = customer.getBranch().getHeadBank().getId();
        return loanOffersRepository.findByHeadBankIdAndIsActiveTrue(headBankId);
    }

    // ==================== DEBIT CARD REQUESTS ====================

    @Transactional
    public CardRequest requestDebitCard(Long customerId, CardRequestDTO cardDTO) {
        Customer customer = getCustomer(customerId);

        Account account = accountRepository.findById(cardDTO.getAccountId())
                .orElseThrow(() -> new BusinessRuleException("Account not found"));

        // VERIFY: Account belongs to customer
        if (!account.getCustomer().getId().equals(customerId)) {
            throw new BusinessRuleException("Access denied: Account does not belong to this customer");
        }

        // Check if account already has an active card
        if (debitCardRepository.existsByAccountAndIsActiveTrue(account)) {
            throw new BusinessRuleException("Account already has an active debit card");
        }

        // Check for pending card requests
        if (cardRequestRepository.existsByAccountAndStatus(account, RequestStatus.PENDING)) {
            throw new BusinessRuleException("A card request is already pending for this account");
        }

        CardRequest cardRequest = new CardRequest();
        cardRequest.setRequestedBy(customer);
        cardRequest.setAccount(account);
        cardRequest.setCardType(cardDTO.getCardType());
        cardRequest.setStatus(RequestStatus.PENDING);
        cardRequest.setCreatedAt(LocalDateTime.now());

        return cardRequestRepository.save(cardRequest);
    }

    public List<CardRequest> getMyCardRequests(Long customerId) {
        Customer customer = getCustomer(customerId);
        return cardRequestRepository.findByRequestedBy(customer);
    }

    // ==================== CHEQUE BOOK REQUESTS ====================

    @Transactional
    public ChequeBookRequest requestChequeBook(Long customerId, ChequeBookRequestDTO chequeDTO) {
        Customer customer = getCustomer(customerId);

        Account account = accountRepository.findById(chequeDTO.getAccountId())
                .orElseThrow(() -> new BusinessRuleException("Account not found"));

        // VERIFY: Account belongs to customer
        if (!account.getCustomer().getId().equals(customerId)) {
            throw new BusinessRuleException("Access denied: Account does not belong to this customer");
        }

        // Only savings and current accounts can have cheque books
        if (account.getAccountType() == AccountType.LOAN) {
            throw new BusinessRuleException("Loan accounts cannot have cheque books");
        }

        ChequeBookRequest chequeRequest = new ChequeBookRequest();
        chequeRequest.setRequestedBy(customer);
        chequeRequest.setAccount(account);
        chequeRequest.setNumberOfLeaves(chequeDTO.getNumberOfLeaves());
        chequeRequest.setStatus(RequestStatus.PENDING);
        chequeRequest.setCreatedAt(LocalDateTime.now());

        return chequeBookRequestRepository.save(chequeRequest);
    }

    public List<ChequeBookRequest> getMyChequeBookRequests(Long customerId) {
        Customer customer = getCustomer(customerId);
        return chequeBookRequestRepository.findByRequestedBy(customer);
    }

    // ==================== CARD MANAGEMENT ====================

    @Transactional
    public void blockCard(Long customerId, Long cardId) {
        DebitCard card = debitCardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessRuleException("Card not found"));

        // VERIFY: Card belongs to customer
        if (!card.getAccount().getCustomer().getId().equals(customerId)) {
            throw new BusinessRuleException("Access denied: Card does not belong to this customer");
        }

        card.setIsBlocked(true);
        card.setBlockedAt(LocalDateTime.now());
        debitCardRepository.save(card);
    }

    @Transactional
    public void unblockCard(Long customerId, Long cardId, String transactionPin) {
        DebitCard card = debitCardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessRuleException("Card not found"));

        // VERIFY: Card belongs to customer
        if (!card.getAccount().getCustomer().getId().equals(customerId)) {
            throw new BusinessRuleException("Access denied: Card does not belong to this customer");
        }

        // TODO: Add PIN verification logic here

        card.setIsBlocked(false);
        card.setBlockedAt(null);
        debitCardRepository.save(card);
    }

    public List<DebitCard> getMyCards(Long customerId) {
        Customer customer = getCustomer(customerId);
        return debitCardRepository.findByAccountCustomer(customer);
    }

    // ==================== ACCOUNT DETAILS ====================

    public List<Account> getMyAccounts(Long customerId) {
        Customer customer = getCustomer(customerId);
        return accountRepository.findByCustomer(customer);
    }

    public Account getAccountDetails(Long customerId, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessRuleException("Account not found"));

        // VERIFY: Account belongs to customer
        if (!account.getCustomer().getId().equals(customerId)) {
            throw new BusinessRuleException("Access denied: Account does not belong to this customer");
        }

        return account;
    }

    public List<Transaction> getTransactionHistory(Long customerId, Long accountId,
                                                   LocalDate startDate, LocalDate endDate) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessRuleException("Account not found"));

        // VERIFY: Account belongs to customer
        if (!account.getCustomer().getId().equals(customerId)) {
            throw new BusinessRuleException("Access denied: Account does not belong to this customer");
        }

        return transactionRepository.findByAccountAndDateRange(account,
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }

    // ==================== TRANSACTION PIN ====================

    @Transactional
    public void updateTransactionPin(Long customerId, String oldPin, String newPin) {
        Customer customer = getCustomer(customerId);

        // TODO: Add proper PIN verification with BCrypt
        // if (!passwordEncoder.matches(oldPin, customer.getTransactionPinHash())) {
        //     throw new BusinessRuleException("Invalid old PIN");
        // }

        // Validate new PIN (should be 4-6 digits)
        if (!newPin.matches("\\d{4,6}")) {
            throw new BusinessRuleException("PIN must be 4-6 digits");
        }

        customer.setTransactionPinHash(newPin);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }
}