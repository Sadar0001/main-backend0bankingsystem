package com.banksystem.repository;

import com.banksystem.entity.HeadBankAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HeadBankAdminRepository extends JpaRepository<HeadBankAdmin, Long> {
    Optional<HeadBankAdmin> findByUsername(String username);
}