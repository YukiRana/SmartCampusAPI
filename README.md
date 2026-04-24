# SmartCampusAPI

SmartCampusAPI is a Jakarta REST coursework project for managing rooms, sensors, and sensor readings in memory. It uses a singleton `ConcurrentHashMap` store, Jersey on Tomcat, and no database layer.

## Runtime

- Java 17
- Maven WAR packaging
- Tomcat 10.1+
- Jersey 3.x

## Build

```bash
cd SmartCampusAPI
mvn clean package
```

The WAR is written to `target/SmartCampusAPI-v1.war`.

## Core Endpoints

- `GET /api/v1` discovery metadata and HATEOAS-style links
- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{roomId}`
- `DELETE /api/v1/rooms/{roomId}`
- `GET /api/v1/sensors?type=...`
- `POST /api/v1/sensors`
- `GET /api/v1/sensors/{sensorId}`
- `GET /api/v1/sensors/{sensorId}/readings`
- `POST /api/v1/sensors/{sensorId}/readings`

## Error Handling

The API returns JSON error bodies for 403, 404, 409, 422, and 500 responses. The global mapper keeps the 500 response sanitized.

## Submission Checks

1. Build the WAR with Maven.
2. Deploy it to Tomcat 10.1+.
3. Confirm discovery, room CRUD, sensor filtering, and nested readings all work.
4. Confirm the exception mappers return JSON responses.