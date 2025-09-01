# Spring Security JWT Authentication Project

## 📋 Overview

A Spring Boot application demonstrating secure REST API implementation with JWT-based authentication and role-based access control (RBAC). This project showcases modern security practices including stateless authentication, password encryption, and hierarchical role management.

## 🚀 Features

- **JWT Authentication**: Stateless token-based authentication using HS256 algorithm
- **Token Refresh**: Extend session without re-authentication via `/api/token/refresh`
- **Role-Based Access Control**: Hierarchical roles (ADMIN > USER) with endpoint-level security
- **Secure Password Storage**: BCrypt password encoding
- **Environment-Based Configuration**: Secure secrets management using environment variables
- **Auto-loading .env Files**: Automatic loading of environment variables from .env file
- **H2 In-Memory Database**: Quick setup for development and testing
- **Comprehensive Error Handling**: Custom JSON error responses for authentication/authorization failures
- **Spring Security 6**: Latest security configurations and best practices

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.5.5**
- **Spring Security 6**
- **Spring OAuth2 Resource Server** (JWT support)
- **Nimbus JOSE + JWT** (JWT operations)
- **H2 Database** (in-memory)
- **Maven** (build tool)
- **Lombok** (reducing boilerplate)

## 📁 Project Structure

```
spring-security-project/
├── src/main/java/com/example/demo/
│   ├── config/
│   │   ├── SecurityConfig.java       # Main security configuration
│   │   ├── JwtBeans.java             # JWT encoder/decoder beans
│   │   ├── AuthManagerConfig.java    # Authentication manager setup
│   │   └── UserStoreConfig.java      # User details configuration
│   ├── controller/
│   │   ├── TokenController.java      # JWT token generation endpoint
│   │   ├── AdminController.java      # Admin-only endpoints
│   │   ├── UserController.java       # User endpoints
│   │   └── HelloController.java      # Public endpoints
│   └── errors/
│       ├── JsonAuthEntryPoint.java   # 401 error handler
│       └── JsonAccessDeniedHandler.java # 403 error handler
├── src/main/resources/
│   ├── application.yaml              # Main configuration
│   ├── application.yaml.template     # Template for configuration
│   ├── schema.sql                    # Database schema
│   └── data.sql                       # Initial data
├── .env.example                       # Environment variables template
└── pom.xml                           # Maven dependencies
```

## 🔧 Setup & Installation

### Prerequisites
- Java 21 or higher
- Maven 3.6+

### Quick Start

1. **Clone the repository**
```bash
git clone git@github.com:manvi-gupta-87/spring-security-project.git
cd spring-security-project
```

2. **Set up environment variables**
```bash
cp .env.example .env
# Edit .env and add your JWT secret (optional - defaults provided for dev)
```

3. **Build and run**
```bash
mvn clean compile
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 🔐 Security Configuration

### Default Users
| Username | Password | Role |
|----------|----------|------|
| user | password | ROLE_USER |
| admin | admin | ROLE_ADMIN |

### JWT Configuration
- **Algorithm**: HS256 (HMAC with SHA-256)
- **Token Expiry**: 30 minutes (configurable)
- **Secret**: Configured via `JWT_SECRET` environment variable

### Endpoint Security
| Endpoint | Access Level |
|----------|-------------|
| `/hello`, `/health` | Public |
| `/api/token` | Public (authentication endpoint) |
| `/api/token/refresh` | Authenticated users (with valid token) |
| `/api/users/me` | Authenticated users |
| `/api/admin/**` | Admin role only |
| `/api/manager/**` | Method-level security |

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Test Coverage
- Integration tests with MockMvc
- Security context testing with @WithMockUser
- JWT token generation and validation
- Role-based access control verification

### Quick API Test
```bash
# Get JWT token
TOKEN=$(curl -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}' \
  | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

# Use token to access protected endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users/me
```

## 📝 API Documentation

See [docs/api-testing-commands.md](docs/api-testing-commands.md) for comprehensive curl commands to test all endpoints.

## 🔄 Recent Updates

### September 1, 2025
- ✅ Fixed JWT algorithm mismatch issue (RS256 → HS256)
- ✅ Implemented secure secrets management with environment variables
- ✅ Added spring-dotenv for automatic .env file loading
- ✅ Created comprehensive API testing documentation
- ✅ Set up SSH authentication for GitHub
- ✅ Added JWT token refresh endpoint (`/api/token/refresh`)

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is open source and available for educational purposes.

## 🙏 Acknowledgments

- Spring Security documentation
- Spring Boot guides
- JWT.io for JWT debugging tools

## 📧 Contact

GitHub: [@manvi-gupta-87](https://github.com/manvi-gupta-87)

---

**Note**: This is a demonstration project for learning Spring Security concepts. For production use, ensure proper security auditing and use production-grade secret management solutions.