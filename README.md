# TravelMate

**TravelMate** is a Spring Boot-based backend application designed to manage trip expenses, split costs among participants, and generate financial summaries for each trip. It provides RESTful endpoints for managing trips, expenses, and participants.

## Features
- ### `budget-service`:
  - Add, update, delete, and retrieve expenses for a trip
  - Calculate trip budget summaries
  - Track who paid how much and how costs should be split
  - Patch individual fields in an expense
- ### General:
  - Base URL: http://localhost:8081
  - H2 in-memory database for fast development and testing
    - Database URL: jdbc:h2:mem:demo
    - H2 Console: http://localhost:8081/h2-console
    - Port: 9093 (for remote access, if needed)
    - Username: sa
    - Password: (empty)
  - OpenAPI (Swagger) documentation

---

## Getting Started

### Prerequisites

- Java 21
- Maven
