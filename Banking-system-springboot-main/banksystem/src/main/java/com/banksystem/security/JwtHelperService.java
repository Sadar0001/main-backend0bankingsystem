package com.banksystem.security;

import com.banksystem.entity.*;
import com.banksystem.exception.BusinessRuleException;
import com.banksystem.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtHelperService {

    private final JwtUtils jwtUtils;
    private final TellerRepository tellerRepository;
    private final BranchManagerRepository branchManagerRepository;
    private final CustomerRepository customerRepository;
    private final HeadBankAdminRepository headBankAdminRepository;
    private final CentralBankAdminRepository centralBankAdminRepository;

    public JwtHelperService(JwtUtils jwtUtils,
                            TellerRepository tellerRepository,
                            BranchManagerRepository branchManagerRepository,
                            CustomerRepository customerRepository,
                            HeadBankAdminRepository headBankAdminRepository,
                            CentralBankAdminRepository centralBankAdminRepository) {
        this.jwtUtils = jwtUtils;
        this.tellerRepository = tellerRepository;
        this.branchManagerRepository = branchManagerRepository;
        this.customerRepository = customerRepository;
        this.headBankAdminRepository = headBankAdminRepository;
        this.centralBankAdminRepository = centralBankAdminRepository;
    }

    /**
     * Extract JWT token from Authorization header
     */
    public String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new BusinessRuleException("Invalid authorization header");
    }

    /**
     * ✅ ADDED THIS MISSING METHOD
     * Get Specific ID directly from JWT (Used in ChargesBookController, AdminController etc.)
     */
    public Long getSpecificIdFromJwtToken(String token) {
        return jwtUtils.getSpecificIdFromJwtToken(token);
    }

    /**
     * Get Teller ID from JWT + Cache result
     */
    @Cacheable(value = "tellerCache", key = "#token")
    public Long getTellerIdFromToken(String token) {
        Long tellerId = jwtUtils.getSpecificIdFromJwtToken(token);
        log.debug("Extracted Teller ID from token: {}", tellerId);
        return tellerId;
    }

    /**
     * Get Teller with Branch ID (Cached)
     */
    @Cacheable(value = "tellerDetailsCache", key = "#token")
    public TellerDetails getTellerDetails(String token) {
        Long tellerId = getTellerIdFromToken(token);
        Teller teller = tellerRepository.findById(tellerId)
                .orElseThrow(() -> new BusinessRuleException("Teller not found"));

        return new TellerDetails(tellerId, teller.getBranch().getId());
    }

    /**
     * Get Branch Manager ID from JWT
     */
    @Cacheable(value = "managerCache", key = "#token")
    public Long getBranchManagerIdFromToken(String token) {
        return jwtUtils.getSpecificIdFromJwtToken(token);
    }

    /**
     * Get Branch Manager with Branch ID (Cached)
     */
    @Cacheable(value = "managerDetailsCache", key = "#token")
    public ManagerDetails getManagerDetails(String token) {
        Long managerId = getBranchManagerIdFromToken(token);
        BranchManager manager = branchManagerRepository.findById(managerId)
                .orElseThrow(() -> new BusinessRuleException("Branch Manager not found"));

        return new ManagerDetails(managerId, manager.getBranch().getId());
    }

    /**
     * Get Customer ID from JWT
     */
    @Cacheable(value = "customerCache", key = "#token")
    public Long getCustomerIdFromToken(String token) {
        return jwtUtils.getSpecificIdFromJwtToken(token);
    }

    /**
     * Get Head Bank Admin ID from JWT
     */
    @Cacheable(value = "headAdminCache", key = "#token")
    public Long getHeadBankAdminIdFromToken(String token) {
        return jwtUtils.getSpecificIdFromJwtToken(token);
    }

    /**
     * Get Head Bank Admin with Head Bank ID (Cached)
     */
    @Cacheable(value = "headAdminDetailsCache", key = "#token")
    public HeadAdminDetails getHeadAdminDetails(String token) {
        Long adminId = getHeadBankAdminIdFromToken(token);
        HeadBankAdmin admin = headBankAdminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessRuleException("Head Bank Admin not found"));

        return new HeadAdminDetails(adminId, admin.getHeadBank().getId());
    }

    /**
     * Verify customer owns the resource
     */
    public void verifyCustomerOwnership(String token, Long resourceCustomerId) {
        Long tokenCustomerId = getCustomerIdFromToken(token);
        if (!tokenCustomerId.equals(resourceCustomerId)) {
            throw new BusinessRuleException("Access denied: Resource does not belong to you");
        }
    }

    /**
     * Verify teller belongs to the branch
     */
    public void verifyTellerBranch(String token, Long branchId) {
        TellerDetails details = getTellerDetails(token);
        if (!details.getBranchId().equals(branchId)) {
            throw new BusinessRuleException("Access denied: Request belongs to different branch");
        }
    }

    /**
     * Verify manager belongs to the branch
     */
    public void verifyManagerBranch(String token, Long branchId) {
        ManagerDetails details = getManagerDetails(token);
        if (!details.getBranchId().equals(branchId)) {
            throw new BusinessRuleException("Access denied: Request belongs to different branch");
        }
    }

    // Inner classes for cached data
    public static class TellerDetails {
        private final Long tellerId;
        private final Long branchId;

        public TellerDetails(Long tellerId, Long branchId) {
            this.tellerId = tellerId;
            this.branchId = branchId;
        }

        public Long getTellerId() { return tellerId; }
        public Long getBranchId() { return branchId; }
    }

    public static class ManagerDetails {
        private final Long managerId;
        private final Long branchId;

        public ManagerDetails(Long managerId, Long branchId) {
            this.managerId = managerId;
            this.branchId = branchId;
        }

        public Long getManagerId() { return managerId; }
        public Long getBranchId() { return branchId; }
    }

    public static class HeadAdminDetails {
        private final Long adminId;
        private final Long headBankId;

        public HeadAdminDetails(Long adminId, Long headBankId) {
            this.adminId = adminId;
            this.headBankId = headBankId;
        }

        public Long getAdminId() { return adminId; }
        public Long getHeadBankId() { return headBankId; }
    }
}