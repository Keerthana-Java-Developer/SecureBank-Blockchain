
package com.keerthana.bank.service;

import com.keerthana.bank.model.*;
import com.keerthana.bank.repository.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BankingService {

 private final BankUserRepository users;
 private final BankAccountRepository accounts;
 private final BankTransactionRepository transactions;
 private final PasswordEncoder encoder;

 private final SecureRandom random = new SecureRandom();

 public BankingService(
         BankUserRepository users,
         BankAccountRepository accounts,
         BankTransactionRepository transactions,
         PasswordEncoder encoder) {
  this.users = users;
  this.accounts = accounts;
  this.transactions = transactions;
  this.encoder = encoder;
 }

 public BankUser register(
         String username, String email, String password) {

  String u = username == null
          ? "" : username.trim().toLowerCase(Locale.ROOT);

  String e = email == null
          ? "" : email.trim().toLowerCase(Locale.ROOT);

  if (u.length() < 3) {
   throw new IllegalArgumentException(
           "Username must contain at least 3 characters.");
  }

  if (!e.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
   throw new IllegalArgumentException(
           "Enter a valid email address.");
  }

  if (password == null || password.length() < 8) {
   throw new IllegalArgumentException(
           "Password must contain at least 8 characters.");
  }

  if (users.existsByUsernameIgnoreCase(u)) {
   throw new IllegalArgumentException(
           "Username already exists.");
  }

  if (users.existsByEmailIgnoreCase(e)) {
   throw new IllegalArgumentException(
           "Email already exists.");
  }

  BankUser user = users.save(
          new BankUser(u, e, encoder.encode(password)));

  accounts.save(new BankAccount(newAccountNumber(), user));

  return user;
 }

 public BankUser authenticate(String username, String password) {

  BankUser user = users.findByUsernameIgnoreCase(
                  username == null ? "" : username.trim())
          .orElse(null);

  if (user == null ||
          !encoder.matches(
                  password == null ? "" : password,
                  user.getPasswordHash())) {
   return null;
  }

  return user;
 }

 private String newAccountNumber() {
  String number;

  do {
   number = String.format(
           "%010d",
           random.nextLong(10_000_000_000L));
  } while (accounts.findByAccountNumber(number).isPresent());

  return number;
 }

 public BankAccount accountFor(BankUser user) {
  return accounts.findByUserId(user.getId())
          .orElseThrow(() ->
                  new IllegalStateException(
                          "Bank account was not found."));
 }

 public List<BankTransaction> history(BankAccount account) {
  return transactions
          .findByFromAccountOrToAccountOrderByCreatedAtDesc(
                  account.getAccountNumber(),
                  account.getAccountNumber());
 }

 public BankTransaction transactionForAccount(
         Long id, BankAccount account) {

  BankTransaction tx = transactions.findById(id)
          .orElseThrow(() ->
                  new IllegalArgumentException(
                          "Transaction receipt was not found."));

  String number = account.getAccountNumber();

  if (!number.equals(tx.getFromAccount()) &&
          !number.equals(tx.getToAccount())) {
   throw new IllegalArgumentException(
           "This transaction does not belong to your account.");
  }

  return tx;
 }

 public BigDecimal todayOutgoingTotal(BankAccount account) {
  LocalDateTime start = LocalDate.now().atStartOfDay();

  return transactions
          .findByFromAccountAndCreatedAtGreaterThanEqualAndStatus(
                  account.getAccountNumber(),
                  start,
                  "COMPLETED")
          .stream()
          .map(BankTransaction::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
 }

 @Transactional
 public void transfer(
         BankUser user,
         String recipientNumber,
         BigDecimal amount) {

  if (amount == null ||
          amount.scale() > 2 ||
          amount.compareTo(BigDecimal.ZERO) <= 0) {
   throw new IllegalArgumentException(
           "Enter an amount greater than zero with up to 2 decimal places.");
  }

  if (amount.compareTo(new BigDecimal("25000.00")) > 0) {
   throw new IllegalArgumentException(
           "The per-transfer demo limit is ₹25,000.00.");
  }

  BankAccount from = accountFor(user);

  BigDecimal alreadySent = todayOutgoingTotal(from);

  if (alreadySent.add(amount)
          .compareTo(new BigDecimal("50000.00")) > 0) {
   throw new IllegalArgumentException(
           "Daily demo transfer limit of ₹50,000.00 exceeded.");
  }

  BankAccount to = accounts.findByAccountNumber(
                  recipientNumber == null
                          ? "" : recipientNumber.trim())
          .orElseThrow(() ->
                  new IllegalArgumentException(
                          "Recipient account number was not found."));

  if (from.getId().equals(to.getId())) {
   throw new IllegalArgumentException(
           "You cannot transfer money to your own account.");
  }

  if (from.getBalance().compareTo(amount) < 0) {
   throw new IllegalArgumentException("Insufficient balance.");
  }

  String previous = transactions.findAll().stream()
          .max(Comparator.comparing(BankTransaction::getId))
          .map(BankTransaction::getTransactionHash)
          .orElse("0".repeat(64));

  LocalDateTime createdAt = LocalDateTime.now();

  String payload = buildPayload(
          from.getAccountNumber(),
          to.getAccountNumber(),
          amount,
          createdAt,
          previous);

  String hash = sha256(payload);

  from.setBalance(from.getBalance().subtract(amount));
  to.setBalance(to.getBalance().add(amount));

  accounts.save(from);
  accounts.save(to);

  transactions.save(new BankTransaction(
          from.getAccountNumber(),
          to.getAccountNumber(),
          amount,
          createdAt,
          previous,
          hash));
 }

 public long transactionCount() {
  return transactions.count();
 }

 /*
  * Checks the previous-hash chain.
  * This does not independently verify each transaction's data hash.
  */
 public boolean verifyLedger() {

  List<BankTransaction> all = transactions.findAll().stream()
          .sorted(Comparator.comparing(BankTransaction::getId))
          .toList();

  String previous = "0".repeat(64);

  for (BankTransaction transaction : all) {

   if (transaction.getPreviousHash() == null ||
           transaction.getTransactionHash() == null) {
    return false;
   }

   if (!transaction.getPreviousHash().equals(previous)) {
    return false;
   }

   previous = transaction.getTransactionHash();
  }

  return true;
 }

 private String buildPayload(
         String from,
         String to,
         BigDecimal amount,
         LocalDateTime createdAt,
         String previousHash) {

  return from + "|"
          + to + "|"
          + amount.toPlainString() + "|"
          + createdAt + "|"
          + previousHash;
 }

 private String sha256(String value) {
  try {
   byte[] bytes = MessageDigest.getInstance("SHA-256")
           .digest(value.getBytes(StandardCharsets.UTF_8));

   return HexFormat.of().formatHex(bytes);

  } catch (Exception e) {
   throw new IllegalStateException(
           "SHA-256 unavailable", e);
  }
 }
}
