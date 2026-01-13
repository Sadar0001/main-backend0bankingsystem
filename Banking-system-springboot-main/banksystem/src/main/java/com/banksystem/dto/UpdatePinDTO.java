
package com.banksystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePinDTO {
    @NotBlank(message = "Old PIN is required")
    private String oldPin;

    @NotBlank(message = "New PIN is required")
    private String newPin;
}