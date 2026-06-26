# 🏦 SecureBankPro

<div align="center">

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1.5-brightgreen?style=for-the-badge&logo=spring-boot)
![PostgreSQL](https://img.shields.io/badge/Supabase_PostgreSQL-cloud-blue?style=for-the-badge&logo=supabase)
![JWT](https://img.shields.io/badge/JWT-Auth-purple?style=for-the-badge&logo=jsonwebtokens)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger)
![Maven](https://img.shields.io/badge/Maven-3.9.6-C71A36?style=for-the-badge&logo=apache-maven)

**A full-stack Spring Boot banking REST API built across 17 learning phases**  
*From Java basics → OOP → Spring Boot → JPA → Security → JWT → Testing → Swagger → GitHub*

[📖 API Docs (Swagger)](#api-documentation) • [⚙️ Setup](#getting-started) • [🧪 Testing](#testing) • [🗂️ Project Phases](#project-phases)

</div>

---

## 📌 Overview

**SecureBankPro** is a production-grade banking application built as a structured learning project covering all major Java and Spring Boot concepts. It features:

- ✅ **REST API** with full CRUD for Users, Accounts, and Transactions
- ✅ **JWT-based stateless authentication** with role-based access (ADMIN / CUSTOMER)
- ✅ **Spring Data JPA + Hibernate** backed by **Supabase PostgreSQL** (cloud persistent)
- ✅ **Transaction management** with `@Transactional`, rollback, and ACID guarantees
- ✅ **Multithreading** — async transaction logging, synchronized money transfers
- ✅ **Global exception handling** + Bean Validation (`@Valid`)
- ✅ **JUnit 5 + Mockito** unit & integration tests (33 tests pass)
- ✅ **Swagger UI / OpenAPI 3** interactive documentation
- ✅ **Interactive frontend dashboard** for manual API testing

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17+ (tested on Java 25) |
| Maven | Bundled via `.maven/` (no install needed) |
| Supabase account | Free tier at [supabase.com](https://supabase.com) |

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/SecureBankPro.git
cd SecureBankPro
```

### 2. Configure the database

Create a Supabase project and update `src/main/resources/application.properties`:

```properties
# Replace with your Supabase Session Pooler details
spring.datasource.url=jdbc:postgresql://aws-X-REGION.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0
spring.datasource.username=postgres.YOUR_PROJECT_REF
spring.datasource.password=YOUR_PASSWORD
```

> 💡 **Where to find these:** Supabase Dashboard → Settings → Database → **Session pooler** (port 6543)

### 3. Run the application

```bash
# Windows
.\.maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run

# Mac/Linux
./mvnw spring-boot:run
```

The server starts at **http://localhost:8080**

### 4. Open the dashboard or Swagger UI

| URL | Description |
|-----|-------------|
| http://localhost:8080/dashboard.html | Interactive banking dashboard |
| http://localhost:8080/swagger-ui.html | Swagger API documentation |
| http://localhost:8080/v3/api-docs | Raw OpenAPI JSON spec |

---

## 🔑 Default Credentials

Seeded automatically on every startup:

| Role | Email | Password |
|------|-------|----------|
| **ADMIN** | `admin@securebank.com` | `Admin@123` |
| **CUSTOMER** | `nishant@example.com` | `Pass@123` |

Pre-seeded accounts:
- `SBP1001` — Savings account (Nishant)
- `SBP2001` — Current account (Admin)

---

## 📡 API Reference

### Authentication (`/api/auth`)

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| `POST` | `/api/auth/register` | ❌ | Register new user |
| `POST` | `/api/auth/login` | ❌ | Login and get JWT token |
| `POST` | `/api/auth/logout` | ✅ | Logout session |
| `GET`  | `/api/auth/session` | ✅ | Check session status |

### Accounts (`/api/accounts`)

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| `POST` | `/api/accounts/create` | ✅ CUSTOMER/ADMIN | Create new account |
| `GET`  | `/api/accounts/{accountNumber}` | ✅ CUSTOMER/ADMIN | Get account details |
| `GET`  | `/api/accounts` | ✅ CUSTOMER/ADMIN | List all accounts |

### Transactions (`/api/transactions`)

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| `POST` | `/api/transactions/deposit` | ✅ CUSTOMER/ADMIN | Deposit money |
| `POST` | `/api/transactions/withdraw` | ✅ CUSTOMER/ADMIN | Withdraw money |
| `POST` | `/api/transactions/transfer` | ✅ CUSTOMER/ADMIN | Transfer between accounts |
| `GET`  | `/api/transactions/history/{accountNumber}` | ✅ CUSTOMER/ADMIN | Transaction history |

### Admin (`/api/admin`)

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| `PUT`  | `/api/admin/accounts/{accountNumber}/freeze` | ✅ ADMIN only | Freeze account |
| `PUT`  | `/api/admin/accounts/{accountNumber}/unblock` | ✅ ADMIN only | Unblock account |
| `DELETE` | `/api/admin/users/{id}` | ✅ ADMIN only | Delete user |

---

## 📖 API Documentation

Interactive Swagger UI with JWT auth testing:

1. Start the application
2. Open **http://localhost:8080/swagger-ui.html**
3. Call `POST /api/auth/login` and copy the token
4. Click **Authorize 🔒** → paste the token → click **Authorize**
5. All protected endpoints now work with one click

---

## 🏗️ Architecture

```
SecureBankPro/
├── src/main/java/com/securebank/pro/
│   ├── controller/          # REST endpoints (Auth, Account, Transaction, Admin)
│   ├── service/             # Business logic (BankService, AuthService, UserService)
│   │   └── impl/            # Service implementations
│   ├── repository/          # JPA repositories (User, Account, Transaction)
│   │   └── impl/            # Custom repository wrappers
│   ├── entity/              # JPA entities (User, Account, Transaction)
│   ├── dto/
│   │   ├── request/         # RegisterRequestDTO, LoginRequestDTO, TransferRequestDTO...
│   │   ├── response/        # AccountResponseDTO, TransactionResponseDTO...
│   │   └── mapper/          # DtoMapper (Entity ↔ DTO conversion)
│   ├── config/
│   │   ├── SecurityConfig.java       # Spring Security + CORS
│   │   ├── SwaggerConfig.java        # OpenAPI 3 + JWT Bearer scheme
│   │   ├── JwtAuthenticationFilter   # JWT token extraction & validation
│   │   └── AppConfig.java
│   ├── util/
│   │   ├── JwtUtils.java    # Token generation & validation
│   │   └── BankLogger.java  # Custom file-based logging
│   ├── exception/           # Global exception handler (@ControllerAdvice)
│   ├── enums/               # AccountType (SAVINGS, CURRENT), Role (ADMIN, CUSTOMER)
│   └── SecureBankProApplication.java
├── src/main/resources/
│   ├── application.properties   # Supabase datasource, JPA, Swagger config
│   ├── schema.sql               # PostgreSQL schema (IF NOT EXISTS — preserves data)
│   └── static/
│       └── dashboard.html       # Interactive frontend dashboard
├── src/test/java/               # JUnit 5 + Mockito tests (33 tests)
├── docs/dashboard.html          # Standalone dashboard copy
├── pom.xml                      # Maven: Spring Boot 3.1.5, SpringDoc, jjwt, PostgreSQL
└── README.md
```

### Request Flow

```
Client Request
    ↓
CorsFilter (permits all origins)
    ↓
JwtAuthenticationFilter (validates Bearer token, sets SecurityContext)
    ↓
AuthorizationFilter (checks ROLE_ADMIN / ROLE_CUSTOMER)
    ↓
@RestController (validates @RequestBody with @Valid)
    ↓
@Service (business logic, @Transactional)
    ↓
JpaRepository (Hibernate → Supabase PostgreSQL)
```

---

## 🧪 Testing

```bash
# Run all 33 unit + integration tests
.\.maven\apache-maven-3.9.6\bin\mvn.cmd test

# Run with verbose output
.\.maven\apache-maven-3.9.6\bin\mvn.cmd test -Dsurefire.failIfNoSpecifiedTests=false
```

**Test coverage:**
- `shouldLoginSuccessfully()` — valid credentials return JWT
- `shouldRejectInvalidLogin()` — wrong password returns 400
- `shouldDepositMoney()` — balance increases correctly
- `shouldWithdrawMoney()` — balance decreases correctly
- `shouldTransferMoney()` — atomic debit + credit
- `shouldRejectInvalidTransfer()` — insufficient balance rejected
- `shouldRegisterNewUser()` — new user created and persisted
- ... 26 more tests

---

## 📚 Project Phases

This project was built phase-by-phase as a Java learning journey:

| Phase | Topic | Key Concepts |
|-------|-------|-------------|
| 1 | Java Basics | Variables, data types, operators, control flow |
| 2 | OOP | Classes, inheritance, polymorphism, interfaces |
| 3 | Collections & Streams | List, Map, lambda, Stream API |
| 4 | Exception Handling | try-catch, custom exceptions, finally |
| 5 | File I/O & Serialization | FileWriter, ObjectOutputStream, backups |
| 6 | Java Concurrency | Threads, ExecutorService, synchronized, deadlock prevention |
| 7 | Spring Boot Setup | `@SpringBootApplication`, CommandLineRunner, H2/PostgreSQL |
| 8 | REST API | `@RestController`, `@GetMapping`, `ResponseEntity`, HTTP verbs |
| 9 | Spring Data JPA | `@Entity`, `@OneToMany`, `JpaRepository`, Hibernate |
| 10 | DTOs & Mapping | Request/Response DTOs, `DtoMapper`, separation of concerns |
| 11 | Spring Security & JWT | `SecurityFilterChain`, `JwtAuthenticationFilter`, BCrypt |
| 12 | Validation & Exceptions | `@Valid`, `@NotBlank`, `@ControllerAdvice`, error responses |
| 13 | Transaction Management | `@Transactional`, ACID, rollback on failure |
| 14 | Multithreading | Async logging, synchronized transfers, lock ordering |
| 15 | Testing | JUnit 5, Mockito, MockMvc, 33 passing tests |
| 16 | Swagger / OpenAPI | SpringDoc, JWT Bearer auth, request/response examples |
| **17** | **Git & GitHub** | **git init, branching, clean commits, README, CI/CD ready** |

---

## 🔐 Security Notes

- Passwords are hashed with **BCrypt** (strength 10) — never stored in plaintext
- JWT tokens are signed with **HS256** and expire after 24 hours
- All banking endpoints require a valid JWT in the `Authorization: Bearer <token>` header
- Admin endpoints additionally require `ROLE_ADMIN` in the JWT claims
- **CORS** is enabled for all origins (development mode — restrict in production)
- **CSRF** is disabled (stateless JWT — no session cookies)

> ⚠️ **Production checklist:** Move DB credentials to environment variables, restrict CORS origins, use HTTPS, rotate the JWT secret key.

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 25 |
| Framework | Spring Boot 3.1.5 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| ORM | Hibernate / Spring Data JPA |
| Database | Supabase PostgreSQL (cloud) |
| Connection Pool | HikariCP |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5, Mockito, MockMvc |
| Build | Apache Maven 3.9.6 |

---

## 📄 License

This project is for educational purposes. MIT License.
