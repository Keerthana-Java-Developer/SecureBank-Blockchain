package com.keerthana.bank.repository;
import com.keerthana.bank.model.BankAccount; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface BankAccountRepository extends JpaRepository<BankAccount,Long>{Optional<BankAccount> findByUserId(Long userId); Optional<BankAccount> findByAccountNumber(String accountNumber);}
