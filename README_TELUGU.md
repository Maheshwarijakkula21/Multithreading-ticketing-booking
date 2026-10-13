# Ticket Booking System — Java Spring Boot + MySQL

## Requirements
- JDK 17+
- MySQL Server + MySQL Workbench
- Eclipse / Spring Tool Suite
- Maven (or add Maven Wrapper)

## Step 1 — Database
Open MySQL Workbench and run `database/schema.sql`. It creates `ticket_booking` only if missing and does not drop existing data.

## Step 2 — Configure password
Open `src/main/resources/application.properties`, replace `CHANGE_ME` with your MySQL password.

## Step 3 — Import into Eclipse
File → Import → Maven → Existing Maven Projects → select this extracted folder containing `pom.xml` → Finish. Wait for dependencies.

## Step 4 — Run backend
Run `TicketBookingSystemApplication.java` as Java Application, or run `mvn spring-boot:run` in PowerShell from the folder containing `pom.xml`.

If port 8082 is busy, stop your old Spring Boot instance or change `server.port`.

## Step 5 — Open
Visit `http://localhost:8082/`. The frontend is served by Spring Boot (same origin).

## Step 6 — Create customer and admin
Register a customer through the UI. For local learning only, promote a user in MySQL Workbench:
`USE ticket_booking; UPDATE users SET role='ADMIN' WHERE username='your_username';`
Logout and log in again. Never expose this database command to public users.

## API
- POST `/api/auth/register`: `{ "name":"Alex", "username":"alex", "password":"StrongPass123!" }`
- POST `/api/auth/login`: `{ "username":"alex", "password":"StrongPass123!" }`
- POST `/api/auth/logout`
- GET `/api/auth/me`
- GET `/api/events`
- GET `/api/events/{id}/seats`
- POST `/api/events` (ADMIN): `{ "name":"Movie", "venue":"Screen 1", "eventDate":"2026-12-20T18:30:00", "price":250, "seatCount":40 }`
- POST `/api/bookings`: `{ "eventId":1, "seatIds":[1,2] }`
- GET `/api/bookings/my`
- GET `/api/admin/bookings` (ADMIN)

## Concurrency
Booking is transactional. It obtains pessimistic write locks on requested seat rows, verifies that all seats belong to the chosen event and are AVAILABLE, then marks seats BOOKED and creates bookings. Unique constraints on seat number per event and on booking seat_id add database-level protection. Concurrent requests for the same seat cannot both successfully book it.

## Existing bookings table warning
If an old database has an incompatible `bookings` table, `ddl-auto=update` may fail. Back up data and inspect `SHOW CREATE TABLE bookings;` before altering it. Do not drop tables blindly.

## Security note
This is an educational starter. It uses BCrypt password hashing and server-side HTTP sessions. CSRF is disabled for simplicity; enable CSRF protection and use HTTPS before production. Production admin provisioning, audit logs, rate limiting, payment integration, and deployment hardening are still needed.
