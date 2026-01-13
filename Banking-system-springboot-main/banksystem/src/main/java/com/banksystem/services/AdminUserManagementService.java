package com.banksystem.services;

import com.banksystem.entity.*;
import com.banksystem.enums.RolesType;
import com.banksystem.exception.BusinessRuleException;
import com.banksystem.exception.ResourceNotFoundException;
import com.banksystem.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminUserManagementService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final HeadBankRepository headBankRepository;
    private final CentralBankRepository centralBankRepository;
    private final TellerRepository tellerRepository;
    private final BranchManagerRepository branchManagerRepository;
    private final HeadBankAdminRepository headBankAdminRepository;
    private final CentralBankAdminRepository centralBankAdminRepository;

    // Constructor Injection
    public AdminUserManagementService(UserRepository userRepository, BranchRepository branchRepository, HeadBankRepository headBankRepository, CentralBankRepository centralBankRepository, TellerRepository tellerRepository, BranchManagerRepository branchManagerRepository, HeadBankAdminRepository headBankAdminRepository, CentralBankAdminRepository centralBankAdminRepository) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.headBankRepository = headBankRepository;
        this.centralBankRepository = centralBankRepository;
        this.tellerRepository = tellerRepository;
        this.branchManagerRepository = branchManagerRepository;
        this.headBankAdminRepository = headBankAdminRepository;
        this.centralBankAdminRepository = centralBankAdminRepository;
    }

    /**
     * PROMOTE USER TO TELLER (Done by Branch Manager)
     */
    @Transactional
    public void promoteToTeller(Long userId, Long branchId) {
        User user = getUser(userId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        // Create new Teller Entity based on User data
        Teller teller = new Teller();
        teller.setUser(user); // Link
        teller.setBranch(branch);
        teller.setFullName(resolveFullName(user));
        teller.setUsername(user.getUsername());
        teller.setEmail(user.getUsername() + "@bank.com"); // Or fetch from customer details if available
        teller.setPasswordHash(user.getPassword());
        teller.setIsActive(true);
        teller.setAccountId(100L); // DUMMY: Teller needs an internal account ID logic
        teller.setAccountNumber("TELLER-" + user.getId());

        tellerRepository.save(teller);

        // Update User Role - THIS IS KEY ("One account at a time")
        user.setRole(RolesType.TELLER);
        userRepository.save(user);
    }

    /**
     * PROMOTE USER TO BRANCH MANAGER (Done by Head Bank Admin)
     */
    @Transactional
    public void promoteToBranchManager(Long userId, Long branchId) {
        User user = getUser(userId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        // Check if branch already has manager
        if(branchManagerRepository.findByBranchId(branchId).isPresent()){
            throw new BusinessRuleException("Branch already has a manager");
        }

        BranchManager manager = new BranchManager();
        manager.setUser(user);
        manager.setBranch(branch);
        manager.setFullName(resolveFullName(user));
        manager.setUsername(user.getUsername());
        manager.setEmail(user.getUsername() + "@manager.com");
        manager.setPasswordHash(user.getPassword());
        manager.setIsActive(true);

        branchManagerRepository.save(manager);

        // Switch Role
        user.setRole(RolesType.BRANCHMANAGER);
        userRepository.save(user);
    }

    /**
     * PROMOTE USER TO HEAD BANK ADMIN (Done by Central Bank Admin)
     */
    @Transactional
    public void promoteToHeadBankAdmin(Long userId, Long headBankId) {
        User user = getUser(userId);
        HeadBank headBank = headBankRepository.findById(headBankId)
                .orElseThrow(() -> new ResourceNotFoundException("HeadBank", "id", headBankId));

        HeadBankAdmin admin = new HeadBankAdmin();
        admin.setUser(user);
        admin.setHeadBank(headBank);
        admin.setFullName(resolveFullName(user));
        admin.setUsername(user.getUsername());
        admin.setEmail(user.getUsername() + "@headbank.com");
        admin.setPasswordHash(user.getPassword());
        admin.setIsActive(true);

        headBankAdminRepository.save(admin);

        // Switch Role
        user.setRole(RolesType.HEADMANAGER);
        userRepository.save(user);
    }

    // Helper to find user
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    // Helper to get name from linked customer entity if exists, or generic
    private String resolveFullName(User user) {
        if(user.getCustomer() != null) {
            return user.getCustomer().getFirstName() + " " + user.getCustomer().getLastName();
        }
        return user.getUsername();
    }
}