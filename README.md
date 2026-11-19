🍽️ CampusBite
CampusBite is a web-based campus food ordering and management system. It connects customers with campus food outlets and provides separate tools for staff, owners, and administrators to manage orders and day-to-day operations.
✨ Features
Customer
- Register, log in, and browse food items by outlet.
- Place orders and view personal orders.
- Follow order progress and view outlet queue information.
- View loyalty information, submit food ratings, and send cooking requests.
Staff
- View orders and queue information for an outlet.
- Update order status as orders move through preparation and collection.
Owner and Admin
- Manage outlets and team accounts.
- View dashboard information and low-stock items.
- Manage menu availability and stock.
- Manage suppliers, purchase orders, and staff shifts.
Access to management features depends on the signed-in user's role and permissions.
🔄 How CampusBite works
1. A customer, staff member, owner, or admin uses the frontend in a web browser.
2. The frontend sends HTTP requests to the Spring Boot backend through REST API endpoints.
3. After login, the frontend stores the returned JWT and sends it with protected requests as a Bearer token.
4. Spring Security checks authentication and access permissions.
5. The backend applies the application logic and reads or updates the MongoDB database.
6. The backend returns a response, and the frontend refreshes the relevant information.
Order lifecycle
A typical order progresses through these statuses:

PENDING → ACCEPTED → PREPARING → READY → DELIVERED

An order may also be cancelled when cancellation is permitted. Customers can check their orders and queue information, while outlet staff handle order preparation and status updates.
🧰 Technology stack
Part	Technologies
Frontend	HTML, CSS, JavaScript
Backend	Java 21, Spring Boot
API	REST, HTTP, JSON
Database	MongoDB
Authentication	Spring Security, JWT
Build	Maven Wrapper


📁 Project structure
CampusBite/
├── frontend/
│   ├── index.html
│   ├── styles.css
│   ├── app.js
│   └── backend-integration.js
├── backend/
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── pom.xml
│   └── mvnw
├── .github/
├── CampusBite.postman_collection.json
├── START_PROJECT.md
└── README.md
- frontend/ — user interface, styling, and JavaScript used to communicate with the backend.
- backend/ — Spring Boot application, REST controllers, services, security, and MongoDB integration.
- CampusBite.postman_collection.json — Postman collection for testing API requests.
- START_PROJECT.md — additional project startup instructions.
🚀 Run the project locally
Prerequisites
- Java 21 or newer
- MongoDB Atlas account or a local MongoDB server
- Python 3
- Internet connection for downloading Maven dependencies
The Maven Wrapper is included, so a separate Maven installation is not required.
1. Get the project
Clone the repository and enter the project directory:
git clone https://github.com/dhruva-17-tech/CampusBite.git
cd CampusBite
2. Configure MongoDB
Choose either MongoDB Atlas or a local MongoDB server.

MongoDB Atlas

Set your connection URI in the terminal that will run the backend:
export MONGODB_URI='mongodb+srv://<username>:<password>@<cluster-host>/campusbite?retryWrites=true&w=majority'
Replace the placeholders with your own Atlas details. Allow your IP address in Atlas Network Access and URL-encode special characters in the database password.

Local MongoDB

Start your local MongoDB server and set:
export MONGODB_URI='mongodb://localhost:27017/campusbite'
3. Start the backend
From the project root:
cd backend

export JWT_SECRET="$(openssl rand -base64 32)"
export CAMPUSBITE_BOOTSTRAP_OWNER_EMAIL='owner@campusbite.local'
export CAMPUSBITE_BOOTSTRAP_OWNER_PASSWORD='Choose-A-Strong-Password'
export CAMPUSBITE_BOOTSTRAP_OWNER_NAME='CampusBite Owner'

./mvnw clean compile
./mvnw spring-boot:run
Keep this terminal open. The backend runs at:
- API base: http://localhost:8080
- Health check: http://localhost:8080/api/health
The bootstrap settings create the initial owner account if it does not already exist. They do not reset the password of an existing account. Use a strong password and keep your JWT secret stable between restarts if you want existing sessions to remain valid.
4. Start the frontend
Open a second terminal. From the project root:
cd frontend
python3 -m http.server 5500
Open the frontend in your browser:

http://localhost:5500

Keep both terminals running while using the application.
5. Set up accounts
- Public registration creates customer accounts.
- The initial owner account is configured using the bootstrap environment variables in Step 3.
- Owners can create team accounts through the Owner dashboard.
- Staff accounts need to be assigned to an outlet.
- The staff/admin registration access code defaults to CAMPUS-STAFF. It can be changed using the CAMPUSBITE_STAFF_SIGNUP_CODE environment variable.
🔌 Main API areas
The backend exposes REST endpoints under /api.
Endpoint	Purpose
/api/health	Backend health check
/api/auth	Registration and login
/api/users	Current-user information
/api/menu	Browse and manage menu items
/api/orders	Place orders, view orders, queues, and update order status
/api/admin	Admin dashboard, outlets, and team-user management
/api/inventory	Stock, restocking, and availability
/api/suppliers	Supplier management
/api/purchases	Purchase-order management
/api/shifts	Staff shift management
/api/ratings	Food ratings
/api/loyalty	Customer loyalty information
/api/cooking-requests	Cooking requests


For detailed request formats, use the included Postman collection and the backend controller classes.
🔐 Configuration and security
The backend supports these environment variables:
Variable	Purpose
MONGODB_URI	MongoDB connection string
JWT_SECRET	Secret used to sign JWTs
CAMPUSBITE_BOOTSTRAP_OWNER_EMAIL	Initial owner email
CAMPUSBITE_BOOTSTRAP_OWNER_PASSWORD	Initial owner password
CAMPUSBITE_BOOTSTRAP_OWNER_NAME	Initial owner display name
CAMPUSBITE_STAFF_SIGNUP_CODE	Staff/admin registration access code


Never commit real database credentials, passwords, JWT secrets, or other private configuration to GitHub.
🛠️ Troubleshooting
- MongoDB connection error: Check MONGODB_URI, confirm MongoDB is running, and verify Atlas Network Access settings.
- Backend port is busy: Port 8080 is already in use. Stop the other process or change the backend server port and frontend API configuration.
- Frontend port is busy: Use another port, for example python3 -m http.server 5501.
- Login/session issue after restart: Keep the same JWT_SECRET between backend starts, or log in again after changing it.
- Maven build issue: Check your internet connection and retry ./mvnw clean compile.
To stop either server, press Control + C in its terminal.
CampusBite — making campus food ordering and outlet management simpler.
