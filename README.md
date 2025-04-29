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
- **Validation** – To ensure data integrity using annotations like `@NotNull`, `@Size`, etc

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


