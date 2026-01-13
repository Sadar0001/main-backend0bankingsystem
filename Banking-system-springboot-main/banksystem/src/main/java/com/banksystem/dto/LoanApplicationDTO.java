
package com.banksystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanApplicationDTO {
    @NotNull(message = "Loan offer ID is required")
    private Long loanOfferId;

    @NotNull(message = "Requested amount is required")
    @Positive(message = "Requested amount must be positive")
    private BigDecimal requestedAmount;

    @NotNull(message = "Requested tenure is required")
    @Positive(message = "Requested tenure must be positive")
    private Integer requestedTenure;

    private String purpose;
}