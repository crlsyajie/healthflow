# 🏥 Simple Health Management System

A basic Health Management System built with **Spring Boot** and **Maven**.  
It allows you to manage patients, doctors, and appointments using a RESTful API and optionally a simple web interface.

---

## 🚀 Features

- Add / update / delete patients
- Register and manage doctors
- Schedule appointments
- View appointment history
- Simple web interface using Thymeleaf (optional)
- RESTful API endpoints

---

## 🧩 Dependencies Used

This project uses the following Spring Boot dependencies:

- **Lombok** – Reduces boilerplate code (getters/setters, constructors)
- **Spring Web** – To build RESTful web services and controllers
- **Thymeleaf** – For server-side HTML rendering (optional UI)
- **Spring Security** – For user authentication and security layers
- **JDBC API** – Low-level database interaction (optional with JPA)
- **Spring Data JPA** – Simplifies database access using repositories
- **MySQL Driver** – For connecting the app to a MySQL database
- **Validation** – To ensure data integrity using annotations like `@NotNull`, `@Size`, etc.
- **CycloneDX SBOM support** – For generating a Software Bill of Materials (helps with software supply chain security)


## 🔐 Role-Based Access Control (RBAC)

The system supports three user roles with different access levels:

- **👨‍⚕️ Doctor**
  - View assigned patients
  - Manage appointment schedules
  - Access patient medical records

- **🧍 Patient**
  - View and update personal info
  - Book and cancel appointments
  - See upcoming & past appointments

- **🛡️ Admin**
  - Full access to all system features
  - Manage doctors and patients
  - View system-wide analytics
  - Assign roles and permissions


# HealthFlow Setup Guide

## Part 1: MySQL Setup

### Step 1: Download and Install MySQL
1. Go to MySQL official website: https://dev.mysql.com/downloads/installer/
2. Download "MySQL Installer for Windows"
3. Run the installer and follow these steps:
   - Choose "Developer Default" installation type
   - Click "Next" through the installation process
   - When prompted, set a root password (IMPORTANT: Remember this password!)
   - Complete the installation

### Step 2: Verify MySQL Installation
1. Open MySQL Command Line Client from Start Menu
2. Enter your root password when prompted
3. You should see the MySQL prompt: `mysql>`

### Step 3: Create Database
1. In the MySQL Command Line Client, run:
```sql
CREATE DATABASE healthflow;
```
2. Verify the database was created:
```sql
SHOW DATABASES;
```
You should see `healthflow` in the list.

### Step 4: Configure Application Properties
1. Open `healthflow/src/main/resources/application.properties`and open healthflow/target/classes/application.properties
2. Update the MySQL configuration with your root password:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/healthflow?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_ROOT_PASSWORD
```
Replace `YOUR_ROOT_PASSWORD` with the password you set during MySQL installation.

## Part 2: Java Development Environment Setup

### Step 1: Install Java Development Kit (JDK)
1. Download JDK 17 from: https://www.oracle.com/java/technologies/downloads/#java17
2. Run the installer
3. Set JAVA_HOME environment variable:
   - Open System Properties > Advanced > Environment Variables
   - Add new System Variable:
     - Variable name: `JAVA_HOME`
     - Variable value: `C:\Program Files\Java\jdk-17` (or your JDK installation path)

### Step 2: Install Maven
1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to a directory (e.g., `C:\Program Files\Apache\maven`)
3. Add Maven to PATH:
   - Add new System Variable:
     - Variable name: `MAVEN_HOME`
     - Variable value: `C:\Program Files\Apache\maven` (or your Maven installation path)
   - Add to PATH: `%MAVEN_HOME%\bin`

## Part 3: Running the Application

### Step 1: Build the Project
1. Open Command Prompt
2. Navigate to project directory:
```bash
cd path\to\healthflow-main
```
3. Build the project:
```bash
mvn clean install
```

### Step 2: Run the Application
1. In the same directory, run:
```bash
mvn spring-boot:run
```
2. The application should start on port 9090
3. Access the application at: http://localhost:9090

## Troubleshooting Common Issues

### MySQL Connection Issues
- If you get "Access denied" error:
  1. Verify MySQL is running (check Services on Windows)
  2. Confirm root password in application.properties
  3. Try connecting with MySQL Workbench to test credentials

### Java/Maven Issues
- If Maven commands aren't recognized:
  1. Verify JAVA_HOME is set correctly
  2. Verify Maven is in PATH
  3. Restart Command Prompt after setting environment variables

### Application Startup Issues
- If application fails to start:
  1. Check MySQL is running
  2. Verify database 'healthflow' exists
  3. Check application.properties configuration
  4. Look for error messages in console output

## Additional Notes
- Make sure all services (MySQL, Java) are running before starting the application
- Keep your MySQL root password secure
- The application runs on port 9090 by default
- For any issues, check the console output for detailed error messages 
