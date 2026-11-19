# Start CampusBite

## Requirements

- macOS with Java 21 or newer
- Maven Wrapper (included in `backend/mvnw`; no global Maven installation needed)
- MongoDB Atlas or a local MongoDB server
- Python 3 (for the frontend server)

## 1. Open a terminal in the project folder


## 2. Start MongoDB

### Option A: MongoDB Atlas

Set the database URI in the terminal that will run the backend:

```bash
export MONGODB_URI='mongodb+srv://<database-user>:<url-encoded-password>@<cluster-host>/campusbite?retryWrites=true&w=majority'
```

Allow your current IP address in Atlas Network Access.

### Option B: Local MongoDB

Start MongoDB and set:

```bash
export MONGODB_URI='mongodb://localhost:27017/campusbite'
```

If MongoDB is managed with Homebrew, it can normally be started with:

```bash
brew services start mongodb-community
```

## 3. Start the backend

Open a terminal and run:

```bash
cd backend
export JWT_SECRET="$(openssl rand -base64 32)"

export CAMPUSBITE_BOOTSTRAP_OWNER_EMAIL='owner@campusbite.local'
export CAMPUSBITE_BOOTSTRAP_OWNER_PASSWORD='Choose-A-Strong-Password'
export CAMPUSBITE_BOOTSTRAP_OWNER_NAME='CampusBite Owner'

./mvnw clean compile
./mvnw spring-boot:run
```

Keep this terminal open. The backend runs at:

- `http://localhost:8080`
- Health check: `http://localhost:8080/api/health`

The owner variables create the first owner account once. If the account already exists, changing these variables does not change its password.

Use a stable `JWT_SECRET` for later backend starts if existing login sessions should remain valid. Do not commit database or password secrets.

## 4. Start the frontend

Open a second terminal:

```bash
cd "/Users/rahi260707/Downloads/CampusBite_Fixed 3/frontend"
python3 -m http.server 5500
```

Keep this terminal open and visit:

- `http://localhost:5500`

## 5. Basic account setup

- Public registration creates **customer** accounts.
- The bootstrap owner account is created from the owner environment variables above.
- Owners can create **owner, staff, and admin** team accounts through the Owner dashboard.
- Staff accounts must be assigned to an outlet.
- The public staff/admin access code defaults to `CAMPUS-STAFF`. It can be changed with `CAMPUSBITE_STAFF_SIGNUP_CODE`.

## 6. Stop the project

Press `Control+C` in both terminal windows.

## Troubleshooting

- **Port 8080 is busy:** stop the process using that port, or change `server.port` in `backend/src/main/resources/application.properties` and update `API_BASE` in `frontend/app.js`.
- **Port 5500 is busy:** start Python on another port, for example `python3 -m http.server 5501`, then open `http://localhost:5501`.
- **Cannot connect to MongoDB:** verify `MONGODB_URI`, MongoDB is running, the Atlas password is URL-encoded, and your IP is allowed by Atlas.
- **Login session problems:** use the same `JWT_SECRET` on repeated starts, or log in again after changing it.
- **Maven dependency errors:** check the internet connection and run `./mvnw clean compile` again.
