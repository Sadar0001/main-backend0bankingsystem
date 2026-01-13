package com.banksystem.repository;


import com.banksystem.entity.CentralBank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CentralBankRepository extends JpaRepository<CentralBank,Long> {

    Optional<CentralBank> findById(Long id);
}
