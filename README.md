# 🎓 Student Fee Management System (JavaFX & MySQL)

This is a comprehensive desktop application developed using JavaFX for the front-end and MySQL for data persistence. It allows for dual roles: Admin and Student.

---

## 🚀 1. Requirements

To run this project, you need the following installed:

* **Java Development Kit (JDK):** Version 21 (or newer LTS version).
* **JavaFX SDK:** Version 21 (or compatible version).
* **MySQL Database Server:** MySQL 8.0 or compatible.
* **MySQL JDBC Connector:** (The JAR is provided in the `/libs` folder).

---

## 🛠️ 2. Setup Instructions

Follow these steps to set up the database and run the application.

### A. Database Setup (CRITICAL!)

1.  **Start MySQL Server:** Ensure your local MySQL server is running.
2.  **Run SQL Script:** Open your MySQL client (or command line) and execute the entire `database_setup.sql` script provided in this repository. This script will:
    * Create the database named `student_fees_db`.
    * Create all 5 required tables (`users`, `students`, `fees`, `transactions`, `studentfees`).
    * Insert the initial user data and fee templates.

### B. IDE Setup (IntelliJ IDEA Recommended)

1.  **Open Project:** Open the project folder in your IDE.
2.  **Configure JDK:** Set the Project SDK to **JDK 21**.
3.  **Add JavaFX Libraries:** Configure the project to use the local JavaFX SDK libraries as modules, pointing to your local JavaFX SDK installation path.
4.  **Add JAR Dependency:** Ensure the `mysql-connector-j-8.x.x.jar` file in the `/libs` folder is added to your project's module path or build path.

### C. Update Database Credentials

The application connects using hardcoded credentials. If your local MySQL setup uses different details than the defaults below, you must update the file:

* **File:** `utility/DBConnection.java`
* **Default Connection:**
    * `URL`: `jdbc:mysql://localhost:3306/student_fees_db`
    * `USERNAME`: `root`
    * `PASSWORD`: `[Your root password or a dedicated user's password]`

---

## 🔑 3. Default Login Credentials

Use the following credentials to test the application immediately after running the SQL setup script:

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Full access to management views (Fees, Students, Transactions). |
| **Student** | `alice` | `testpass` | View fee status and transaction history for Student ID 1. |
