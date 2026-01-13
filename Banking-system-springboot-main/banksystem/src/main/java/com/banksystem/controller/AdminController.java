package com.banksystem.controller;

import com.banksystem.dto.ApiResponse;
import com.banksystem.security.JwtHelperService;
import com.banksystem.services.AdminUserManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    private final AdminUserManagementService adminService;
    private final JwtHelperService jwtHelper;

    public AdminController(AdminUserManagementService adminService, JwtHelperService jwtHelper) {
        this.adminService = adminService;
        this.jwtHelper = jwtHelper;
    }

    // ==================== PROMOTIONS ====================

    /**
     * Branch Manager can promote to Teller
     */
    @PostMapping("/promote/teller")
    @PreAuthorize("hasAuthority('BRANCHMANAGER')")
    public ResponseEntity<?> makeTeller(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Long userId,
            @RequestParam Long branchId) {

        String token = jwtHelper.extractToken(authHeader);
        Long managerId = jwtHelper.getBranchManagerIdFromToken(token);

        // Verify manager is promoting to their own branch
        jwtHelper.verifyManagerBranch(token, branchId);

        log.info("Branch Manager {} promoting user {} to Teller in branch {}", managerId, userId, branchId);
        adminService.promoteToTeller(userId, branchId);

        return ResponseEntity.ok(ApiResponse.success("User promoted to Teller", null));
    }

    /**
     * Head Admin can promote to Branch Manager
     */
    @PostMapping("/promote/branch-manager")
    @PreAuthorize("hasAuthority('HEADMANAGER')")
    public ResponseEntity<?> makeBranchManager(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Long userId,
            @RequestParam Long branchId) {

        String token = jwtHelper.extractToken(authHeader);
        Long adminId = jwtHelper.getHeadBankAdminIdFromToken(token);

        log.info("Head Admin {} promoting user {} to Branch Manager in branch {}", adminId, userId, branchId);
        adminService.promoteToBranchManager(userId, branchId);

        return ResponseEntity.ok(ApiResponse.success("User promoted to Branch Manager", null));
    }

    /**
     * Central Admin can promote to Head Admin
     */
    @PostMapping("/promote/head-admin")
    @PreAuthorize("hasAuthority('CENTRALADMIN')")
    public ResponseEntity<?> makeHeadAdmin(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Long userId,
            @RequestParam Long headBankId) {

        String token = jwtHelper.extractToken(authHeader);
        Long centralAdminId = jwtHelper.getSpecificIdFromJwtToken(token);

        log.info("Central Admin {} promoting user {} to Head Bank Admin in head bank {}",
                centralAdminId, userId, headBankId);
        adminService.promoteToHeadBankAdmin(userId, headBankId);

        return ResponseEntity.ok(ApiResponse.success("User promoted to Head Bank Admin", null));
    }
}