package com.banksystem.dto;

import com.banksystem.enums.AccountHolderType;
import com.banksystem.enums.BankType;
import com.banksystem.enums.TransactionStatus;
import com.banksystem.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDto {

  private String senderAccountNumber;
  private String receiverAccountNumber;
  private BankType bankType;
  private Long bankId;
//  private Long headBankId;
//  private Long CentralBankId;
  private TransactionType transactionType;
  private AccountHolderType  accountHolderType;
  private BigDecimal amount;
  private String description="simple transaction";
  private TransactionStatus transactionStatus=TransactionStatus.PENDING;
}



