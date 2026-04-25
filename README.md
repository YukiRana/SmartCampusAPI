# SmartCampusAPI

**Module:** 5COSC022W — Client-Server Architectures (2025/26)
**Author:** Yuki Ranathilaka
**University:** University of Westminster
**Weight:** 60% of final grade

A fully RESTful JAX-RS API for Smart Campus room, sensor, and sensor reading
management. All data is stored in-memory using `ConcurrentHashMap` — no database
is used. Built with Jersey 3.x deployed on Apache Tomcat 10.1+.

---

## API Design Overview

The API is versioned under the base path `/api/v1` and exposes three primary
resource collections reflecting the physical structure of the campus:

| Resource | Path | Description |
|---|---|---|
| Discovery | `GET /api/v1` | Metadata, contact info, HATEOAS links |
| Rooms | `/api/v1/rooms` | Manage physical campus rooms |
| Sensors | `/api/v1/sensors` | Manage IoT sensors assigned to rooms |
| Readings | `/api/v1/sensors/{sensorId}/readings` | Historical reading sub-resource |

All responses are `application/json`. Errors return structured JSON — no raw
stack traces are ever exposed. A cross-cutting `ApiLoggingFilter` logs every
request method/URI and every response status code.

Data model matches the specification exactly:
- **Room**: `id`, `name`, `capacity`, `sensorIds` (List)
- **Sensor**: `id`, `type`, `status` (ACTIVE/MAINTENANCE/OFFLINE), `currentValue`, `roomId`
- **SensorReading**: `id`, `timestamp` (epoch ms), `value`

---

## Technology Stack

- Java 17
- JAX-RS — Jersey 3.1.6 (bundled in WAR, no external app server runtime needed)
- Apache Tomcat 10.1+ (servlet container)
- Maven WAR packaging
- In-memory storage: `ConcurrentHashMap` / `ArrayList` — **no database**

---

## Build and Launch Instructions

### Prerequisites

- JDK 17 or later (`java -version` to verify)
- Maven 3.8+ (`mvn -version` to verify)
- Apache Tomcat 10.1.x downloaded and extracted

### Step 1 — Clone the repository

```bash
git clone https://github.com/<your-username>/SmartCampusAPI.git
cd SmartCampusAPI/SmartCampusAPI
```

### Step 2 — Build the WAR file

```bash
mvn clean package
```

Output: `SmartCampusAPI/target/SmartCampusAPI-v1.war`

### Step 3 — Deploy to Tomcat

```bash
cp target/SmartCampusAPI-v1.war /path/to/tomcat/webapps/SmartCampusAPI.war
```

### Step 4 — Start Tomcat

```bash
# Linux / macOS
/path/to/tomcat/bin/startup.sh

# Windows
\path\to\tomcat\bin\startup.bat
```

### Step 5 — Verify

```bash
curl http://localhost:8080/SmartCampusAPI/api/v1
```

You should receive a JSON discovery response. The API is live.

---

## Endpoint Reference

| Method | URL | Success | Error |
|---|---|---|---|
| GET | `/api/v1` | 200 | — |
| GET | `/api/v1/rooms` | 200 | — |
| POST | `/api/v1/rooms` | 201 + Location | 400, 409 |
| GET | `/api/v1/rooms/{roomId}` | 200 | 404 |
| DELETE | `/api/v1/rooms/{roomId}` | 204 | 404, 409 |
| GET | `/api/v1/sensors` | 200 | — |
| GET | `/api/v1/sensors?type={t}` | 200 filtered | — |
| POST | `/api/v1/sensors` | 201 + Location | 400, 409, 422 |
| GET | `/api/v1/sensors/{sensorId}` | 200 | 404 |
| GET | `/api/v1/sensors/{sensorId}/readings` | 200 | 404 |
| POST | `/api/v1/sensors/{sensorId}/readings` | 201 | 400, 403, 404 |

---

## Sample curl Commands

### 1. GET /api/v1 — Discovery with HATEOAS links

```bash
curl -s http://localhost:8080/SmartCampusAPI/api/v1
```

Expected: `200 OK` — JSON with API name, version, contact (Yuki Ranathilaka),
and `_links` map pointing to rooms and sensors.

---

### 2. POST /api/v1/rooms — Create a room (201 Created)

```bash
curl -s -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CS-101","name":"Computer Science Lab","capacity":50}'
```

Expected: `201 Created` with `Location` header and room JSON body.

---

### 3. POST /api/v1/sensors — Register a sensor (422 if roomId missing)

```bash
curl -s -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":21.0,"roomId":"CS-101"}'
```

Expected: `201 Created`. If `CS-101` did not exist, returns `422 Unprocessable
Entity` because the `roomId` payload reference is invalid.

---

### 4. GET /api/v1/sensors?type=Temperature — Filtered sensor list

```bash
curl -s "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature"
```

Expected: `200 OK` — JSON array of sensors whose type contains "Temperature"
(case-insensitive). Returns empty array if none match.

---

### 5. POST /api/v1/sensors/{sensorId}/readings — Add reading, updates currentValue

```bash
curl -s -X POST \
  http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-999/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}'
```

Expected: `201 Created` — response includes reading ID, timestamp, and
confirmation that `updatedCurrentValue` on the parent sensor is now `23.7`.

---

### 6. DELETE /api/v1/rooms/{roomId} — 409 when sensors still assigned

```bash
curl -s -X DELETE \
  http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

Expected: `409 Conflict` — JSON ApiError body explaining the room still has
sensors and cannot be deleted until they are removed.

---

### 7. POST reading to MAINTENANCE sensor — 403 Forbidden

```bash
curl -s -X POST \
  http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":415.0}'
```

Expected: `403 Forbidden` — CO2-001 is seeded in MAINTENANCE status.
SensorUnavailableExceptionMapper returns a JSON ApiError body.

---

## Error Response Format

All errors return a consistent JSON structure:

```json
{
  "timestamp": 1714040130000,
  "status": 409,
  "error": "Conflict",
  "message": "Room 'LIB-301' cannot be deleted: it still has 1 sensor(s) assigned.",
  "path": "rooms/LIB-301"
}
```

---

## Report: Answers to Coursework Questions

### Part 1 — Q1: JAX-RS Resource Class Lifecycle

By default, JAX-RS instantiates a **new resource class instance for every
incoming HTTP request** (per-request lifecycle). Instance fields on resource
classes are therefore not shared between requests and cannot safely hold
persistent application state such as data collections.

In this project the consequence is that all shared mutable data lives in the
**`DataStore` class as static final `ConcurrentHashMap` fields**. These maps
exist for the full lifetime of the deployed application regardless of how many
resource instances JAX-RS creates. `ConcurrentHashMap` is used rather than
a plain `HashMap` because multiple HTTP requests arrive concurrently on
different threads. A plain `HashMap` is not thread-safe: concurrent `put`/`get`
operations can corrupt its internal structure, causing silent data loss or
`ConcurrentModificationException`. Multi-step check-then-act sequences such as
`addRoom` (check existence, then insert) are additionally wrapped in
`synchronized` blocks to guarantee atomicity and prevent race conditions.

---

### Part 1 — Q2: Why HATEOAS Is a Hallmark of Advanced REST Design

HATEOAS (Hypermedia As The Engine Of Application State) means that API responses
embed hyperlinks to related resources, allowing clients to navigate the entire
API dynamically by following links embedded in responses rather than relying on
hardcoded URLs or static external documentation.

This benefits client developers in several ways. When the server changes a
resource path, clients that discover paths through the discovery response adapt
automatically without requiring a code change on the client side. The API
becomes self-describing — a new developer can start at `/api/v1` and navigate
to rooms and sensors purely by reading the response. Static documentation cannot
offer this: it becomes stale the moment the server changes, and clients that
hard-code paths break silently. HATEOAS eliminates this coupling and makes the
API more resilient to server-side evolution over time.

---

### Part 2 — Q1: Returning Full Room Objects vs IDs Only

Returning **full Room objects** in list responses provides all attributes
(id, name, capacity, sensorIds) in a single HTTP round-trip, which is convenient
for clients that need to display or process multiple fields. The downside is a
larger payload per response, which increases network bandwidth usage.

Returning **IDs only** reduces payload size significantly, but creates the
N+1 problem: a client must send one additional GET request per room to fetch
its details. For a list of 100 rooms, this means 101 total requests instead of
one, which dramatically increases total latency. For a Smart Campus system where
room counts are manageable, returning full objects is the correct trade-off.
In a large-scale system with thousands of rooms, pagination or sparse field
projection would be the preferred solution.

---

### Part 2 — Q2: Is DELETE Idempotent?

The DELETE operation is **idempotent in terms of server-side state** but not
in terms of response code. The first `DELETE /api/v1/rooms/{id}` on an existing
room removes it and returns `204 No Content`. A second identical request returns
`404 Not Found` because the room is already absent. The server-side state after
both calls is identical — the room does not exist — which satisfies the HTTP
specification's definition of idempotency: the operation produces the same effect
on the server regardless of how many times it is applied.

The business logic constraint (refusing to delete a room that still has sensors)
is enforced before any removal to prevent orphaned sensor records. If a client
retries a DELETE after removing the sensors, the room will be absent and a 404
returned, which is again consistent with idempotent behaviour.

---

### Part 3 — Q1: Consequences of @Consumes Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells the JAX-RS runtime
to only accept request bodies whose `Content-Type` header is `application/json`.
If a client sends a request with `Content-Type: text/plain` or
`Content-Type: application/xml`, JAX-RS **automatically returns HTTP 415
Unsupported Media Type** before the resource method is ever invoked. This happens
at the framework level during content negotiation: the runtime searches for a
`MessageBodyReader` capable of deserialising the incoming content type to the
target Java type (`Sensor`), finds none, and rejects the request immediately.
No application code executes. This protects resource methods from receiving
malformed or unexpected data formats without any defensive code in the method.

---

### Part 3 — Q2: @QueryParam vs Path Segment for Filtering

Using `@QueryParam` for filtering (e.g. `GET /sensors?type=CO2`) is semantically
correct because query parameters represent **optional constraints applied to a
collection**. The path `/sensors` identifies the sensors collection; the query
string narrows what is returned from it.

A path-segment design (`/sensors/type/CO2`) is semantically wrong because it
implies that `CO2` is a **uniquely addressable sub-resource** with its own
identity in the URI hierarchy. There is no resource named `CO2` — it is a filter
criterion. Path segments also compose poorly: filtering by two criteria would
require a new route definition such as `/sensors/type/CO2/status/ACTIVE`, which
is rigid and hard to extend. Query parameters compose naturally without any
routing changes: `?type=CO2&status=ACTIVE` works immediately. This is why
query parameters are the standard approach for search, filter, and sort
operations in RESTful API design.

---

### Part 4 — Q1: Benefits of the Sub-Resource Locator Pattern

The Sub-Resource Locator pattern delegates responsibility for a nested URI path
to a dedicated class. In this project, `SensorResource.getReadingsLocator()`
has no HTTP method annotation — JAX-RS recognises it as a locator and delegates
all `/sensors/{sensorId}/readings` requests to a separate `SensorReadingResource`
instance.

The key benefits in large APIs are:

1. **Single Responsibility** — `SensorResource` manages sensor CRUD only.
   `SensorReadingResource` manages reading history only. Neither class needs
   to know about the other's internal details.
2. **Reduced complexity** — without this pattern every nested path would be a
   method in one growing controller, making it difficult to read and maintain.
3. **Independent extensibility** — adding new reading sub-endpoints (e.g.
   `/readings/aggregate`) only requires changes to `SensorReadingResource`,
   leaving `SensorResource` untouched.
4. **Testability** — `SensorReadingResource` can be unit-tested by constructing
   it directly with a test sensor ID, without routing a full HTTP request through
   `SensorResource`.

---

### Part 5 — Q2: Why HTTP 422 Is More Accurate Than 404 for a Bad roomId

HTTP `404 Not Found` means the **requested URI itself does not exist** on the
server. In this scenario the URI `POST /api/v1/sensors` is perfectly valid — the
endpoint exists and is functional. The problem is that a **value inside the JSON
request body** (`roomId`) references a room that does not exist.

HTTP `422 Unprocessable Entity` is more semantically accurate because it signals
that the server successfully received the request, understood its structure, and
parsed the JSON, but **cannot process it because a business constraint is
violated** — the referenced linked resource is absent. Using `404` would mislead
client developers into thinking the endpoint itself is missing, which is incorrect
and makes client-side error handling logic ambiguous. `422` precisely communicates:
"your request is syntactically valid, but semantically I cannot fulfil it because
a resource you referenced does not exist."

---

### Part 5 — Q4: Cybersecurity Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers creates serious security
vulnerabilities:

1. **Internal package and class path disclosure** — an attacker learns the exact
   package structure (e.g. `com.mycompany.smartcampusapi.service.DataStore`) and
   can search for known vulnerabilities in those specific classes or cross-reference
   leaked source code.
2. **Framework and library version leakage** — stack traces include Jersey, HK2,
   Tomcat, and Jackson class names. With exact version information, an attacker can
   look up CVEs for those precise versions and craft targeted exploits.
3. **Business logic exposure** — the sequence of method calls in a trace reveals
   exactly how the application processes requests internally, making it easier to
   craft inputs that trigger specific code paths, bypass validation, or cause
   intentional failures.
4. **Sensitive data in exception messages** — exceptions sometimes embed SQL
   queries, file system paths, configuration keys, or user-supplied data in their
   message strings. All of this becomes visible in a trace sent to the client.

The `GlobalExceptionMapper` in this project prevents all of the above by logging
the full stack trace server-side (accessible only to authorised administrators)
while returning only the sanitised message "An unexpected server error occurred"
in the HTTP response body.

---

### Part 5 — Q5: JAX-RS Filters vs Manual Logger Calls

Using a JAX-RS filter (`ContainerRequestFilter` / `ContainerResponseFilter`) for
logging is architecturally superior to inserting `Logger.info()` calls in every
resource method for four key reasons:

1. **DRY (Don't Repeat Yourself)** — a single filter class handles logging for
   every endpoint. Manually inserting log statements in every method produces
   hundreds of lines of duplicated boilerplate. Any change to the log format
   would require editing every resource class individually.
2. **Completeness** — a filter intercepts 100% of requests, including those that
   fail before reaching a resource method such as `404` for unknown paths or
   `415` for wrong Content-Type. Manual in-method logging would miss all these
   cases silently.
3. **Separation of concerns** — resource methods should focus solely on business
   logic. Mixing logging code into them violates the single responsibility
   principle and makes methods harder to read and unit-test.
4. **Centralised control** — the entire logging behaviour can be modified,
   enhanced (e.g. adding request body logging in non-production), or disabled
   by changing one class, with no impact on any resource class.
