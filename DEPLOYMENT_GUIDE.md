# Deployment Guide

## Frontend on Vercel

Import the repository, set the root directory to `frontend`, and add `VITE_API_URL=https://your-api.onrender.com/api/v1`. Vercel uses the supplied `vercel.json` to serve SPA routes.

## Backend on Render

Create a Docker web service rooted at `backend` (or apply `render.yaml`). Add MySQL credentials and a production `JWT_SECRET`; set `CORS_ORIGINS` to the Vercel URL. Attach a managed MySQL instance, then deploy. Verify `GET /actuator/health` after deployment.

## Payment and AI providers

The application models and records payment attempts and gives deterministic service recommendations without sending customer data to a third party. Before taking live card, UPI, or wallet payments, integrate a compliant payment service, configure its server-side credentials, implement verified webhooks, and complete your required legal and PCI reviews. Before connecting a hosted AI model, add its credential through the deployment secret store and publish appropriate privacy disclosures.

