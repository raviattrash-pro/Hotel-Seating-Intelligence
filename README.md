# Hotel Seating Intelligence

Full-stack hotel/table management application with React, Spring Boot, and MySQL.

The system models real floor seating, online/offline bookings, refundable booking deposits, zone preferences such as family/smoking/bar seating, queue pressure, occupancy prediction, and admin analytics for overbooking and low-crowd offers.

## Tech Stack

- Frontend: React + Vite
- Backend: Spring Boot 3
- Database: MySQL in production, H2 profile for quick local demo
- Analytics: moving-average forecasting, queue pressure, overbooking risk, and off-peak opportunity scoring

## Project Structure

```text
backend/   Spring Boot API and persistence
frontend/  React floor-plan booking UI and admin dashboard
```

## Quick Start

### Backend

```powershell
cd backend
C:\Users\ASUS\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Demo profile uses H2 in-memory data and starts at `http://localhost:8080`.

For MySQL, create a database and run with normal profile:

```sql
CREATE DATABASE hotel_seating;
```

Set these environment variables if needed:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/hotel_seating?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
$env:DB_USER="root"
$env:DB_PASSWORD="password"
```

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

Admin login:

- Admin ID: `admin`
- Password: `password`

Customers do not need login. They directly select a floor, choose a seat/table, enter details, pay the refundable booking amount by UPI, then upload transaction ID and payment screenshot.

## Troubleshooting

### Port 8080 Already In Use

If Spring Boot prints `Web server failed to start. Port 8080 was already in use`, another backend is already running.

Check the process:

```powershell
Get-NetTCPConnection -LocalPort 8080 | Select-Object LocalPort,State,OwningProcess
```

If you want to stop the running backend:

```powershell
Stop-Process -Id <OwningProcess> -Force
```

Or run this backend on another port:

```powershell
C:\Users\ASUS\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=demo" "-Dspring-boot.run.arguments=--server.port=8081"
```

## Core Features

- Interactive real-hotel floor plan with clickable highlighted tables
- Admin-uploadable floor-plan image URL and hotel name branding
- White-label app name: admin enters hotel name and the app title/header changes for that hotel
- Floor-plan upload: admin can upload a JPG/PNG/WebP so clickable tables appear over the real layout
- Multiple floors: each floor can have its own uploaded floor plan and table positions
- Admin seat placement: select a table in admin setup, then click its real position on the uploaded plan
- UPI payment proof: admin uploads QR code and UPI ID; customer uploads payment screenshot and transaction ID
- Table zones: family, smoking, bar, outdoor, quiet/private
- Online/offline booking source control
- `50 INR` refundable booking deposit tracking
- Waitlist recommendations for last-minute guests
- Queueing-theory utilization metrics by time slot
- Forecasted utilization using previous booking history
- Overbooking and off-peak opportunity dashboard
- Seeded sample hotel data for fast demo

## Important APIs

- `GET /api/config`
- `PUT /api/config`
- `GET /api/tables`
- `POST /api/bookings`
- `POST /api/bookings/{id}/check-in`
- `POST /api/bookings/{id}/cancel`
- `GET /api/dashboard?date=YYYY-MM-DD`
- `GET /api/availability?date=YYYY-MM-DD&time=19:00&partySize=4&zone=FAMILY`
