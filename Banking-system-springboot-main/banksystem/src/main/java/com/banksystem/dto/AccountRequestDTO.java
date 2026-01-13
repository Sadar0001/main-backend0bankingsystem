package com.banksystem.dto;

import com.banksystem.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountRequestDTO {
    @NotNull(message = "Customer id is required")
    private Long customerId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message="HeadBank id is required")
    private Long headBankId;

    @NotNull(message="Branch Id is required")
    private Long branchId;
}
