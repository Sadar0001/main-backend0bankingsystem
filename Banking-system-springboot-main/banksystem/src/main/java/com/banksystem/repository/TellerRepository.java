package com.banksystem.repository;

import com.banksystem.entity.Teller;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TellerRepository extends JpaRepository<Teller, Long> {
    List<Teller> findByBranchIdAndIsActive(Long branchId, boolean b);
    boolean existsByUsername(String username);
    Optional<Teller> findByUsername(String username); // Ye zaroori hai
}