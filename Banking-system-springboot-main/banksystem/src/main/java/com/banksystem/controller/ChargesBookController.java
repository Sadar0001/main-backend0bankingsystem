package com.banksystem.controller;

import com.banksystem.dto.ApiResponse;
import com.banksystem.entity.ChargesBook;
import com.banksystem.enums.BankType;
import com.banksystem.security.JwtHelperService;
import com.banksystem.services.ChargesBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/chargesBook")
@Slf4j
public class ChargesBookController {

    private final ChargesBookService chargesBookService;
    private final JwtHelperService jwtHelper;

    public ChargesBookController(ChargesBookService chargesBookService, JwtHelperService jwtHelper) {
        this.chargesBookService = chargesBookService;
        this.jwtHelper = jwtHelper;
    }

    // ==================== ADD CHARGES ====================

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ChargesBook>> addCharges(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChargesBook chargesBook) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getSpecificIdFromJwtToken(token);

        log.info("Admin {} adding charge: {}", adminId, chargesBook.getFeeName());
        ChargesBook chargesBook1 = chargesBookService.addCharge(chargesBook);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Charge added successfully", chargesBook1));
    }

    // ==================== GET CHARGES ====================

    @GetMapping("/bank/{bankId}/type/{bankType}")
    public ResponseEntity<ApiResponse<List<ChargesBook>>> getChargesByBankId(
            @PathVariable Long bankId,
            @PathVariable BankType bankType) {

        log.info("Fetching charges for bank ID: {}, type: {}", bankId, bankType);
        List<ChargesBook> charges = chargesBookService.getChargesByBankId(bankId, bankType);

        return ResponseEntity.ok(ApiResponse.success("Charges retrieved successfully", charges));
    }

    @GetMapping("/bank/{bankId}/type/{bankType}/all")
    public ResponseEntity<ApiResponse<List<ChargesBook>>> getAllChargesByBankId(
            @PathVariable Long bankId,
            @PathVariable BankType bankType) {

        log.info("Fetching all charges (including inactive) for bank ID: {}, type: {}", bankId, bankType);
        List<ChargesBook> charges = chargesBookService.getAllChargesByBankId(bankId, bankType);

        return ResponseEntity.ok(ApiResponse.success("All charges retrieved successfully", charges));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChargesBook>> getChargeById(@PathVariable Long id) {
        log.info("Fetching charge by ID: {}", id);
        ChargesBook charge = chargesBookService.getChargeById(id);

        return ResponseEntity.ok(ApiResponse.success("Charge retrieved successfully", charge));
    }

    // ==================== UPDATE CHARGES ====================

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ChargesBook>> updateCharges(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody ChargesBook chargesBook) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getSpecificIdFromJwtToken(token);

        log.info("Admin {} updating charge ID: {}", adminId, id);
        ChargesBook updatedCharges = chargesBookService.updateCharge(id, chargesBook);

        return ResponseEntity.ok(ApiResponse.success("Charge updated successfully", updatedCharges));
    }

    // ==================== DELETE CHARGES ====================

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCharges(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getSpecificIdFromJwtToken(token);

        log.info("Admin {} deleting charge ID: {}", adminId, id);
        chargesBookService.deleteCharge(id);

        return ResponseEntity.ok(ApiResponse.success("Charge deleted successfully", null));
    }
}