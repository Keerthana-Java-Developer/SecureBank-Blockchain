
package com.keerthana.bank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_transactions")
public class BankTransaction {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable = false, length = 16)
 private String fromAccount;

 @Column(nullable = false, length = 16)
 private String toAccount;

 @Column(nullable = false, precision = 14, scale = 2)
 private BigDecimal amount;

 @Column(nullable = false)
 private LocalDateTime createdAt;

 @Column(nullable = false, length = 64)
 private String previousHash;

 @Column(nullable = false, length = 64, unique = true)
 private String transactionHash;

 @Column(nullable = false, length = 20)
 private String status = "COMPLETED";

 protected BankTransaction() {
 }


  public BankTransaction(
          String from,
          String to,
          BigDecimal amount,
          LocalDateTime createdAt,
          String previousHash,
          String hash){

  this.fromAccount = from;
  this.toAccount = to;
  this.amount = amount;
  this.createdAt = createdAt;
  this.previousHash = previousHash;
  this.transactionHash = hash;
 }

 public Long getId() {
  return id;
 }

 public String getFromAccount() {
  return fromAccount;
 }

 public String getToAccount() {
  return toAccount;
 }

 public BigDecimal getAmount() {
  return amount;
 }

 public LocalDateTime getCreatedAt() {
  return createdAt;
 }

 public String getPreviousHash() {
  return previousHash;
 }

 public String getTransactionHash() {
  return transactionHash;
 }

 public String getStatus() {
  return status;
 }
}
