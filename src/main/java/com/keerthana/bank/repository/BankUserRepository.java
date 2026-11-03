package com.keerthana.bank.repository;
import com.keerthana.bank.model.BankUser; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface BankUserRepository extends JpaRepository<BankUser,Long>{Optional<BankUser> findByUsernameIgnoreCase(String username); boolean existsByUsernameIgnoreCase(String username); boolean existsByEmailIgnoreCase(String email);}
