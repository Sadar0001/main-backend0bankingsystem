
package com.banksystem.repository;

import com.banksystem.entity.DebitCardRules;
import com.banksystem.entity.HeadBank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DebitCardRulesRepository extends JpaRepository<DebitCardRules, Long> {


    Optional<DebitCardRules> findByHeadBankAndCardType(HeadBank headBank, String cardType);

    // Add this method
    List<DebitCardRules> findByHeadBankAndIsActive(HeadBank headBank, Boolean isActive);

    // You might also want this method
    List<DebitCardRules> findByHeadBank(HeadBank headBank);
}
