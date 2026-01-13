
package com.banksystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class ChequeBookRequestDTO {
    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Number of leaves is required")
    @Min(value = 10, message = "Minimum 10 leaves required")
    @Max(value = 100, message = "Maximum 100 leaves allowed")
    private Integer numberOfLeaves;
}