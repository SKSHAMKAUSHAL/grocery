# Grocery Ordering Platform

A microservices-based application for online grocery ordering with user authentication, product catalog, shopping cart, and order management functionalities.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Services](#services)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Docker Configuration](#docker-configuration)
- [Troubleshooting](#troubleshooting)

## Architecture Overview

The application consists of four microservices:

- User Service (Authentication & User Management)
- Product Catalog Service (Product Listings)
- Cart Service (Shopping Cart Management)
- Order Service (Order Processing)

## Services

1.  **User Service (Port: 8081)**

    - Handles user authentication using Firebase
    - Manages user profiles and credentials
    - Key files:
      - `user-service/src/main/java/com/example/user/Application.java`
      - `user-service/src/main/java/com/example/user/controller/UserController.java`

2.  **Product Catalog Service (Port: 8082)**

    - Manages product inventory
    - Provides product information and search
    - Key files:
      - `product-catalog-service/src/main/java/com/example/product/Application.java`
      - `product-catalog-service/src/main/java/com/example/product/controller/ProductController.java`

3.  **Cart Service (Port: 8083)**

    - Handles shopping cart operations
    - Manages cart items and quantities
    - Key files:
      - `cart-service/src/main/java/com/example/cart/Application.java`
      - `cart-service/src/main/java/com/example/cart/controller/CartController.java`

4.  **Order Service (Port: 8084)**
    - Processes order placement
    - Manages order status and history
    - Key files:
      - `order-service/src/main/java/com/example/order/Application.java`
      - `order-service/src/main/java/com/example/order/controller/OrderController.java`

## Technologies Used

- Java 17
- Spring Boot
- PostgreSQL
- Firebase Authentication
- Docker & Docker Compose
- Maven
- JUnit 5
- Testcontainers
- Swagger/OpenAPI

## Prerequisites

**Development Tools**

- Java 17 JDK
- Maven 3.8+
- Docker Desktop
- Git

**Firebase Setup**

- Firebase project with Email/Password authentication enabled
- Firebase service account JSON file

## Setup & Installation

1.  Clone the repository
2.  **Firebase Configuration**

    - Place `firebase-service-account.json` in:
      - `user-service/src/main/resources/`
      - `product-catalog-service/src/main/resources/`
      - `order-service/src/main/resources/`

3.  Build all services

## Running the Application

1.  Start using Docker Compose
2.  Verify services
3.  Access service endpoints
    - User Service: [http://localhost:8081](http://localhost:8081)
    - Product Catalog: [http://localhost:8082](http://localhost:8082)
    - Cart Service: [http://localhost:8083](http://localhost:8083)
    - Order Service: [http://localhost:8084](http://localhost:8084)

## API Documentation

**User Service Endpoints**

- `POST /api/user/signup` - Register new user
- `POST /api/user/signin` - User login
- `GET /api/user/profile` - Get user profile (Protected)

**Product Catalog Endpoints**

- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product details
- `POST /api/products` - Add new product (Admin only)
- `PUT /api/products/{id}` - Update product (Admin only)

**Cart Service Endpoints**

- `POST /api/cart` - Add to cart
- `GET /api/cart/{userId}` - View cart
- `PUT /api/cart` - Update cart
- `DELETE /api/cart/clear/{userId}` - Clear cart

**Order Service Endpoints**

- `POST /api/order` - Place order
- `GET /api/order` - View orders
- `PUT /api/order/{orderId}/status` - Update order status (Admin only)

## Testing

**Running Tests**

**Test Categories**

- Unit Tests
- Integration Tests (using Testcontainers)
- Controller Tests

## Docker Configuration

Each service has its own Dockerfile and the entire application is orchestrated using `docker-compose.yml`.

**Service Ports**

- User Service: 8081
- Product Catalog: 8082
- Cart Service: 8083
- Order Service: 8084
- PostgreSQL instances: 5432-5435

## Troubleshooting

**Common Issues**

**Service Connection Issues**

- Verify all containers are running: `docker ps`
- Check logs: `docker logs <container-id>`

**Authentication Errors**

- Verify Firebase configuration
- Check token validity
- Ensure `firebase-service-account.json` is present

**Database Issues**

- Check PostgreSQL container status
- Verify connection strings in `application.yml`

## Contributing

1.  Fork the repository
2.  Create feature branch
3.  Commit changes
4.  Push to branch
5.  Create Pull Request
