# 🎓 UniLink - Student Affairs Management System (Backend)

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-green)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-lightblue)

---

## 📋 Overview

**UniLink** is a digital backend solution designed to streamline communication and service delivery between **students** and **university administration**.  
Built with **Java Spring Boot**, it offers secure authentication, request tracking, appointment management, and document handling through a robust RESTful API.

---

## 🎯 Key Features

- 🔐 Dual-role authentication (Student & Staff)
- 📨 Service request management (Submit, track, update)
- 📅 Appointment scheduling with conflict prevention
- 🔔 Real-time notifications
- 📂 Document upload & management
- 🧩 Role-based access control
- 🌐 RESTful API architecture

---

## 🛠 Tech Stack

| Category | Technologies |
|-----------|---------------|
| **Backend** | Java 17, Spring Boot 3.0 |
| **Security** | Spring Security, JWT |
| **Database** | MySQL 8.0, JPA, Hibernate |
| **Documentation** | Swagger / OpenAPI |
| **Testing** | JUnit, Mockito, Postman |
| **Build Tool** | Maven |

---

## 🏗 System Architecture
```text

Controller Layer  (REST APIs)
        ↓
Service Layer     (Business Logic)
        ↓
Repository Layer  (Data Access)
        ↓
Database          (MySQL)
```


## 📁 Project Structure
```text
src/
├── main/
│   ├── java/com/unilink/
│   │   ├── controller/     # REST API endpoints
│   │   ├── service/        # Business logic layer
│   │   ├── repository/     # Data access layer
│   │   ├── entity/         # JPA entities
│   │   ├── dto/            # Data transfer objects
│   │   ├── security/       # Authentication & authorization
│   │   └── config/         # Application configuration
│   └── resources/
│       ├── application.properties
│       └── data.sql        # Initial data
```

