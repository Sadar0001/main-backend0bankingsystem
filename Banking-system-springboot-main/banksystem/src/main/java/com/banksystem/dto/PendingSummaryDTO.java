package com.banksystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingSummaryDTO {
    private int pendingAccountRequests;
    private int pendingCardRequests;
    private int pendingChequeBookRequests;
    private int totalPendingRequests;

    // Custom constructor that auto-calculates total
    public PendingSummaryDTO(int pendingAccountRequests, int pendingCardRequests, int pendingChequeBookRequests) {
        this.pendingAccountRequests = pendingAccountRequests;
        this.pendingCardRequests = pendingCardRequests;
        this.pendingChequeBookRequests = pendingChequeBookRequests;
        this.totalPendingRequests = pendingAccountRequests + pendingCardRequests + pendingChequeBookRequests;
    }
}