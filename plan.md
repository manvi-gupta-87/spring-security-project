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