package com.banksystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CardRequestDTO {
    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotBlank(message = "Card type is required")
    private String cardType;
}