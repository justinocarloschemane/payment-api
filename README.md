# Payment API

A RESTful payment API built with Java Spring Boot, PostgreSQL and M-Pesa integration.

## Tech Stack
- Java 21
- Spring Boot 3.5.0
- PostgreSQL
- Flyway (database migrations)
- Docker

## Requirements
- Docker and Docker Compose

## Running with Docker

```bash
docker-compose up --build
```

API will be available at `http://localhost:8080`

## Running locally

```bash
# Start PostgreSQL
sudo service postgresql start

# Run the app
./mvnw spring-boot:run
```

## Endpoints

### Create Payment
POST /payments
Content-Type: application/json
{
"phoneNumber": "841234567",
"amount": 500.00
}

### Get Payment
GET /payments/{id}

### M-Pesa Webhook
POST /webhooks/mpesa
Content-Type: application/json
{
"transactionId": "MPESA-XXXXXXXX",
"status": "COMPLETED"
}

## Payment Status Flow
PENDING → COMPLETED
PENDING → FAILED

## Validation
- Phone number must be a valid Mozambican number (84, 85, 86, 87 prefixes)
- Minimum amount is 1.00 MZN

## M-Pesa Integration
The API integrates with M-Pesa Mozambique (Vodacom).
A mock implementation is provided for testing without credentials.