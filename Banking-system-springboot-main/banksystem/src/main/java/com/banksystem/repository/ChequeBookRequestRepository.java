
package com.banksystem.repository;

import com.banksystem.entity.ChequeBookRequest;
import com.banksystem.entity.Customer;
import com.banksystem.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChequeBookRequestRepository extends JpaRepository<ChequeBookRequest, Long> {

    List<ChequeBookRequest> findByRequestedBy(Customer customer);

    @Query("SELECT cbr FROM ChequeBookRequest cbr WHERE cbr.account.branch.id = :branchId AND cbr.status = :status")
    List<ChequeBookRequest> findByBranchAndStatus(
            @Param("branchId") Long branchId,
            @Param("status") RequestStatus status);
}