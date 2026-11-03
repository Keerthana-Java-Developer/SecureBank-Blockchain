package com.keerthana.bank.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="bank_accounts")
public class BankAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=16) private String accountNumber;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal balance=BigDecimal.ZERO;
 @OneToOne(optional=false) @JoinColumn(name="user_id",unique=true) private BankUser user;
 protected BankAccount(){}
 public BankAccount(String accountNumber,BankUser user){this.accountNumber=accountNumber;this.user=user;this.balance=new BigDecimal("1000.00");}
 public Long getId(){return id;} public String getAccountNumber(){return accountNumber;} public BigDecimal getBalance(){return balance;} public BankUser getUser(){return user;}
 public void setBalance(BigDecimal balance){this.balance=balance;}
}
