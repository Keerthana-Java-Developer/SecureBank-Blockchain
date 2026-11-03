package com.keerthana.bank.repository;

import com.keerthana.bank.model.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findTop50ByFromAccountOrToAccountOrderByCreatedAtDesc(String from, String to);
    List<BankTransaction> findByFromAccountOrToAccountOrderByCreatedAtDesc(String from, String to);
    List<BankTransaction> findByFromAccountAndCreatedAtGreaterThanEqualAndStatus(String from, LocalDateTime start, String status);
}
