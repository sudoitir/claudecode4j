# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ClaudeCode4J is a Java library for integrating with Claude Code CLI. It wraps the CLI tool and provides a clean Java API for executing prompts, streaming responses, and managing sessions.

## Build Commands

```bash
# Build entire project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run unit tests only
mvn test

# Run a single test class
mvn test -Dtest=ClaudeRestIntegrationTest

# Run a single test method
mvn test -Dtest=ClaudeRestIntegrationTest#shouldExecutePromptAndReturnResponse

# Run integration tests (requires Docker for Testcontainers)
mvn verify -Pintegration-tests

# Format code (Spotless with Palantir Java Format)
mvn spotless:apply

# Check formatting
mvn spotless:check
```

## Architecture

### Module Structure

- **claudecode4j-bom**: Bill of Materials for version management
- **claudecode4j-api**: Interfaces, DTOs, and SPIs (no implementation)
- **claudecode4j-core**: Pure Java implementation (no Spring dependency)
- **claudecode4j-spring-boot-starter**: Spring Boot 4 auto-configuration
- **claudecode4j-rest-adapter**: REST/SSE endpoints
- **claudecode4j-kafka-adapter**: Kafka request-reply messaging

### Key Design Patterns

**Sealed Types**: `ClaudeResponse` and `ClaudeException` use sealed interfaces/classes with pattern matching:

```java
switch (response) {
    case TextResponse text -> ...
    case StreamResponse stream -> ...
    case ErrorResponse error -> ...
}
```

**SPI Extension Points** (in `claudecode4j-api/spi/`):
- `BinaryResolver` - Finds Claude CLI binary (chained via CompositeBinaryResolver)
- `InputSanitizer` - Sanitizes prompts before execution
- `OutputParser` - Parses CLI output into response objects
- `ProcessExecutor` - Executes CLI commands

**Virtual Threads**: All async operations use `Thread.ofVirtual()` for efficient concurrency.

**Concurrency Limiting**: `Semaphore`-based limiting in `DefaultClaudeClient` + AOP `@ConcurrencyLimit` annotation for Spring.

### Core Flow

1. `ClaudeClient.execute(Prompt)` sanitizes input via `InputSanitizer`
2. `ClaudeCommandBuilder` constructs CLI arguments (uses stdin mode to avoid OS command length limits)
3. `ProcessExecutor` runs the CLI with timeout
4. `OutputParser` converts stdout to `ClaudeResponse`

**Streaming**: Uses `Flow.Publisher<StreamEvent>` (Java 9+ Flow API), not `java.util.stream.Stream`.

### Spring Configuration

Properties prefix: `claude.code.*`
- `binaryPath` - Path to claude binary (auto-detected if not set)
- `concurrencyLimit` - Max concurrent executions (default: 4)
- `defaultTimeout` - Execution timeout (default: 5m)
- `dangerouslySkipPermissions` - Skip permission prompts

## Code Style

- Java 25 with preview features enabled (`--enable-preview`)
- Palantir Java Format via Spotless
- JSpecify `@Nullable` annotations for null-safety
- Records for immutable DTOs
- License header required on all Java files (see `license-header` file)

## Testing

- Unit tests use JUnit 5 + Mockito
- Integration tests use `@SpringBootTest` with `@MockitoBean`
- Kafka tests use Testcontainers
- E2E tests require Claude CLI to be installed and authenticated

## Docker

Sample Dockerfile in `docker/` for applications that use ClaudeCode4J:

```bash
# Build runtime image
docker build -f docker/Dockerfile -t claudecode4j-app --target runtime .

# Build development image (includes JDK + Maven)
docker build -f docker/Dockerfile -t claudecode4j-dev --target development .

# Run with docker-compose
cd docker && docker-compose up dev

# Run with Kafka for integration tests
cd docker && docker-compose --profile kafka up
```

