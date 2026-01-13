
package com.banksystem.repository;

import com.banksystem.entity.Account;
import com.banksystem.entity.Customer;
import com.banksystem.enums.AccountType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByCustomer(Customer customer);

//    boolean existsByCustomerAndAccountType(Customer customer, AccountType accountType);

    Account findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "5000")
    })
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Account findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);


    Optional<Account> findById(Long id);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Account a WHERE a.customer.id = :customerId AND a.accountType = :accountType AND a.branch.id = :branchId AND a.status = 'ACTIVE'")
    boolean existsByCustomerAndAccountTypeAndBranchId(
            @Param("customerId") Long customerId,
            @Param("accountType") AccountType accountType,
            @Param("branchId") Long branchId
    );
}
