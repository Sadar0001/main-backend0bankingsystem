package com.banksystem.repository;


import com.banksystem.entity.HeadBank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeadBankRepository extends JpaRepository<HeadBank,Long> {
    Optional<HeadBank> findByName(String name);
}
