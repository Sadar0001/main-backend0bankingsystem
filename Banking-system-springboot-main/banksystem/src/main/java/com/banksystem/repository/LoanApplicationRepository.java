package com.banksystem.repository;

import com.banksystem.entity.LoanApplication;
import com.banksystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByCustomer(Customer customer);

    @Query("SELECT la FROM LoanApplication la WHERE la.customer.branch.id = :branchId AND la.status = 'PENDING' ORDER BY la.createdAt ASC")
    List<LoanApplication> findPendingApplicationsByBranch(@Param("branchId") Long branchId);
}