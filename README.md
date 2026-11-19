
# 🍽️ CampusBite

### A Smart Campus Food Ordering & Management System

CampusBite is a web-based food ordering and management platform designed to simplify campus food services. It allows customers to browse menus, place orders, and track their orders, while providing dedicated dashboards for staff, owners, and administrators to manage food outlets and daily operations.

---

## ✨ Features

### 👨‍🎓 Customer
- User registration and login.
- Browse food menus and available items.
- Place food orders.
- Track order status and queue information.
- Loyalty points and discount coupons.
- Submit food ratings and cooking requests.

### 👨‍🍳 Staff
- View incoming orders.
- Manage assigned outlet orders.
- Update order preparation status.
- Handle order collection.

### 👨‍💼 Owner
- Manage food outlets and menus.
- Manage staff accounts and shifts.
- Monitor orders and inventory.
- Manage suppliers and purchase orders.
- View business dashboard.

### 🛡️ Administrator
- Manage users and outlets.
- Monitor system operations.
- Access administrative dashboards.

---

## ⚙️ How It Works

CampusBite follows a client-server architecture.

1. **Frontend:** Users interact with the application through a web browser.
2. **Backend API:** Spring Boot receives HTTP requests from the frontend.
3. **Authentication:** Spring Security validates JWT tokens and user permissions.
4. **Controller:** Handles incoming requests and delegates operations.
5. **Service Layer:** Implements business logic and application rules.
6. **Repository Layer:** Communicates with MongoDB.
7. **Database:** Stores users, outlets, menu items, orders, and other application data.

### 🔄 Order Workflow

Orders generally follow this lifecycle:

`PENDING → ACCEPTED → PREPARING → READY → DELIVERED`

Customers can track their orders, while staff members update the order status as preparation progresses.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| Frontend | HTML, CSS, JavaScript |
| Backend | Java 21, Spring Boot |
| Database | MongoDB |
| API | REST API |
| Authentication | Spring Security, JWT |
| Build Tool | Maven |

---

## 📁 Project Structure

```text
CampusBite/
│
├── frontend/
│   ├── index.html
│   ├── styles.css
│   ├── app.js
│   └── backend-integration.js
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── pom.xml
│   └── mvnw
│
├── CampusBite.postman_collection.json
├── START_PROJECT.md
└── README.md
```

---

## 🚀 Installation & Setup

### Prerequisites

- Java 21 or above
- MongoDB (Local or Atlas)
- Python 3
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/dhruva-17-tech/CampusBite.git

cd CampusBite
```

### 2. Configure MongoDB

For local MongoDB:

```bash
export MONGODB_URI='mongodb://localhost:27017/campusbite'
```

For MongoDB Atlas, configure your Atlas connection URI instead.

### 3. Start the Backend

```bash
cd backend
```

Configure the required environment variables:

```bash
export JWT_SECRET="$(openssl rand -base64 32)"

export CAMPUSBITE_BOOTSTRAP_OWNER_EMAIL='owner@campusbite.local'

export CAMPUSBITE_BOOTSTRAP_OWNER_PASSWORD='Your-Strong-Password'

export CAMPUSBITE_BOOTSTRAP_OWNER_NAME='CampusBite Owner'
```

Compile and run:

```bash
./mvnw clean compile

./mvnw spring-boot:run
```

Backend runs at:

`http://localhost:8080`

Health check:

`http://localhost:8080/api/health`

### 4. Start the Frontend

Open another terminal:

```bash
cd frontend

python3 -m http.server 5500
```

Open your browser:

`http://localhost:5500`

Keep both servers running while using the application.

---

## 🔌 Main API Endpoints

| Endpoint | Function |
|---|---|
| `/api/health` | Backend health check |
| `/api/auth` | Authentication |
| `/api/users` | User management |
| `/api/menu` | Menu management |
| `/api/orders` | Order management |
| `/api/admin` | Administrative operations |
| `/api/inventory` | Inventory management |
| `/api/suppliers` | Supplier management |
| `/api/purchases` | Purchase orders |
| `/api/shifts` | Staff shift management |
| `/api/ratings` | Food ratings |
| `/api/loyalty` | Loyalty management |
| `/api/cooking-requests` | Cooking requests |

---

## 🔐 Security

- JWT-based authentication.
- Role-based access control.
- Protected backend endpoints.
- Secure password handling.
- Restricted administrative operations.

**Note:** Never upload database credentials, passwords, or JWT secrets to GitHub.

---

## 🧪 API Testing

The project includes a Postman collection:

`CampusBite.postman_collection.json`

Import it into Postman to test the available backend API endpoints.

---

## 🎯 Project Objective

The objective of CampusBite is to digitize campus food services by providing a centralized platform for food ordering, order tracking, outlet management, and inventory operations.

---

### 👥 CampusBite

**Making Campus Food Ordering Smarter, Faster, and Easier.**
