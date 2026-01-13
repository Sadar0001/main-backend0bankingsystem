package com.banksystem.services;

import com.banksystem.dto.DebitCardRulesDTO;
import com.banksystem.entity.DebitCardRules;
import com.banksystem.entity.HeadBank;
import com.banksystem.exception.BusinessRuleException;
import com.banksystem.exception.ResourceNotFoundException;
import com.banksystem.repository.DebitCardRulesRepository;
import com.banksystem.repository.HeadBankRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DebitCardRulesService {

    private final DebitCardRulesRepository debitCardRulesRepository;
    private final HeadBankRepository headBankRepository;

    public DebitCardRulesService(DebitCardRulesRepository debitCardRulesRepository,
                                 HeadBankRepository headBankRepository) {
        this.debitCardRulesRepository = debitCardRulesRepository;
        this.headBankRepository = headBankRepository;
    }

    /**
     * Add new debit card rules
     */
    @Transactional
    public DebitCardRules addDebitCardRules(DebitCardRulesDTO rulesDTO) {
        log.info("Adding new debit card rules for card type: {} and head bank ID: {}",
                rulesDTO.getCardType(), rulesDTO.getHeadBankId());

        HeadBank headBank = headBankRepository.findById(rulesDTO.getHeadBankId())
                .orElseThrow(() -> new ResourceNotFoundException("HeadBank", "id", rulesDTO.getHeadBankId()));

        // Check if card rules already exist for this card type in the head bank
        if (debitCardRulesRepository.findByHeadBankAndCardType(headBank, rulesDTO.getCardType()).isPresent()) {
            throw new BusinessRuleException("Debit card rules already exist for card type: " +
                    rulesDTO.getCardType() + " in this head bank");
        }

        DebitCardRules debitCardRules = new DebitCardRules();
        debitCardRules.setHeadBank(headBank);
        debitCardRules.setCardType(rulesDTO.getCardType());
        debitCardRules.setDailyWithdrawalLimit(rulesDTO.getDailyWithdrawalLimit());
        debitCardRules.setDailyTransactionLimit(rulesDTO.getDailyPurchaseLimit());
        debitCardRules.setAnnualFee(rulesDTO.getAnnualFee());
        debitCardRules.setInternationalUsage(false); // Default value
        debitCardRules.setIsActive(true);
        debitCardRules.setCreatedAt(LocalDateTime.now());

        DebitCardRules savedRules = debitCardRulesRepository.save(debitCardRules);
        log.info("Successfully added debit card rules with ID: {}", savedRules.getId());

        return savedRules;
    }

    /**
     * Update existing debit card rules
     */
    @Transactional
    public DebitCardRules updateDebitCardRules(Long rulesId, DebitCardRulesDTO rulesDTO) {
        log.info("Updating debit card rules with ID: {}", rulesId);

        DebitCardRules existingRules = debitCardRulesRepository.findById(rulesId)
                .orElseThrow(() -> new ResourceNotFoundException("DebitCardRules", "id", rulesId));

        // If card type is being changed, check for duplicates
        if (!existingRules.getCardType().equals(rulesDTO.getCardType())) {
            HeadBank headBank = headBankRepository.findById(rulesDTO.getHeadBankId())
                    .orElseThrow(() -> new ResourceNotFoundException("HeadBank", "id", rulesDTO.getHeadBankId()));

            if (debitCardRulesRepository.findByHeadBankAndCardType(headBank, rulesDTO.getCardType()).isPresent()) {
                throw new BusinessRuleException("Debit card rules already exist for card type: " +
                        rulesDTO.getCardType() + " in this head bank");
            }
        }

        // Update fields
        existingRules.setCardType(rulesDTO.getCardType());
        existingRules.setDailyWithdrawalLimit(rulesDTO.getDailyWithdrawalLimit());
        existingRules.setDailyTransactionLimit(rulesDTO.getDailyPurchaseLimit());
        existingRules.setAnnualFee(rulesDTO.getAnnualFee());
        existingRules.setInternationalUsage(existingRules.getInternationalUsage()); // Keep existing or add to DTO

        DebitCardRules updatedRules = debitCardRulesRepository.save(existingRules);
        log.info("Successfully updated debit card rules with ID: {}", rulesId);

        return updatedRules;
    }

    /**
     * Deactivate debit card rules (soft delete)
     */
    @Transactional
    public void deactivateDebitCardRules(Long rulesId) {
        log.info("Deactivating debit card rules with ID: {}", rulesId);

        DebitCardRules existingRules = debitCardRulesRepository.findById(rulesId)
                .orElseThrow(() -> new ResourceNotFoundException("DebitCardRules", "id", rulesId));

        if (!existingRules.getIsActive()) {
            throw new BusinessRuleException("Debit card rules are already deactivated");
        }

        existingRules.setIsActive(false);
        debitCardRulesRepository.save(existingRules);
        log.info("Successfully deactivated debit card rules with ID: {}", rulesId);
    }

    /**
     * Get all debit card rules for a head bank
     */
    public List<DebitCardRules> getAllDebitCardRulesByHeadBank(Long headBankId) {
        log.info("Fetching all debit card rules for head bank ID: {}", headBankId);

        HeadBank headBank = headBankRepository.findById(headBankId)
                .orElseThrow(() -> new ResourceNotFoundException("HeadBank", "id", headBankId));

        return headBank.getDebitCardRules().stream()
                .filter(DebitCardRules::getIsActive)
                .toList();
    }

    /**
     * Get debit card rules by ID
     */
    public DebitCardRules getDebitCardRulesById(Long rulesId) {
        return debitCardRulesRepository.findById(rulesId)
                .orElseThrow(() -> new ResourceNotFoundException("DebitCardRules", "id", rulesId));
    }
}