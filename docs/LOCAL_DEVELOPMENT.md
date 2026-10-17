# Local Development Guide for PulseCart

This guide provides instructions for setting up, running, testing, and debugging the PulseCart platform on your local workstation.

---

## Prerequisites

- **Java Development Kit**: JDK 21 LTS or JDK 26
- **Build Tool**: Apache Maven 3.9+
- **Container Engine**: Docker Desktop or Docker Engine with Docker Compose v2
- **Node.js**: Node.js v20+ and npm v10+
- **Kubernetes CLI (Optional)**: `kubectl` v1.28+ and `helm` v3+

---

## 1. Starting Isolated Infrastructure Services

PulseCart uses a strictly project-scoped Docker Compose configuration (`name: pulsecart`).

To launch the backing infrastructure (PostgreSQL 16, Redis 7, Apache Kafka 4.3.1 in KRaft mode):

```powershell
docker compose -p pulsecart -f compose.yaml --profile commerce up -d
```

To verify the health of the isolated containers:

```powershell
docker compose -p pulsecart ps
```

Expected output:
- `pulsecart-postgres` — port 5432 (healthy)
- `pulsecart-redis` — port 6379 (healthy)
- `pulsecart-kafka` — port 9092 (healthy)

To include Prometheus and Grafana for observability:

```powershell
docker compose -p pulsecart -f compose.yaml --profile observability up -d
```

---

## 2. Compiling and Testing Backend Microservices

To build the entire multi-module monorepo and execute the automated test suite across all 15 services:

```powershell
mvn clean test
```

To run the Flash-Sale High-Concurrency Benchmark suite:

```powershell
mvn test -Dtest=FlashSaleBenchmarkSuite -pl services/inventory-service -am
```

---

## 3. Running Frontend Applications

### PulseCart Store (Port 3000)
The customer-facing commerce experience:

```powershell
cd frontends/store
npm install
npm run dev
```

Visit: `http://localhost:3000`

### PulseCart Studio (Port 3001)
The microservices architecture and failure laboratory:

```powershell
cd frontends/studio
npm install
npm run dev
```

Visit: `http://localhost:3001`

---

## 4. Kubernetes Local Deployment (Optional)

Deploying with standalone Kustomize manifests:

```powershell
kubectl apply -k infrastructure/kubernetes/base
```

Or deploying via Helm:

```powershell
helm install pulsecart infrastructure/helm/pulsecart -n pulsecart --create-namespace
```

---

## 5. Stopping the Environment

To stop the containers safely without deleting persistent volumes:

```powershell
docker compose -p pulsecart stop
```

To shut down containers and networks cleanly:

```powershell
docker compose -p pulsecart down
```
