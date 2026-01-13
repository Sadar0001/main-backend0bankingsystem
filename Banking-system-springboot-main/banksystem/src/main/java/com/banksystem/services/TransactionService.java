package com.banksystem.services;

import com.banksystem.dto.TransactionDto;
import com.banksystem.entity.*;
import com.banksystem.enums.AccountStatus;
import com.banksystem.enums.BankType;
import com.banksystem.enums.TransactionStatus;
import com.banksystem.exception.BusinessRuleException;
import com.banksystem.repository.*;
import jakarta.persistence.LockTimeoutException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TransactionService {

    private final ChargesService chargesService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ChargesRepository chargesRepository;
    private final BranchRepository branchRepository;
    private final HeadBankRepository headBankRepository;
    private final CentralBankRepository centralBankRepository;

    public TransactionService(ChargesService chargesService,
                              AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              ChargesRepository chargesRepository,
                              BranchRepository branchRepository,
                              HeadBankRepository headBankRepository,
                              CentralBankRepository centralBankRepository) {
        this.chargesService = chargesService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.chargesRepository = chargesRepository;
        this.branchRepository = branchRepository;
        this.headBankRepository = headBankRepository;
        this.centralBankRepository = centralBankRepository;
    }

    @Retryable(
            retryFor = {
                    PessimisticLockingFailureException.class,
                    LockTimeoutException.class,
                    CannotAcquireLockException.class
            },
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    @Transactional
    public Transaction makeTransaction(TransactionDto transactionDto) {

        // Prevent self-transfers
        if (transactionDto.getSenderAccountNumber().equals(transactionDto.getReceiverAccountNumber())) {
            throw new BusinessRuleException("Cannot transfer to the same account");
        }

        // Deadlock prevention - lock accounts in alphabetical order
        String acc1 = transactionDto.getSenderAccountNumber();
        String acc2 = transactionDto.getReceiverAccountNumber();

        // Determine lock order (alphabetical)
        boolean firstIsSender = acc1.compareTo(acc2) < 0;
        if (!firstIsSender) {
            String temp = acc1;
            acc1 = acc2;
            acc2 = temp;
        }

        // Lock accounts in consistent order to prevent deadlock
        Account firstAccount = accountRepository.findByAccountNumberWithLock(acc1);
        Account secondAccount = accountRepository.findByAccountNumberWithLock(acc2);

        // Validate accounts exist
        if (firstAccount == null) {
            throw new BusinessRuleException("Account not found: " + acc1);
        }
        if (secondAccount == null) {
            throw new BusinessRuleException("Account not found: " + acc2);
        }

        // Assign sender/receiver based on original order
        Account senderAccount = firstIsSender ? firstAccount : secondAccount;
        Account receiverAccount = firstIsSender ? secondAccount : firstAccount;

        log.info("Transaction initiated - Sender: {}, Receiver: {}",
                senderAccount.getAccountNumber(), receiverAccount.getAccountNumber());

        // Validate account statuses
        if (!senderAccount.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new BusinessRuleException("Sender account is not active");
        }
        if (!receiverAccount.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new BusinessRuleException("Receiver account is not active");
        }

        // Get branch hierarchy (already loaded via eager fetch or join)
        Branch branch = senderAccount.getBranch();
        HeadBank headBank = branch.getHeadBank();
        CentralBank centralBank = headBank.getCentralBank();

        log.info("Bank hierarchy - Branch: {}, HeadBank: {}, CentralBank: {}",
                branch.getId(), headBank.getId(), centralBank.getId());

        // Collect charges from all three levels
        List<Charges> allCharges = new ArrayList<>();
        allCharges.addAll(chargesService.getChargesList(
                createTransactionDto(transactionDto, branch.getId(), BankType.BANK_BRANCH)));
        allCharges.addAll(chargesService.getChargesList(
                createTransactionDto(transactionDto, headBank.getId(), BankType.HEAD_BANK)));
        allCharges.addAll(chargesService.getChargesList(
                createTransactionDto(transactionDto, centralBank.getId(), BankType.CENTRAL_BANK)));

        // Calculate total charges
        BigDecimal totalCharges = allCharges.stream()
                .map(charge -> BigDecimal.valueOf(charge.getChargedAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netAmount = transactionDto.getAmount().subtract(totalCharges);

        log.info("Transaction amounts - Total: {}, Charges: {}, Net: {}",
                transactionDto.getAmount(), totalCharges, netAmount);

        // Validate netAmount is positive
        if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException(
                    String.format("Transaction amount (%.2f) must be greater than total charges (%.2f)",
                            transactionDto.getAmount(), totalCharges));
        }

        // Check sufficient balance
        if (senderAccount.getAvailableBalance().compareTo(transactionDto.getAmount()) < 0) {
            throw new BusinessRuleException(
                    String.format("Insufficient balance. Required: %.2f, Available: %.2f",
                            transactionDto.getAmount(), senderAccount.getAvailableBalance()));
        }

        // Create and save transaction
        Transaction newTransaction = new Transaction();
        newTransaction.setFromAccount(senderAccount);
        newTransaction.setToAccount(receiverAccount);
        newTransaction.setAmount(transactionDto.getAmount());
        newTransaction.setTransactionType(transactionDto.getTransactionType());
        newTransaction.setDescription(transactionDto.getDescription());
        newTransaction.setTransactionReference(generateTransactionReference());
        newTransaction.setTotalCharges(totalCharges);
        newTransaction.setNetAmount(netAmount);
        newTransaction.setStatus(TransactionStatus.COMPLETED);

        Transaction savedTransaction = transactionRepository.save(newTransaction);
        log.info("Transaction saved: {}", savedTransaction.getTransactionReference());

        // Associate charges with transaction
        allCharges.forEach(charge -> charge.setTransaction(savedTransaction));
        chargesRepository.saveAll(allCharges);
        log.debug("Charges saved: {} records", allCharges.size());

        // Update sender balances (deduct full amount including charges)
        senderAccount.setCurrentBalance(
                senderAccount.getCurrentBalance().subtract(transactionDto.getAmount()));
        senderAccount.setAvailableBalance(
                senderAccount.getAvailableBalance().subtract(transactionDto.getAmount()));

        // Update receiver balances (credit net amount after charges)
        receiverAccount.setCurrentBalance(
                receiverAccount.getCurrentBalance().add(netAmount));
        receiverAccount.setAvailableBalance(
                receiverAccount.getAvailableBalance().add(netAmount));

        // Distribute charges to banks (using already-fetched entities)
        distributeChargesToBanks(allCharges, branch, headBank, centralBank);

        // Save all updated entities
        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);
        branchRepository.save(branch);
        headBankRepository.save(headBank);
        centralBankRepository.save(centralBank);

        log.info("Transaction completed successfully: {} | Sender new balance: {}, Receiver new balance: {}",
                savedTransaction.getTransactionReference(),
                senderAccount.getAvailableBalance(),
                receiverAccount.getAvailableBalance());

        return savedTransaction;
    }

    @Recover
    public Transaction transferRecover(Exception exception, TransactionDto transactionDto) {
        log.error("Transaction FAILED after all retries - Sender: {}, Receiver: {}, Amount: {}, Reason: {}",
                transactionDto.getSenderAccountNumber(),
                transactionDto.getReceiverAccountNumber(),
                transactionDto.getAmount(),
                exception.getMessage());
        throw new BusinessRuleException("Transaction failed due to system contention. Please try again later.");
    }

    /**
     * Helper method to create TransactionDto with bank-specific details
     */
    private TransactionDto createTransactionDto(TransactionDto original, Long bankId, BankType bankType) {
        TransactionDto dto = new TransactionDto();
        dto.setSenderAccountNumber(original.getSenderAccountNumber());
        dto.setReceiverAccountNumber(original.getReceiverAccountNumber());
        dto.setBankId(bankId);
        dto.setBankType(bankType);
        dto.setTransactionType(original.getTransactionType());
        dto.setAccountHolderType(original.getAccountHolderType());
        dto.setAmount(original.getAmount());
        dto.setDescription(original.getDescription());
        dto.setTransactionStatus(original.getTransactionStatus());
        return dto;
    }

    /**
     * Distributes transaction charges to Central Bank, Head Bank, and Branch.
     * Uses already-fetched entities to avoid N+1 queries.
     */
    private void distributeChargesToBanks(List<Charges> charges,
                                          Branch branch,
                                          HeadBank headBank,
                                          CentralBank centralBank) {
        log.debug("Distributing {} charges to banks", charges.size());

        for (Charges charge : charges) {
            BigDecimal chargeAmount = BigDecimal.valueOf(charge.getChargedAmount());

            switch (charge.getBankType()) {
                case CENTRAL_BANK:
                    if (charge.getBankId().equals(centralBank.getId())) {
                        BigDecimal currentEarning = centralBank.getTotalEarning() != null
                                ? centralBank.getTotalEarning() : BigDecimal.ZERO;
                        centralBank.setTotalEarning(currentEarning.add(chargeAmount));
                        log.debug("Central Bank earning updated: {}", centralBank.getTotalEarning());
                    } else {
                        log.warn("Charge bankId {} doesn't match centralBank id {}",
                                charge.getBankId(), centralBank.getId());
                    }
                    break;

                case HEAD_BANK:
                    if (charge.getBankId().equals(headBank.getId())) {
                        BigDecimal currentEarning = headBank.getTotalEarning() != null
                                ? headBank.getTotalEarning() : BigDecimal.ZERO;
                        headBank.setTotalEarning(currentEarning.add(chargeAmount));
                        log.debug("Head Bank earning updated: {}", headBank.getTotalEarning());
                    } else {
                        log.warn("Charge bankId {} doesn't match headBank id {}",
                                charge.getBankId(), headBank.getId());
                    }
                    break;

                case BANK_BRANCH:
                    if (charge.getBankId().equals(branch.getId())) {
                        BigDecimal currentEarning = branch.getTotalEarning() != null
                                ? branch.getTotalEarning() : BigDecimal.ZERO;
                        branch.setTotalEarning(currentEarning.add(chargeAmount));
                        log.debug("Branch earning updated: {}", branch.getTotalEarning());
                    } else {
                        log.warn("Charge bankId {} doesn't match branch id {}",
                                charge.getBankId(), branch.getId());
                    }
                    break;

                default:
                    throw new BusinessRuleException("Unknown bank type: " + charge.getBankType());
            }
        }

        log.debug("Charge distribution completed");
    }

    private String generateTransactionReference() {
        return "TXN" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}