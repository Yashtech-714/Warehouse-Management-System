# Run Commands - WMS System

## 1) Backend (Spring Boot)

Open PowerShell:

```powershell
cd "c:\Users\gokul\OneDrive\Desktop\OOAD PROJECT\wms-system\backend"
mvn spring-boot:run
```

If `mvn` is not recognized in the current terminal, use:

```powershell
cd "c:\Users\gokul\OneDrive\Desktop\OOAD PROJECT\wms-system\backend"
& "C:\Users\gokul\tools\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
```

Backend will run at:
- http://localhost:8080

H2 console:
- http://localhost:8080/h2-console

---

## 2) Frontend (React + Vite)

Open a second PowerShell:

```powershell
cd "c:\Users\gokul\OneDrive\Desktop\OOAD PROJECT\wms-system\frontend"
npm install
npm run dev
```

Frontend will run at:
- http://localhost:5173

---

## 3) Production Build (Frontend)

```powershell
cd "c:\Users\gokul\OneDrive\Desktop\OOAD PROJECT\wms-system\frontend"
npm run build
npm run preview
```

Preview URL:
- http://localhost:4173

---

## 4) Quick Verification APIs

```powershell
curl http://localhost:8080/products
curl http://localhost:8080/inventory
curl http://localhost:8080/orders
curl http://localhost:8080/auth/me
```

---

## 5) If Maven is not found

If `mvn` is not recognized, install Maven and add it to PATH, then restart terminal.
