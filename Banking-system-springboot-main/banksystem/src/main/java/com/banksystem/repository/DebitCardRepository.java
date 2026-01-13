
package com.banksystem.repository;

import com.banksystem.entity.DebitCard;
import com.banksystem.entity.Account;
import com.banksystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DebitCardRepository extends JpaRepository<DebitCard, Long> {

    boolean existsByAccountAndIsActiveTrue(Account account);

    @Query("SELECT dc FROM DebitCard dc WHERE dc.account.customer = :customer")
    List<DebitCard> findByAccountCustomer(@Param("customer") Customer customer);
}