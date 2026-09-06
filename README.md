# Employee Management System

## Project Overview

The Employee Management System is a comprehensive, full-stack Spring Boot web application designed for HR administrators and employees. It provides a centralized dashboard to manage employee records, organizational departments, and user roles. Built with a server-rendered Thymeleaf interface and a robust Spring Security backend, it offers a secure and efficient way to handle workforce data.

## Features

- **Authentication**: Secure login/logout using Spring Security.
- **Role-based authorization**: Differentiated access controls for `ADMIN` and `USER` roles.
- **Employee management**: Create, read, update, and delete (CRUD) operations for employee records.
- **Department management**: Manage organizational departments and associate them with employees.
- **Search/filter**: Advanced filtering of employees by name, department, and employment status.
- **Dashboard**: Real-time Key Performance Indicators (KPIs) and recent activity metrics.
- **Validation**: Strict server-side validation using Hibernate Validator and Bean Validation.
- **Error handling**: Custom global exception handler with friendly error pages (403 Forbidden, 404 Not Found, 500 Internal Server Error).

## Technology Stack

- **Backend Framework**: Java 17, Spring Boot 3.x
- **Data Access**: Spring Data JPA, Hibernate
- **Database**: MySQL 8+
- **Security**: Spring Security (BCrypt Password Hashing, CSRF Protection)
- **Frontend Template Engine**: Thymeleaf
- **UI Framework**: Bootstrap 5 (Responsive Design)
- **Icons**: Bootstrap Icons
- **Build Tool**: Maven

## Project Structure

The codebase follows standard Domain-Driven Design (DDD) layered architecture:

- `entity`: JPA Entity classes mapped to database tables (`Employee`, `Department`, `User`).
- `repository`: Spring Data JPA interfaces for database operations.
- `service`: Business logic interfaces and implementations (`EmployeeService`, `DepartmentService`).
- `controller`: Spring MVC controllers handling HTTP requests and model mapping.
- `config`: Configuration classes, particularly for `SecurityConfig` and `AdminUserInitializer`.
- `dto`: Data Transfer Objects for form binding and validation.
- `exception`: Custom exceptions and `@ControllerAdvice` global error handling.

## Database Setup

To run this application locally, you must create a MySQL database named `employee_management`.

1. Open your MySQL client or command line.
2. Execute the following SQL command:
   ```sql
   CREATE DATABASE employee_management;
   ```
3. The application uses Spring Data JPA to automatically generate and update the necessary tables based on the entity definitions.

## Configuration

Sensitive configuration such as database credentials and the default admin user account should be provided via environment variables. The application falls back to local development defaults if these are not set.

**Environment Variables:**
- `DB_URL`: The JDBC URL (default: `jdbc:mysql://localhost:3306/employee_management`)
- `DB_USERNAME`: The database username (default: `root`)
- `DB_PASSWORD`: The database password 
- `APP_ADMIN_USERNAME`: The initial admin username to seed the database
- `APP_ADMIN_PASSWORD`: The password for the initial admin account

*Note: Never hardcode production credentials in `application.properties`.*

## Running the Application

To run the application locally, use the provided Maven wrapper:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

The application will be accessible at `http://localhost:8080`.

## Testing

The project includes a comprehensive suite of unit and integration tests covering the service layer, controllers, security, and validation rules.

To execute the test suite, run:

```bash
# Windows
.\mvnw.cmd clean test

# Linux/macOS
./mvnw clean test
```

## User Roles

The system recognizes two primary user roles:

1. **ADMIN**: Full access to the system. Can create, edit, and delete employees and departments. Has access to all dashboard metrics.
2. **USER**: Read-only access. Can view the dashboard, employee lists, and department lists, but is restricted from performing any modifications.
