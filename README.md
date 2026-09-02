# CSCI318 AI Event Management Platform

## Project Overview

This project is an AI-powered event management platform developed for CSCI318.

The system follows a microservice-based, event-driven architecture using Spring Boot, Spring Cloud Stream, Apache Kafka, and LangChain4j.

## Microservices

- event-service — Event creation, management, search, and AI-assisted event discovery.
- registration-service — Event registration and cancellation.
- payment-service — Simulated payment and refund processing.
- notification-service — Event-related and registration-related notifications.

## Technology Stack

- Java 21
- Spring Boot 4.0.8
- Spring Web MVC
- Spring Data JPA
- H2 Database
- Spring Cloud Stream
- Apache Kafka
- LangChain4j

## Service Ports

| Service | Port |
|---|---:|
| Event Service | 8081 |
| Registration Service | 8082 |
| Payment Service | 8083 |
| Notification Service | 8084 |

## Project Structure

Each microservice follows a layered architecture:

- Presentation Layer
- Service Layer
- Domain Layer
- Infrastructure Layer

## Current Status

Initial project skeleton and microservice architecture setup.