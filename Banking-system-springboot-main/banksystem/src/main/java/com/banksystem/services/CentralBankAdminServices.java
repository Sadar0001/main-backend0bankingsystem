package com.banksystem.services;

import com.banksystem.dto.HeadBankDTO;
import com.banksystem.entity.CentralBank;
import com.banksystem.entity.CentralBankAdmin;
import com.banksystem.entity.HeadBank;
import com.banksystem.exception.BusinessRuleException;
import com.banksystem.repository.*;
import com.banksystem.util.SecurityUtils; // Import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CentralBankAdminServices {

    private final HeadBankRepository headBankRepository;
    private final CentralBankRepository centralBankRepository;
    private final BranchRepository branchRepository;
    private final CentralBankAdminRepository centralBankAdminRepository;

    public CentralBankAdminServices(HeadBankRepository headBankRepository, CentralBankRepository centralBankRepository,
                                    BranchRepository branchRepository, CentralBankAdminRepository centralBankAdminRepository) {
        this.headBankRepository = headBankRepository;
        this.centralBankRepository = centralBankRepository;
        this.branchRepository = branchRepository;
        this.centralBankAdminRepository = centralBankAdminRepository;
    }

    // --- SECURITY HELPER ---
    private CentralBankAdmin getAuthenticatedAdmin() {
        String username = SecurityUtils.getCurrentUsername();
        return centralBankAdminRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessRuleException("Authenticated user is not a Central Bank Admin"));
    }

    @Transactional
    public HeadBank addHeadBank(HeadBankDTO headBankDTO){
        CentralBankAdmin admin = getAuthenticatedAdmin();
        CentralBank centralBank = admin.getCentralBank();

        if(headBankRepository.findByName(headBankDTO.getName()).isPresent()){
            throw new BusinessRuleException("Head Bank already exists");
        }

        HeadBank newHeadBank = new HeadBank();
        newHeadBank.setName(headBankDTO.getName());
        newHeadBank.setCode(headBankDTO.getCode());
        newHeadBank.setRoutingNumber(headBankDTO.getRoutingNumber());
        newHeadBank.setAddress(headBankDTO.getAddress());
        newHeadBank.setContactPhone(headBankDTO.getContactNumber());
        newHeadBank.setContactEmail(headBankDTO.getEmail());
        newHeadBank.setIsActive(true);
        newHeadBank.setCreatedAt(LocalDateTime.now());

        newHeadBank.setCentralBank(centralBank);

        return headBankRepository.save(newHeadBank);
    }

    @Transactional
    public void deActivateHeadBank(Long bankId){
        HeadBank headBank = headBankRepository.findById(bankId)
                .orElseThrow(() -> new BusinessRuleException("HeadBank not found"));

        // Ensure Admin owns this head bank (via Central Bank)
        CentralBankAdmin admin = getAuthenticatedAdmin();
        if (!headBank.getCentralBank().getId().equals(admin.getCentralBank().getId())) {
            throw new BusinessRuleException("Unauthorized");
        }

        if(!headBank.getIsActive()){
            throw new BusinessRuleException("Head Bank is already inactive");
        }

        if(!branchRepository.findByHeadBank(headBank).isEmpty()){
            throw new BusinessRuleException("Deactivate branches first");
        }

        headBank.setIsActive(false);
        headBankRepository.save(headBank);
    }

    public List<HeadBank> getAllBanks() {
        CentralBankAdmin admin = getAuthenticatedAdmin();
        // Return only head banks belonging to this admin's central bank
        return admin.getCentralBank().getHeadBanks();
    }
}