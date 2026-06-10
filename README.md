# 🏨 Hotel Booking Application

A comprehensive Spring Boot-based REST API for managing hotel bookings, with role-based access control for hotel owners, managers, and customers. The system supports hotel management, room bookings, payment processing, and customer reviews.

## 📋 Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database](#database)
- [Authentication](#authentication)
- [User Roles & Permissions](#user-roles--permissions)
- [Testing](#testing)
- [Project Architecture](#project-architecture)
- [Key Components](#key-components)

## ✨ Features

### User Management

- **Multi-role authentication system** (Owner, Manager, Customer)
- User registration and login with JWT token-based authentication
- Role-based access control (RBAC)
- User permission management

### Hotel Management

- Create and manage hotel properties
- Owner dashboard for hotel oversight
- Hotel profile customization
- Hotel search and discovery

### Room Management

- Add and manage rooms per hotel
- Room type classification
- Room status tracking (Available, Booked, Maintenance)
- Dynamic room availability

### Booking System

- Customer room booking with date selection
- Booking status tracking (Pending, Confirmed, Cancelled)
- Booking history and management
- Optimistic locking for concurrent booking prevention

### Payment Processing

- Payment method tracking
- Payment status management
- Integration-ready payment gateway endpoints

### Review System

- Customer reviews and ratings for hotels
- Review management

### Additional Features

- Password security with BCrypt encryption
- JWT token-based authorization
- CORS support for frontend integration
- Comprehensive error handling
- Validation of inputs
- Database migrations using Flyway
- Retry mechanism for resilient operations
- JPA Auditing for tracking entity changes

## 🛠️ Technology Stack

| Technology            | Version | Purpose                        |
| --------------------- | ------- | ------------------------------ |
| **Java**              | 1.8 (8) | Programming Language           |
| **Spring Boot**       | 2.7.18  | Framework                      |
| **Spring Data JPA**   | 2.7.x   | ORM & Data Access              |
| **Spring Security**   | 2.7.x   | Authentication & Authorization |
| **Spring Retry**      | Latest  | Resilience & Fault Tolerance   |
| **MySQL**             | 8.0+    | Relational Database            |
| **Flyway**            | Latest  | Database Migration             |
| **JWT (JJWT)**        | 0.10.0  | Token-based Authentication     |
| **Lombok**            | Latest  | Boilerplate Reduction          |
| **Springdoc-OpenAPI** | Latest  | Swagger/OpenAPI Documentation  |
| **Maven**             | 3.6+    | Build Tool                     |

## 📁 Project Structure

```
hotelbooking/
├── src/
│   ├── main/
│   │   ├── java/com/example/hotelbooking/
│   │   │   ├── HotelbookingApplication.java          # Main Spring Boot entry point
│   │   │   ├── Configs/                              # Configuration classes (Security, CORS, etc.)
│   │   │   ├── Controllers/                          # REST API endpoints
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AuthController.java              # Authentication endpoints
│   │   │   │   ├── OwnerController/                 # Owner-specific endpoints
│   │   │   │   ├── ManagerController/               # Manager-specific endpoints
│   │   │   │   └── CustomerController/              # Customer-specific endpoints
│   │   │   ├── Models/                               # JPA Entity classes
│   │   │   │   ├── User.java
│   │   │   │   ├── Hotel.java
│   │   │   │   ├── Room.java
│   │   │   │   ├── Booking.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── Review.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Permission.java
│   │   │   │   └── ... (other domain models)
│   │   │   ├── DTOs/                                 # Data Transfer Objects
│   │   │   │   ├── AuthRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   └── ... (other DTOs)
│   │   │   ├── Services/                             # Business Logic
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── HotelService.java
│   │   │   │   ├── Booking/
│   │   │   │   ├── Payment/
│   │   │   │   ├── Review/
│   │   │   │   └── ... (other services)
│   │   │   ├── Repositories/                         # JPA Repository interfaces
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── HotelRepository.java
│   │   │   │   ├── BookingRepository.java
│   │   │   │   └── ... (other repositories)
│   │   │   ├── ExceptionHandler/                     # Global exception handling
│   │   │   ├── Utils/                                # Utility classes
│   │   │   └── Configs/                              # Configuration classes
│   │   └── resources/
│   │       ├── application.properties                 # Application configuration
│   │       ├── application-prod.properties           # Production configuration
│   │       ├── db/migration/                         # Flyway database migrations
│   │       │   ├── V1__initial_schema.sql
│   │       │   ├── V2__adding_more_fields_in_hotel.sql
│   │       │   ├── V3__create_manager_profile_table_for_owner_link.sql
│   │       │   ├── V4__create_rooms_booking_and_review_model.sql
│   │       │   ├── V5__adding_customer_details_fields_and_making_customer_reference_nullable.sql
│   │       │   └── V6__add_payment.sql
│   │       └── templates/static/
│   └── test/
│       └── java/com/example/hotelbooking/            # Unit & Integration Tests
├── pom.xml                                            # Maven configuration & dependencies
├── mvnw                                               # Maven wrapper (Unix)
├── mvnw.cmd                                           # Maven wrapper (Windows)
└── README.md                                          # This file
```

## 📋 Prerequisites

Before running this application, ensure you have the following installed:

- **Java Development Kit (JDK)**: Version 8 or higher
- **Maven**: Version 3.6 or higher
- **MySQL**: Version 8.0 or higher
- **Git**: For version control
- **IDE**: IntelliJ IDEA, VS Code, or any Java IDE (optional)

## 🚀 Setup & Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd hotelbooking
```

### 2. Ensure MySQL is Running

Make sure MySQL server is running on your machine:

```bash
# On Windows
net start MySQL80

# On macOS
brew services start mysql

# On Linux
sudo systemctl start mysql
```

### 3. Create Database (Optional)

The application will create the database automatically if it doesn't exist. However, you can manually create it:

```sql
CREATE DATABASE IF NOT EXISTS hotel_booking;
```

### 4. Install Dependencies

```bash
./mvnw clean install
```

Or using Maven:

```bash
mvn clean install
```

## ⚙️ Configuration

### Application Properties

Update [application.properties](src/main/resources/application.properties) with your environment settings:

```properties
# Server
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_booking?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password

# JWT Configuration
app.jwt.secret=YourSecretKeyHere
app.jwt.expiration=86400000  # 24 hours in milliseconds

# CORS Configuration
app.cors.allowed-origins=http://localhost:3000,http://localhost:4200,http://localhost:5173

# BCrypt Strength
app.bcrypt.strength=10
```

### Production Configuration

For production deployment, use [application-prod.properties](src/main/resources/application-prod.properties):

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

## ▶️ Running the Application

### Using Maven Wrapper (Recommended)

```bash
# On Windows
mvnw.cmd spring-boot:run

# On macOS/Linux
./mvnw spring-boot:run
```

### Using Maven

```bash
mvn spring-boot:run
```

### Using IDE

1. Open the project in your IDE
2. Locate `HotelbookingApplication.java`
3. Right-click and select **Run**

### Using JAR File

After building the project:

```bash
java -jar target/hotelbooking-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Swagger/OpenAPI Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Main API Endpoints

#### Authentication (`/api/authenticate`)

- `POST /register-as-owner` - Register as hotel owner
- `POST /register-as-customer` - Register as customer
- `POST /login` - Login and receive JWT token

#### Hotels (`/api/hotels`)

- `GET /` - List all hotels
- `GET /{id}` - Get hotel details
- `POST /` - Create hotel (Owner only)
- `PUT /{id}` - Update hotel (Owner only)

#### Rooms (`/api/rooms`)

- `GET /` - List all rooms
- `GET /{id}` - Get room details
- `POST /` - Create room (Manager only)
- `PUT /{id}` - Update room status

#### Bookings (`/api/bookings`)

- `GET /` - List bookings (role-based)
- `POST /` - Create booking (Customer)
- `PUT /{id}` - Update booking status (Manager/Owner)
- `DELETE /{id}` - Cancel booking

#### Reviews (`/api/reviews`)

- `GET /` - List reviews
- `POST /` - Submit review (Customer)
- `DELETE /{id}` - Delete review (Customer)

#### Payments (`/api/payments`)

- `GET /` - List payments
- `POST /` - Process payment
- `GET /{id}` - Get payment details

## 🗄️ Database

### Database Design

The application uses a relational database with the following main entities:

- **Users**: Store user accounts and credentials
- **Hotels**: Store hotel information
- **Rooms**: Store room details and availability
- **Bookings**: Track customer bookings
- **Payments**: Process and track payments
- **Reviews**: Store customer reviews
- **Roles & Permissions**: RBAC implementation

### Database Migrations

Database migrations are managed using **Flyway**. Migration scripts are located in [src/main/resources/db/migration/](src/main/resources/db/migration/):

- `V1__initial_schema.sql` - Initial database schema
- `V2__adding_more_fields_in_hotel.sql` - Hotel enhancements
- `V3__create_manager_profile_table_for_owner_link.sql` - Manager profiles
- `V4__create_rooms_booking_and_review_model.sql` - Rooms, bookings, reviews
- `V5__adding_customer_details_fields_and_making_customer_reference_nullable.sql` - Customer details
- `V6__add_payment.sql` - Payment system

Migrations run automatically on application startup.

## 🔐 Authentication

### JWT Token-Based Authentication

The application uses **JSON Web Tokens (JWT)** for stateless authentication:

1. **Register/Login**: Send credentials to `/api/authenticate/login`
2. **Token Response**: Receive JWT token with 24-hour expiration
3. **Use Token**: Include token in `Authorization: Bearer <token>` header
4. **Access Protected Resources**: API validates token for each request

### Security Features

- **Password Encryption**: BCrypt with configurable strength (default: 10 rounds)
- **Token Signing**: HMAC-SHA256 with secret key
- **Token Expiration**: 24 hours (configurable)
- **CORS Support**: Whitelist-based cross-origin requests
- **Spring Security**: Integrated with Spring Security framework

## 👥 User Roles & Permissions

### Roles

1. **OWNER** - Hotel owner
   - Create and manage hotels
   - View bookings and payments
   - Manage hotel managers

2. **MANAGER** - Hotel manager
   - Manage rooms for assigned hotel
   - View and process bookings
   - Monitor payments

3. **CUSTOMER** - Regular user
   - Search and book hotels
   - View booking history
   - Submit reviews and ratings
   - Process payments

4. **ADMIN** - System administrator
   - Manage users
   - View system analytics
   - Configure system settings

### Permission System

The application uses a granular permission system allowing fine-grained access control beyond roles.

## 🧪 Testing

### Running Tests

```bash
./mvnw test
```

Or using Maven:

```bash
mvn test
```

### Test Classes

Located in [src/test/java/com/example/hotelbooking/](src/test/java/com/example/hotelbooking/):

- `HotelbookingApplicationTests.java` - Application startup tests
- `OptimisticLockingTest.java` - Concurrency control tests
- Additional integration and unit tests

### Test Reports

After running tests, view reports in `target/surefire-reports/`:

```bash
# View HTML report
target/surefire-reports/index.html
```

## 🏗️ Project Architecture

### Layered Architecture

```
┌─────────────────────────┐
│   REST Controllers      │  ← HTTP Endpoints
├─────────────────────────┤
│   Service Layer         │  ← Business Logic
├─────────────────────────┤
│   Repository Layer      │  ← Data Access (JPA)
├─────────────────────────┤
│   Entity/Model Layer    │  ← Domain Objects
├─────────────────────────┤
│   MySQL Database        │  ← Persistent Storage
└─────────────────────────┘
```

### Design Patterns Used

- **DTO Pattern**: Data Transfer Objects for API contracts
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Separation of business logic
- **Strategy Pattern**: Different behaviors for different roles
- **Builder Pattern**: Complex object construction (Lombok @Builder)
- **Singleton Pattern**: Spring beans

## 🔑 Key Components

### Controllers

Handles HTTP requests and routing to appropriate services. Includes validation, error handling, and response formatting.

### Services

Contains business logic for domains like:

- Authentication and authorization
- Hotel management
- Booking processing
- Payment handling
- Review management

### Repositories

JPA Repository interfaces for CRUD operations on domain entities, with custom query methods.

### Models (Entities)

JPA-annotated domain classes representing database tables:

- User, Hotel, Room, Booking, Payment, Review, Role, Permission

### DTOs

Data classes for API request/response payloads, decoupling internal models from external contracts.

### Exception Handler

Global exception handling for consistent error responses and proper HTTP status codes.

### Configurations

Spring configuration classes for:

- Security configuration (JWT, authentication)
- CORS configuration
- Bean definitions
- Database connections

## 📝 Additional Notes

### Troubleshooting

**MySQL Connection Error**

- Verify MySQL is running
- Check connection credentials in `application.properties`
- Ensure database exists

**Port Already in Use**

- Change `server.port` in `application.properties`
- Or kill the process using port 8080

**JWT Token Expired**

- Request a new token by logging in again
- Adjust expiration time in `app.jwt.expiration`

### Performance Optimizations

- Lazy loading for related entities
- Database indexing on frequently queried columns
- Connection pooling via HikariCP
- Caching strategies for frequently accessed data
- Optimistic locking to prevent concurrent modification issues


**Happy Coding! 🚀**

Last Updated: June 2026
