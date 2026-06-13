# Reminder App — Spring Boot Backend

## Stack
- Java 17 + Spring Boot 3.2
- PostgreSQL (via JPA/Hibernate)
- JWT Authentication
- WebSocket (STOMP) — in-app real-time notifications
- Web Push (VAPID) — browser push when app is closed
- Twilio — SMS notifications
- Spring Scheduler — checks due reminders every 60 seconds

---

## Setup

### 1. PostgreSQL
```sql
CREATE DATABASE reminderdb;
```

### 2. application.properties
Fill in these values in `src/main/resources/application.properties`:
```
spring.datasource.password=YOUR_POSTGRES_PASSWORD
app.jwt.secret=YOUR_64_CHAR_HEX_SECRET
app.vapid.public-key=YOUR_VAPID_PUBLIC_KEY
app.vapid.private-key=YOUR_VAPID_PRIVATE_KEY
twilio.account.sid=YOUR_TWILIO_SID       (optional)
twilio.auth.token=YOUR_TWILIO_TOKEN      (optional)
```

### 3. Generate VAPID keys
Visit https://vapidkeys.com — copy the public and private keys into properties.

### 4. Run locally
```bash
mvn spring-boot:run
```
Server starts at http://localhost:8080

---

## API Endpoints

### Auth
| Method | URL | Body | Description |
|--------|-----|------|-------------|
| POST | /api/auth/register | email, password, fullName, phoneNumber | Register |
| POST | /api/auth/login | email, password | Login → returns JWT |

### Reminders (all require `Authorization: Bearer <token>`)
| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/reminders | All reminders |
| GET | /api/reminders/upcoming | Future pending reminders |
| POST | /api/reminders | Create reminder |
| PUT | /api/reminders/{id} | Update reminder |
| DELETE | /api/reminders/{id} | Delete reminder |
| POST | /api/reminders/{id}/dismiss | Dismiss a reminder |

### Notifications
| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/notifications/unread | Unread notifications |
| GET | /api/notifications/count | Unread count |
| POST | /api/notifications/mark-read | Mark all as read |
| POST | /api/notifications/push-subscription | Save browser push subscription |

---

## How reminders fire (the Zomato popup)
1. User creates reminder with `eventAt` and `leadMinutes` (e.g. 10)
2. Backend calculates `remindAt = eventAt - 10 minutes`
3. `ReminderScheduler` runs every 60s, finds all reminders where `remindAt <= now`
4. For each due reminder it fires:
   - **WebSocket** → React app receives popup in real time
   - **Web Push** → service worker shows OS notification even if browser closed
   - **SMS** → Twilio sends text message (if phone number set)

---

## Deploy to Railway
```bash
# Push to GitHub, then in Railway:
# New Project → Deploy from GitHub → add PostgreSQL plugin
# Set environment variables from application.properties
```
