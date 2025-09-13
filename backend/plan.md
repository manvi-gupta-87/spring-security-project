# Future Implementation Plans

## API Key Authentication

### Overview
Alternative authentication method to JWT for service-to-service communication and public APIs.

### Key Components
1. **ApiKey Model** - Stores key, owner, roles, rate limit, expiration
2. **ApiKeyService** - Creates, validates, and manages API keys
3. **ApiKeyAuthenticationFilter** - Intercepts requests with `X-API-Key` header
4. **ApiKeyController** - REST endpoints for key management

### How It Works
- Client sends API key in `X-API-Key` header
- Filter validates key and sets SecurityContext
- Each key has its own roles and rate limit
- Works alongside JWT (either method can authenticate)

### Use Cases
- Service-to-service communication
- Mobile/desktop applications  
- Webhooks and CI/CD pipelines
- Public API with per-key rate limiting

### Example Usage
```bash
curl -H "X-API-Key: sk_abc123..." http://localhost:8080/api/data
```

### Benefits
- No token refresh needed
- Easy immediate revocation
- Per-key rate limiting
- Better for long-lived integrations

---

## Day 11: Complete Authentication Module

### Overview
Build a production-ready authentication system with multiple auth methods, user management, and comprehensive documentation.

### Current Status
✅ **Already Implemented:**
- JWT authentication (login, refresh, logout)
- Basic user registration
- Security configuration with CORS, headers
- Rate limiting and validation

❌ **To Be Implemented:**
- Form-based login for web users
- Password reset flow
- Enhanced registration
- Comprehensive documentation

### Implementation Features

#### 1. Form-Based Login (Web UI)
**Components:**
- `LoginController` - Serves login pages
- HTML templates (login, success, error pages)
- Form login configuration in SecurityConfig
- Session management for web users

**Files to Create:**
- `/controller/LoginController.java`
- `/resources/templates/login.html`
- `/resources/templates/home.html`
- `/resources/templates/login-success.html`

**Why Important:**
- Shows understanding of session vs token auth
- Real-world requirement for web applications
- Demonstrates hybrid authentication approach

#### 2. Password Reset Flow
**Components:**
- `PasswordResetToken` model - Token entity with expiry
- `PasswordResetService` - Token generation and validation
- `PasswordResetController` - REST endpoints
- Email simulation (console output for demo)

**Files to Create:**
- `/model/PasswordResetToken.java`
- `/service/PasswordResetService.java`
- `/controller/PasswordResetController.java`

**Security Features:**
- Token expiry (1 hour)
- One-time use tokens
- Secure token generation (UUID)
- Token validation before password update

#### 3. Enhanced Registration
**Improvements:**
- Email format validation
- Password strength requirements (8+ chars, uppercase, lowercase, digit)
- Username uniqueness check
- Registration confirmation response

#### 4. Comprehensive README
**Sections:**
- Project overview
- Features list
- Architecture diagram
- API documentation
- Setup instructions
- Testing guide
- Security best practices

### Architecture Overview
```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Web UI     │     │  REST API    │     │   Mobile     │
│  (Forms)     │     │   (JSON)     │     │    Apps      │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                     │
       ▼                    ▼                     ▼
┌──────────────────────────────────────────────────────────┐
│              Spring Security Filter Chain                 │
├────────────────────────────────────────────────────────────┤
│  Rate Limit → CORS → Auth (Form/JWT/Basic) → Authorization│
└──────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────┐
│                    Controllers                            │
│  Login │ Token │ User │ Password Reset │ Documents       │
└──────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────┐
│                     Services                              │
│  UserService │ TokenService │ PasswordResetService        │
└──────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────┐
│                   Data Storage                            │
│  In-Memory (H2) │ JWT Keys │ Blacklist │ Reset Tokens    │
└──────────────────────────────────────────────────────────┘
```

### Testing Checklist
- [ ] Form login with valid/invalid credentials
- [ ] JWT login and token refresh
- [ ] Password reset request and confirmation
- [ ] Registration with validation
- [ ] Rate limiting across all endpoints
- [ ] CORS from different origins
- [ ] Security headers presence

### Interview Talking Points
1. **Mixed Authentication:** Explain session vs token trade-offs
2. **Security Layers:** Filter chain, method security, validation
3. **Token Management:** JWT lifecycle, refresh rotation, blacklisting
4. **Password Security:** BCrypt, reset tokens, expiry
5. **Production Considerations:** Rate limiting, CORS, headers
6. **Best Practices:** Input validation, error handling, logging