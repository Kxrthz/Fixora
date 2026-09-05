# Setup Guide

## Prerequisites

- Java 21 or newer and Maven 3.9+
- Node 20+ and npm
- MySQL 8+ (or Docker Desktop)

## Configuration

Use `.env.example` as the canonical inventory of environment variables. Never commit `.env`. For a local database, the Docker Compose defaults are intentionally development-only.

Before deploying, replace `JWT_SECRET` with a random secret that is at least 32 bytes and set `CORS_ORIGINS` to the exact Vercel site URL. Create real user accounts rather than relying on seed credentials.

