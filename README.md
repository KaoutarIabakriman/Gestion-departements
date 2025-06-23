# GestionDepartements - Department Management System

`GestionDepartements` is a Java Spring Boot application designed for managing academic departments, modules, teachers, workloads, and internal voting processes. It provides role-based access for Administrators, Department Heads (Chefs de Département), and Teachers (Enseignants).

## Table of Contents

1.  [Overview](#overview)
2.  [Features](#features)
3.  [Technologies Used](#technologies-used)
4.  [Project Structure](#project-structure)
5.  [Setup and Installation](#setup-and-installation)
6.  [Usage](#usage)
    *   [User Roles](#user-roles)
    *   [Key Endpoints](#key-endpoints)
7.  [Architectural Notes](#architectural-notes)
8.  [Future Enhancements (TODO)](#future-enhancements-todo)
9.  [Contributing](#contributing)
10. [License](#license)

## Overview

The system aims to streamline various administrative and academic tasks within an educational institution's departmental structure. Key functionalities include:

*   Managing departments, their members, and heads.
*   Overseeing academic modules, their assignment to departments and teachers.
*   Tracking teacher workloads.
*   Facilitating a voting system for electing department heads.
*   Providing a notification system for important events.
*   Allowing teachers to manage their profiles and request module assignments.

## Features

*   **User Authentication & Authorization:**
    *   Secure login using Spring Security.
    *   Role-based access control (ADMIN, DEPARTMENT_HEAD, ENSEIGNANT).
    *   Custom success handlers for role-based redirection after login.
*   **Admin-Specific Features:**
    *   CRUD operations for Departments.
    *   Initiate votes for Department Heads.
    *   View and manage all Teachers and their workloads (filterable).
    *   View and manage all Candidates and vote results.
    *   CRUD operations for Modules (assign to departments).
    *   View system action history.
    *   Assign Department Heads based on vote results.
*   **Department Head (Chef de Département) Specific Features:**
    *   View teachers within their department and their workloads.
    *   View modules within their department.
    *   Assign modules to teachers within their department using different strategies (even distribution, specific workload).
    *   Manage module requests from teachers (approve/reject).
*   **Teacher (Enseignant) Specific Features:**
    *   View personal dashboard with assigned modules and notifications.
    *   View and manage their profile (personal info, skills, education).
    *   View notifications (e.g., new modules, vote announcements, module assignments).
    *   Request to teach specific modules.
    *   Participate in votes: declare candidacy, cast votes.
    *   Join a department (if not already a member).
    *   View their assigned modules with filtering and sorting.
*   **Module Management:**
    *   Creation, update, and deletion of modules.
    *   Association of modules with departments and teachers.
    *   Workload definition for modules.
*   **Workload Management:**
    *   Automatic calculation of teacher workloads based on assigned modules.
    *   Strategies for assigning module workloads (Evenly, Specifically).
*   **Voting System:**
    *   Admins can start votes for department head positions with an end date.
    *   Teachers can declare candidacy for active votes in their department.
    *   Teachers can cast votes for candidates.
    *   Votes are automatically finalized after the end date (via a scheduled task).
    *   Admins can view vote results and assign the elected head.
*   **Notification System:**
    *   Teachers receive notifications for new modules, department assignments, vote starts, module requests, etc.
    *   Department heads receive notifications for module requests.
    *   Admins receive notifications for completed votes.
*   **History Logging:**
    *   Tracks important system actions (e.g., department creation, vote start, head assignment).
*   **Data Transfer Objects (DTOs):** Used for clean data transfer between layers.
*   **Command Pattern:** Used for encapsulating department-related operations (add, update, set head).

## Technologies Used

*   **Backend:**
    *   Java (JDK 17+ recommended)
    *   Spring Boot
    *   Spring Security (for authentication and authorization)
    *   Spring Data JPA (for database interaction)
    *   Hibernate (as JPA provider)
    *   Lombok (to reduce boilerplate code)
*   **Database:**
    *   Relational Database (e.g., H2, PostgreSQL, MySQL - configuration dependent)
*   **Frontend (implied by controllers returning view names):**
    *   Thymeleaf (Server-side templating engine)
    *   HTML, CSS, JavaScript
    *   WebJars (for client-side libraries like Bootstrap, jQuery - implied by `/webjars/**` permitAll)
*   **Build Tool:**
    *   Maven (or Gradle, depending on project setup)

## Setup and Installation

1.  **Prerequisites:**
    *   Java Development Kit (JDK 17 or higher)
    *   Apache Maven (3.6.x or higher)
    *   A relational database (e.g., H2, PostgreSQL, MySQL). H2 can be used for easy development.

2.  **Clone the Repository:**
    ```bash
    git clone <repository-url>
    cd GestionDepartements
    ```

3.  **Configure Database:**
    *   Open `src/main/resources/application.properties` (or `application.yml`).
    *   Configure the database connection properties:
        ```properties
        # Example for H2 (in-memory)
        spring.datasource.url=jdbc:h2:mem:gestiondepartementsdb
        spring.datasource.driverClassName=org.h2.Driver
        spring.datasource.username=sa
        spring.datasource.password=password
        spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
        spring.h2.console.enabled=true # To access H2 console at /h2-console

        # Example for PostgreSQL
        # spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
        # spring.datasource.username=your_db_user
        # spring.datasource.password=your_db_password
        # spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
        ```
    *   Adjust `spring.jpa.hibernate.ddl-auto` (e.g., `update`, `create-drop`) as needed for development. For production, use `validate` or `none` and manage schema with migration tools.

4.  **Build the Project:**
    ```bash
    mvn clean install
    ```

5.  **Run the Application:**
    ```bash
    mvn spring-boot:run
    ```
    Alternatively, you can run the main application class (`GestionDepartementsApplication.java`) from your IDE.

6.  **Initial Data Setup (Roles & Admin User):**
    The application uses `SecurityServiceImpl` which has methods to save users and roles. You might need to:
    *   Create a `CommandLineRunner` bean to pre-populate roles (`ADMIN`, `DEPARTMENT_HEAD`, `ENSEIGNANT`) and an initial admin user on startup.
    *   Example roles to create:
        *   `ADMIN`
        *   `DEPARTMENT_HEAD`
        *   `ENSEIGNANT`
    *   Refer to `SecurityServiceImpl#saveRole` and `SecurityServiceImpl#saveNewUser`, `SecurityServiceImpl#addRoleToUser`.

7.  **Access the Application:**
    Open your web browser and go to `http://localhost:8080` (or the configured port).

## Usage

### User Roles

*   **ADMIN:** Has full control over the system. Manages departments, users, modules, votes, and views system-wide information.
*   **DEPARTMENT_HEAD (Chef de Département):** Manages their specific department, including assigning modules to teachers, managing module requests, and viewing department members.
*   **ENSEIGNANT (Teacher):** Manages their profile, views assigned modules, requests new modules, participates in votes, and receives notifications.

### Key Endpoints

*   **General:**
    *   `/`: Redirects to `/index`
    *   `/index`: Home page
    *   `/login`: Login page
    *   `/logout`: Logout
    *   `/403`: Access Denied page
*   **Admin:** (All prefixed with `/admin`)
    *   `/admin/dashboard` or `/admin/departments`: Department management
    *   `/admin/modules`: Module management (system-wide)
    *   `/admin/modules/add`: Add new module
    *   `/admin/modules/edit/{moduleId}`: Edit module
    *   `/admin/teachers`: View all teachers and their workloads
    *   `/admin/candidates`: View candidates and vote results
    *   `/admin/history`: View system action history
    *   `/admin/departments/startVote/{departmentId}`: Start a vote for a department
    *   `/admin/departments/set-head`: Set a department head
*   **Department Head (Chef de Département):** (All prefixed with `/chef`)
    *   `/chef/dashboard` or `/chef/demandes`: Manage module requests
    *   `/chef/enseignants`: View teachers in their department
    *   `/chef/modules`: View modules in their department
    *   `/chef/modules/assign/{moduleId}`: Assign a module to teachers
    *   `/chef/demandes/approve/{requestId}`: Approve a module request
    *   `/chef/demandes/reject/{requestId}`: Reject a module request
*   **Teacher (Enseignant):** (All prefixed with `/enseignant`)
    *   `/enseignant/dashboard`: Teacher dashboard (redirects to notifications)
    *   `/enseignant/notifications`: View notifications
    *   `/enseignant/notifications/markAsRead/{notificationId}`: Mark notification as read
    *   `/enseignant/notifications/joinDepartment/{departmentId}`: Join a department
    *   `/enseignant/my-modules`: View assigned modules
    *   `/enseignant/profile`: View and update profile
    *   `/enseignant/modules/request/{moduleId}`: Request to teach a module
    *   `/enseignant/vote/{voteId}`: View vote page and candidates
    *   `/enseignant/vote/declareCandidacy/{voteId}`: Declare candidacy
    *   `/enseignant/vote/cast`: Cast a vote

## Architectural Notes

*   **Layered Architecture:** The project follows a typical layered architecture (Controller, Service, Repository).
*   **Spring Security:** Handles authentication and role-based authorization. `UserDetailsServiceImpl` and `SecurityConfig` are central to this. The `@Lazy` annotation on `UserDetailsServiceImpl` in `SecurityConfig` is used to resolve circular dependencies.
*   **JPA Entities & Repositories:** Used for ORM and database interaction.
*   **DTO Pattern:** `Dto` package contains Data Transfer Objects for cleaner data flow between layers, especially to and from controllers.
*   **Command Pattern:** Used in `DepartmentController` for actions like adding/updating departments and setting department heads (`AddDepartmentCommand`, `UpdateDepartmentCommand`, `SetDepartmentHeadCommand`), promoting separation of concerns and enabling potential undo/redo or logging capabilities.
*   **Strategy Pattern:** Implemented for module workload assignment (`EvenWorkloadAssignmentStrategy`, `SpecificWorkloadAssignmentStrategy`) allowing for flexible ways to distribute teaching load.
*   **Mediator Pattern:** `ModuleRequestMediator` (used by `EnseignantModuleService`) likely centralizes the logic for handling module requests, coordinating between teachers, modules, and department heads.
*   **Scheduled Tasks:** `DepartmentServiceImpl` includes a `@Scheduled` method (`checkAndFinalizeVotes`) to automatically process votes that have reached their end date.
*   **Transactional Management:** `@Transactional` annotation is used extensively in service and controller methods to ensure data consistency.


