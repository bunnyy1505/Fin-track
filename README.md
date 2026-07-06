# FinTrack – Personal Finance Dashboard

FinTrack is a modern, production-ready Full Stack Personal Finance Management System. It enables users to securely track income, expenses, budgets, recurring transactions, and financial analytics through an interactive single-page dashboard.

---

## Technical Architecture & Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.x** (REST Controller API layer)
- **Spring Security & JWT** (Stateless authentication/authorization with access and refresh tokens)
- **Spring Data JPA & Hibernate** (Data persistence mapping)
- **MySQL 8** (Production DBMS) / **H2** (In-memory testing DBMS)
- **Clean Java POJOs** (Lombok-free for standard portability)
- **Bean Validation** (Entity constraint checks)
- **ModelMapper** (DTO / Entity translator)
- **OpenAPI 3 / Swagger** (Interactive REST documentation)
- **Apache PDFBox** (PDF report exports)
- **Apache Commons CSV** (CSV report exports)
- **SLF4J** (System activity logging)

### Frontend
- **HTML5 & CSS3** (Vanilla UI styling)
- **Bootstrap 5** (Responsive layout components)
- **JavaScript (ES6)** (Client-side routing, API fetching, JWT token validation)
- **Chart.js** (Visual interactive graphs)

---

## Features
1. **JWT Auth Security**: Stateless session management with robust signup, login, logout, password change, deactivation, and token refresh.
2. **Income Module**: CRUD operations with pagination, sorting, search, date range filters, and monthly summaries.
3. **Expense Module**: Log expenses with categories, and trigger automatic budget exceeded notifications.
4. **Budget Planner**: Limit settings with real-time spent calculation, progress bars, and alerts.
5. **Scheduled Recurrences**: Daily/Weekly/Monthly transaction auto-insertion with Spring Scheduler checking.
6. **Alert Notification Widget**: Upcoming bills and budget warnings mapped in the navigation dropdown.
7. **Visual Analytics**: Dynamic Cash Flow bar charts, category breakdowns, budget lines, and variance maps via Chart.js.
8. **Exportable Reports**: Generate summaries for custom periods and download as PDF or CSV.

---

## Directory Structure
```
fintrack/
├── src/
│   ├── main/
│   │   ├── java/com/fintrack/
│   │   │   ├── config/              # Infrastructure and Beans Config
│   │   │   ├── security/            # Security Filters and Providers
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── service/             # Services Interfaces
│   │   │   │   └── impl/            # Business Logic Implementations
│   │   │   ├── repository/          # JPA Repositories
│   │   │   ├── entity/              # Database JPA Mapping Entities
│   │   │   ├── dto/                 # Request/Response Data Objects
│   │   │   ├── exception/           # Custom Exceptions & Controllers Advice
│   │   │   ├── response/            # Standardized API response format
│   │   │   ├── scheduler/           # Spring Scheduled Tasks
│   │   │   └── mapper/
│   │   └── resources/
│   │       ├── static/              # Frontend Assets
│   │       │   ├── css/             # Custom style files
│   │       │   └── js/              # Client-side router and fetch logic
│   │       ├── application.properties
│   │       ├── application-h2.properties
│   │       ├── application-mysql.properties
│   │       └── data.sql             # SQL Data Seed file for local run
├── database.sql                     # MySQL database schema setup script
├── fintrack_postman_collection.json # API endpoint testing collection
├── pom.xml                          # Maven build script dependencies
└── README.md
```

---

## Installation & Setup

### Prerequisites
- **JDK 21** or later.
- **Maven** (A portable version is included in local builds).

### Running Locally (Embedded H2 Profile)
By default, the project runs with an H2 database profile. All tables are automatically created and seeded with test data on boot.
1. Clean and compile the application:
   ```bash
   mvn clean package
   ```
2. Start the application:
   ```bash
   mvn spring-boot:run
   ```
3. Open your browser and navigate to `http://localhost:8080`.
4. Log in using the seeded test credentials:
   - **Username**: `user`
   - **Password**: `password`

### Switching to MySQL Profile
1. Create a MySQL database named `fintrack`.
2. Open `src/main/resources/application.properties` and change:
   ```properties
   spring.profiles.active=mysql
   ```
3. Update connection credentials in `application-mysql.properties`.
4. Run standard Maven startup:
   ```bash
   mvn spring-boot:run
   ```

---

## API Documentation
Once running, explore complete swagger documentation at:
`http://localhost:8080/swagger-ui.html`
