# API Documentation

Base URL: `/api/v1`. All protected endpoints require `Authorization: Bearer <accessToken>`.

| Area | Endpoints |
| --- | --- |
| Authentication | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh` |
| Marketplace | `GET /services`, `GET /providers`, `GET /users/me` |
| Bookings | `GET /bookings/me`, `POST /bookings`, `PATCH /bookings/{id}/status` |
| Chat | `GET/POST /bookings/{id}/messages` |
| Notifications | `GET /notifications`, `PATCH /notifications/{id}/read` |
| Payments | `POST /bookings/{id}/payment` |
| AI concierge | `POST /assistant` |
| Role dashboards | `GET /provider/dashboard`, `GET /admin/overview` |

`POST /bookings` expects `serviceId`, `providerId`, `address`, `scheduledAt` (ISO local date-time), and an optional `notes`. Validation errors are returned as a JSON object containing `message`.

