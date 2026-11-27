# 🛡️ DummyJSON API Security & Authentication Test Automation Framework

Automated backend test suite built with **Java**, **REST Assured**, **TestNG**, and **Allure Reports**. This framework validates the complete JWT (JSON Web Token) authentication lifecycle, session renewal mechanisms, and critical security controls against the [DummyJSON Auth API](https://dummyjson.com).

[![API Security & Auth Automation CI](https://github.com/your-org/dummyJsonRestAssured/actions/workflows/ci.yml/badge.svg)](https://github.com/your-org/dummyJsonRestAssured/actions/workflows/ci.yml)
[![Allure Live Report](https://img.shields.io/badge/Allure-Report-brightgreen)](https://your-org.github.io/dummyJsonRestAssured/)
![Java](https://img.shields.io/badge/Java-25-blue)
![REST Assured](https://img.shields.io/badge/REST%20Assured-5.4.0-green)
![TestNG](https://img.shields.io/badge/TestNG-7.9.0-red)

> 📊 **Live Test Execution Report:** View the latest interactive Allure Dashboard with full HTTP request/response logs and defect tracking [**here**](https://your-org.github.io/dummyJsonRestAssured/).

---

## 🎯 Project Objectives & Risk Coverage

Rather than limiting coverage to basic happy-path assertions, this suite is engineered using **Risk-Based Testing** principles to verify:

1. **Operational Continuity:** Ensuring valid users can authenticate, access protected resources, and seamlessly refresh expiring sessions.
2. **Security Compliance (OWASP API Top 10):** Proactively testing for **OWASP API2:2023 (Broken Authentication)** vulnerabilities, such as token tampering, scheme enforcement (RFC 6750), and token confusion attacks.
3. **Contract Compliance:** Validating API responses against the OpenAPI specification and JSON Schema definitions.

---

## 🧪 Test Design Methodologies

Test scenarios were systematically derived using industry-standard black-box and security testing techniques:

* **Equivalence Partitioning (ISTQB):** Input conditions are divided into valid classes (well-formed `accessToken` and `refreshToken`) and invalid classes (missing `Authorization` header, malformed signatures, raw tokens without the `Bearer` prefix).
* **State Transition Testing (ISTQB):** Validates the full token lifecycle across state changes: `Unauthenticated` → `Authenticated (Active)` → `Session Renewed` → `Resource Access`.
* **Security & Negative Testing (OWASP API2 / RFC 6750):** Evaluates server resilience against manipulated JWT payloads/signatures and cross-token misuse.
* **Contract Testing:** Validates request/response payloads against OpenAPI 3.0 specification and JSON Schema definitions.

---

## 📋 Test Suite & Defect Findings

The test suite (`AuthTests.java`) covers **9 core scenarios** and has successfully uncovered **3 real security/handling defects** in the target public API:

| Test Case | Category | Methodology | Expected Result | Status / Finding |
| :--- | :--- | :--- | :--- | :--- |
| `verifyPostValidTokensGenerationReturns200WhenValidLoginCredentials` | Positive | Equivalence Partitioning | `200 OK` + Valid JWT format (`Header.Payload.Signature`) | ✅ **PASS** |
| `verifyGetAuthorizedAccessReturns200WhenValidLoginCredentials` | Positive | State Transition | `200 OK` + Authenticated user profile data | ✅ **PASS** |
| `verifyPostSessionRenovationReturns200WhenValidRefreshToken` | Positive | State Transition | `200 OK` + New distinct `accessToken` | ✅ **PASS** |
| `verifyGetMeReturns200WhenUsingRefreshedAccessToken` | Positive | State Transition | `200 OK` using the newly refreshed token | ✅ **PASS** |
| `verifyGetMeReturns401WhenRequestWithoutAuthorizationHeader` | Negative | OWASP API2 | `401 Unauthorized` without leaking user data | ✅ **PASS** |
| `verifyPostRefreshReturns403WhenUsingFakeRefreshToken` | Negative | OWASP API2 | `401/403` rejection on forged `refreshToken` | ✅ **PASS** |
| `verifyGetMeReturns401WhenTokenIsTampered` | Security | Token Tampering | `401 Unauthorized` (`invalid signature`) | 🐛 **BUG-01:** Server throws unhandled `500 Internal Server Error` |
| `verifyGetMeReturns401WhenBearerWordIsMissing` | Security | RFC 6750 Compliance | `401 Unauthorized` when `Bearer ` prefix is omitted | 🐛 **BUG-02:** Server accepts raw token (`200 OK`) |
| `verifyGetMeReturns401WhenUsingRefreshTokenInsteadOfAccessToken` | Security | Token Confusion | `401 Unauthorized` when using `refreshToken` on resource endpoints | 🐛 **BUG-03:** Server accepts `refreshToken` as `accessToken` (`200 OK`) |

---

## 🏗️ Project Architecture

```text
dummyJsonRestAssured/
├── src/
│   └── test/
│       ├── java/
│       │   ├── base/
│       │   │   └── BaseTest.java              # Centralized RequestSpecification, OpenAPI validation, Allure filter & failure logging
│       │   ├── pojo/
│       │   │   ├── LoginRequest.java          # Login payload (username, password, optional expiresInMins)
│       │   │   ├── LoginResponse.java         # Full login response with user data + tokens
│       │   │   ├── RefreshRequest.java        # Refresh payload (refreshToken, expiresInMins)
│       │   │   ├── RefreshResponse.java       # Refresh response (new accessToken, refreshToken)
│       │   │   ├── UserResponse.java          # Complete user profile with nested records (Address, Hair, Bank, Company, Crypto)
│       │   │   └── ErrorResponse.java         # Error response wrapper (message)
│       │   ├── tests/
│       │   │   └── AuthTests.java             # Positive, negative & OWASP security test scenarios (9 tests)
│       │   └── utils/
│       │       └── ConfigReader.java          # Externalized configuration & credentials reader
│       └── resources/
│           ├── config.properties              # Environment properties (base URI, test credentials)
│           ├── testng.xml                     # TestNG suite configuration
│           ├── openapi.json                   # OpenAPI 3.0 contract for DummyJSON Auth API
│           └── schemas/
│               ├── login-response-schema.json     # JSON Schema for login response
│               ├── me-response-schema.json        # JSON Schema for /auth/me response
│               ├── me-401-response-schema.json    # JSON Schema for 401 error on /auth/me
│               ├── refresh-response-schema.json   # JSON Schema for refresh response
│               └── refresh-401-response-schema.json # JSON Schema for 401/403 error on refresh
├── pom.xml                                    # Maven dependencies & Surefire/Allure plugins
├── .github/workflows/ci.yml                   # GitHub Actions CI pipeline with Allure report deployment
└── README.md
```

---

## 🔑 Key Framework Highlights

### 🎯 **Reusable RequestSpecification**
Eliminates boilerplate code across test classes by centralizing base URIs, headers, OpenAPI validation filter, and Allure filter in `BaseTest`.

### 📝 **Clean Console Logs**
Uses `RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()` to keep CI/CD logs clean while printing full HTTP traffic upon assertion failures.

### 📄 **Living Documentation (Allure)**
Integrated with Allure Annotations (`@Epic`, `@Feature`, `@Story`, `@Severity`, `@Description`, `@Issue`, `@Step`) to generate interactive executive reports with attached HTTP request/response payloads.

### 🔒 **OpenAPI Contract Validation**
Leverages **Atlassian OAI Validator** to validate every request/response against the OpenAPI 3.0 specification (`openapi.json`). Configured to:
- Ignore `additionalProperties` warnings (known issues in DummyJSON contract)
- Treat security validation issues as warnings for negative/security tests

### ✅ **JSON Schema Validation**
Validates response payloads against JSON Schema definitions in `src/test/resources/schemas/` for:
- Login response
- User profile (`/auth/me`) response
- Token refresh response
- Error responses (401/403)

### 🧩 **Type-Safe POJOs (Java Records)**
All API payloads modeled as immutable Java Records with Jackson annotations for seamless serialization/deserialization. Nested records for complex objects (Address, Hair, Bank, Company, Crypto).

### ⚙️ **Externalized Configuration**
Environment-specific properties managed via `config.properties` and accessed through thread-safe `ConfigReader` utility.

---

## ⚙️ Tech Stack & Prerequisites

| Component | Version |
| :--- | :--- |
| **Java** | **JDK 25** (configured via `maven.compiler.source/target`) |
| **Build Tool** | Apache Maven 3.8+ |
| **API Client** | REST Assured 5.4.0 |
| **Test Runner** | TestNG 7.9.0 |
| **Reporting** | Allure Framework 2.29.1 + AspectJ Weaver 1.9.27 |
| **Contract Validation** | Atlassian OAI Validator 3.0.0 |
| **Schema Validation** | REST Assured JSON Schema Validator 5.4.0 |
| **JSON Processing** | Jackson 2.17.0 (BOM managed) |

---

## 🚀 Setup & Execution

### 1. Clone the repository
```bash
git clone https://github.com/your-org/dummyJsonRestAssured.git
cd dummyJsonRestAssured
```

### 2. Configure environment properties
Ensure `src/test/resources/config.properties` contains the target configuration:
```properties
base.uri=https://dummyjson.com
username=sophiab
password=sophiabpass
```

> **Note:** The default credentials (`sophiab` / `sophiabpass`) are valid test users provided by DummyJSON.

### 3. Run the test suite
Execute all tests via Maven:
```bash
mvn clean test
```
> **Note:** The build will report **3 failures** corresponding to the documented **BUG-01, BUG-02, and BUG-03** security findings. These are *expected* failures that demonstrate real vulnerabilities in the target API.

### 4. Generate & View Allure Report
Launch the interactive HTML report in your default browser:
```bash
mvn allure:serve
```

Or generate a static report:
```bash
mvn allure:report
```
The report will be available at `target/site/allure-maven/index.html`.

---

## 🔄 Continuous Integration (GitHub Actions)

The project includes a complete CI pipeline (`.github/workflows/ci.yml`) that:

1. **Checks out** the repository
2. **Sets up JDK 25** (Temurin distribution with Maven caching)
3. **Runs the test suite** with `continue-on-error: true` to ensure Allure report generation even when known security bugs fail
4. **Generates Allure Report with History** using `simple-elf/allure-report-action`
5. **Deploys to GitHub Pages** (`gh-pages` branch) for live report access

### Required Repository Settings
- Enable **GitHub Pages** → Source: `Deploy from a branch` → Branch: `gh-pages` / `/ (root)`
- The workflow requires `contents: write` permission (enabled by default for `GITHUB_TOKEN`)

---

## 📊 Allure Report Features

The generated Allure report includes:

- **Executive Dashboard** with pass/fail trends, duration graphs, and severity distribution
- **Test Case Details** with step-by-step execution logs
- **HTTP Request/Response Attachments** for every API call (headers, body, cookies)
- **Defect Tracking** via `@Issue` annotations linked to BUG-01, BUG-02, BUG-03
- **Category Classification** (Positive, Negative, Security) via `@Story` and `@Epic`
- **History & Trend Analysis** across pipeline runs

---

## 🐛 Known Issues & Security Findings

| ID | Title | Severity | Description |
| :--- | :--- | :--- | :--- |
| **BUG-01** | Unhandled `JsonWebTokenError` on signature tampering | **Critical** | Tampering the JWT signature (last character) causes HTTP 500 instead of 401. Server leaks stack trace. |
| **BUG-02** | Missing `Bearer` scheme enforcement (RFC 6750 violation) | **Normal** | Server accepts raw token without `Bearer ` prefix, violating OAuth 2.0 Bearer Token specification. |
| **BUG-03** | Token confusion: `refreshToken` accepted as `accessToken` | **Critical** | Refresh tokens can be used to access protected resources (`/auth/me`), breaking token purpose isolation. |

> These issues are documented in the test code with `@Issue` annotations and appear in the Allure report under the **Defects** tab.

---

## 📁 Configuration Files

### `config.properties`
```properties
base.uri=https://dummyjson.com
username=sophiab
password=sophiabpass
```

### `testng.xml`
Minimal configuration - tests are discovered via TestNG annotations. Can be extended for parallel execution, groups, or suite parameters.

### `openapi.json`
Local copy of DummyJSON Auth API OpenAPI 3.0 specification used for contract validation. Can be updated by downloading from `https://dummyjson.com/openapi.json`.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [DummyJSON](https://dummyjson.com) for providing a free, public API for testing
- [REST Assured](https://rest-assured.io) for the fluent API testing DSL
- [Allure Framework](https://allurereport.org) for the beautiful reporting
- [Atlassian OAI Validator](https://github.com/atlassian/openapi-request-validator) for contract testing
- [TestNG](https://testng.org) for the powerful testing framework