# Smart Campus Sensor & Room Management API

## Overview

The **Smart Campus API** is a RESTful web service built using **JAX-RS (Jersey 2.32)** for the University of Westminster's "Smart Campus" initiative. It provides a robust and scalable interface for managing campus **Rooms**, **Sensors** (e.g., CO₂ monitors, occupancy trackers, smart lighting controllers), and **Sensor Readings**.

The API follows RESTful architectural principles and is designed for use by campus facilities managers and automated building management systems. All data is stored **in-memory** using thread-safe Java data structures (`ArrayList`, `HashMap`, `AtomicInteger`), with no external database dependency.

---

## Architecture & Project Structure

```
SmartCampusAPI/
├── pom.xml                          # Maven build configuration
└── src/main/java/com/smartcampus/
    ├── config/
    │   └── SmartCampusApplication.java   # JAX-RS Application class (@ApplicationPath)
    ├── dao/
    │   └── DataStore.java                # In-memory data store with sample data
    ├── exception/
    │   ├── GenericExceptionMapper.java          # Catch-all 500 mapper
    │   ├── LinkedResourceNotFoundException.java # Custom exception for invalid roomId
    │   ├── LinkedResourceNotFoundExceptionMapper.java  # Maps to 422
    │   ├── RoomNotEmptyException.java           # Custom exception for occupied rooms
    │   ├── RoomNotEmptyExceptionMapper.java     # Maps to 409
    │   ├── SensorUnavailableException.java      # Custom exception for maintenance sensors
    │   └── SensorUnavailableExceptionMapper.java # Maps to 403
    ├── filter/
    │   └── LoggingFilter.java            # Request/Response logging filter
    ├── model/
    │   ├── ErrorMessage.java             # Standardised JSON error response model
    │   ├── Room.java                     # Room POJO
    │   ├── Sensor.java                   # Sensor POJO
    │   └── SensorReading.java            # SensorReading POJO
    └── resource/
        ├── DiscoveryResource.java        # GET /api/v1 (API metadata & links)
        ├── RoomResource.java             # /api/v1/rooms (CRUD operations)
        ├── SensorResource.java           # /api/v1/sensors (CRUD + filtering)
        └── SensorReadingResource.java    # Sub-resource for sensor readings
```

### Technology Stack

| Component        | Technology                   |
|------------------|------------------------------|
| Framework        | JAX-RS (Jersey 2.32)         |
| Build Tool       | Apache Maven                 |
| JSON Binding     | Jackson (jersey-media-json-jackson) |
| DI Container     | HK2 (jersey-hk2)            |
| Servlet Container| Any Java EE / Servlet container (e.g., Apache Tomcat, GlassFish) |
| Java Version     | Java 8+                     |
| Data Storage     | In-memory (HashMap, ArrayList) |

### Core Resource Models

| Model           | Key Fields                                      | Description                              |
|-----------------|------------------------------------------------|------------------------------------------|
| `Room`          | `id`, `name`, `capacity`, `sensorIds`          | Represents a physical room on campus     |
| `Sensor`        | `id`, `type`, `status`, `currentValue`, `roomId` | A sensor device deployed in a room       |
| `SensorReading` | `id`, `timestamp`, `value`                     | A historical reading from a sensor       |

---

## How to Build and Run

### Prerequisites

- **Java 8** (or higher) — JDK installed and `JAVA_HOME` set
- **Apache Maven 3.6+** — installed and available on `PATH`
- **Apache Tomcat 9** (or any Servlet 4.0-compatible container)

### Step 1: Clone the Repository

```bash
git clone https://github.com/<your-username>/SmartCampusAPI.git
cd SmartCampusAPI
```

### Step 2: Build the Project

```bash
mvn clean package
```

This compiles the source code and produces the deployable WAR file at:

```
target/SmartCampusAPI-1.0-SNAPSHOT.war
```

### Step 3: Deploy to Tomcat

1. Copy the generated WAR file into Tomcat's `webapps/` directory:
   ```bash
   cp target/SmartCampusAPI-1.0-SNAPSHOT.war /path/to/tomcat/webapps/
   ```
2. Start Tomcat (if not already running):
   ```bash
   /path/to/tomcat/bin/startup.sh      # Linux/Mac
   /path/to/tomcat/bin/startup.bat     # Windows
   ```
3. The API will be available at:
   ```
   http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1
   ```

> **Tip:** To simplify the URL, rename the WAR file to `smartcampus.war` before deploying. The base URL then becomes `http://localhost:8080/smartcampus/api/v1`.

---

## Sample `curl` Commands

Below are sample `curl` commands demonstrating successful interactions with the API. Replace the base URL as needed for your deployment.

**Base URL:** `http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1`

### 1. Discovery Endpoint — Get API Metadata

```bash
curl -X GET http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1
```

**Expected Response (200 OK):**
```json
{
  "apiName": "Smart Campus API",
  "version": "1.0.0",
  "contact": {
    "name": "Smart Campus Admin",
    "email": "admin@university.edu"
  },
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

### 2. Create a New Room

```bash
curl -X POST http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Library Quiet Study", "capacity": 50}'
```

**Expected Response (201 Created):**
```json
{
  "id": "R3",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": []
}
```

### 3. Get All Rooms

```bash
curl -X GET http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/rooms
```

**Expected Response (200 OK):**
```json
[
  { "id": "R1", "name": "Lecture Hall A", "capacity": 120, "sensorIds": ["S1", "S2"] },
  { "id": "R2", "name": "Lab 3", "capacity": 30, "sensorIds": ["S3"] },
  { "id": "R3", "name": "Library Quiet Study", "capacity": 50, "sensorIds": [] }
]
```

### 4. Create a Sensor (with Room Validation)

```bash
curl -X POST http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "Temperature", "status": "ACTIVE", "currentValue": 22.5, "roomId": "R1"}'
```

**Expected Response (201 Created):**
```json
{
  "id": "S4",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 22.5,
  "roomId": "R1"
}
```

### 5. Get Sensors Filtered by Type

```bash
curl -X GET "http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/sensors?type=CO2"
```

**Expected Response (200 OK):**
```json
[
  { "id": "S1", "type": "CO2", "status": "ACTIVE", "currentValue": 455.0, "roomId": "R1" }
]
```

### 6. Post a Sensor Reading (with Side Effect)

```bash
curl -X POST http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/sensors/S1/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 470.0}'
```

**Expected Response (201 Created):**
```json
{
  "id": "RD4",
  "timestamp": 1745300000000,
  "value": 470.0
}
```

> After this POST, the parent sensor `S1`'s `currentValue` is automatically updated to `470.0`.

### 7. Get Reading History for a Sensor

```bash
curl -X GET http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/sensors/S1/readings
```

**Expected Response (200 OK):**
```json
[
  { "id": "RD1", "timestamp": 1745296400000, "value": 450.0 },
  { "id": "RD2", "timestamp": 1745300000000, "value": 455.0 },
  { "id": "RD4", "timestamp": 1745300000000, "value": 470.0 }
]
```

### 8. Delete a Room (Blocked — 409 Conflict)

```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/rooms/R1
```

**Expected Response (409 Conflict):**
```json
{
  "status": 409,
  "code": "ROOM_NOT_EMPTY",
  "message": "Room R1 cannot be deleted because it has active sensors."
}
```

---

## Report: Coursework Questions & Answers

### Part 1: Service Architecture & Setup

#### Q1.1: Default Lifecycle of a JAX-RS Resource Class

In JAX-RS, the default lifecycle of a resource class is **per-request** (request-scoped). This means that the JAX-RS runtime creates a **new instance** of the resource class for every incoming HTTP request. Once the request has been processed and the response has been sent, the instance is discarded and becomes eligible for garbage collection.

This architectural decision has significant implications for managing in-memory data. Since each request gets its own fresh resource instance, any instance-level fields on the resource class will **not** persist between requests. If data were stored in instance variables, it would be lost as soon as the request completes.

To solve this, our application uses a separate `DataStore` class with **static fields** (`static final List<Room>`, `static final List<Sensor>`, `static final Map<String, List<SensorReading>>`). Because these fields are `static`, they exist at the class level and are shared across all instances and all requests — effectively creating a single source of truth for the entire application.

However, this shared-state design introduces **concurrency and thread-safety concerns**. In a multi-threaded servlet environment, multiple requests may attempt to read and write to the same `ArrayList` or `HashMap` simultaneously, leading to **race conditions** and data corruption. To mitigate this, we employ `AtomicInteger` counters for generating unique IDs (`roomCounter`, `sensorCounter`, `readingCounter`), which guarantee thread-safe atomic increments. For full production readiness, the `ArrayList` and `HashMap` collections could be replaced with their concurrent counterparts such as `CopyOnWriteArrayList` and `ConcurrentHashMap`, or access could be synchronised using `synchronized` blocks.

#### Q1.2: Why is HATEOAS Important?

**HATEOAS** (Hypermedia as the Engine of Application State) is considered the highest level of RESTful API maturity (Level 3 of the Richardson Maturity Model). It dictates that API responses should include hyperlinks that guide the client to available actions and related resources, making the API **self-documenting** and **self-navigable**.

Our Discovery endpoint at `GET /api/v1` implements this principle by returning a JSON object containing navigation links:
```json
{
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

The key benefits over static documentation include:

1. **Reduced Client Coupling:** Clients do not need to hard-code URLs. They can discover endpoints dynamically by following links from the root, meaning the server can restructure its URL paths without breaking clients.
2. **Discoverability:** A new developer can hit the root endpoint and immediately understand what resources are available and how to access them, without needing external documentation.
3. **Evolvability:** The server can add new resources or capabilities over time and expose them through the root metadata. Clients that follow links will automatically discover these additions.
4. **Reduced Errors:** Because URLs are provided by the server rather than constructed by the client, there is less room for typos or malformed paths.

---

### Part 2: Room Management

#### Q2.1: Returning IDs Only vs. Full Room Objects

When returning a list of rooms via `GET /api/v1/rooms`, there are two approaches:

**Returning Full Objects (our approach):**
- **Pros:** The client receives all the data it needs in a single request, which reduces the number of round-trips to the server. This is ideal when clients typically need the full details of each room (e.g., name, capacity) to render a list in a dashboard.
- **Cons:** Larger payload size. If the system scales to thousands of rooms, each with many fields, the response can become very large, increasing bandwidth usage and parsing time.

**Returning IDs Only:**
- **Pros:** Extremely lightweight response — just an array of strings. Excellent for bandwidth-constrained environments or when clients only need to check existence.
- **Cons:** The client must make **N additional HTTP requests** (one per room) to fetch the details, leading to the **N+1 query problem**. This dramatically increases latency and server load.

For our Smart Campus use case, returning full objects is the better choice. The number of rooms on a campus is finite (hundreds, not millions), and facilities dashboards typically need to display names and capacities, so full objects minimise client-side processing and round-trips.

#### Q2.2: Is DELETE Idempotent?

Yes, the `DELETE` operation in our implementation is **idempotent**. Idempotency means that making the same request multiple times produces the same server state as making it once.

Here is what happens when `DELETE /api/v1/rooms/R3` is called multiple times:

1. **First call:** The room `R3` is found, validated to have no sensors, and removed from the `DataStore`. The server returns `204 No Content`. Server state: `R3` no longer exists.
2. **Second call (and all subsequent calls):** The room `R3` is not found in the data store. The server returns `404 Not Found` with a descriptive JSON error message. Server state: unchanged — `R3` still does not exist.

The critical point is that the **server state after the first DELETE and all subsequent DELETEs is identical** — room `R3` does not exist. Although the HTTP status code changes from `204` to `404`, the actual resource state on the server remains the same, fulfilling the definition of idempotency. The different response code is simply informational, telling the client whether the deletion actually occurred or the resource was already gone.

---

### Part 3: Sensor Operations & Linking

#### Q3.1: Consequences of Sending a Non-JSON Content-Type

The `@Consumes(MediaType.APPLICATION_JSON)` annotation on our `POST /api/v1/sensors` method explicitly declares that this endpoint **only accepts** request bodies with the `Content-Type: application/json` header.

If a client attempts to send data in a different format (e.g., `text/plain` or `application/xml`), the JAX-RS runtime will **reject the request before it even reaches the resource method**. The framework automatically returns an **HTTP 415 Unsupported Media Type** response. This is a built-in content negotiation mechanism — the server compares the client's `Content-Type` header against the `@Consumes` annotation and, if there is no match, short-circuits the request.

This behaviour provides several benefits:
- **Security:** Prevents the server from attempting to parse unexpected or potentially malicious data formats.
- **Clarity:** The `415` status code clearly communicates to the client developer exactly what went wrong, unlike a `400` or `500` which can be ambiguous.
- **Separation of Concerns:** Content negotiation is handled declaratively by JAX-RS annotations, keeping the resource method code clean and focused on business logic.

#### Q3.2: Query Parameters vs. Path Parameters for Filtering

Our implementation uses `@QueryParam("type")` for filtering sensors:
```
GET /api/v1/sensors?type=CO2
```

An alternative design would embed the filter in the URL path:
```
GET /api/v1/sensors/type/CO2
```

The **query parameter approach is superior** for filtering for the following reasons:

1. **Optionality:** Query parameters are inherently optional. `GET /api/v1/sensors` returns all sensors, and adding `?type=CO2` filters the same collection. With path-based filtering, you would need separate route definitions for the filtered and unfiltered cases, increasing complexity.

2. **Composability:** Multiple query parameters can be easily combined: `?type=CO2&status=ACTIVE`. This scales gracefully as new filters are added. Path-based filtering would require deeply nested paths like `/sensors/type/CO2/status/ACTIVE`, which becomes unwieldy and order-dependent.

3. **RESTful Semantics:** In REST, the URL path identifies a **resource** or a **resource collection**. `/api/v1/sensors` identifies the sensors collection. A filter does not change which resource is being accessed — it merely constrains the representation. Query parameters are the correct semantic mechanism for this purpose, as they modify the representation of a resource rather than identifying a new one.

4. **Caching & Standards:** Query-parameterised URLs are well-understood by HTTP caches, CDNs, and browser history. Path-based filtering can create confusion about whether `/sensors/type/CO2` is a sub-resource of `/sensors/type/` or a filtered view of `/sensors`.

---

### Part 4: Deep Nesting with Sub-Resources

#### Q4.1: Benefits of the Sub-Resource Locator Pattern

The sub-resource locator pattern is implemented in our `SensorResource` class:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

This method does not have an HTTP method annotation (`@GET`, `@POST`, etc.). Instead, it returns a new instance of `SensorReadingResource`, which contains its own `@GET` and `@POST` methods. JAX-RS then delegates the request handling to this sub-resource class.

The architectural benefits are significant:

1. **Separation of Concerns:** Each resource class focuses on a single entity. `SensorResource` handles sensor-level operations, while `SensorReadingResource` handles reading-level operations. Without this pattern, all reading-related endpoints (`GET /sensors/{id}/readings`, `POST /sensors/{id}/readings`, `GET /sensors/{id}/readings/{rid}`) would clutter the `SensorResource` class.

2. **Maintainability:** In a large API with dozens of nested resources, having a single "god controller" class with every possible endpoint would be extremely difficult to navigate, test, and maintain. Sub-resources keep classes small and cohesive.

3. **Reusability:** The `SensorReadingResource` class could potentially be reused or adapted in other contexts. Its logic is encapsulated and independent from its parent.

4. **Testability:** Smaller, focused classes are easier to unit test. You can test `SensorReadingResource` in isolation by passing a mock `sensorId` to its constructor.

5. **Scalability of Development:** In a team environment, different developers can work on different sub-resource classes simultaneously without merge conflicts.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### Q5.1: Why HTTP 422 is More Accurate Than 404

When a client sends a `POST /api/v1/sensors` request with a valid JSON payload that references a non-existent `roomId`, we return **HTTP 422 Unprocessable Entity** rather than 404. This choice is deliberate:

- **HTTP 404 Not Found** semantically means "the resource identified by the URL does not exist." The URL in question is `/api/v1/sensors`, which **does exist** — it is alive and capable of processing requests. Returning 404 would mislead the client into thinking the sensors endpoint itself is broken or missing.

- **HTTP 422 Unprocessable Entity** means "the server understood the request, the syntax of the JSON is correct, but the content is semantically invalid." This precisely describes our scenario: the JSON is well-formed and parseable, but the `roomId` field references a room that does not exist in the system. The entity was **processable syntactically** but **unprocessable semantically**.

This distinction matters because:
1. **Client-side error handling becomes more precise.** A 404 on a POST might trigger "URL not found" recovery logic, while 422 correctly triggers "fix your data" logic.
2. **Debugging is faster.** Developers immediately know the issue is with the payload's data references, not the endpoint.
3. **HTTP 400 Bad Request**, while acceptable, is more generic and used for malformed syntax. 422 adds a layer of specificity that aids discovery.

#### Q5.2: Cybersecurity Risks of Exposing Stack Traces

Our `GenericExceptionMapper` (the catch-all `ExceptionMapper<Throwable>`) ensures that raw Java stack traces are **never** exposed to API consumers. Instead, it returns a clean, generic JSON error:

```json
{
  "status": 500,
  "code": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred. Please contact support."
}
```

Exposing internal stack traces to external users is a **significant security vulnerability** because an attacker can extract:

1. **Internal File Paths and Package Structure:** Stack traces reveal the full class hierarchy (e.g., `com.smartcampus.resource.SensorResource.createSensor(SensorResource.java:42)`), exposing the application's internal naming conventions and directory structure.

2. **Library and Framework Versions:** Traces include library class names and versions (e.g., `org.glassfish.jersey.servlet.WebComponent`). Attackers can cross-reference these versions against **known CVE vulnerability databases** to identify exploits specific to that version.

3. **Business Logic and Code Flow:** Stack traces expose the sequence of method calls, revealing how the application processes data internally. This information can be used to craft targeted attacks that exploit specific code paths.

4. **Database or Data Layer Details:** In applications with persistence, stack traces may reveal SQL query structures, connection strings, or ORM configurations.

5. **Server and OS Information:** Java stack traces can reveal the server OS, Java version, and web container version, narrowing the attacker's search for exploits.

By intercepting all `Throwable` exceptions and returning a sanitised response, we follow the **principle of least privilege in information disclosure**. The detailed error is still logged server-side (using `Logger.log(Level.SEVERE, ...)`) for debugging purposes, but the external consumer sees only a safe, generic message.

#### Q5.3: Why Use JAX-RS Filters Instead of Manual Logging

Our `LoggingFilter` implements both `ContainerRequestFilter` and `ContainerResponseFilter` to log every request and response centrally:

```java
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    // Logs method + URI for requests, status code for responses
}
```

Using JAX-RS filters for cross-cutting concerns like logging is greatly advantageous over manually inserting `Logger.info()` calls inside every resource method:

1. **DRY Principle (Don't Repeat Yourself):** Without filters, every single resource method would need identical logging boilerplate code. With 10+ endpoints, this means 10+ duplicated logging blocks that must be maintained in sync.

2. **Guaranteed Coverage:** Filters intercept **every** request and response automatically, even for endpoints added in the future. Manual logging relies on developers remembering to add it — one missed endpoint creates a blind spot in observability.

3. **Separation of Concerns:** Logging is a **cross-cutting concern** — it applies across all resources but is not part of any resource's core business logic. Mixing it into resource methods violates the Single Responsibility Principle.

4. **Centralised Modification:** If the log format needs to change (e.g., adding request headers or timestamps), you modify one class instead of every resource method across the codebase.

5. **Consistency:** A filter guarantees the same log format and level across all endpoints, making log analysis and monitoring tools more effective.

---

## API Endpoint Summary

| Method   | Endpoint                                | Description                              | Success Code |
|----------|-----------------------------------------|------------------------------------------|-------------|
| `GET`    | `/api/v1`                               | API discovery & metadata                 | 200         |
| `GET`    | `/api/v1/rooms`                         | List all rooms                           | 200         |
| `POST`   | `/api/v1/rooms`                         | Create a new room                        | 201         |
| `GET`    | `/api/v1/rooms/{roomId}`                | Get room details by ID                   | 200         |
| `DELETE` | `/api/v1/rooms/{roomId}`                | Delete a room (if no sensors assigned)   | 204         |
| `GET`    | `/api/v1/sensors`                       | List all sensors (optional `?type=` filter) | 200      |
| `POST`   | `/api/v1/sensors`                       | Register a new sensor (validates roomId) | 201         |
| `GET`    | `/api/v1/sensors/{sensorId}`            | Get sensor details by ID                 | 200         |
| `GET`    | `/api/v1/sensors/{sensorId}/readings`   | Get reading history for a sensor         | 200         |
| `POST`   | `/api/v1/sensors/{sensorId}/readings`   | Add a new reading (updates currentValue) | 201         |

## Error Responses

| Status Code | Scenario                                       | Exception                          |
|-------------|-------------------------------------------------|------------------------------------|
| 404         | Room or Sensor not found                        | Handled inline with `ErrorMessage` |
| 409         | Deleting a room that still has sensors          | `RoomNotEmptyException`            |
| 415         | Sending non-JSON content to a JSON endpoint     | Handled by JAX-RS framework        |
| 422         | Creating a sensor with a non-existent `roomId`  | `LinkedResourceNotFoundException`  |
| 403         | Posting a reading to a sensor in MAINTENANCE    | `SensorUnavailableException`       |
| 500         | Any unhandled runtime exception                 | `GenericExceptionMapper`           |
