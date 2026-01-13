
package com.banksystem.repository;

import com.banksystem.entity.LoanOffers;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LoanOffersRepository extends JpaRepository<LoanOffers, Long> {

    List<LoanOffers> findByHeadBankIdAndIsActiveTrue(Long headBankId);

    List<LoanOffers> findAllByHeadBankId(Long headBankId);

    @Override
    Optional<LoanOffers> findById(Long id);
}