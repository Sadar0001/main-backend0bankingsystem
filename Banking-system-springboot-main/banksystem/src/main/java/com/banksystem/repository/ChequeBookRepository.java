
package com.banksystem.repository;

import com.banksystem.entity.ChequeBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChequeBookRepository extends JpaRepository<ChequeBook, Long> {
}