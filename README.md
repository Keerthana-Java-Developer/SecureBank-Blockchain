<div align="center">

# Secure Banking Transaction System

**A Java and Spring Boot banking demonstration focused on secure authentication and transaction integrity concepts.**

[Source Code](https://github.com/keerthanavinoth2003-maker/SecureBank-Blockchain) · [Developer Portfolio](https://keerthanavinoth2003-maker.github.io/keerthana-portfolio/)

</div>

> **Project status:** Local portfolio demonstration. No public live deployment is currently available. This application is not connected to real banking or payment networks.

## Overview

This project demonstrates a core banking-style workflow using Java and Spring Boot. It includes account registration, login, virtual transfers and transaction history, with BCrypt password hashing and a SHA-256 hash-linked transaction ledger.

## Key Features

- User registration and login
- Virtual demo bank accounts
- Transfers between application users
- Transaction history with filtering and pagination
- Account-authorized transaction receipts
- BCrypt password hashing
- SHA-256 hash-linked ledger records
- H2 local database, with optional MySQL configuration

## Technology Stack

| Area | Technologies |
|---|---|
| Language | Java |
| Backend | Spring Boot |
| Security | Spring Security, BCrypt |
| Data | Spring Data JPA, H2 / MySQL |
| Integrity concept | SHA-256 hash-linked ledger |
| Development | IntelliJ IDEA, Maven |

## Run Locally

### Requirements

- JDK 17 or newer
- Maven 3.6+ or IntelliJ IDEA

### Start the application

1. Clone or download this repository.
2. Open the project folder containing `pom.xml` in IntelliJ IDEA.
3. Allow Maven to resolve dependencies.
4. Run `SecureBankingApplication.java`.
5. Open http://localhost:8080.

Or run:

```bash
mvn spring-boot:run
```

The default configuration uses a local H2 file database. MySQL can be configured using environment variables described in the application configuration.

## Important Security Notes

This is an educational portfolio application, not a real banking system. Transfers update virtual balances in the local application database only. It does not connect to banks, UPI, cards or payment providers. Do not enter real banking credentials or use it to handle real funds.

The hash-linked ledger demonstrates a basic integrity concept; it is not a distributed blockchain and does not implement distributed consensus.

## Links

- **Source code:** https://github.com/keerthanavinoth2003-maker/SecureBank-Blockchain
- **Portfolio:** https://keerthanavinoth2003-maker.github.io/keerthana-portfolio/
- **Live deployment:** Not published; run locally for the demonstration.

