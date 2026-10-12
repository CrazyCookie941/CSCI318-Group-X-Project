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

Initial project skeleton and microservice architecture setup. The registration service publishes account lifecycle events to the Kafka topic `account-events`, and the notification service consumes those messages for downstream processing.

## Kafka and microservices

Kafka is a distributed event-stream platform. In this project, services do not call each other directly for account lifecycle events. Instead, the registration service publishes an event such as `ACCOUNT_CREATED`, `ACCOUNT_UPDATED`, or `ACCOUNT_DELETED` to a Kafka topic. Other services like notification-service can subscribe to that topic and react asynchronously.

This keeps the system decoupled: the account service can keep working even when the notification service is temporarily unavailable, and the account service does not need to know how the notification service is implemented.

## Event Maps

Event managers can search for a venue or address during event creation and select the matching OpenStreetMap geocoding result. The event service stores that result's coordinates with the event, and the event details page uses Leaflet and OpenStreetMap tiles to display the pinned location. The event service backfills known coordinates for the sample venues (Afterpay Arena, Wollongong Central, Sydney Central Park, and Wollongong Community Hall) when it starts; restart it to apply the migration to existing records. Other old events without coordinates show a notice instead of a guessed map.

Location searches are submitted only when the manager presses **Find location**. The registration service caches geocoding results and limits requests to one per second, in line with the public Nominatim service policy. The map requires an internet connection to load Leaflet and map tiles; event creation also sends the searched address to Nominatim. OpenStreetMap attribution is displayed on the map.

## Notifications

The JavaFX notification bell loads persisted notifications from notification-service (port 8084), displays the unread count, and supports marking individual notifications read before clearing them. The Notifications section in Settings controls notifications and the area followed for new event announcements. Area matching is case-insensitive substring matching against the event's location text.

Creating an event notifies users who enabled notifications and set a matching area. Event managers can change an event's status to **POSTPONED**, **CANCELLED**, or **REFUNDED** from Manage Events; registered attendees receive a notification when one of these statuses is applied. Deleting an event also sends a cancellation notification before its registrations are removed. Keep event-service, notification-service, and the JavaFX registration application running together for notification delivery. Notifications are persisted in the notification-service H2 database.