
package com.banksystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectionDTO {
    @NotBlank(message = "Rejection reason is required")
    private String reason;
}