# 🏦 SecureBankPro — Frontend API Integration Specification

This document provides a comprehensive integration guide, page-by-page options checklist, API specifications, and JavaScript (`fetch`) code snippets to help build a fully decoupled frontend application (e.g., React, Vue, Angular, or Vanilla JS) for the **SecureBankPro** REST API.

---

## 🌐 Base URL & CORS Settings

- **Local Dev Server**: `http://localhost:8080`
- **CORS Configuration**: The backend has CORS fully enabled for all origins (`*`) and supports methods `GET`, `POST`, `PUT`, `DELETE`, and `OPTIONS`. You do not need to configure any proxies; direct AJAX/fetch requests from your local frontend domain (e.g. `http://localhost:3000`) will succeed.

---

## 🔒 Authentication Flow (JWT Bearer Token)

SecureBankPro uses stateless JWT authentication.
1. The client submits credentials to `/api/auth/login`.
2. The server responds with a signed JWT token string.
3. The client must store this token (usually in `localStorage` or `sessionStorage`).
4. For all subsequent calls to protected endpoints, the client must attach the token in the headers:
   ```http
   Authorization: Bearer <your_jwt_token_here>
   ```

---

## 📄 Page-by-Phase Integration Guide

### 1️⃣ Sign Up / Registration Page
Allows visitors to create a customer account.

#### **Form Fields Required:**
- **Full Name** (Input: Text, Required)
- **Email** (Input: Email, Required)
- **Password** (Input: Password, Required)

#### **Validation Rules (Front-End & Back-End Checked):**
- **Email**: Must be a valid email format matching `^[\\w.%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$`
- **Password**: 
  - Minimum **8 characters**
  - At least one **uppercase letter** (`A–Z`)
  - At least one **digit** (`0–9`)
  - At least one **special character** (e.g., `@`, `#`, `!`, `%`)

#### **API Spec:**
- **Method**: `POST`
- **Endpoint**: `/api/auth/register`
- **Headers**: 
  - `Content-Type: application/json`
- **Request Payload**:
  ```json
  {
    "fullName": "Jane Doe",
    "email": "jane.doe@example.com",
    "password": "SecurePass@123"
  }
  ```
- **Responses**:
  - **Success (200 OK)**:
    ```json
    {
      "success": true,
      "message": "User registered successfully with ID 4"
    }
    ```
  - **Error (400 Bad Request)**:
    ```json
    {
      "success": false,
      "message": "Password must be at least 8 characters long. Provided length: 7."
    }
    ```

---

### 2️⃣ Sign In / Login Page
Allows users to authenticate and retrieve their access token.

#### **Form Fields Required:**
- **Email** (Input: Text/Email, Required)
- **Password** (Input: Password, Required)

#### **API Spec:**
- **Method**: `POST`
- **Endpoint**: `/api/auth/login`
- **Headers**:
  - `Content-Type: application/json`
- **Request Payload**:
  ```json
  {
    "email": "jane.doe@example.com",
    "password": "SecurePass@123"
  }
  ```
- **Responses**:
  - **Success (200 OK)**:
    ```json
    {
      "email": "jane.doe@example.com",
      "success": true,
      "message": "Login successful",
      "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqYW5lL..."
    }
    ```
    *Developer Action: Store the `token` immediately in storage!*
    ```javascript
    localStorage.setItem("authToken", responseData.token);
    ```
  - **Error (400 Bad Request)**:
    ```json
    {
      "email": "jane.doe@example.com",
      "success": false,
      "message": "Invalid credentials",
      "token": null
    }
    ```

---

### 3️⃣ Main Dashboard / Landing State
Once logged in, verify the user session and fetch user accounts.

#### **Step A: Session Check**
- **Method**: `GET`
- **Endpoint**: `/api/auth/session`
- **Headers**:
  - `Authorization: Bearer <token>`
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Session is active"
  }
  ```
  *(Redirect to login page if this returns a 401 Unauthorized status).*

#### **Step B: List User Accounts**
Fetches all active bank accounts belonging to the authenticated user.
- **Method**: `GET`
- **Endpoint**: `/api/accounts`
- **Headers**:
  - `Authorization: Bearer <token>`
- **Response (200 OK)**:
  ```json
  [
    {
      "accountId": 1,
      "accountNumber": "SBP1001",
      "balance": 1000.00,
      "accountType": "SAVINGS",
      "active": true
    }
  ]
  ```

---

### 4️⃣ Create Account Page
Allows customers to create a new Savings or Current account.

#### **Form Fields Required:**
- **Account Number** (Input: Text, Required, alphanumeric, 10-20 characters. E.g. `SBP` prefix + random digits)
- **Account Type** (Input: Select/Dropdown, Choices: `SAVINGS`, `CURRENT`)
- **Initial Deposit** (Input: Number, Minimum `0.00`)

#### **API Spec:**
- **Method**: `POST`
- **Endpoint**: `/api/accounts/create`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>`
- **Request Payload**:
  ```json
  {
    "accountNumber": "SBP-SAV-49382",
    "accountType": "SAVINGS",
    "balance": 500.00
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "accountId": 4,
    "accountNumber": "SBP-SAV-49382",
    "balance": 500.00,
    "accountType": "SAVINGS",
    "active": true
  }
  ```

---

### 5️⃣ Banking Actions (Deposit, Withdraw, Transfer)

#### **A. Deposit Page / Dialog**
- **Method**: `POST`
- **Endpoint**: `/api/transactions/deposit`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>`
- **Request Payload**:
  ```json
  {
    "accountNumber": "SBP1001",
    "amount": 250.00
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Deposited 250.00 USD successfully."
  }
  ```

#### **B. Withdraw Page / Dialog**
- **Method**: `POST`
- **Endpoint**: `/api/transactions/withdraw`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>`
- **Request Payload**:
  ```json
  {
    "accountNumber": "SBP1001",
    "amount": 100.00
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Withdrew 100.00 USD successfully."
  }
  ```

#### **C. Transfer Page / Dialog**
- **Method**: `POST`
- **Endpoint**: `/api/transactions/transfer`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>`
- **Request Payload**:
  ```json
  {
    "sourceAccountNumber": "SBP1001",
    "destinationAccountNumber": "SBP2001",
    "amount": 150.00
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Transferred 150.00 USD from SBP1001 to SBP2001 successfully."
  }
  ```

---

### 6️⃣ Account Detail & Transaction History Page
Displays account details and lists transaction logs.

#### **Step A: Get Account Info**
- **Method**: `GET`
- **Endpoint**: `/api/accounts/{accountNumber}`
  - Example: `/api/accounts/SBP1001`
- **Headers**:
  - `Authorization: Bearer <token>`
- **Response (200 OK)**:
  ```json
  {
    "accountId": 1,
    "accountNumber": "SBP1001",
    "balance": 1000.00,
    "accountType": "SAVINGS",
    "active": true
  }
  ```

#### **Step B: Get Transactions Logs**
- **Method**: `GET`
- **Endpoint**: `/api/transactions/history/{accountNumber}`
  - Example: `/api/transactions/history/SBP1001`
- **Headers**:
  - `Authorization: Bearer <token>`
- **Response (200 OK)**:
  ```json
  [
    {
      "transactionId": 12,
      "referenceNumber": "TXN-FD3644BE",
      "transactionType": "DEPOSIT",
      "sourceAccount": {
        "accountNumber": "SBP1001"
      },
      "destinationAccount": null,
      "amount": 150.00,
      "createdAt": "2026-06-27T01:03:42.000+00:00"
    }
  ]
  ```

---

### 7️⃣ Admin Panel Page (Requires ROLE_ADMIN Token)
Restricted options accessible only by administrators.

#### **A. Freeze Account**
Temporarily disables an account, rejecting all deposits, withdrawals, and transfers.
- **Method**: `PUT`
- **Endpoint**: `/api/admin/accounts/{accountNumber}/freeze`
- **Headers**:
  - `Authorization: Bearer <token>`
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Account SBP1001 has been frozen successfully."
  }
  ```

#### **B. Unblock / Unfreeze Account**
Re-activates a frozen account.
- **Method**: `PUT`
- **Endpoint**: `/api/admin/accounts/{accountNumber}/unblock`
- **Headers**:
  - `Authorization: Bearer <token>`
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Account SBP1001 has been unblocked successfully."
  }
  ```

#### **C. Delete User Profile**
Permanently removes a user and cascades to delete all their accounts.
- **Method**: `DELETE`
- **Endpoint**: `/api/admin/users/{id}`
  - Example: `/api/admin/users/4`
- **Headers**:
  - `Authorization: Bearer <token>`
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "User with ID 4 has been deleted successfully."
  }
  ```

---

## ⚡ JavaScript API Client Template (`api.js`)

You can copy and drop this helper library directly into your frontend project to easily communicate with the backend:

```javascript
const BASE_URL = "http://localhost:8080";

// Helper to retrieve the JWT token
function getAuthHeader() {
  const token = localStorage.getItem("authToken");
  return token ? { "Authorization": `Bearer ${token}` } : {};
}

// Global request wrapper handles JSON extraction & unauthorized redirection
async function request(endpoint, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...getAuthHeader(),
    ...options.headers
  };

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers
  });

  if (response.status === 401) {
    console.warn("Session expired or unauthorized. Redirecting to login...");
    localStorage.removeItem("authToken");
    window.location.href = "/login.html";
    throw new Error("Unauthorized");
  }

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || "Something went wrong");
  }
  return data;
}

// Authentication API
export const authAPI = {
  register: (fullName, email, password) => 
    request("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({ fullName, email, password })
    }),

  login: async (email, password) => {
    const data = await request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password })
    });
    if (data.token) {
      localStorage.setItem("authToken", data.token);
    }
    return data;
  },

  logout: () => {
    localStorage.removeItem("authToken");
    window.location.href = "/login.html";
  },

  checkSession: () => request("/api/auth/session")
};

// Account API
export const accountAPI = {
  list: () => request("/api/accounts"),
  getDetails: (accountNum) => request(`/api/accounts/${accountNum}`),
  create: (accountNumber, accountType, initialBalance) => 
    request("/api/accounts/create", {
      method: "POST",
      body: JSON.stringify({ accountNumber, accountType, balance: initialBalance })
    })
};

// Transactions API
export const transactionAPI = {
  deposit: (accountNumber, amount) => 
    request("/api/transactions/deposit", {
      method: "POST",
      body: JSON.stringify({ accountNumber, amount })
    }),

  withdraw: (accountNumber, amount) => 
    request("/api/transactions/withdraw", {
      method: "POST",
      body: JSON.stringify({ accountNumber, amount })
    }),

  transfer: (sourceAccountNumber, destinationAccountNumber, amount) => 
    request("/api/transactions/transfer", {
      method: "POST",
      body: JSON.stringify({ sourceAccountNumber, destinationAccountNumber, amount })
    }),

  getHistory: (accountNum) => request(`/api/transactions/history/${accountNum}`)
};

// Admin API (Requires Admin token credentials)
export const adminAPI = {
  freeze: (accountNum) => 
    request(`/api/admin/accounts/${accountNum}/freeze`, { method: "PUT" }),

  unblock: (accountNum) => 
    request(`/api/admin/accounts/${accountNum}/unblock`, { method: "PUT" }),

  deleteUser: (userId) => 
    request(`/api/admin/users/${userId}`, { method: "DELETE" })
};
```
