# Security Implementation Guide

## Overview

Konstlotteriet uses JWT (JSON Web Token) based authentication with role-based authorization.

## Roles

- **ADMIN** - Full access to all endpoints, can manage members, artworks, lotteries
- **MEMBER** - Limited access, can view own profile and manage own ranking lists

## Authentication Flow

### 1. Login

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "role": "MEMBER",
  "userId": 1,
  "memberId": 5
}
```

### 2. Using the Token

Include the JWT token in the Authorization header for all protected endpoints:

```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Token Expiration

- Access tokens expire after **24 hours**
- Refresh tokens expire after **7 days** (not yet implemented)

## API Endpoints

### Public Endpoints (No authentication required)
- `POST /api/auth/login` - Login

### Protected Endpoints

#### Admin Only (`@RolesAllowed("ADMIN")`)
- `POST /api/auth/register` - Register new user
- All `/api/admin/*` endpoints:
  - `/api/admin/members/**`
  - `/api/admin/artworks/**`
  - `/api/admin/lotteries/**`
  - `/api/admin/rankings/**`

#### Member & Admin (`@RolesAllowed({"ADMIN", "MEMBER"})`)
- `POST /api/auth/change-password` - Change own password
- `GET /api/auth/me` - Get current user info
- `/api/member/profile` - Get own profile
- `/api/member/rankings/**` - Manage own ranking lists

## User Management

### Default Admin User

A default admin user is created during database migration:
- **Email:** `admin@konstlotteriet.se`
- **Password:** `admin123`

**⚠️ IMPORTANT:** Change this password immediately in production!

```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "admin123",
    "newPassword": "NewSecurePassword123!"
  }'
```

### Register New User (Admin only)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123",
    "role": "MEMBER",
    "memberId": 5
  }'
```

**Note:** `memberId` is optional for ADMIN users but required for MEMBER users.

## JWT Token Structure

The JWT token contains the following claims:

```json
{
  "iss": "https://konstlotteriet.se",
  "upn": "user@example.com",
  "groups": ["MEMBER"],
  "userId": 1,
  "memberId": 5,
  "exp": 1699999999
}
```

- `iss` - Issuer
- `upn` - User Principal Name (email)
- `groups` - User roles
- `userId` - Database user ID
- `memberId` - Associated member ID (null for admin users)
- `exp` - Expiration timestamp

## Security Features

### Password Hashing

Passwords are hashed using **BCrypt** with 10 iterations:

```java
String hashedPassword = BcryptUtil.bcryptHash(plainPassword);
```

### Password Validation

Passwords are verified against the BCrypt hash:

```java
boolean matches = BcryptUtil.matches(plainPassword, hashedPassword);
```

### Soft Delete

Users are never deleted from the database. Instead, they are deactivated:

```java
user.setActive(false);
```

## Testing Authentication

### 1. Login as Admin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@konstlotteriet.se",
    "password": "admin123"
  }'
```

Save the token from the response.

### 2. Access Protected Endpoint

```bash
curl -X GET http://localhost:8080/api/admin/members \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3. Get Current User Info

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Common Error Responses

### 401 Unauthorized
```json
{
  "message": "Felaktiga inloggningsuppgifter"
}
```

**Causes:**
- Invalid email or password
- User account is deactivated

### 403 Forbidden
```json
{
  "message": "Access Forbidden"
}
```

**Causes:**
- Valid token but insufficient permissions
- Trying to access ADMIN endpoint as MEMBER

### 400 Bad Request
```json
{
  "message": "Användaren är inte kopplad till en medlem"
}
```

**Causes:**
- Member endpoint accessed by user without associated member

## Security Best Practices

### For Development

1. Use the dev services PostgreSQL (automatic)
2. Keep default credentials for local testing
3. JWT keys are auto-generated in `META-INF/resources/`

### For Production

1. **Change default admin password immediately**
2. **Generate new JWT keys:**
   ```bash
   cd src/main/resources/META-INF/resources
   openssl genrsa -out rsaPrivateKey.pem 2048
   openssl rsa -pubout -in rsaPrivateKey.pem -out publicKey.pem
   openssl pkcs8 -topk8 -nocrypt -inform pem -in rsaPrivateKey.pem -outform pem -out privateKey.pem
   rm rsaPrivateKey.pem
   ```
3. **Update CORS settings:**
   ```properties
   quarkus.http.cors.origins=https://your-frontend-domain.com
   ```
4. **Enable HTTPS:**
   ```properties
   quarkus.http.ssl-port=8443
   quarkus.http.ssl.certificate.key-file=path/to/key.pem
   quarkus.http.ssl.certificate.file=path/to/cert.pem
   ```
5. **Secure JWT issuer:**
   ```properties
   mp.jwt.verify.issuer=https://your-production-domain.com
   ```
6. **Use environment variables for secrets:**
   ```bash
   export DB_PASSWORD=secure_password
   export JWT_ISSUER=https://prod.konstlotteriet.se
   ```

## Troubleshooting

### Token Verification Failed

**Error:** "Failed to verify token"

**Solutions:**
1. Check that public/private keys match
2. Verify issuer matches in token and config
3. Check token hasn't expired
4. Ensure token format is correct: `Bearer <token>`

### User Not Found

**Error:** "Användare hittades inte"

**Solutions:**
1. Verify user exists in database
2. Check user is active (`active = true`)
3. Confirm email is correct

### LazyInitializationException

**Error:** "Could not initialize proxy"

**Solutions:**
1. Ensure JWT injection is working: `@Inject JsonWebToken jwt`
2. Check entity relationships are properly loaded
3. Verify transaction boundaries

## Implementation Details

### User Entity

Located in: `src/main/java/se/konstlotteriet/domain/User.java`

- Uses `@UserDefinition` from Quarkus Security JPA
- Password hashed with BCrypt
- Linked to Member entity (optional)

### JWT Token Service

Located in: `src/main/java/se/konstlotteriet/service/TokenService.java`

- Generates JWT tokens with SmallRye JWT
- Includes user claims (userId, memberId, role)
- 24-hour expiration

### Auth Service

Located in: `src/main/java/se/konstlotteriet/service/AuthService.java`

- Handles user authentication
- User registration
- Password management

### Auth Endpoints

Located in: `src/main/java/se/konstlotteriet/rest/AuthResource.java`

- `/login` - User authentication
- `/register` - User registration (admin)
- `/change-password` - Password change
- `/me` - Current user info

## Database Schema

### users table

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed
    role VARCHAR(50) NOT NULL,        -- ADMIN or MEMBER
    member_id BIGINT REFERENCES members(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Migration Files

- `V1__create_initial_schema.sql` - Initial schema
- `V2__add_users_table.sql` - Users table and default admin

## Future Enhancements

- [ ] Refresh token implementation
- [ ] Password reset via email
- [ ] Two-factor authentication (2FA)
- [ ] OAuth2/OpenID Connect integration
- [ ] Session management
- [ ] Login attempt limiting
- [ ] Audit logging for security events

---

**Last Updated:** 2025-10-10
**Version:** 1.0.0
