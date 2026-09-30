# Hotel Booking Platform

A small hotel booking system built as four separate repositories that together form one application: customers can be registered, rooms can be booked, and completed stays can be reviewed.

| Repo | Role | Port | Database |
|---|---|---|---|
| [`customerservice`](https://github.com/Cratairx/customerservice) | Manages customer records | `8080` | `customerdb` (MySQL) |
| [`orderservice`](https://github.com/Cratairx/orderservice) | Manages rooms & bookings | `8081` | `bookingdb` (MySQL) |
| [`reviewservice`](https://github.com/EmilDrougge/reviewservice) | Manages reviews of completed stays | `8082` | `review_db` (MySQL) |
| [`webhotell`](https://github.com/Cratairx/webhotell) | React frontend | `5173` | — |

All three backend services are Spring Boot (Java 17) apps, each with its own MySQL database, and they talk to each other over plain HTTP using each other's Docker service name. The frontend is a React + Vite single-page app that talks to all three backend services directly from the browser.

> **Why port 5173 matters:** each backend service's CORS policy only allows requests from `http://localhost:5173`. The frontend's `vite.config.js` pins the dev server to that exact port (`strictPort: true`) for this reason — if Vite falls back to another port because 5173 is busy, every API call will be blocked by the browser's CORS check.

## 1. How the services fit together

```
                         ┌────────────────────────────┐
                         │   webhotell (React + Vite)  │
                         │   http://localhost:5173     │
                         └──────┬──────────┬──────┬────┘
                                │          │       │
                   (direct browser fetch calls to each service)
                                │          │       │
              ┌─────────────────┘          │       └─────────────────┐
              │                            ▼                         │
              ▼                 ┌────────────────────┐               ▼
     ┌────────────────┐         │  booking-service    │      ┌──────────────────┐
     │ customer-service│◄────────┤  (orderservice)     ├─────►│  review-service   │
     │   :8080         │         │   :8081             │      │  :8082            │
     └────────┬────────┘         └──────────┬──────────┘      └─────────┬─────────┘
              │                             │                            │
              ▼                             ▼                            ▼
        customer-db                    booking-db                  review-db
        (MySQL, customerdb)          (MySQL, bookingdb)          (MySQL, review_db)
```

The frontend does **not** go through a gateway — it calls all three services directly from the browser, using base URLs from its own `.env` file:

```
VITE_CUSTOMER_SERVICE_URL=http://localhost:8080
VITE_BOOKING_SERVICE_URL=http://localhost:8081
VITE_REVIEW_SERVICE_URL=http://localhost:8082
```

Cross-service calls (all plain REST over the Docker network):

- **booking-service → customer-service**: when a booking is created, it calls `GET /api/customer/{id}` to confirm the customer exists before saving the booking.
- **customer-service → booking-service**: when a customer is deleted, it calls `GET /api/bookings/exists?customerId={id}` first — a customer with active bookings cannot be deleted (returns `409 Conflict`).
- **review-service → booking-service**: when a review is created, it calls `GET /api/bookings/getbooking/{id}` to fetch the booking, confirms the review's `customerId` matches the booking's owner, confirms the stay's `endDate` has already passed, and confirms a review doesn't already exist for that booking — otherwise it rejects with `403 Forbidden`.

Because of this, **booking-service needs customer-service reachable, and review-service needs booking-service reachable** — start them in that order (the Docker Compose file below handles this automatically).

## 2. Prerequisites

- Docker and Docker Compose (for the three backend services)
- Node.js (for the frontend, run outside Docker — see Section 4)
- The four repositories cloned as sibling folders:

```
hotel-app/
├── customerservice/
├── orderservice/
├── reviewservice/
├── webhotell/
└── docker-compose.yml   ← the file from this README
```

```bash
mkdir hotel-app && cd hotel-app
git clone https://github.com/Cratairx/customerservice.git
git clone https://github.com/Cratairx/orderservice.git
git clone https://github.com/EmilDrougge/reviewservice.git
git clone https://github.com/Cratairx/webhotell.git
```

Each backend repo already ships its own `Dockerfile` (multi-stage Maven build → runnable jar), so no local Java/Maven install is required — Docker handles the build.

## 3. Running the whole stack in Docker

Save the following as `docker-compose.yml` at the root of `hotel-app/` (next to the three cloned service folders). It builds all three services and their databases, and names each container to match the hostnames the code already expects (`customer-db`, `customer-service`, `booking-db`, `booking-service`, `review-db`, `review-service`), so no extra environment variables are needed for the services to find each other.

```yaml
services:

  # ---------- Customer Service ----------
  customer-db:
    image: mysql:8
    container_name: customer-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: customerdb
    ports:
      - "3306:3306"
    volumes:
      - customer-db-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot"]
      interval: 5s
      timeout: 5s
      retries: 20

  customer-service:
    build: ./customerservice
    container_name: customer-service
    ports:
      - "8080:8080"
    depends_on:
      customer-db:
        condition: service_healthy

  # ---------- Booking / Order Service ----------
  booking-db:
    image: mysql:8
    container_name: booking-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: bookingdb
    ports:
      - "3307:3306"
    volumes:
      - booking-db-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot"]
      interval: 5s
      timeout: 5s
      retries: 20

  booking-service:
    build: ./orderservice
    container_name: booking-service
    ports:
      - "8081:8081"
    depends_on:
      booking-db:
        condition: service_healthy
      customer-service:
        condition: service_started

  # ---------- Review Service ----------
  review-db:
    image: mysql:8
    container_name: review-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: review_db
    ports:
      - "3308:3306"
    volumes:
      - review-db-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot"]
      interval: 5s
      timeout: 5s
      retries: 20

  review-service:
    build: ./reviewservice
    container_name: review-service
    ports:
      - "8082:8082"
    depends_on:
      review-db:
        condition: service_healthy
      booking-service:
        condition: service_started

volumes:
  customer-db-data:
  booking-db-data:
  review-db-data:
```

Then, from `hotel-app/`:

```bash
docker compose up --build
```

This will:
1. Build the three Spring Boot jars inside Docker (Maven, Java 17).
2. Start the three MySQL databases and wait for them to report healthy.
3. Start `customer-service`, then `booking-service`, then `review-service`.

On first boot:
- `customer-service` seeds three sample customers (John Doe, Jane Smith, Bob Johnson).
- `booking-service` seeds three sample rooms (`101`/SINGLE, `102`/SINGLE, `103`/DOUBLE).

Once everything is up:

| Service | URL |
|---|---|
| Customer Service | http://localhost:8080/api |
| Booking Service | http://localhost:8081/api |
| Review Service | http://localhost:8082/api |

To stop everything: `docker compose down` (add `-v` to also wipe the database volumes).

## 4. Interacting with the app

### 4a. Via the frontend (recommended)

With the three backend services already up via `docker compose up` (Section 3), run the frontend separately with Node — it isn't part of the compose file since it's a dev-server SPA, not something you'd normally containerize for local use:

```bash
cd webhotell
npm install
npm run dev
```

Vite will start on `http://localhost:5173` (pinned in `vite.config.js`) — open that in your browser. The `.env` file already points it at `localhost:8080/8081/8082`, so as long as the backend stack from Section 3 is running, no configuration changes are needed.

The app has three pages, reachable from the nav bar:

| Page | Route | What it does |
|---|---|---|
| **Customers** | `/customers` | List, create, edit, and delete customers (backed by customer-service) |
| **Rooms** | `/rooms` | List, create, edit, and delete rooms (backed by booking-service) |
| **Bookings** | `/bookings` | Check room availability for a date range, create/update/delete bookings, and — once a booking's `endDate` has passed — leave a review for that stay directly from the booking row |

A few behaviors worth knowing about while clicking around:
- Deleting a customer who still has bookings will show an error (the backend refuses with `409 Conflict`).
- The **Review** button on a booking only appears once its stay has ended, and only shows once per booking — the backend refuses a second review for the same booking, or a review submitted by anyone other than the customer on that booking.
- Any network hiccup (a service not running, wrong port, etc.) surfaces as a status banner rather than a silent failure — if you see "Could not reach http://localhost:808X. Is the service running?", it means that particular backend service isn't up.

### 4b. Directly against the APIs

`curl`, Postman, or Insomnia work too, and are useful for testing the backend independently of the UI. All endpoints below assume the stack from Section 3 is running.

**Customer Service — `http://localhost:8080/api`**

| Method | Path | Body | Notes |
|---|---|---|---|
| GET | `/customers` | — | List all customers |
| GET | `/customer/{id}` | — | Get one customer |
| POST | `/customer` | `{"firstName","lastName","email"}` | Register a customer |
| POST | `/customer/{id}` | `{"firstName","lastName","email"}` | Update a customer |
| POST | `/customer/{id}/delete` | — | Delete a customer (fails with `409` if they have active bookings) |

```bash
curl -X POST http://localhost:8080/api/customer \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com"}'
```

**Booking Service — `http://localhost:8081/api`**

| Method | Path | Params | Notes |
|---|---|---|---|
| GET | `/rooms` | — | List all rooms |
| GET | `/rooms/{id}` | — | Get one room |
| POST | `/rooms` | `roomNumber`, `roomType` (`SINGLE`\|`DOUBLE`) | Create a room |
| PUT | `/rooms/{id}` | `roomNumber`, `roomType` | Edit a room |
| DELETE | `/rooms/{id}` | — | Delete a room |
| GET | `/bookings` | — | List all bookings |
| GET | `/bookings/available` | `startDate`, `endDate` (`YYYY-MM-DD`) | Rooms free for a date range |
| POST | `/bookings` | `customerId`, `roomId`, `startDate`, `endDate` | Create a booking |
| PUT | `/bookings/{id}` | `roomId`, `startDate`, `endDate` | Update a booking |
| DELETE | `/bookings/{id}` | — | Delete a booking |
| GET | `/bookings/getbooking/{id}` | — | Get a booking (looked up by booking id) |
| GET | `/bookings/exists` | `customerId` | Whether a customer has any bookings |

Booking creation/update/room creation take query params, not a JSON body:

```bash
curl -X POST "http://localhost:8081/api/bookings?customerId=1&roomId=1&startDate=2025-01-01&endDate=2025-01-05"
```

**Review Service — `http://localhost:8082/api`**

| Method | Path | Body / Params | Notes |
|---|---|---|---|
| POST | `/reviews` | `{"bookingId","customerId","rating","comment"}` | Review a completed stay |
| GET | `/reviews/room/{roomId}` | — | All reviews for a room |

A review is only accepted if: the booking exists, `customerId` matches the customer on that booking, the stay's `endDate` is in the past, and no review already exists for that booking.

```bash
curl -X POST http://localhost:8082/api/reviews \
  -H "Content-Type: application/json" \
  -d '{"bookingId":1,"customerId":1,"rating":5,"comment":"Great stay!"}'
```

### Typical end-to-end flow

1. `POST /api/customer` (customer-service) → get a `customerId`.
2. `GET /api/bookings/available?startDate=...&endDate=...` (booking-service) → pick a free room.
3. `POST /api/bookings` (booking-service) with that `customerId` and `roomId`.
4. After the `endDate` has passed, `GET /api/bookings/getbooking/{id}` to find the booking id, then `POST /api/reviews` (review-service) referencing it.

## 5. Notes / known quirks

- Each backend repo also ships its own standalone `docker-compose.yml` / `k8s/` manifests for running that single service in isolation during development — these are useful for local iteration on one service, but the compose file in Section 3 is what's needed to run the full application together.
- `customerservice`'s Kubernetes/legacy compose files reference a `booking-service.base-url` property, but that value is currently unused in code — the actual cross-call to booking-service is hardcoded to `http://booking-service:8081`, which is why the container names in this compose file matter.
- The frontend is not included in `docker-compose.yml` on purpose: it's a Vite dev server meant to run on the host so its strict `5173` port requirement (needed for CORS) is simple to guarantee. If you want it containerized too, run `npm run build` inside `webhotell` and serve the resulting `dist/` folder from any static file server or nginx container published on port `5173`.
- Customer update is `POST /api/customer/{id}` (not `PUT`) — this trips people up when testing the API by hand, and the frontend's `customerApi.js` calls this out explicitly for the same reason.

