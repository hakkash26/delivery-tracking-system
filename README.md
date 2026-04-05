# 🚚 Delivery Tracking System
**Agile DevOps Course — Project 41**

A Spring Boot REST API for tracking deliveries through status lifecycle stages, with full CI/CD via Jenkins, Docker, and Kubernetes.

---

## 📁 Project Structure

```
delivery-tracking-system/
├── src/
│   ├── main/java/com/delivery/
│   │   ├── DeliveryTrackingApplication.java   ← Spring Boot entry point
│   │   ├── model/
│   │   │   ├── Delivery.java                  ← JPA Entity
│   │   │   └── DeliveryStatus.java            ← Enum with transition rules
│   │   ├── service/
│   │   │   ├── DeliveryRepository.java        ← Spring Data JPA
│   │   │   └── DeliveryService.java           ← Business logic
│   │   ├── controller/
│   │   │   ├── DeliveryController.java        ← REST endpoints
│   │   │   └── StatusUpdateRequest.java       ← Request DTO
│   │   └── exception/
│   │       ├── DeliveryNotFoundException.java
│   │       └── InvalidStatusTransitionException.java
│   └── test/java/com/delivery/
│       ├── DeliveryServiceTest.java           ← Unit tests (Mockito)
│       └── DeliveryControllerIntegrationTest.java ← Integration tests
├── k8s/
│   ├── namespace.yaml
│   ├── deployment.yaml
│   └── service.yaml
├── jenkins/
│   └── docker-compose-jenkins.yml
├── Dockerfile
├── Jenkinsfile
├── docker-compose.yml
└── pom.xml
```

---

## 🔄 Delivery Status Flow

```
PENDING ──→ PICKED_UP ──→ IN_TRANSIT ──→ OUT_FOR_DELIVERY ──→ DELIVERED
   │              │               │                │
   └──→ CANCELLED └──→ FAILED     └──→ FAILED      └──→ FAILED
```
**Terminal states:** DELIVERED, FAILED, CANCELLED — no further updates allowed.

---

## 🌐 REST API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET    | `/api/deliveries/health`         | Health check |
| POST   | `/api/deliveries`                | Create delivery |
| GET    | `/api/deliveries`                | Get all deliveries |
| GET    | `/api/deliveries/{trackingNo}`   | Get by tracking number |
| GET    | `/api/deliveries/status/{status}`| Filter by status |
| PUT    | `/api/deliveries/{trackingNo}/status` | Update status |
| DELETE | `/api/deliveries/{trackingNo}`   | Delete (PENDING/CANCELLED only) |

### Example: Create Delivery
```bash
curl -X POST http://localhost:8080/api/deliveries \
  -H "Content-Type: application/json" \
  -d '{
    "trackingNumber": "TRK-2024-001",
    "recipientName": "John Doe",
    "deliveryAddress": "42 Main Street, Chennai"
  }'
```

### Example: Update Status
```bash
curl -X PUT http://localhost:8080/api/deliveries/TRK-2024-001/status \
  -H "Content-Type: application/json" \
  -d '{"status": "PICKED_UP", "remarks": "Picked up from warehouse"}'
```

---

## 🛠️ STEP-BY-STEP SETUP GUIDE

---

### STEP 1 — Prerequisites

Make sure you have installed:
- ✅ **Java 17** — https://adoptium.net
- ✅ **Maven 3.9+** — https://maven.apache.org/download.cgi
- ✅ **Docker Desktop** — https://www.docker.com/products/docker-desktop (you already have this)
- ✅ **Jenkins** — (you already have this — OR use Docker method below)
- ✅ **kubectl** — https://kubernetes.io/docs/tasks/tools/

Verify everything works:
```bash
java -version
mvn -version
docker --version
kubectl version --client
```

---

### STEP 2 — Enable Kubernetes in Docker Desktop

1. Open **Docker Desktop**
2. Go to ⚙️ **Settings → Kubernetes**
3. Check ✅ **Enable Kubernetes**
4. Click **Apply & Restart**
5. Wait until the green Kubernetes dot appears in Docker Desktop's bottom bar

Test it:
```bash
kubectl get nodes
# Should show:   docker-desktop   Ready
```

---

### STEP 3 — Run the App Locally (no Docker yet)

```bash
# From project root folder
mvn spring-boot:run
```

Open browser: http://localhost:8080/api/deliveries/health
You should see: `{"status":"UP","service":"Delivery Tracking System"}`

Also visit H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:deliverydb`
- Username: `sa` | Password: (blank)

---

### STEP 4 — Run JUnit Tests

```bash
mvn test
```

Expected output:
```
Tests run: 25+, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Test reports are saved to: `target/surefire-reports/`

---

### STEP 5 — Build Docker Image

```bash
# Build the image
docker build -t delivery-tracking-system:latest .

# Verify it was created
docker images | grep delivery-tracking-system
```

---

### STEP 6 — Run with Docker

```bash
docker run -d \
  --name delivery-app \
  -p 8080:8080 \
  delivery-tracking-system:latest

# Check logs
docker logs delivery-app -f

# Stop when done
docker stop delivery-app && docker rm delivery-app
```

Or use Docker Compose:
```bash
docker-compose up -d
docker-compose logs -f
docker-compose down
```

---

### STEP 7 — Push to Docker Hub (needed for Kubernetes + Jenkins)

1. Sign up at https://hub.docker.com (free)
2. Create a public repository named `delivery-tracking-system`

```bash
# Login
docker login

# Tag image with your username
docker tag delivery-tracking-system:latest YOUR_USERNAME/delivery-tracking-system:latest

# Push
docker push YOUR_USERNAME/delivery-tracking-system:latest
```

3. **Update these files with your Docker Hub username:**
   - `k8s/deployment.yaml` → line with `image:`
   - `Jenkinsfile` → `DOCKER_REGISTRY` variable

---

### STEP 8 — Deploy to Kubernetes

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Deploy the app
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Watch pods start up
kubectl get pods -n delivery-app -w
```

Wait until pods show `Running`:
```
NAME                                  READY   STATUS    RESTARTS
delivery-tracking-6f8d9b-xxx          1/1     Running   0
delivery-tracking-6f8d9b-yyy          1/1     Running   0
```

Access the app via Kubernetes:
```
http://localhost:30080/api/deliveries/health
```

Useful kubectl commands:
```bash
kubectl get all -n delivery-app                      # See everything
kubectl describe pod -n delivery-app                 # Pod details
kubectl logs -n delivery-app -l app=delivery-tracking-system  # View logs
kubectl delete -f k8s/                               # Remove deployment
```

---

### STEP 9 — Set Up Jenkins Pipeline

#### Option A — Jenkins already installed locally

1. Open Jenkins → http://localhost:8080 (or whichever port yours runs on)
2. Install these plugins (Manage Jenkins → Plugins):
   - **Pipeline**
   - **Git**
   - **Docker Pipeline**
   - **Kubernetes CLI**
3. Configure tools (Manage Jenkins → Tools):
   - JDK: Name = `JDK-17`, JAVA_HOME = your Java path
   - Maven: Name = `Maven-3.9`, auto-install or set path
4. Add Credentials (Manage Jenkins → Credentials → Global):
   - **Docker Hub**: Kind = Username/Password, ID = `dockerhub-credentials`
   - **Kubeconfig**: Kind = Secret File, ID = `kubeconfig-credentials`
     - File = your kubeconfig at `~/.kube/config`

#### Option B — Run Jenkins via Docker (easiest)

```bash
docker-compose -f jenkins/docker-compose-jenkins.yml up -d
```

Access Jenkins at: http://localhost:8081

Get initial admin password:
```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

#### Create the Pipeline Job

1. Click **New Item** → Enter name `delivery-tracking-pipeline` → Select **Pipeline**
2. Under **Pipeline** section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: your Git repo URL (or use local path)
   - Script Path: `Jenkinsfile`
3. Click **Save** then **Build Now**

---

### STEP 10 — Verify Full Pipeline

After Jenkins runs successfully, you should see all 6 stages green:

```
✅ Checkout
✅ Build & Test
✅ Package
✅ Docker Build
✅ Docker Push
✅ Deploy to Kubernetes
```

Final verification:
```bash
kubectl get pods -n delivery-app
curl http://localhost:30080/api/deliveries/health
```

---

## 📊 JUnit Test Coverage

| Test Class | Tests | What it Tests |
|------------|-------|---------------|
| `DeliveryServiceTest` | 20+ | Unit tests — valid transitions, invalid transitions, CRUD |
| `DeliveryControllerIntegrationTest` | 9 | End-to-end REST API tests |

Key test scenarios:
- ✅ All valid status transitions (PENDING→PICKED_UP→IN_TRANSIT→etc.)
- ✅ 21 invalid transition combinations (parameterized)
- ✅ Terminal states (DELIVERED, FAILED, CANCELLED)
- ✅ Duplicate tracking number
- ✅ Not found (404)
- ✅ Integration: create → update → validate → error handling

---

## ❓ Troubleshooting

| Problem | Fix |
|---------|-----|
| Port 8080 busy | Change `server.port=9090` in application.properties |
| Docker build fails | Run `mvn package -DskipTests` first, then `docker build` |
| Kubernetes pods not starting | Run `kubectl describe pod -n delivery-app` for details |
| Jenkins can't run Docker | Make sure `/var/run/docker.sock` is mounted (see jenkins compose file) |
| kubectl not found in Jenkins | Install **Kubernetes CLI** Jenkins plugin |
