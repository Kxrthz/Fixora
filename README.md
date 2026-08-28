# Fixora

Fixora is a full-stack marketplace for booking trusted home-service professionals. The repository contains a React + TypeScript single-page application and a Java Spring Boot REST API backed by MySQL.

## Included

- Apple-inspired responsive interface with light/dark themes and motion.
- JWT access and refresh-token authentication, BCrypt passwords, and role protection.
- Service discovery, provider profiles, bookings, booking status, chat, notifications, payments, and provider/admin metrics.
- Flyway migrations with normalized MySQL tables, indexes, seed data, a reporting view, procedure, and trigger.
- Vercel SPA routing, Render Docker deployment, environment templates, and local Docker Compose for MySQL.

## Run locally

1. Copy `.env.example` to `.env` and set a strong `JWT_SECRET` of 32+ characters.
2. Start MySQL: `docker compose up -d mysql`.
3. In `backend`, run `mvn spring-boot:run`.
4. In `frontend`, run `npm install --cache .npm-cache` then `npm run dev`.
5. Visit `http://localhost:5173`.

The frontend uses `VITE_API_URL=http://localhost:8080/api/v1` by default. Database migrations run automatically at startup.

## Seed accounts

The seed providers are `arjun@fixora.local` and `priya@fixora.local`; the administrator is `admin@fixora.local`. All use the password `password`. Change or remove these accounts before any public deployment.

## Quality checks

```sh
cd frontend && npm run build
cd ../backend && mvn test
```

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md), [SETUP_GUIDE.md](SETUP_GUIDE.md), and [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for the complete operational guide.

