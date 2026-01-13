
package com.banksystem.repository;

import com.banksystem.entity.CardRequest;
import com.banksystem.entity.Customer;
import com.banksystem.entity.Account;
import com.banksystem.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CardRequestRepository extends JpaRepository<CardRequest, Long> {

    List<CardRequest> findByRequestedBy(Customer customer);

    @Query("SELECT cr FROM CardRequest cr WHERE cr.account.branch.id = :branchId AND cr.status = :status")
    List<CardRequest> findByBranchAndStatus(
            @Param("branchId") Long branchId,
            @Param("status") RequestStatus status);

    boolean existsByAccountAndStatus(Account account, RequestStatus status);
}