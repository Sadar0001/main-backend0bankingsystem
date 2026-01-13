// ============ AccountRequestRepository.java ============
package com.banksystem.repository;

import com.banksystem.entity.AccountRequest;
import com.banksystem.entity.Customer;
import com.banksystem.enums.AccountType;
import com.banksystem.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AccountRequestRepository extends JpaRepository<AccountRequest, Long> {

//    @Query("SELECT ar FROM AccountRequest ar " +
//            "JOIN FETCH ar.customer c " +
//            "JOIN FETCH c.branch b " +
//            "WHERE b.id = :branchId AND ar.status = :status")
//    List<AccountRequest> findPendingAccountRequestsByBranch(
//            @Param("branchId") Long branchId,
//            @Param("status") RequestStatus status);

    List<AccountRequest> findByCustomer(Customer customer);

    List<AccountRequest> findByStatus(RequestStatus status);

    List<AccountRequest> findByBranchId(Long branchId);


    boolean existsByCustomer_IdAndAccountTypeAndBranchIdAndStatus(
            Long customerId,
            AccountType accountType,
            Long branchId,
            RequestStatus statuses
    );
}