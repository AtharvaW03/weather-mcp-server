# Weather MCP Server

A Model Context Protocol (MCP) server built with Spring Boot and Spring AI that exposes weather tools for AI agents. It uses the [National Weather Service API](https://www.weather.gov/documentation/services-web-api) to provide real-time weather data for the United States.

## Tools

- **getWeatherForecastByLocation** -- Returns a detailed weather forecast for a given latitude/longitude.
- **getAlerts** -- Returns active weather alerts for a given US state (two-letter code, e.g. `NY`, `CA`).

## Tech Stack

- Java 21
- Spring Boot 4.0
- Spring AI MCP Server (`spring-ai-starter-mcp-server`)
- NWS API (no API key required)

## Getting Started

**Prerequisites:** Java 21+, Maven

```bash
# Clone
git clone https://github.com/AtharvaW03/weather-mcp-server.git
cd weather-mcp-server

# Build
./mvnw clean install

# Run
./mvnw spring-boot:run
```

The server starts in STDIO transport mode. Connect any MCP-compatible client to use the exposed tools.

## Author

[Atharva W](https://github.com/AtharvaW03)
