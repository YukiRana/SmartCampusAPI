# SmartCampusAPI

IoT coursework API for smart campus room, sensor, and reading management. Developed by Yuki Ranathilaka.

## Coursework Coverage

- **Part 1: Setup and discovery**
  - Application path is `/api/v1`.
  - `GET /api/v1` returns discovery metadata, contact details, and HATEOAS-style resource links.
- **Part 2: Room management**
  - `POST /api/v1/rooms` creates a room and returns `201 Created` with a `Location` header.
  - `GET /api/v1/rooms` lists rooms.
  - `GET /api/v1/rooms/{id}` fetches one room.
  - `DELETE /api/v1/rooms/{id}` returns `409 Conflict` when sensors still belong to the room.
- **Part 3: Sensors and filtering**
  - `POST /api/v1/sensors` validates the supplied `roomId` before creation.
  - `GET /api/v1/sensors?type=...` filters sensors by type.
  - `GET /api/v1/sensors/{id}` fetches one sensor.
- **Part 4: Sub-resources**
  - `GET /api/v1/sensors/{id}/readings` returns the nested reading history for a sensor.
  - `POST /api/v1/sensors/{id}/readings` creates a new reading and updates the parent sensor `currentValue`.
- **Part 5: Error handling**
  - `422 Unprocessable Entity` is returned for invalid payload data.
  - `403 Forbidden` is mapped to JSON.
  - `404 Not Found` is mapped to JSON for missing rooms or sensors.
  - `409 Conflict` is mapped to JSON for room deletion with existing sensors.
  - `500 Internal Server Error` is sanitized so stack traces are not exposed.

## Runtime

- Java 17
- Maven WAR packaging
- Tomcat 10.1+ with Jersey 3.x bootstrap configured in `web.xml`
- Hibernate ORM + H2 file database

## Build

```bash
cd SmartCampusAPI
mvn clean package
```

The compiled artifact is written to `SmartCampusAPI/target/SmartCampusAPI-v1.war`.

## Endpoints

### Discovery

- `GET /api/v1`

### Rooms

- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{id}`
- `DELETE /api/v1/rooms/{id}`

### Sensors

- `GET /api/v1/sensors?type={type}`
- `POST /api/v1/sensors`
- `GET /api/v1/sensors/{id}`
- `GET /api/v1/sensors/{id}/readings`
- `POST /api/v1/sensors/{id}/readings`

## Sample Payloads

### Create Room

```json
{
  "name": "Lecture Hall A"
}
```

### Create Sensor

```json
{
  "type": "TEMPERATURE",
  "roomId": 1,
  "currentValue": 24.5
}
```

### Add Reading

```json
{
  "value": 25.1
}
```

## Error Format

Handled errors return JSON in the following shape:

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "roomId must be greater than zero.",
  "timestamp": "2026-04-24T12:34:56Z"
}
```

## Manual Verification

1. Build the WAR with Maven.
2. Deploy it to Tomcat 10.1+.
3. Confirm `GET /api/v1` returns discovery metadata and links to `rooms` and `sensors`.
4. POST a room and confirm the response is `201 Created` and includes a `Location` header.
5. POST a sensor with a non-existent `roomId` and confirm the API returns a JSON error.
6. GET `/api/v1/sensors?type=TEMPERATURE` and verify the filtered list changes with the query string.
7. GET `/api/v1/sensors/{id}/readings`, then POST a reading to the same sub-resource and confirm the sensor `currentValue` changes.
8. Delete a room that still has sensors and confirm `409 Conflict`.
9. Trigger `403 Forbidden` with `GET /api/v1?trigger=forbidden`.
10. Trigger a sanitized `500 Internal Server Error` with `GET /api/v1?trigger=boom` and confirm no stack trace is exposed.