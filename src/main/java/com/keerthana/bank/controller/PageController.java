
package com.keerthana.bank.controller;

import com.keerthana.bank.model.BankAccount;
import com.keerthana.bank.model.BankUser;
import com.keerthana.bank.repository.BankUserRepository;
import com.keerthana.bank.service.BankingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.keerthana.bank.model.BankTransaction;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

 private final BankingService banking;
 private final BankUserRepository users;

 public PageController(
         BankingService banking,
         BankUserRepository users) {
  this.banking = banking;
  this.users = users;
 }

 private BankUser current(HttpSession session) {
  Object id = session.getAttribute("userId");

  if (!(id instanceof Long)) {
   return null;
  }

  return users.findById((Long) id).orElse(null);
 }

 @GetMapping("/")
 public String home(HttpSession session, Model model) {
  model.addAttribute("user", current(session));
  return "home";
 }

 @GetMapping("/login")
 public String login(HttpSession session) {
  return current(session) == null
          ? "login"
          : "redirect:/dashboard";
 }

 @PostMapping("/login")
 public String doLogin(
         @RequestParam String username,
         @RequestParam String password,
         HttpServletRequest request,
         Model model) {

  BankUser user = banking.authenticate(username, password);

  if (user == null) {
   model.addAttribute("error", "Invalid username or password.");
   model.addAttribute("username", username);
   return "login";
  }

  // Rotate the session ID after successful login.
  HttpSession session = request.getSession(true);
  request.changeSessionId();
  session.setAttribute("userId", user.getId());

  return "redirect:/dashboard";
 }

 @GetMapping("/signup")
 public String signup(HttpSession session) {
  if (current(session) != null) {
   return "redirect:/dashboard";
  }

  return "signup";
 }

 @PostMapping("/signup")
 public String doSignup(
         @RequestParam String username,
         @RequestParam String email,
         @RequestParam String password,
         @RequestParam String confirmPassword,
         Model model,
         HttpSession session) {

  try {
   if (!password.equals(confirmPassword)) {
    throw new IllegalArgumentException(
            "Passwords do not match.");
   }

   BankUser user = banking.register(username, email, password);

   // Rotate session ID after registration.
   session.setAttribute("userId", user.getId());

   return "redirect:/dashboard";

  } catch (IllegalArgumentException ex) {
   model.addAttribute("error", ex.getMessage());
   model.addAttribute("username", username);
   model.addAttribute("email", email);
   return "signup";
  }
 }

 @GetMapping("/dashboard")
 public String dashboard(HttpSession session, Model model) {
  BankUser user = current(session);

  if (user == null) {
   return "redirect:/login";
  }

  BankAccount account = banking.accountFor(user);

  model.addAttribute("user", user);
  model.addAttribute("account", account);
  model.addAttribute("transactions", banking.history(account));
  model.addAttribute("transactionCount", banking.transactionCount());
  model.addAttribute("ledgerValid", banking.verifyLedger());

  return "dashboard";
 }

 @GetMapping("/transfer")
 public String transfer(HttpSession session, Model model) {
  BankUser user = current(session);

  if (user == null) {
   return "redirect:/login";
  }

  model.addAttribute("user", user);
  BankAccount account = banking.accountFor(user);
  model.addAttribute("account", account);
  model.addAttribute("dailySent", banking.todayOutgoingTotal(account));
  model.addAttribute("dailyRemaining", new BigDecimal("50000.00").subtract(banking.todayOutgoingTotal(account)).max(BigDecimal.ZERO));

  return "transfer";
 }

 @PostMapping("/transfer")
 public String doTransfer(
         HttpSession session,
         @RequestParam String recipientAccount,
         @RequestParam BigDecimal amount,
         @RequestParam(required = false) String enteredOtp,
         Model model) {

  BankUser user = current(session);

  if (user == null) {
   return "redirect:/login";
  }

  BankAccount account = banking.accountFor(user);

  String generatedOtp =
          (String) session.getAttribute("bankOtp");

  Boolean otpVerified =
          (Boolean) session.getAttribute("bankOtpVerified");

  if (generatedOtp == null
          || !Boolean.TRUE.equals(otpVerified)
          || enteredOtp == null
          || !generatedOtp.equals(enteredOtp)) {

   model.addAttribute(
           "error",
           "Please generate and verify a valid OTP before transferring.");

   model.addAttribute("user", user);
   model.addAttribute("account", account);
   model.addAttribute("dailySent", banking.todayOutgoingTotal(account));
   model.addAttribute("dailyRemaining", new BigDecimal("50000.00").subtract(banking.todayOutgoingTotal(account)).max(BigDecimal.ZERO));

   return "transfer";
  }

  try {
   banking.transfer(user, recipientAccount, amount);

   // Consume OTP after a successful transfer only.
   session.removeAttribute("bankOtp");
   session.removeAttribute("bankOtpVerified");

   model.addAttribute(
           "success",
           "Demo transfer completed and recorded in the hash-linked ledger.");

  } catch (IllegalArgumentException ex) {
   model.addAttribute("error", ex.getMessage());
  }

  model.addAttribute("user", user);
  BankAccount refreshed = banking.accountFor(user);
  model.addAttribute("account", refreshed);
  model.addAttribute("dailySent", banking.todayOutgoingTotal(refreshed));
  model.addAttribute("dailyRemaining", new BigDecimal("50000.00").subtract(banking.todayOutgoingTotal(refreshed)).max(BigDecimal.ZERO));

  return "transfer";
 }

 @GetMapping("/ledger")
 public String ledger(HttpSession session, Model model,
         @RequestParam(required = false) String search,
         @RequestParam(required = false) String type,
         @RequestParam(required = false) String status,
         @RequestParam(required = false) String fromDate,
         @RequestParam(required = false) String toDate,
         @RequestParam(required = false) String minAmount,
         @RequestParam(required = false) String maxAmount,
         @RequestParam(defaultValue = "1") int page) {
  BankUser user = current(session);
  if (user == null) return "redirect:/login";
  BankAccount account = banking.accountFor(user);
  String own = account.getAccountNumber();
  List<BankTransaction> all = banking.history(account).stream().filter(tx -> {
   boolean outgoing = own.equals(tx.getFromAccount());
   boolean incoming = own.equals(tx.getToAccount());
   String direction = outgoing ? "DEBIT" : "CREDIT";
   if (type != null && !type.isBlank() && !type.equalsIgnoreCase("ALL") && !type.equalsIgnoreCase(direction)) return false;
   if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase(tx.getStatus())) return false;
   if (search != null && !search.isBlank()) {
    String q = search.trim().toLowerCase();
    if (!tx.getFromAccount().toLowerCase().contains(q) && !tx.getToAccount().toLowerCase().contains(q)
      && !String.valueOf(tx.getId()).contains(q)) return false;
   }
   if (fromDate != null && !fromDate.isBlank() && tx.getCreatedAt().toLocalDate().isBefore(LocalDate.parse(fromDate))) return false;
   if (toDate != null && !toDate.isBlank() && tx.getCreatedAt().toLocalDate().isAfter(LocalDate.parse(toDate))) return false;
   if (minAmount != null && !minAmount.isBlank() && tx.getAmount().compareTo(new BigDecimal(minAmount)) < 0) return false;
   if (maxAmount != null && !maxAmount.isBlank() && tx.getAmount().compareTo(new BigDecimal(maxAmount)) > 0) return false;
   return true;
  }).collect(Collectors.toList());
  int pageSize = 10;
  int totalPages = Math.max(1, (int)Math.ceil(all.size() / (double)pageSize));
  int safePage = Math.min(Math.max(page, 1), totalPages);
  List<BankTransaction> visible = all.subList(Math.min((safePage-1)*pageSize, all.size()), Math.min(safePage*pageSize, all.size()));
  model.addAttribute("user", user);
  model.addAttribute("account", account);
  model.addAttribute("transactions", visible);
  model.addAttribute("ledgerValid", banking.verifyLedger());
  model.addAttribute("search", search);
  model.addAttribute("type", type == null ? "ALL" : type);
  model.addAttribute("status", status == null ? "ALL" : status);
  model.addAttribute("fromDate", fromDate);
  model.addAttribute("toDate", toDate);
  model.addAttribute("minAmount", minAmount);
  model.addAttribute("maxAmount", maxAmount);
  model.addAttribute("page", safePage);
  model.addAttribute("totalPages", totalPages);
  model.addAttribute("totalResults", all.size());
  return "ledger";
 }

 @GetMapping("/receipt/{id}")
 public String receipt(@PathVariable Long id, HttpSession session, Model model) {
  BankUser user = current(session);
  if (user == null) return "redirect:/login";
  BankAccount account = banking.accountFor(user);
  try {
   model.addAttribute("transaction", banking.transactionForAccount(id, account));
   model.addAttribute("user", user);
   model.addAttribute("account", account);
   return "receipt";
  } catch (IllegalArgumentException ex) {
   return "redirect:/ledger";
  }
 }

 @GetMapping("/pki")
 public String pki(HttpSession session, Model model) {
  BankUser user = current(session);

  if (user == null) {
   return "redirect:/login";
  }

  model.addAttribute("user", user);
  model.addAttribute(
          "message",
          "Passwords are BCrypt-hashed. Transaction integrity uses SHA-256 hash links. This starter does not implement email OTP delivery or production-grade RSA key management.");

  return "pki";
 }

 @PostMapping("/logout")
 public String logout(HttpSession session) {
  session.invalidate();
  return "redirect:/";
 }
}
