# Frontend Integration & Backend API Specification

This document provides the complete technical specification required to build a frontend application (React, Angular, Vue, or mobile client) against the **Online Food Ordering System** Spring Boot backend.

---

## 1. Server & Base URL Configuration

| Setting | Value |
| :--- | :--- |
| **Server Host & Port** | `http://localhost:8080` |
| **API Base URL** | `http://localhost:8080/api` |
| **Content-Type** | `application/json` |
| **Auth Header Format** | `Authorization: Bearer <jwt_token>` |
| **JWT Validity** | `10 hours` (`36,000,000 ms`) |

### Endpoint Base Paths
* **Auth**: `http://localhost:8080/api/auth`
* **Categories**: `http://localhost:8080/api/categories`
* **Food Items**: `http://localhost:8080/api/foods`
* **Shopping Cart**: `http://localhost:8080/api/cart`
* **Orders**: `http://localhost:8080/api/orders`
* **Payments**: `http://localhost:8080/api/payments`
* **Users**: `http://localhost:8080/api/users`

---

## 2. Enums Reference

Use these exact uppercase string values in JSON requests and responses:

| Enum Name | Allowed Values | Used In |
| :--- | :--- | :--- |
| **`Role`** | `"ADMIN"`, `"CUSTOMER"` | `User`, `RegisterRequest`, `UserResponse` |
| **`FoodStatus`** | `"AVAILABLE"`, `"OUT_OF_STOCK"` | `FoodItem`, `FoodItemRequest`, `FoodItemResponse` |
| **`OrderStatus`** | `"PLACED"`, `"PREPARING"`, `"DELIVERED"`, `"CANCELLED"` | `Order`, `OrderStatusUpdateRequest`, `OrderResponse` |
| **`PaymentStatus`** | `"PENDING"`, `"COMPLETED"`, `"FAILED"` | `Payment`, `PaymentStatusUpdateRequest`, `PaymentResponse`, `OrderResponse` |

---

## 3. Domain Entities Overview

| Entity | Database Table | Primary Fields | Relationships |
| :--- | :--- | :--- | :--- |
| **`User`** | `users` | `id`, `email` (unique), `password` (BCrypt), `role` (`Role`) | `1:1` with `Cart`, `1:N` with `Order` |
| **`Category`** | `categories` | `id`, `name` (unique), `description` | `1:N` with `FoodItem` |
| **`FoodItem`** | `food_items` | `id`, `name`, `description`, `price` (`BigDecimal`), `status` (`FoodStatus`) | `N:1` with `Category` |
| **`Cart`** | `carts` | `id` | `1:1` with `User`, `1:N` with `CartItem` |
| **`CartItem`** | `cart_items` | `id`, `quantity` (`Integer`) | `N:1` with `Cart`, `N:1` with `FoodItem` |
| **`Order`** | `orders` | `id`, `orderDate` (`LocalDateTime`), `totalAmount` (`BigDecimal`), `status` (`OrderStatus`), `deliveryAddress` | `N:1` with `User`, `1:N` with `OrderItem`, `1:1` with `Payment` |
| **`OrderItem`** | `order_items` | `id`, `quantity` (`Integer`), `unitPrice` (`BigDecimal`) | `N:1` with `Order`, `N:1` with `FoodItem` |
| **`Payment`** | `payments` | `id`, `amount` (`BigDecimal`), `paymentMethod`, `status` (`PaymentStatus`), `transactionRef`, `paymentDate` (`LocalDateTime`) | `1:1` with `Order` |

---

## 4. DTO Schemas (TypeScript / JSON Shapes)

> **Note:** The backend never exposes raw JPA entities directly over HTTP. All requests and responses use the following DTO shapes.

### 4.1 Auth & User DTOs

```ts
// POST /api/auth/signup
interface RegisterRequest {
  email: string;          // Required, valid email format
  password: string;       // Required, minimum 6 characters
  role?: "ADMIN" | "CUSTOMER"; // Optional (defaults to "CUSTOMER" if omitted)
}

// POST /api/auth/signin
interface AuthRequest {
  email: string;          // Required, valid email format
  password: string;       // Required
}

// Response from POST /api/auth/signin
interface AuthResponse {
  token: string;          // JWT Bearer token
}

// Response from POST /api/auth/signup and /api/users/**
interface UserResponse {
  id: number;
  email: string;
  role: "ADMIN" | "CUSTOMER";
}
```

### 4.2 Category DTOs

```ts
// POST /api/categories, PUT /api/categories/{id}
interface CategoryRequest {
  name: string;           // Required, not blank, unique (case-insensitive)
  description?: string;   // Optional
}

// Response from /api/categories/**
interface CategoryResponse {
  id: number;
  name: string;
  description: string | null;
}
```

### 4.3 Food Item DTOs

```ts
// POST /api/foods, PUT /api/foods/{id}
interface FoodItemRequest {
  name: string;           // Required, not blank
  description?: string;   // Optional
  price: number;          // Required, minimum 0.01
  status?: "AVAILABLE" | "OUT_OF_STOCK"; // Optional (defaults to "AVAILABLE" on create)
  categoryId: number;     // Required, must reference an existing Category ID
}

// Response from /api/foods/**
interface FoodItemResponse {
  id: number;
  name: string;
  description: string | null;
  price: number;
  status: "AVAILABLE" | "OUT_OF_STOCK";
  categoryId: number;
  categoryName: string;
}
```

### 4.4 Shopping Cart DTOs

```ts
// POST /api/cart/items
interface CartItemRequest {
  foodItemId: number;     // Required, food item must be AVAILABLE
  quantity: number;       // Required, minimum 1
}

interface CartItemResponse {
  id: number;
  foodItemId: number;
  foodItemName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;       // unitPrice * quantity
}

// Response from all /api/cart/** endpoints
interface CartResponse {
  id: number;
  userId: number;
  items: CartItemResponse[];
  totalAmount: number;    // Sum of all item subtotals
}
```

### 4.5 Order DTOs

```ts
// POST /api/orders
interface OrderRequest {
  deliveryAddress: string; // Required, not blank
}

// PATCH /api/orders/{id}/status
interface OrderStatusUpdateRequest {
  status: "PLACED" | "PREPARING" | "DELIVERED" | "CANCELLED"; // Required
}

interface OrderItemResponse {
  id: number;
  foodItemId: number;
  foodItemName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

// Response from /api/orders/**
interface OrderResponse {
  id: number;
  userId: number;
  userEmail: string;
  orderDate: string;      // ISO-8601 timestamp, e.g. "2026-11-20T20:15:30"
  totalAmount: number;
  status: "PLACED" | "PREPARING" | "DELIVERED" | "CANCELLED";
  deliveryAddress: string;
  items: OrderItemResponse[];
  paymentStatus: "PENDING" | "COMPLETED" | "FAILED" | null;
}
```

### 4.6 Payment DTOs

```ts
// POST /api/payments
interface PaymentRequest {
  orderId: number;        // Required
  paymentMethod: string;  // Required, e.g. "CARD", "CASH_ON_DELIVERY", "ONLINE"
  transactionRef?: string; // Optional (auto-generated as "TXN-XXXXXXXX" if omitted)
}

// PATCH /api/payments/{id}/status
interface PaymentStatusUpdateRequest {
  status: "PENDING" | "COMPLETED" | "FAILED"; // Required
}

// Response from /api/payments/**
interface PaymentResponse {
  id: number;
  orderId: number;
  amount: number;
  paymentMethod: string;
  status: "PENDING" | "COMPLETED" | "FAILED";
  transactionRef: string;
  paymentDate: string;    // ISO-8601 timestamp
}
```

### 4.7 Standard Error Response DTO (`ErrorResponse`)

All `4xx` and `5xx` errors handled by `GlobalExceptionHandler` return this consistent JSON format:

```ts
interface ErrorResponse {
  timestamp: string;      // ISO-8601 timestamp
  status: number;         // HTTP status code (400, 401, 403, 404, 409, 500)
  error: string;          // HTTP reason phrase ("Bad Request", "Not Found", etc.)
  message: string;        // Human-readable error message
  path: string;           // Request URI that triggered the error
  validationErrors?: Record<string, string>; // Only present on @Valid field validation errors
}
```

**Sample Validation Error (`400 Bad Request`):**
```json
{
  "timestamp": "2026-11-20T20:25:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Input validation failed",
  "path": "/api/foods",
  "validationErrors": {
    "name": "Food item name is required",
    "price": "Price must be greater than zero"
  }
}
```

---

## 5. Complete REST Endpoints Reference

### 5.1 Authentication (`/api/auth`)

#### `POST /api/auth/signup`
* **Access**: Public
* **Description**: Registers a new user and automatically creates an empty shopping cart for them.
* **Request Body**: `RegisterRequest`
  ```json
  {
    "email": "customer@example.com",
    "password": "password123",
    "role": "CUSTOMER"
  }
  ```
* **Success Response** (`201 Created` — `UserResponse`):
  ```json
  {
    "id": 1,
    "email": "customer@example.com",
    "role": "CUSTOMER"
  }
  ```
* **Error Responses**: `400 Bad Request` (validation failure), `409 Conflict` (email already taken).

#### `POST /api/auth/signin`
* **Access**: Public
* **Description**: Authenticates user credentials and returns a JWT token.
* **Request Body**: `AuthRequest`
  ```json
  {
    "email": "customer@example.com",
    "password": "password123"
  }
  ```
* **Success Response** (`200 OK` — `AuthResponse`):
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
  ```
* **Error Responses**: `401 Unauthorized` (invalid email or password).

---

### 5.2 Categories (`/api/categories`)

#### `GET /api/categories`
* **Access**: Public
* **Success Response** (`200 OK` — `CategoryResponse[]`):
  ```json
  [
    {
      "id": 1,
      "name": "Pizza",
      "description": "Wood-fired Italian pizzas"
    }
  ]
  ```

#### `GET /api/categories/{id}`
* **Access**: Public
* **Path Parameter**: `id` (`Long`)
* **Success Response**: `200 OK` — `CategoryResponse`
* **Error Responses**: `404 Not Found`

#### `POST /api/categories`
* **Access**: `ADMIN` (`Authorization: Bearer <admin_jwt>`)
* **Request Body**: `CategoryRequest`
  ```json
  {
    "name": "Pizza",
    "description": "Wood-fired Italian pizzas"
  }
  ```
* **Success Response**: `201 Created` — `CategoryResponse`
* **Error Responses**: `400 Bad Request`, `403 Forbidden`, `409 Conflict` (duplicate name)

#### `PUT /api/categories/{id}`
* **Access**: `ADMIN`
* **Path Parameter**: `id` (`Long`)
* **Request Body**: `CategoryRequest`
* **Success Response**: `200 OK` — `CategoryResponse`

#### `DELETE /api/categories/{id}`
* **Access**: `ADMIN`
* **Path Parameter**: `id` (`Long`)
* **Success Response**: `204 No Content`

---

### 5.3 Food Items (`/api/foods`)

#### `GET /api/foods`
* **Access**: Public
* **Description**: Returns all food items across all categories.
* **Success Response** (`200 OK` — `FoodItemResponse[]`):
  ```json
  [
    {
      "id": 1,
      "name": "Margherita Pizza",
      "description": "Classic tomato, mozzarella, and basil",
      "price": 1850.00,
      "status": "AVAILABLE",
      "categoryId": 1,
      "categoryName": "Pizza"
    }
  ]
  ```

#### `GET /api/foods/available`
* **Access**: Public
* **Description**: Returns only food items where `status == "AVAILABLE"`.
* **Success Response**: `200 OK` — `FoodItemResponse[]`

#### `GET /api/foods/category/{categoryId}`
* **Access**: Public
* **Path Parameter**: `categoryId` (`Long`)
* **Success Response**: `200 OK` — `FoodItemResponse[]`
* **Error Responses**: `404 Not Found` (if category does not exist)

#### `GET /api/foods/search?keyword={keyword}`
* **Access**: Public
* **Query Parameter**: `keyword` (`String`, case-insensitive partial match on food name)
* **Success Response**: `200 OK` — `FoodItemResponse[]`

#### `GET /api/foods/{id}`
* **Access**: Public
* **Path Parameter**: `id` (`Long`)
* **Success Response**: `200 OK` — `FoodItemResponse`
* **Error Responses**: `404 Not Found`

#### `POST /api/foods`
* **Access**: `ADMIN`
* **Request Body**: `FoodItemRequest`
  ```json
  {
    "name": "Margherita Pizza",
    "description": "Classic tomato, mozzarella, and basil",
    "price": 1850.00,
    "status": "AVAILABLE",
    "categoryId": 1
  }
  ```
* **Success Response**: `201 Created` — `FoodItemResponse`

#### `PUT /api/foods/{id}`
* **Access**: `ADMIN`
* **Path Parameter**: `id` (`Long`)
* **Request Body**: `FoodItemRequest`
* **Success Response**: `200 OK` — `FoodItemResponse`

#### `PATCH /api/foods/{id}/status?status={status}`
* **Access**: `ADMIN`
* **Path Parameter**: `id` (`Long`)
* **Query Parameter**: `status` (`AVAILABLE` or `OUT_OF_STOCK`)
* **Success Response**: `200 OK` — `FoodItemResponse`

#### `DELETE /api/foods/{id}`
* **Access**: `ADMIN`
* **Path Parameter**: `id` (`Long`)
* **Success Response**: `204 No Content`

---

### 5.4 Shopping Cart (`/api/cart`)

All cart endpoints operate automatically on the **currently authenticated user's cart** (resolved from the JWT token).

#### `GET /api/cart`
* **Access**: Authenticated (`CUSTOMER` or `ADMIN`)
* **Success Response** (`200 OK` — `CartResponse`):
  ```json
  {
    "id": 1,
    "userId": 1,
    "items": [
      {
        "id": 10,
        "foodItemId": 1,
        "foodItemName": "Margherita Pizza",
        "unitPrice": 1850.00,
        "quantity": 2,
        "subtotal": 3700.00
      }
    ],
    "totalAmount": 3700.00
  }
  ```

#### `POST /api/cart/items`
* **Access**: Authenticated
* **Description**: Adds a food item to the cart (or increments its quantity if already in the cart). Fails with `400 Bad Request` if the food item is `OUT_OF_STOCK`.
* **Request Body**: `CartItemRequest`
  ```json
  {
    "foodItemId": 1,
    "quantity": 2
  }
  ```
* **Success Response**: `200 OK` — `CartResponse`

#### `PUT /api/cart/items/{foodItemId}?quantity={quantity}`
* **Access**: Authenticated
* **Path Parameter**: `foodItemId` (`Long`)
* **Query Parameter**: `quantity` (`Integer` — setting `0` removes the item from the cart)
* **Success Response**: `200 OK` — `CartResponse`

#### `DELETE /api/cart/items/{foodItemId}`
* **Access**: Authenticated
* **Path Parameter**: `foodItemId` (`Long`)
* **Success Response**: `200 OK` — `CartResponse`

#### `DELETE /api/cart`
* **Access**: Authenticated
* **Description**: Clears all items from the user's cart.
* **Success Response**: `200 OK` — `CartResponse` (with empty `items: []` and `totalAmount: 0`)

---

### 5.5 Orders (`/api/orders`)

#### `POST /api/orders`
* **Access**: Authenticated
* **Description**: Converts all items currently in the user's cart into a new `Order` with status `PLACED`, snapshots each item's `unitPrice`, and clears the user's cart.
* **Request Body**: `OrderRequest`
  ```json
  {
    "deliveryAddress": "123 Galle Road, Colombo 03"
  }
  ```
* **Success Response** (`201 Created` — `OrderResponse`):
  ```json
  {
    "id": 1,
    "userId": 1,
    "userEmail": "customer@example.com",
    "orderDate": "2026-11-20T20:20:00",
    "totalAmount": 3700.00,
    "status": "PLACED",
    "deliveryAddress": "123 Galle Road, Colombo 03",
    "items": [
      {
        "id": 1,
        "foodItemId": 1,
        "foodItemName": "Margherita Pizza",
        "quantity": 2,
        "unitPrice": 1850.00,
        "subtotal": 3700.00
      }
    ],
    "paymentStatus": null
  }
  ```
* **Error Responses**: `400 Bad Request` (if cart is empty or contains an `OUT_OF_STOCK` item).

#### `GET /api/orders/my`
* **Access**: Authenticated
* **Description**: Returns all orders placed by the logged-in user, newest first.
* **Success Response**: `200 OK` — `OrderResponse[]`

#### `GET /api/orders/{id}`
* **Access**: Order Owner or `ADMIN`
* **Path Parameter**: `id` (`Long`)
* **Success Response**: `200 OK` — `OrderResponse`
* **Error Responses**: `403 Forbidden` (if another customer tries to view it), `404 Not Found`

#### `PATCH /api/orders/{id}/cancel`
* **Access**: Order Owner (only while `status == "PLACED"`) or `ADMIN`
* **Path Parameter**: `id` (`Long`)
* **Success Response**: `200 OK` — `OrderResponse` (with `status: "CANCELLED"`)

#### `GET /api/orders?status={status}`
* **Access**: `ADMIN`
* **Query Parameter**: `status` (optional — `PLACED`, `PREPARING`, `DELIVERED`, or `CANCELLED`)
* **Description**: Lists all orders across the platform (filtered by status if provided), newest first.
* **Success Response**: `200 OK` — `OrderResponse[]`

#### `PATCH /api/orders/{id}/status`
* **Access**: `ADMIN`
* **Path Parameter**: `id` (`Long`)
* **Request Body**: `OrderStatusUpdateRequest`
  ```json
  {
    "status": "PREPARING"
  }
  ```
* **Success Response**: `200 OK` — `OrderResponse`

---

### 5.6 Payments (`/api/payments`)

#### `POST /api/payments`
* **Access**: Order Owner or `ADMIN`
* **Description**: Processes payment for an order.
  * If `paymentMethod` is `"CASH_ON_DELIVERY"` (case-insensitive), initial status is set to `"PENDING"`.
  * For all other methods (e.g. `"CARD"`), initial status is set to `"COMPLETED"`.
  * If `transactionRef` is omitted or blank, the backend generates one (e.g. `"TXN-A1B2C3D4"`).
* **Request Body**: `PaymentRequest`
  ```json
  {
    "orderId": 1,
    "paymentMethod": "CARD",
    "transactionRef": "TXN-20261120-001"
  }
  ```
* **Success Response** (`201 Created` — `PaymentResponse`):
  ```json
  {
    "id": 1,
    "orderId": 1,
    "amount": 3700.00,
    "paymentMethod": "CARD",
    "status": "COMPLETED",
    "transactionRef": "TXN-20261120-001",
    "paymentDate": "2026-11-20T20:22:15"
  }
  ```
* **Error Responses**: `400 Bad Request` (if order is `CANCELLED`), `409 Conflict` (if order is already paid).

#### `GET /api/payments/{id}`
* **Access**: Order Owner or `ADMIN`
* **Path Parameter**: `id` (`Long` — Payment ID)
* **Success Response**: `200 OK` — `PaymentResponse`

#### `GET /api/payments/order/{orderId}`
* **Access**: Order Owner or `ADMIN`
* **Path Parameter**: `orderId` (`Long` — Order ID)
* **Success Response**: `200 OK` — `PaymentResponse`

#### `GET /api/payments`
* **Access**: `ADMIN`
* **Description**: Lists all payments in the system.
* **Success Response**: `200 OK` — `PaymentResponse[]`

#### `PATCH /api/payments/{id}/status`
* **Access**: `ADMIN`
* **Path Parameter**: `id` (`Long` — Payment ID)
* **Request Body**: `PaymentStatusUpdateRequest`
  ```json
  {
    "status": "COMPLETED"
  }
  ```
* **Success Response**: `200 OK` — `PaymentResponse`

---

### 5.7 Users (`/api/users`)

#### `GET /api/users/me`
* **Access**: Authenticated (`CUSTOMER` or `ADMIN`)
* **Description**: Returns the profile (`id`, `email`, `role`) of the currently logged-in user. Use this on frontend startup/login to determine whether to show the Customer UI or Admin Dashboard.
* **Success Response** (`200 OK` — `UserResponse`):
  ```json
  {
    "id": 1,
    "email": "customer@example.com",
    "role": "CUSTOMER"
  }
  ```

#### `GET /api/users`
* **Access**: `ADMIN`
* **Success Response**: `200 OK` — `UserResponse[]`

#### `GET /api/users/{id}`
* **Access**: `ADMIN`
* **Path Parameter**: `id` (`Long`)
* **Success Response**: `200 OK` — `UserResponse`

#### `DELETE /api/users/{id}`
* **Access**: `ADMIN`
* **Path Parameter**: `id` (`Long`)
* **Success Response**: `204 No Content`

---

## 6. Recommended Frontend Flows

### Customer Flow
1. **Browse Menu (No login required)**:
   * Fetch categories: `GET /api/categories`
   * Fetch available foods: `GET /api/foods/available` (or filter by category via `GET /api/foods/category/{categoryId}` or search via `GET /api/foods/search?keyword=...`)
2. **Sign Up / Sign In**:
   * `POST /api/auth/signup` → `POST /api/auth/signin` → Save `token` in `localStorage`.
   * Fetch current user role: `GET /api/users/me` with `Authorization: Bearer <token>`.
3. **Build Cart**:
   * Add item: `POST /api/cart/items` (`{ foodItemId, quantity }`)
   * View/adjust cart: `GET /api/cart`, `PUT /api/cart/items/{foodItemId}?quantity=...`
4. **Checkout & Pay**:
   * Place order from cart: `POST /api/orders` (`{ deliveryAddress }`) → receives `OrderResponse` with `id` and `totalAmount`.
   * Submit payment: `POST /api/payments` (`{ orderId, paymentMethod }`) → receives `PaymentResponse`.
5. **Track Orders**:
   * View order history & live status: `GET /api/orders/my`.

### Admin Flow
1. **Sign In & Verify Role**:
   * `POST /api/auth/signin` → `GET /api/users/me` confirms `role === "ADMIN"`.
2. **Manage Menu**:
   * Create/edit categories (`POST /api/categories`, `PUT /api/categories/{id}`)
   * Create/edit food items (`POST /api/foods`, `PUT /api/foods/{id}`) or toggle stock (`PATCH /api/foods/{id}/status?status=OUT_OF_STOCK`).
3. **Manage Orders & Payments**:
   * View incoming orders: `GET /api/orders` (or `GET /api/orders?status=PLACED`)
   * Advance order status: `PATCH /api/orders/{id}/status` (`PREPARING` → `DELIVERED`)
   * Mark cash-on-delivery payments as completed: `PATCH /api/payments/{id}/status` (`{ "status": "COMPLETED" }`).
