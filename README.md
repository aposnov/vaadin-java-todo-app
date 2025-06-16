# Vaadin Todo Application

A simple, modern Todo application built with Java, Vaadin, and Spring Boot. Features a clean UI, MySQL integration, Docker support, and ready-to-extend architecture for internal tools or game economy management.

## Quick Start

1. Build the application:
```bash
mvn clean install
```

2. Start with Docker:
```bash
docker compose up --build
```

Open your browser and navigate to `http://localhost:8080`

## Features

- Create, read, and update todo items
- Mark todos as complete/incomplete
- Responsive UI with Vaadin components
- MySQL database integration
- Spring Boot backend
- Docker support for easy deployment

## Technical Stack

- Java 17
- Spring Boot 3.2.3
- Vaadin 24.4.9
- MySQL
- Docker

## Docker Configuration

The application includes Docker support with:
- `Dockerfile` for building the application container
- `docker-compose.yml` for orchestrating the application and database
- Persistent volume for MySQL data
- Environment variables for database configuration

## Changing the Application Port

By default, the application runs on standard ports:
- **Spring Boot**: 8080
- **Docker Compose**: 8081 (mapped to 8080 inside the container)

If these ports are busy or you want to use a different port:

### For Spring Boot (locally)
In the file `src/main/resources/application.properties`, add or change the line:
```
server.port=8082
```

### For Docker Compose
In the file `docker-compose.yml`, change the line:
```
    ports:
      - "8081:8080"
```
Here, `8081` is the external port, which you can change to any available port.

After making changes, restart the application.

## Architecture Overview

This project is a simple Vaadin-based Todo application. It uses Spring Boot for backend logic, Vaadin Flow for the UI, and MySQL as the database (via Docker Compose). The architecture is layered: model (JPA entity), repository (Spring Data JPA), and view (Vaadin UI). All business logic is in Java, and the UI is designed for non-technical users.

## How to Add a New Feature
1. Add a new field to the `Todo` entity (e.g., priority, category).
2. Update the repository and UI to support the new field.
3. Add tests for the new feature in `src/test/java`.

## How to Run Tests
Run all tests with:
```
mvn test
```

## How to Run with Docker Compose
Start the app and database with:
```
docker compose up --build
```

## Extensibility
The codebase is structured for easy extension. To integrate with external APIs or add new business logic, add new services and wire them into the UI and repository layers. Example: to add a currency or task type, extend the `Todo` entity and update the UI accordingly.

## Code Review & Collaboration
I am open to code reviews and always ready to improve code quality. The project follows best practices and is ready for team collaboration. 