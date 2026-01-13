package com.banksystem.services;

import com.banksystem.entity.ChargesBook;
import com.banksystem.enums.BankType;
import com.banksystem.exception.BusinessRuleException;
import com.banksystem.repository.ChargesBookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class ChargesBookService {


    private final ChargesBookRepository chargesBookRepository;

    public ChargesBookService(ChargesBookRepository chargesBookRepository) {
        this.chargesBookRepository = chargesBookRepository;
    }

    @Transactional
    public ChargesBook addCharge(ChargesBook chargesBook) {
        ChargesBook chargesBook1=new ChargesBook();

        // Check per bank and type
        if(chargesBookRepository.existsByBankIdAndBankTypeAndFeeNameAndIsActiveTrue(
                chargesBook.getBankId(),
                chargesBook.getBankType(),
                chargesBook.getFeeName())) {
            throw new BusinessRuleException("ChargesBook with fee name '" + chargesBook.getFeeName() +
                    "' already exists for this bank and type");
        }

        chargesBook1.setBankId(chargesBook.getBankId());
        chargesBook1.setBankType(chargesBook.getBankType());
        chargesBook1.setTransactionType(chargesBook.getTransactionType());
        chargesBook1.setMinValue(chargesBook.getMinValue());
        chargesBook1.setMaxValue(chargesBook.getMaxValue());
        chargesBook1.setFeeAmount(chargesBook.getFeeAmount());
        chargesBook1.setFeeName(chargesBook.getFeeName());
        chargesBookRepository.save(chargesBook1);
        return chargesBook1;
    }


    // Get all active charges by bank ID
    public List<ChargesBook> getChargesByBankId(Long bankId, BankType bankType) {
        return chargesBookRepository.findByBankIdAndBankTypeAndIsActiveTrue(bankId,bankType);
    }

    // Get all charges (active + inactive) by bank ID
    public List<ChargesBook> getAllChargesByBankId(Long bankId,BankType  bankType) {
        return chargesBookRepository.findByBankIdAndBankType(bankId,bankType);
    }

    // Update
    @Transactional
    public ChargesBook updateCharge(Long id, ChargesBook chargesBook) {
        ChargesBook existingCharge = chargesBookRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Charge not found with id: " + id));

        // Only check if fee name is being changed
        if (!existingCharge.getFeeName().equals(chargesBook.getFeeName())) {
            // Check for duplicates excluding current record
            if(chargesBookRepository.existsByBankIdAndBankTypeAndFeeNameAndIsActiveTrue(
                    chargesBook.getBankId(),
                    chargesBook.getBankType(),
                    chargesBook.getFeeName())) {
                throw new BusinessRuleException("ChargesBook with fee name '" + chargesBook.getFeeName() +
                        "' already exists for bank ID: " + chargesBook.getBankId() + " and type: " + chargesBook.getBankType());
            }
        }

        // Update fields
        existingCharge.setBankId(chargesBook.getBankId());
        existingCharge.setBankType(chargesBook.getBankType());
        existingCharge.setTransactionType(chargesBook.getTransactionType());
        existingCharge.setMinValue(chargesBook.getMinValue());
        existingCharge.setMaxValue(chargesBook.getMaxValue());
        existingCharge.setFeeAmount(chargesBook.getFeeAmount());
        existingCharge.setFeeName(chargesBook.getFeeName());

        return chargesBookRepository.save(existingCharge);
    }

    // Soft Delete
    @Transactional
    public void deleteCharge(Long id) {
        ChargesBook existingCharge = chargesBookRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Charge not found with id: " + id));

        existingCharge.setActive(false);
        chargesBookRepository.save(existingCharge);
    }

    // Get by ID
    public ChargesBook getChargeById(Long id) {
        return chargesBookRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Charge not found with id: " + id));
    }



}
