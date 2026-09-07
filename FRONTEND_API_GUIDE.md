# 📱 Frontend Developer API Guide

## Quick Reference Card

### Base URL
```
http://localhost:8080
```

---

## 🔥 Critical Endpoints (Must Implement)

### 1. Upload & Analyze Image (MAIN FEATURE)
```http
POST /api/compliance/upload-and-analyze
Content-Type: multipart/form-data
```

**Request Parameters:**
- `file`: Product image file (multipart)
- `productId`: UUID string

**Response:**
```json
{
  "scanId": "uuid",
  "productId": "uuid", 
  "imagePath": "uploads/filename.png",
  "extractedText": "OCR extracted text from image...",
  "compliance": {
    "status": "COMPLIANT" | "NON_COMPLIANT",
    "score": 85,
    "foundDeclarations": ["MRP", "Net Quantity"],
    "missingDeclarations": ["Manufacturer"],
    "violations": ["Missing manufacturer declaration"]
  },
  "violations": [
    {
      "id": "uuid",
      "scanId": "uuid",
      "ruleRef": "Legal Metrology Rules, 2011",
      "severity": "major",
      "description": "Missing mandatory MRP declaration",
      "detectedAt": "2026-09-07T19:28:48.862909Z"
    }
  ]
}
```

**Frontend Implementation:**
```javascript
const formData = new FormData();
formData.append('file', imageFile);
formData.append('productId', productId);

fetch('http://localhost:8080/api/compliance/upload-and-analyze', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => {
  // Display compliance results
  console.log('Compliance Score:', data.compliance.score);
  console.log('Status:', data.compliance.status);
  console.log('Violations:', data.violations);
});
```

---

### 2. Get Products (Product Selection)
```http
GET /api/products
```

**Response:**
```json
[
  {
    "id": "uuid",
    "name": "Product Name",
    "imported": true,
    "soldViaEcommerce": false,
    "createdAt": "2026-09-07T18:12:20.779536Z"
  }
]
```

**Frontend Implementation:**
```javascript
fetch('http://localhost:8080/api/products')
.then(response => response.json())
.then(products => {
  // Populate product dropdown
  products.forEach(product => {
    console.log(product.name, product.id);
  });
});
```

---

### 3. Get Compliance Report (View Saved Results)
```http
GET /api/compliance/report/{scanId}
```

**Response:** Same as upload endpoint

**Frontend Implementation:**
```javascript
fetch(`http://localhost:8080/api/compliance/report/${scanId}`)
.then(response => response.json())
.then(report => {
  // Display historical compliance report
});
```

---

## 📊 Supporting Endpoints

### Health Check
```http
GET /api/health
```
**Response:** `{"status": "UP", "message": "LMPC Compliance Backend is running"}`

### Dashboard Statistics
```http
GET /api/dashboard/summary
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

### Create Product
```http
POST /api/products
Content-Type: application/json
```
**Request:**
```json
{
  "name": "Product Name",
  "imported": true,
  "soldViaEcommerce": false
}
```

---

## 🎨 UI Component Suggestions

### Upload Component
- File input for product images
- Product dropdown (populated from `/api/products`)
- Upload button
- Progress indicator

### Results Display
- Compliance score (circular progress or percentage)
- Status badge (COMPLIANT/NON_COMPLIANT)
- Extracted text display (expandable)
- Found declarations (green checkmarks)
- Missing declarations (red X)
- Violations list with severity indicators

### Dashboard
- Statistics cards (total scans, violations, etc.)
- Recent scans list
- Compliance trend chart

---

## 🔧 Technical Notes

### File Upload Constraints
- Max file size: 10MB
- Supported formats: PNG, JPG, JPEG
- Use `FormData` for multipart uploads

### Error Handling
```javascript
fetch(url, options)
.then(response => {
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.json();
})
.catch(error => {
  console.error('Error:', error);
  // Show error message to user
});
```

### UUID Format
- All IDs are UUID strings
- Example: `ccc5acb4-b733-4d93-99af-88912a7d4352`

### Response Time Expectation
- Upload & analyze: 5-15 seconds (depends on image complexity)
- Other endpoints: < 1 second

---

## 🧪 Testing Locally

### Using curl
```bash
# Health check
curl http://localhost:8080/api/health

# Get products
curl http://localhost:8080/api/products

# Upload & analyze (use Postman for file upload)
```

### Using Postman
1. Create new request
2. Method: POST
3. URL: `http://localhost:8080/api/compliance/upload-and-analyze`
4. Body: form-data
5. Add fields: `file` (File), `productId` (Text)
6. Send request

---

## 📋 Implementation Priority

### Phase 1 (MVP)
1. ✅ Health check integration
2. ✅ Product listing dropdown
3. ✅ Image upload with product selection
4. ✅ Display compliance results

### Phase 2 (Enhanced)
1. ✅ Historical reports view
2. ✅ Dashboard statistics
3. ✅ Error handling & loading states
4. ✅ Image preview before upload

### Phase 3 (Advanced)
1. ✅ Product creation
2. ✅ Report export (PDF/CSV)
3. ✅ Real-time progress updates
4. ✅ Advanced filtering

---

## 🎯 Success Criteria

Frontend is successful when:
- User can select a product from dropdown
- User can upload a product image
- System shows compliance results within 15 seconds
- Results clearly show compliance score and violations
- User can view historical compliance reports
- Error messages are clear and helpful

---

## 🆘 Troubleshooting

### Common Issues

**Upload fails with 400 error**
- Check file size is under 10MB
- Ensure productId is valid UUID
- Verify file format is PNG/JPG

**No products in dropdown**
- Check `/api/products` endpoint
- Create test products if needed

**Slow response time**
- Normal for OCR processing (5-15 seconds)
- Show loading indicator to user

**Empty extracted text**
- Image quality may be too low
- Try with clearer image

---

## 📞 Backend Contact

For API issues or questions:
- Backend URL: `http://localhost:8080`
- Health check: `GET /api/health`
- Check backend logs for errors

---

## 🚀 Ready to Build!

You have all the endpoints needed to build a complete frontend. Start with the upload functionality and results display, then add the supporting features.

Good luck! 🎉
