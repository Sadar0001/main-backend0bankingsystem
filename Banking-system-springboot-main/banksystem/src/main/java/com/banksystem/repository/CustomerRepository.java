package com.banksystem.repository;

import com.banksystem.entity.Customer;
import com.banksystem.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByBranch(Branch branch);
    boolean existsByAadharNumber(String aadharNumber);
    boolean existsByPanNumber(String panNumber);

    // New Method for Security
    Optional<Customer> findByUser_Username(String username);
}