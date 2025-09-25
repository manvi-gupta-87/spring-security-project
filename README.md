# Spring Security + Angular JWT Authentication System

A full-stack application demonstrating secure authentication using Spring Security with JWT tokens on the backend and Angular on the frontend.

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (Angular 18)                   │
├─────────────────────────────────────────────────────────────┤
│  Components:          Services:          Guards:            │
│  - Login             - Auth Service      - Auth Guard       │
│  - Register          - HTTP Interceptor                     │
│  - Home                                                     │
└─────────────────────────────────────────────────────────────┘
                              ↓ HTTP
┌─────────────────────────────────────────────────────────────┐
│                  Backend (Spring Boot 3.5.5)                │
├─────────────────────────────────────────────────────────────┤
│  Security:            Controllers:       Services:          │
│  - JWT Filter        - TokenController   - UserService      │
│  - SecurityConfig    - UserController    - TokenService     │
│  - Rate Limiting     - HomeController                       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Database (H2/In-Memory)                  │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Features

### Security Features
- **JWT Authentication**: Stateless authentication using JSON Web Tokens
- **Role-Based Access Control**: ADMIN and USER roles with hierarchical permissions
- **Rate Limiting**: Per-role request limits (ADMIN: 1000/min, USER: 200/min, Anonymous: 60/min)
- **Automatic Token Refresh**: Seamless token renewal on 401 responses
- **Auth Guard**: Route protection at the Angular router level
- **CORS Configuration**: Secure cross-origin resource sharing
- **Security Headers**: XSS protection, Content-Type sniffing prevention, Frame options

### Application Features
- User Registration
- User Login/Logout
- Protected Routes
- JWT Token Management
- Form Validation
- Error Handling

## 🛠️ Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.5.5**
- **Spring Security 6**
- **Spring OAuth2 Resource Server** (JWT support)
- **H2 Database** (In-memory)
- **Maven**
- **Lombok**
- **Bucket4j** (Rate limiting)

### Frontend
- **Angular 18**
- **TypeScript**
- **Reactive Forms**
- **RxJS**
- **Angular Router**
- **HTTP Interceptors**

## 📁 Project Structure

```
spring-security-project/
├── backend/
│   ├── src/main/java/com/example/demo/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java      # Main security configuration
│   │   │   ├── CorsConfig.java         # CORS settings
│   │   │   ├── UserStoreConfig.java    # User management
│   │   │   └── AuthManagerConfig.java  # Authentication manager
│   │   ├── controller/
│   │   │   ├── TokenController.java    # JWT token endpoints
│   │   │   ├── UserController.java     # User management endpoints
│   │   │   └── HomeController.java     # Protected endpoints
│   │   ├── filter/
│   │   │   └── RateLimitingFilter.java # Rate limiting implementation
│   │   └── service/
│   │       └── TokenService.java       # JWT generation/validation
│   └── src/main/resources/
│       ├── application.yaml             # Application configuration
│       ├── jwt-private.pem             # RSA private key
│       ├── jwt-public.pem              # RSA public key
│       ├── schema.sql                  # Database schema
│       └── data.sql                    # Initial data
│
└── frontend/spring-security-angular/
    └── src/app/
        ├── auth/
        │   ├── login/                  # Login component
        │   ├── register/               # Registration component
        │   ├── auth.ts                 # Auth service
        │   ├── auth-guard.ts          # Route guard
        │   └── auth-interceptor.ts    # HTTP interceptor
        ├── home/                       # Home component
        └── app.routes.ts              # Route configuration
```

## 🔧 Setup Instructions

### Prerequisites
- Java 21
- Node.js 18+
- Maven 3.6+
- Angular CLI 18
- Docker & Docker Compose (for containerized deployment)

### Option 1: Run with Docker (Recommended)

1. Build and start all services:
```bash
# Build images (use --no-cache for clean build)
docker-compose build

# Start services in background
docker-compose up -d
```

2. Access the application:
- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:8081`

3. View logs:
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
```

4. Stop services:
```bash
docker-compose down
```

### Option 2: Run Locally

#### Backend Setup

1. Navigate to backend directory:
```bash
cd backend
```

2. Install dependencies and run:
```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

#### Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend/spring-security-angular
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
ng serve
```

The frontend will start on `http://localhost:4200`

### Docker Commands Reference

```bash
# Build images
docker-compose build

# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f

# Restart services
docker-compose restart

# Check service status
docker-compose ps

# Remove everything (containers, networks, volumes)
docker-compose down -v

# Rebuild and restart
docker-compose down && docker-compose build --no-cache && docker-compose up -d
```

## 🔑 Default Users

| Username | Password | Role |
|----------|----------|------|
| user | password | ROLE_USER |
| admin | admin123 | ROLE_ADMIN |
| manager | manager | ROLE_MANAGER |

## 📝 API Endpoints

### Public Endpoints
- `POST /api/token` - Login and get JWT token
- `POST /api/users/register` - Register new user
- `POST /api/token/refresh` - Refresh access token
- `GET /hello` - Public hello endpoint

### Protected Endpoints
- `GET /api/users/me` - Get current user info
- `POST /api/logout` - Logout user
- `GET /api/admin/**` - Admin only endpoints
- `GET /home` - Authenticated users only

## 🔄 Authentication Flow

1. **Registration**:
   - User submits registration form
   - Backend creates user with ROLE_USER
   - User redirected to login

2. **Login**:
   - User submits credentials
   - Backend validates and returns JWT tokens
   - Frontend stores tokens in localStorage
   - User redirected to home page

3. **Protected Routes**:
   - Auth guard checks for valid token
   - If no token, redirect to login
   - If token exists, allow access

4. **Token Refresh**:
   - On 401 response, interceptor attempts refresh
   - If successful, retry original request
   - If failed, redirect to login

5. **Logout**:
   - Clear tokens from localStorage
   - Redirect to login page

## 🛡️ Security Configurations

### JWT Configuration
- **Algorithm**: RS256 (RSA with SHA-256)
- **Token Expiry**: 30 minutes (configurable)
- **Refresh Token**: UUID-based, stored server-side
- **Claims**: username, roles, token type

### Rate Limiting
```java
ADMIN: 1000 requests/minute
USER: 200 requests/minute  
Anonymous: 60 requests/minute
```

### CORS Settings
- Allowed Origins: localhost:4200, localhost:3000, localhost:5173
- Allowed Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
- Credentials: Enabled

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend/spring-security-angular
ng test
```

### Manual Testing
1. Test registration with new username
2. Test login with registered user
3. Access protected route without login (should redirect)
4. Access protected route with login (should allow)
5. Wait for token expiry and verify refresh works

## 📚 Learning Objectives Completed

This project demonstrates:
- ✅ JWT token-based authentication
- ✅ Spring Security configuration
- ✅ Role-based authorization
- ✅ Rate limiting implementation
- ✅ Angular route guards
- ✅ HTTP interceptors
- ✅ Form validation
- ✅ Error handling
- ✅ CORS configuration
- ✅ Security headers
- ✅ Token refresh mechanism
- ✅ User registration flow

## 🚧 Future Enhancements

- [ ] Password reset functionality
- [ ] Email verification
- [ ] OAuth2 social login
- [ ] Remember me functionality
- [ ] Session management
- [ ] Audit logging
- [ ] Two-factor authentication
- [ ] Account lockout mechanism

## 📄 License

This project is for educational purposes.