package com.banksystem.repository;

import com.banksystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);

    // Add this new method to get specificId based on role
    @Query("""
        SELECT CASE u.role
            WHEN 'CUSTOMER' THEN c.id
            WHEN 'TELLER' THEN t.id
            WHEN 'BRANCHMANAGER' THEN bm.id
            WHEN 'HEADMANAGER' THEN hba.id
            WHEN 'CENTRALADMIN' THEN cba.id
            ELSE u.id
        END
        FROM User u
        LEFT JOIN u.customer c
        LEFT JOIN u.teller t
        LEFT JOIN u.branchManager bm
        LEFT JOIN u.headBankAdmin hba
        LEFT JOIN u.centralBankAdmin cba
        WHERE u.username = :username
    """)
    Long findSpecificIdByUsername(@Param("username") String username);
}