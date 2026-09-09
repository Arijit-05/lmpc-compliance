# 🚀 LMPC Compliance Backend API Guide

## 🔐 Authentication Endpoints

### Register User
```http
POST /api/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Rajesh Kumar Sharma",
  "email": "officer@metroguard.gov.in",
  "password": "Officer@2026",
  "role": "enforcement_officer"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "uuid",
  "email": "officer@metroguard.gov.in",
  "name": "Rajesh Kumar Sharma",
  "role": "enforcement_officer"
}
```

### Login
```http
POST /api/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "officer@metroguard.gov.in",
  "password": "Officer@2026"
}
```

**Response:** Same as register

---

## 📝 Compliance Analysis Endpoints

### Analyze Text (NEW - Main Endpoint)
```http
POST /api/compliance/analyze-text
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "productId": "uuid",
  "extractedText": "Manufactured by ITC Limited\nMRP: ₹85\nNet Quantity: 500g\nManufacturing Date: 08/2026\nConsumer Care: 1800-123-4567",
  "productName": "Aashirvaad Whole Wheat Atta",
  "manufacturer": "ITC Limited",
  "isEcommerce": false
}
```

**Response:**
```json
{
  "scanId": "uuid",
  "productId": "uuid",
  "extractedText": "...",
  "compliance": {
    "status": "COMPLIANT",
    "score": 100,
    "foundDeclarations": ["MRP (Maximum Retail Price)", "Net Quantity", ...],
    "missingDeclarations": [],
    "violations": [],
    "scanId": "uuid"
  },
  "violations": []
}
```

### Get Compliance Report
```http
GET /api/compliance/report/{scanId}
Authorization: Bearer {token}
```

**Response:** Same structure as analyze-text

---

## 📦 Product Management

### Get All Products
```http
GET /api/products
```

### Create Product
```http
POST /api/products
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "name": "Product Name",
  "categoryId": 1,
  "imported": true,
  "soldViaEcommerce": false
}
```

### Get Product by ID
```http
GET /api/products/{id}
Authorization: Bearer {token}
```

---

## 📊 Dashboard & Analytics

### Dashboard Summary
```http
GET /api/dashboard/summary
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalScans": 5,
  "completedScans": 5,
  "totalViolations": 12,
  "criticalViolations": 0,
  "majorViolations": 12,
  "minorViolations": 0
}
```

---

## 🔍 Scan Management

### Get Scans by Product
```http
GET /api/scans/product/{productId}
Authorization: Bearer {token}
```

### Get Scan History
```http
GET /api/scans/history
Authorization: Bearer {token}
```

---

## 🛡️ Security

### Authentication
- All protected endpoints require `Authorization: Bearer {token}` header
- Tokens expire after 24 hours (configurable)
- User roles: `admin`, `enforcement_officer`, `viewer`

### CORS
- CORS is enabled for all origins (development mode)
- In production, configure specific allowed origins

---

## 🔄 Frontend Integration

### Key Changes for Frontend Developers

1. **Authentication Flow**
   - Call `/api/auth/register` or `/api/auth/login` to get token
   - Include token in `Authorization: Bearer {token}` header for all protected requests
   - Store token securely (localStorage or httpOnly cookie)

2. **OCR → Text Flow**
   - Frontend handles OCR extraction from images
   - Send extracted text to `/api/compliance/analyze-text`
   - No need to upload images to backend

3. **Request Format**
   - Use `TextAnalysisRequest` format with extracted text
   - Include productId, productName, manufacturer, isEcommerce flags

### Example Integration Code

```javascript
// Login
const login = async (email, password) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  const data = await response.json();
  localStorage.setItem('token', data.token);
  return data;
};

// Analyze Text
const analyzeText = async (productId, extractedText) => {
  const token = localStorage.getItem('token');
  const response = await fetch('http://localhost:8080/api/compliance/analyze-text', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      productId,
      extractedText,
      productName: "Product Name",
      manufacturer: "Manufacturer Name",
      isEcommerce: false
    })
  });
  return await response.json();
};
```

---

## 🗄️ Database Schema

### New Tables Added
- `users` - User authentication and roles
- `categories` - Product categorization
- `declarations` - Individual declaration extraction results
- `reports` - Generated compliance reports
- `audit_log` - System audit trail

### Updated Tables
- `products` - Added categoryId field
- `scans` - Added principalDisplayPanelAreaCm2 field
- `violations` - Added declarationId field

---

## 🧪 Testing the Updated Backend

### 1. Start the Backend
```bash
cd lmpc-compliance
.\mvnw.cmd spring-boot:run
```

### 2. Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Test User\",\"email\":\"test@test.com\",\"password\":\"password123\",\"role\":\"enforcement_officer\"}"
```

### 3. Login to Get Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@test.com\",\"password\":\"password123\"}"
```

### 4. Analyze Text (Replace {token})
```bash
curl -X POST http://localhost:8080/api/compliance/analyze-text \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d "{\"productId\":\"uuid\",\"extractedText\":\"MRP: ₹85\nNet Quantity: 500g\",\"productName\":\"Test Product\",\"manufacturer\":\"Test Manufacturer\",\"isEcommerce\":false}"
```

---

## 🎯 Compliance Rules

The backend checks for these mandatory declarations:

1. **MRP (Maximum Retail Price)** - Required for all products
2. **Net Quantity** - Must be in standard units (g, kg, ml, l, etc.)
3. **Manufacturer/Packer/Importer** - Name and complete address
4. **Generic Name** - Common or generic name of commodity
5. **Manufacturing Date** - Month and year of manufacture
6. **Consumer Care Details** - Contact information for complaints
7. **Country of Origin** - Required for imported products only

---

## 🚨 Error Handling

### Common Error Codes
- `401` - Unauthorized (invalid or missing token)
- `400` - Bad Request (invalid input data)
- `404` - Not Found (product/scan not found)
- `500` - Internal Server Error (backend processing error)

### Error Response Format
```json
{
  "error": "Error message description"
}
```

---

## 📝 Notes for Frontend Team

1. **Remove OCR Dependencies**: Frontend should handle all OCR processing
2. **Update API Calls**: Replace image upload endpoints with text analysis endpoint
3. **Add Authentication**: Implement login/register flow and token management
4. **Update Data Structures**: Match the new response formats with frontend data models
5. **Test Integration**: Test with real extracted text from your OCR implementation

---

## 🔧 Configuration

### Application Properties
```properties
# Database
spring.datasource.url=${NEON_URL}
spring.datasource.username=${NEON_USERNAME}
spring.datasource.password=${NEON_PASSWORD}

# JWT
jwt.secret=mySecretKeyForLMPCComplianceSystem2024
jwt.expiration=86400000  # 24 hours

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---
