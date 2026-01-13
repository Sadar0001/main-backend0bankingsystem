package com.banksystem.repository;

import com.banksystem.entity.CentralBankAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CentralBankAdminRepository extends JpaRepository<CentralBankAdmin, Long> {
    Optional<CentralBankAdmin> findByUsername(String username);
}