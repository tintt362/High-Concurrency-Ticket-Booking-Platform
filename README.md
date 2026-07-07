# High-Concurrency Ticket Booking System 

A high-concurrency ticket booking system designed to handle flash-sale scenarios and prevent overselling under heavy traffic.
# ⚠️ Note
This project is intended for learning and demonstration purposes only.



The environment configuration files are included to simplify project setup for reviewers and developers who want to explore the source code. All exposed values are non-production configurations and do not contain any sensitive business data.
In a production environment, secrets and credentials should never be committed to source control and must be managed securely.
## 🚀 The project focuses on:
- High Concurrency Processing
- Overselling Prevention
- Distributed Caching
- Cache Consistency
- Distributed Locking
- Monitoring & Observability
## 🛠 Tech Stack

- **Backend:** Java 21, Spring Boot 3.x, Spring Data JPA, Hibernate, MySQL.
- **Caching & Event Distribution:** Redis Server (Lettuce connection pool).
- **Observability:** Prometheus, Grafana.
- **Logging:** ELK Stack (Elasticsearch, Logstash, Kibana).
- **DevOps:** Docker, Docker Compose.



## Key Features
### Ticket Management
- Create, Update, Delete Tickets
- Ticket Availability Management

### Order Management
- Place Orders
- Cancel Orders
- Based Pagination
### High-Concurrency Booking
### Implemented multiple booking strategies:
- Optimistic Lock
- Redis Lua Script

### Performance Optimization
#### Multi-Level Caching
- Redis Cache
- Local Cache
- Cache-Aside Pattern
### Dynamic Order Tables
#### Orders are automatically partitioned by month:
- ticket_order_202601
- ticket_order_202602
- ticket_order_202603

## Getting Started
### Run Infrastructure
- docker-compose up -d
### Start Application
- mvn clean install  
- mvn spring-boot:run
## 👨‍💻 Author

**Thai Trong Tin**
Aspiring Java Backend Developer

* 🎓 Industrial University of Ho Chi Minh City (IUH)
* 📍 Ho Chi Minh City, Vietnam
* ✉️ Email: tintt362@gmail.com
* 🔗 LinkedIn: https://www.linkedin.com/in/thai-trong-tin-6a1529332

---
