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

# Run unit tests only (excludes integration and E2E tests)
mvn test

# Run a single test class
mvn test -Dtest=ClaudeRestIntegrationTest

# Run a single test method
mvn test -Dtest=ClaudeRestIntegrationTest#shouldExecutePromptAndReturnResponse

# Run tests for a specific module
mvn test -pl claudecode4j-core

# Run integration tests (requires Docker for Testcontainers)
mvn verify -Pintegration

# Run all tests including E2E (requires Claude CLI installed and authenticated)
mvn verify -Pall

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
- **claudecode4j-rest-adapter**: REST/SSE endpoints with OpenAI and Anthropic API compatibility
- **claudecode4j-kafka-adapter**: Kafka request-reply messaging
- **claudecode4j-websocket-adapter**: WebSocket terminal for interactive sessions
- **claudecode4j-mcp-server**: MCP Server support for exposing Java methods as Claude tools
- **claudecode4j-context**: Token-aware context optimization using JTokkit

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

> **Note: Single-JVM Limitation**
> The built-in concurrency limiting only works within a single JVM instance. In clustered deployments (multiple JVM instances), you need external rate limiting solutions such as Redis-based distributed rate limiting, API Gateway throttling, or database-backed semaphores.

**Binary Resolution Chain**: `CompositeBinaryResolver` chains resolvers in order:
1. Explicit path from config (`binaryPath`)
2. `PathBinaryResolver` - searches `PATH` environment variable
3. `NpmBinaryResolver` - finds npm global installation

**Stdin Mode**: Prompts are passed via stdin (`-p -`) instead of positional arguments to avoid OS ARG_MAX limits and shell escaping issues. See `ClaudeCommandBuilder.buildWithStdin()`.

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
- `enabled` - Enable/disable auto-configuration (default: true)
- `health.enabled` - Enable health indicator (default: true)
- `metrics.enabled` - Enable Micrometer metrics (default: true)
- `mock.enabled` - Enable mock client for testing (default: false)
- `mock.response` - Static mock response
- `mock.delay` - Artificial delay for mock responses
- `resilience.enabled` - Enable retry with exponential backoff (default: false)
- `resilience.maxRetries` - Maximum retry attempts (default: 3)
- `resilience.initialDelay` - Initial retry delay (default: 1s)
- `resilience.multiplier` - Backoff multiplier (default: 2.0)
- `resilience.maxDelay` - Maximum retry delay (default: 30s)

REST adapter properties (`claude.code.rest.*`):
- `enabled` - Enable REST endpoints (default: true)
- `basePath` - Base path for REST endpoints (default: `/api/claude`)
- `openai.enabled` - Enable OpenAI-compatible endpoints (default: true)
- `anthropic.enabled` - Enable Anthropic API endpoints (default: true)

## JPMS Modules

Each module has a `module-info.java`. Key exports:
- `ir.sudoit.claudecode4j.api` exports all public API packages
- `ir.sudoit.claudecode4j.core` exports implementation, requires `ir.sudoit.claudecode4j.api`

When adding new packages, update the corresponding `module-info.java`.

## Code Style

- Java 25 with preview features enabled (`--enable-preview`)
- Palantir Java Format via Spotless
- JSpecify `@Nullable` annotations for null-safety
- Records for immutable DTOs
- License header required on all Java files (see `license-header` file)

## Testing

### Test Categories

- **Unit Tests** (`*Test.java`): Fast, isolated tests with mocked dependencies
- **Integration Tests** (`*IntegrationTest.java`): Tests with real containers (Testcontainers)
- **E2E Tests** (`*E2ETest.java`): Full end-to-end tests requiring Claude CLI

### Running Tests

```bash
# Unit tests only (default - fast, no external dependencies)
mvn test

# Integration tests (requires Docker for Testcontainers)
mvn verify -Pintegration

# All tests including E2E (requires Claude CLI installation and authentication)
mvn verify -Pall
```

**Note**: E2E tests should only be run manually. They require:
- Claude CLI installed: `npm install -g @anthropic-ai/claude-code`
- Claude CLI authenticated (run `claude` once to authenticate)

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

