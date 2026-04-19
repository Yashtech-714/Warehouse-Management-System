# 🏭 Warehouse Management System (WMS)

A full-stack Warehouse Management System built with **Spring Boot** (backend) and **React + Vite** (frontend), implementing MVC architecture, SOLID principles, GRASP principles, and key design patterns (Factory, Builder, Strategy, Singleton, Observer, Facade).

---

## 📋 Table of Contents

- [System Requirements](#-system-requirements)
- [Installing Prerequisites](#-installing-prerequisites)
  - [Install Java JDK 17](#1-install-java-jdk-17)
  - [Install Apache Maven](#2-install-apache-maven)
  - [Install Node.js](#3-install-nodejs--npm)
- [Project Structure](#-project-structure)
- [Backend Setup](#-backend-setup)
- [Frontend Setup](#-frontend-setup)
- [Running the Project](#-running-the-project)
- [Default Login Credentials](#-default-login-credentials)
- [Role Responsibilities](#-role-responsibilities)
- [API Overview](#-api-overview)
- [H2 Database Console](#-h2-database-console)
- [Design Patterns Used](#-design-patterns-used)
- [Troubleshooting](#-troubleshooting)

---

## ✅ System Requirements

### Backend Requirements

| Requirement      | Version      | Notes                                          |
|------------------|--------------|------------------------------------------------|
| **Java (JDK)**   | 17 or higher | Spring Boot 3.x requires Java 17+             |
| **Apache Maven** | 3.6 or higher| Used to build and run the Spring Boot app      |

### Frontend Requirements

| Requirement | Version      | Notes                                |
|-------------|--------------|--------------------------------------|
| **Node.js** | 18 or higher | Required to run the Vite dev server  |
| **npm**     | 9 or higher  | Comes bundled with Node.js           |

### Verify Your Installations

```powershell
# Check Java version — must show 17.x.x or higher
java -version

# Check Maven version — must show 3.6+ 
mvn -version

# Check Node.js version — must show 18.x.x or higher
node -version

# Check npm version
npm -version
```

---

## 🛠️ Installing Prerequisites

> Skip any tool you already have installed. Run the verify commands first to check.

---

### 1. Install Java JDK 17

#### Option A — Download from Oracle (Recommended for Windows)

1. Go to: **https://www.oracle.com/java/technologies/downloads/#java17**
2. Under **Windows**, download the **x64 Installer** (`.exe` file)
3. Run the installer and follow the setup wizard (keep all defaults)
4. Java installs to `C:\Program Files\Java\jdk-17.x.x\` by default

#### Option B — Download from Adoptium (Free, no Oracle account needed)

1. Go to: **https://adoptium.net/temurin/releases/?version=17**
2. Select: **Windows → x64 → JDK → .msi**
3. Download and run the `.msi` installer
4. ✅ The installer automatically sets `JAVA_HOME` and updates `PATH`

#### Set JAVA_HOME manually (if not set automatically)

```powershell
# 1. Open Environment Variables:
#    Start → Search "Environment Variables" → "Edit the system environment variables"
#    → "Environment Variables" button

# OR set it for the current PowerShell session only:
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"     # adjust path if needed
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# 2. Verify Java is working:
java -version
# Expected: java version "17.x.x" or openjdk version "17.x.x"
```

---

### 2. Install Apache Maven

> Maven is the build tool used to compile and run the Spring Boot backend.

#### Step 1 — Download Maven

1. Go to: **https://maven.apache.org/download.cgi**
2. Under **Binary zip archive**, download: `apache-maven-3.9.x-bin.zip`
3. Extract the zip to a permanent location, for example:
   ```
   C:\tools\apache-maven-3.9.x\
   ```

#### Step 2 — Set Environment Variables

```powershell
# Open Environment Variables (Start → Search "Environment Variables")
# Add the following:

# New System Variable:
#   Variable name:  MAVEN_HOME
#   Variable value: C:\tools\apache-maven-3.9.x

# Edit the 'Path' System Variable → Add new entry:
#   C:\tools\apache-maven-3.9.x\bin
```

Or set them for the current PowerShell session:

```powershell
$env:MAVEN_HOME = "C:\tools\apache-maven-3.9.x"
$env:PATH = "$env:MAVEN_HOME\bin;$env:PATH"
```

#### Step 3 — Verify Maven installation

```powershell
mvn -version
```

Expected output:
```
Apache Maven 3.9.x (...)
Maven home: C:\tools\apache-maven-3.9.x
Java version: 17.x.x, vendor: ...
```

> ⚠️ Maven requires Java to be installed first. `JAVA_HOME` must be set before Maven will work.

#### Step 4 — Test Maven with the project

```powershell
cd "c:\Users\gokul\OneDrive\Desktop\OOAD PROJECT\wms-system\backend"
mvn clean compile
# Expected: [INFO] BUILD SUCCESS
```

Maven will automatically download all Spring Boot dependencies (~100MB) on first run.

---

### 3. Install Node.js + npm

> Node.js is required to run the React frontend development server.

#### Download and Install

1. Go to: **https://nodejs.org/en/download/**
2. Download the **LTS version** (Long Term Support) — currently v20.x or v18.x
3. Run the `.msi` installer — keep all defaults
4. ✅ npm is included automatically with Node.js

#### Verify Node.js installation

```powershell
node -version
# Expected: v18.x.x or v20.x.x

npm -version
# Expected: 9.x.x or 10.x.x
```

#### Install frontend dependencies

```powershell
cd "c:\Users\gokul\OneDrive\Desktop\OOAD PROJECT\wms-system\frontend"
npm install
# Downloads all packages listed in package.json into node_modules/
```

---

## 📁 Project Structure

```
OOAD PROJECT/
└── wms-system/
    ├── README.md                      ← This file
    ├── backend/                       ← Spring Boot application
    │   ├── pom.xml                    ← Maven dependencies
    │   └── src/main/java/com/wms/
    │       ├── controller/            ← REST controllers (role-guarded)
    │       ├── service/               ← Business logic interfaces + impls
    │       ├── model/                 ← JPA entities (Order, Product, etc.)
    │       ├── repository/            ← Spring Data JPA repositories
    │       ├── factory/               ← Factory pattern implementations
    │       ├── builder/               ← Builder pattern (OrderBuilder)
    │       ├── observer/              ← Observer pattern (NotificationService)
    │       ├── facade/                ← Facade pattern (OrderFacade)
    │       ├── config/                ← CORS and Web configuration
    │       └── init/                  ← DataInitializer (seeds demo data)
    │
    └── frontend/                      ← React + Vite application
        ├── package.json               ← npm dependencies
        └── src/
            ├── api/                   ← Axios API client files per module
            ├── components/            ← Reusable UI components (Sidebar, Navbar)
            ├── context/               ← AuthContext (user session management)
            ├── pages/
            │   ├── manager/           ← ManagerDashboard.jsx
            │   ├── staff/             ← StaffDashboard.jsx
            │   ├── customer/          ← CustomerDashboard.jsx
            │   ├── supplier/          ← SupplierDashboard.jsx
            │   ├── Orders.jsx
            │   ├── Shipment.jsx
            │   ├── Products.jsx
            │   ├── Inventory.jsx
            │   ├── PurchaseOrders.jsx
            │   └── StorageLocations.jsx
            ├── services/              ← Utility services (apiError.js)
            └── styles/                ← app.css (global styles)
```

---

## ⚙️ Backend Setup

### Step 1 — Navigate to backend directory

```powershell
cd "c:\Users\gokul\OneDrive\Desktop\OOAD PROJECT\wms-system\backend"
```

### Step 2 — Verify compile (optional, checks for errors)

```powershell
mvn clean compile
```

Expected output: `[INFO] BUILD SUCCESS`

> Maven automatically downloads all required dependencies from the internet on first run.
> This may take 1–2 minutes the first time.

### Backend Dependencies (auto-managed by Maven via `pom.xml`)

| Dependency                     | Version | Purpose                          |
|--------------------------------|---------|----------------------------------|
| spring-boot-starter-web        | 3.2.5   | Embedded Tomcat + REST API       |
| spring-boot-starter-data-jpa   | 3.2.5   | ORM / Hibernate / JPA            |
| h2                             | Latest  | In-memory database (no install!) |
| spring-boot-starter-validation | 3.2.5   | Bean validation (@Valid)         |
| spring-boot-devtools           | 3.2.5   | Hot reload during development    |
| jackson-databind               | Latest  | JSON serialization               |
| lombok                         | Latest  | Code generation (optional)       |

> **No external database installation needed.**
> H2 is fully embedded — it starts automatically with the backend and resets on restart.

---

## 🖥️ Frontend Setup

### Step 1 — Navigate to frontend directory

```powershell
cd "c:\Users\gokul\OneDrive\Desktop\OOAD PROJECT\wms-system\frontend"
```

### Step 2 — Install npm dependencies

```powershell
npm install
```

This installs all packages from `package.json`:

| Package              | Version  | Purpose                         |
|----------------------|----------|---------------------------------|
| react                | ^18.3.1  | UI component framework          |
| react-dom            | ^18.3.1  | DOM rendering for React         |
| axios                | ^1.9.0   | HTTP client for REST API calls  |
| vite                 | ^5.4.10  | Dev build tool + hot reload     |
| @vitejs/plugin-react | ^4.7.0   | JSX transform plugin for Vite   |

---

## 🚀 Running the Project

> **Important:** Start the **backend first**, then start the frontend.
> Each runs in its own **separate terminal window/tab**.

---

### Terminal 1 — Start Backend

```powershell
cd "c:\Users\gokul\OneDrive\Desktop\OOAD PROJECT\wms-system\backend"
mvn spring-boot:run
```

Wait for this message in the console:
```
Tomcat started on port 8080
Started WmsApplication in X.XXX seconds
```

✅ **Backend URL:** `http://localhost:8080`

---

### Terminal 2 — Start Frontend

```powershell
cd "c:\Users\gokul\OneDrive\Desktop\OOAD PROJECT\wms-system\frontend"
npm run dev
```

Wait for this message:
```
  VITE v5.x.x  ready in XXX ms
  ➜  Local:   http://localhost:5173/
```

✅ **Frontend URL:** `http://localhost:5173`

---

### ⚠️ Important Notes

| Rule | Detail |
|------|--------|
| Open in browser | Always use **http://localhost:5173** — NOT port 8080 |
| Both must run | Keep BOTH terminals open while using the app |
| Data resets | H2 is in-memory: **all data resets every backend restart** |
| Demo data | Auto-seeded on startup — no manual DB setup required |

---

## 🔐 Default Login Credentials

> All users are automatically seeded by `DataInitializer.java` on every backend startup.
> Edit names/passwords there: `backend/src/main/java/com/wms/config/DataInitializer.java`

| Role         | Name (seeded)  | Email                | Password       |
|--------------|----------------|----------------------|----------------|
| **Manager**  | Yashas         | `manager@wms.com`    | `manager123`   |
| **Staff**    | Vinod          | `staff@wms.com`      | `staff123`     |
| **Customer** | Vikas          | `customer@wms.com`   | `customer123`  |
| **Supplier** | Vishwas        | `supplier@wms.com`   | `supplier123`  |

---

## 👥 Role Responsibilities

### 🟦 Manager — Monitoring + Planning
- View all Orders and Shipments (read-only, no actions)
- Manage Products: Create, Update, Soft-Delete (deactivate)
- Manage Storage Locations: Create, Update
- Create Purchase Orders → automatically sent to Supplier
- Dashboard: System overview stats, ⚠️ Low Stock alerts, Pending Stock orders

### 🟩 Staff — Warehouse Operator
- **Process Orders** tab: Process CREATED orders (deducts inventory + auto-creates shipment)
- **Receive Stock** tab: Receive goods from DELIVERED Purchase Orders → adds to inventory
- **Inventory Ops** tab: Assign / Move inventory between storage locations
- **Shipments** page: Mark shipments as `SHIPPED` → `DELIVERED` (also updates linked order)

### 🟧 Customer — Shopper
- **Dashboard**: Place orders via product dropdown (quantity only — no IDs needed)
- **My Orders page** (sidebar): View own orders with real-time status, including ⏳ Out of Stock
- **My Orders page**: Cancel CREATED orders
- Orders with insufficient stock are automatically parked as `PENDING_STOCK` at placement time
- `PENDING_STOCK` orders auto-resume when stock is replenished (no manual action needed)

### 🟪 Supplier — Fulfillment Partner
- **Dashboard**: View all assigned Purchase Orders
- **Dashboard**: Click `Mark Delivered` when goods are dispatched (PO status → `DELIVERED`)
- Staff then receives the stock into the warehouse

---

## 📡 API Overview

Base URL: `http://localhost:8080`

Role enforcement is via the `X-User-Role` HTTP header (automatically set by the frontend on every request).

| Endpoint                            | Method  | Allowed Role(s)     | Description                            |
|-------------------------------------|---------|---------------------|----------------------------------------|
| `/auth/login`                       | POST    | Public              | Login — returns user object            |
| `/orders`                           | GET     | All roles           | Get all orders                         |
| `/orders`                           | POST    | CUSTOMER            | Place new order                        |
| `/orders/{id}`                      | DELETE  | CUSTOMER            | Cancel an order                        |
| `/orders/process/{id}`              | POST    | STAFF               | Process order → deduct stock + shipment|
| `/orders/pending-stock`             | GET     | MANAGER             | Orders waiting for restock             |
| `/products`                         | GET     | All roles           | List all active products               |
| `/products`                         | POST    | MANAGER             | Create product                         |
| `/products/{id}`                    | PUT     | MANAGER             | Update product                         |
| `/products/{id}/deactivate`         | PATCH   | MANAGER             | Soft-delete product                    |
| `/inventory`                        | GET     | All roles           | View inventory stock levels            |
| `/inventory/check`                  | GET     | All roles           | Check stock availability               |
| `/inventory/assign`                 | POST    | STAFF               | Assign item to storage location        |
| `/inventory/move`                   | POST    | STAFF               | Move item between locations            |
| `/storage-locations`                | GET     | All roles           | List all storage locations             |
| `/storage-locations`                | POST    | MANAGER             | Create storage location                |
| `/storage-locations/{id}`           | PUT     | MANAGER             | Update storage location                |
| `/purchase-orders`                  | GET     | All roles           | List all purchase orders               |
| `/purchase-orders`                  | POST    | MANAGER             | Create purchase order                  |
| `/purchase-orders/{id}/deliver`     | PATCH   | SUPPLIER            | Mark PO as delivered                   |
| `/purchase-orders/{id}/receive`     | POST    | STAFF               | Receive stock into inventory           |
| `/shipments`                        | GET     | All roles           | List all shipments                     |
| `/shipments/{id}`                   | GET     | All roles           | Track specific shipment                |
| `/shipments/{id}/ship`              | PATCH   | STAFF               | Mark as SHIPPED (also updates Order)   |
| `/shipments/{id}/deliver`           | PATCH   | STAFF               | Mark as DELIVERED (also updates Order) |
| `/suppliers`                        | GET     | All roles           | List suppliers (for PO dropdown)       |
| `/notifications`                    | GET     | All roles           | System event log (Observer pattern)    |
| `/h2-console`                       | Browser | Dev only            | Database admin console                 |

---

## 🗄️ H2 Database Console

Inspect live database tables in the browser during development.

**URL:** `http://localhost:8080/h2-console`

**Connection settings:**
```
Driver Class:  org.h2.Driver
JDBC URL:      jdbc:h2:mem:wmsdb
User Name:     sa
Password:      (leave completely blank)
```

Click **Connect** to browse all tables including:
`ORDERS`, `ORDER_ITEMS`, `PRODUCTS`, `INVENTORY_ITEMS`, `SHIPMENTS`,
`PURCHASE_ORDERS`, `STORAGE_LOCATIONS`, `CUSTOMERS`, `MANAGERS`, `STAFF`, `SUPPLIERS`

---

## 🏗️ Design Patterns Used

| Pattern      | Location                                                         |
|--------------|------------------------------------------------------------------|
| **Factory**  | `OrderFactory`, `ShipmentFactory`, `PurchaseOrderFactory`        |
| **Builder**  | `OrderBuilder` + `Director` — constructs complex Order objects   |
| **Facade**   | `OrderFacade` — single entry point for create/cancel operations  |
| **Observer** | `NotificationService` + `WmsEvent` — event-driven notifications  |
| **Strategy** | `deductForOrder()` — picks stock across multiple storage locations|
| **Singleton**| All Spring `@Service` beans are singletons by default            |

---

## 🔧 Troubleshooting

### ❌ Port 8080 already in use

```powershell
# Find the process using port 8080
netstat -ano | findstr :8080

# Kill it (replace XXXX with the PID from above)
taskkill /PID XXXX /F

# Retry
mvn spring-boot:run
```

### ❌ Frontend shows blank page or spinner stuck

1. Confirm backend is running: visit `http://localhost:8080` — you should see a 404 JSON message
2. Confirm frontend is running at `http://localhost:5173`
3. Open browser DevTools (F12) → Console tab for errors
4. Hard refresh: `Ctrl + Shift + R`

### ❌ "Network Error" when logging in

The frontend cannot reach the backend. Checklist:
- Is `mvn spring-boot:run` still running in Terminal 1?
- Is the backend on port **8080** (check `application.properties`)?
- Are you opening the app on **port 5173** (not 8080)?

### ❌ Maven build fails with Java errors

```powershell
# Confirm JAVA_HOME is set correctly
echo $env:JAVA_HOME

# If empty or wrong, set it (replace path with your JDK-17 install location)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

### ❌ `npm install` fails

```powershell
# Clear cache and retry
npm cache clean --force
npm install
```

### ❌ Login fails with "Invalid credentials"

- Wait for the backend to **fully start** (look for "Started WmsApplication" in Terminal 1)
- Use exact credentials from the table above: `manager123`, `staff123`, `customer123`, `supplier123`
- The H2 database resets on restart — users are re-seeded automatically

### ❌ "Only STAFF can..." / "Only MANAGER can..." error

This means you are logged in as the wrong role for that action.
Log out and log back in with the correct role account.

---

## 📊 Order Lifecycle (End-to-End)

```
Customer places order
        ↓
createOrder() checks inventory immediately:
    ├── Stock OK   → status: CREATED  (Staff can process right away)
    └── No Stock   → status: PENDING_STOCK (Customer & Manager notified immediately)
                        ↓
              Manager sees it in Dashboard → ⚠️ Action Required
              Manager creates Purchase Order → Supplier marks Delivered
                        ↓
              Staff receives stock → auto-recovery scans PENDING_STOCK orders
              → sufficient stock now? → order: PENDING_STOCK → CREATED
                        ↓
              Staff processes order → Shipment auto-created → PROCESSED
        ↓
Staff marks Shipment as SHIPPED  → Order status: SHIPPED
        ↓
Staff marks Shipment as DELIVERED → Order status: DELIVERED
        ↓
Customer sees: ✅ Delivered  (in My Orders page)
```
