# 🛡️ DummyJSON API Security & Authentication Test Automation Framework

Automated backend test suite built with **Java**, **REST Assured**, **TestNG**, and **Allure Reports**. This framework validates the complete JWT (JSON Web Token) authentication lifecycle, session renewal mechanisms, and critical security controls against the [DummyJSON Auth API](https://dummyjson.com).

[![API Security & Auth Automation CI](https://github.com)](https://github.com)
[![Allure Live Report](https://shields.io)](https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/)
![Java](https://shields.io)
![REST Assured](https://shields.io)
![TestNG](https://shields.io)

> 📊 **Live Test Execution Report:** View the latest interactive Allure Dashboard with full HTTP request/response logs and defect tracking [**here**](https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/).

---

## 🎯 Project Objectives & Risk Coverage

Rather than limiting coverage to basic happy-path assertions, this suite is engineered using **Risk-Based Testing** principles to verify:
1. **Operational Continuity:** Ensuring valid users can authenticate, access protected resources, and seamlessly refresh expiring sessions.
2. **Security Compliance (OWASP API Top 10):** Proactively testing for **OWASP API2:2023 (Broken Authentication)** vulnerabilities, such as token tampering, scheme enforcement (RFC 6750), and token confusion attacks.

---

## 🧪 Test Design Methodologies

Test scenarios were systematically derived using industry-standard black-box and security testing techniques:

* **Equivalence Partitioning (ISTQB):** Input conditions are divided into valid classes (well-formed `accessToken` and `refreshToken`) and invalid classes (missing `Authorization` header, malformed signatures, raw tokens without the `Bearer` prefix).
* **State Transition Testing (ISTQB):** Validates the full token lifecycle across state changes: `Unauthenticated` → `Authenticated (Active)` → `Session Renewed` → `Resource Access`.
* **Security & Negative Testing (OWASP API2 / RFC 6750):** Evaluates server resilience against manipulated JWT payloads/signatures and cross-token misuse.

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
│       │   │   └── BaseTest.java          # Centralized RequestSpecification, failure logging & Allure filter
│       │   ├── tests/
│       │   │   └── AuthTests.java         # Positive, negative & OWASP security test scenarios
│       │   └── utils/
│       │       └── ConfigReader.java      # Externalized configuration & credentials reader
│       └── resources/
│           └── config.properties          # Environment properties (base URI, test credentials)
├── pom.xml                                # Maven dependencies & Surefire/Allure plugins
└── README.md



Key Framework Highlights

• Reusable RequestSpecification: Eliminates boilerplate code across test classes by centralizing base URIs, headers, and filters in BaseTest.
• Clean Console Logs: Uses RestAssured.enableLoggingOfRequestAndResponseIfValidationFails() to keep CI/CD logs clean while printing full HTTP traffic upon assertion failures.
• Living Documentation: Integrated with Allure Annotations (@Epic, @Feature, @Story, @Severity, @Description, @Issue, @Step) to generate interactive executive reports with attached HTTP request/response payloads.

⚙️ Tech Stack & Prerequisites

• Java: JDK 21+ (Configured for Java 25)
• Build Tool: Apache Maven 3.8+
• API Client: REST Assured 5.4.0
• Test Runner: TestNG 7.9.0
• Reporting: Allure Framework 2.29.1 + AspectJ Weaver

🚀 Setup & Execution


1. Clone the repository

bash
git clone https://github.com
cd dummyJsonRestAssured


2. Configure environment properties

Ensure src/test/resources/config.properties contains the target configuration:
properties
base.uri=https://dummyjson.com
username=emilys
password=emilyspass


3. Run the test suite

Execute all tests via Maven:
bash
mvn clean test
(Note: The build will report 3 failures corresponding to the documented BUG-01, BUG-02, and BUG-03 security findings).


4. Generate & View Allure Report

Launch the interactive HTML report in your default browser:
bash
mvn allure:serve