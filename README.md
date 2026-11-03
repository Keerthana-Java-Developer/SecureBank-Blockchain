# Secure Banking Transactions — Java + Spring Boot

This project is a Java/Spring Boot conversion starter based on the uploaded `keerthanaproject.zip` (a Django 2.1-era application). It implements registration, login/logout, a demo bank account, transfers, transaction history, BCrypt password hashing, and a SHA-256 hash-linked ledger.

## Requirements
- JDK 17 or newer
- Maven 3.6+ (or open `pom.xml` in IntelliJ IDEA and let Maven import dependencies)

## Run in IntelliJ IDEA
1. Extract `secure-banking-java.zip`.
2. Open the extracted `secure-banking-java` folder (the folder containing `pom.xml`).
3. Allow IntelliJ to load the Maven project and download dependencies.
4. Run `src/main/java/com/keerthana/bank/SecureBankingApplication.java`.
5. Open `http://localhost:8080`.
6. Select **Create account**. The demo starts each new account with ₹1,000. Register a second user in a separate browser/incognito session to test transfers.

Or run in a terminal: `mvn spring-boot:run`.

## Database
By default it uses a local H2 file database at `./data/securebank` so the project can be started without installing MySQL. H2 console is at `/h2-console` while the app is running; JDBC URL: `jdbc:h2:file:./data/securebank;MODE=MySQL;AUTO_SERVER=TRUE`, user `sa`, blank password.

To use MySQL, create a database named `secure_banking`, then set environment variables before starting:
- `DB_URL=jdbc:mysql://localhost:3306/secure_banking`
- `DB_DRIVER=com.mysql.cj.jdbc.Driver`
- `DB_USER=your_mysql_user`
- `DB_PASSWORD=your_mysql_password`

Do not commit real database credentials.

## Pages
- `/` — welcome/home
- `/signup` — registration
- `/login` — login
- `/dashboard` — account overview and recent transactions
- `/transfer` — transfer between demo accounts
- `/ledger` — searchable/filterable transaction history with date, direction, status, amount filters and pagination
- `/receipt/{id}` — account-authorized printable/downloadable transaction receipt
- `/pki` — security notes

## What was and wasn't converted
The uploaded source is a Django web app with custom users, accounts, cards, transactions, user requests/approvals, OTP/TOTP, RSA/PKI pages, PII permissions, logs and many role-specific views. The Java project here implements the core user/account/transfer/history flow, BCrypt password hashing, and a local SHA-256 hash chain. It does **not** yet reproduce all Django roles/approval workflows, email/TOTP OTP delivery, RSA key generation/encryption/decryption, QR code pages, PII access approvals, or full logging screens.

The original source did not show an actual distributed blockchain implementation. The ledger here is a hash-linked database record chain for educational integrity demonstration, not a production blockchain. It has no distributed consensus or independent nodes. The app is for learning/demo only, not real financial use.


## Real-world style upgrade included
- Transaction history filters (search, CREDIT/DEBIT, status, date range, minimum/maximum amount) and 10-row pagination.
- Account-scoped transaction receipts with reference, date/time, sender/receiver, status, and ledger hashes. Use the browser print dialog to save as PDF.
- Demo transfer controls: ₹25,000 maximum per transfer and ₹50,000 total outgoing limit per account per day. OTP must be verified and is consumed after a successful transfer.
- Transfers only change virtual balances stored in this application's local demo database. No bank, UPI, card, or payment-provider integration is configured. Never enter real banking credentials.
- The transaction receipt route checks that the transaction belongs to the logged-in user's account.

## Honest limitations
This remains a portfolio/demo application, not a real bank. Email/SMS OTP delivery, full login audit history, device/session management UI, production-grade rate limiting, and external bank integration are not implemented. Do not deploy this app for real financial use.