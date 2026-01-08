# ClaudeCode4J

> **Bring the agentic power of Claude Code to Java.**

[![English](https://img.shields.io/badge/Language-English-blue)](README.md)
[![Chinese](https://img.shields.io/badge/Language-中文-red)](README_CN.md)
[![Persian](https://img.shields.io/badge/Language-فارسی-green)](README_FA.md)

A modern Java library for integrating with [Claude Code CLI](https://docs.anthropic.com/en/docs/claude-code) - Anthropic's agentic coding tool.

[![Java](https://img.shields.io/badge/Java-25-blue.svg)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sudoitir_claudecode4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sudoitir_claudecode4j)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=sudoitir_claudecode4j&metric=bugs)](https://sonarcloud.io/summary/new_code?id=sudoitir_claudecode4j)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sudoitir_claudecode4j&metric=coverage)](https://sonarcloud.io/summary/new_code?id=sudoitir_claudecode4j)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=sudoitir_claudecode4j&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=sudoitir_claudecode4j)
[![CodeQL](https://github.com/sudoitir/claudecode4j/actions/workflows/codeql.yml/badge.svg)](https://github.com/sudoitir/claudecode4j/actions/workflows/codeql.yml)
[![CI](https://github.com/sudoitir/claudecode4j/actions/workflows/ci.yml/badge.svg)](https://github.com/sudoitir/claudecode4j/actions/workflows/ci.yml)
[![Integration Tests](https://github.com/sudoitir/claudecode4j/actions/workflows/integration-tests.yml/badge.svg)](https://github.com/sudoitir/claudecode4j/actions/workflows/integration-tests.yml)

## Features

- **Pure Java API** - Clean interfaces with sealed types and records
- **Virtual Threads** - Efficient concurrent execution using Project Loom
- **Structured Concurrency** - Safe parallel process management
- **Spring Boot 4 Integration** - Auto-configuration, health checks, and metrics
- **REST API Adapter** - HTTP endpoints with SSE streaming support
- **Kafka Adapter** - Request-reply pattern with correlation IDs
- **JPMS Ready** - Full Java Platform Module System support
- **Null-Safe** - JSpecify annotations throughout

## Requirements

- Java 25+
- Claude Code CLI installed (`npm install -g @anthropic-ai/claude-code`)
- Maven 3.9+

## Installation

### Maven BOM (Recommended)

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>ir.sudoit</groupId>
            <artifactId>claudecode4j-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Core Library (No Spring)

```xml
<dependency>
    <groupId>ir.sudoit</groupId>
    <artifactId>claudecode4j-core</artifactId>
</dependency>
```

### Spring Boot Starter

```xml
<dependency>
    <groupId>ir.sudoit</groupId>
    <artifactId>claudecode4j-spring-boot-starter</artifactId>
</dependency>
```

### REST Adapter

```xml
<dependency>
    <groupId>ir.sudoit</groupId>
    <artifactId>claudecode4j-rest-adapter</artifactId>
</dependency>
```

### Kafka Adapter

```xml
<dependency>
    <groupId>ir.sudoit</groupId>
    <artifactId>claudecode4j-kafka-adapter</artifactId>
</dependency>
```

## Quick Start

### Standalone Usage (No Spring)

```java
import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.core.client.DefaultClaudeClientFactory;

// Create client using SPI
ClaudeClient client = new DefaultClaudeClientFactory().create();

// Execute a prompt
Prompt prompt = Prompt.of("Explain what this code does");
ClaudeResponse response = client.execute(prompt);

// Handle response using pattern matching
switch (response) {
    case TextResponse text -> System.out.println(text.content());
    case StreamResponse stream -> stream.events().forEach(System.out::println);
    case ErrorResponse error -> System.err.println(error.message());
}
```

### Async Execution

```java
CompletableFuture<ClaudeResponse> future = client.executeAsync(prompt);
future.thenAccept(response -> {
    if (response instanceof TextResponse text) {
        System.out.println(text.content());
    }
});
```

### Streaming

```java
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import java.util.stream.Stream;

Stream<StreamEvent> events = client.stream(prompt);
events.forEach(event -> {
    switch (event) {
        case StreamEvent.Text text -> System.out.print(text.content());
        case StreamEvent.Tool tool -> System.out.println("Tool: " + tool.name());
        case StreamEvent.Result result -> System.out.println("\nDone: " + result.success());
    }
});
```

### Session Management

```java
// Create a conversation session
ClaudeSession session = client.createSession();

// Continue conversation with context
ClaudeResponse response1 = session.send(Prompt.of("Create a Java class for User"));
ClaudeResponse response2 = session.send(Prompt.of("Add validation annotations"));

// Session maintains conversation history
session.close();
```

## Spring Boot Integration

### Configuration

```yaml
claude:
  code:
    binary-path: /usr/local/bin/claude  # Optional: auto-detected
    concurrency-limit: 4                 # Max concurrent executions
    default-timeout: 5m                  # Execution timeout
    dangerously-skip-permissions: false  # Security flag
    health:
      enabled: true                      # Enable health indicator
      cache-duration: 30s                # Health check cache
    metrics:
      enabled: true                      # Enable Micrometer metrics
```

### Auto-wired Usage

```java
@Service
public class CodeAssistantService {

    private final ClaudeClient claudeClient;

    public CodeAssistantService(ClaudeClient claudeClient) {
        this.claudeClient = claudeClient;
    }

    public String analyzeCode(String code) {
        Prompt prompt = Prompt.builder()
            .text("Analyze this code for potential issues:\n" + code)
            .outputFormat(OutputFormat.TEXT)
            .build();

        ClaudeResponse response = claudeClient.execute(prompt);
        return switch (response) {
            case TextResponse text -> text.content();
            case ErrorResponse error -> "Error: " + error.message();
            default -> "Unexpected response";
        };
    }
}
```

### Concurrency Limiting with AOP

```java
@Service
public class RateLimitedService {

    private final ClaudeClient claudeClient;

    @ConcurrencyLimit(permits = 2)  // Max 2 concurrent calls to this method
    public ClaudeResponse processWithLimit(Prompt prompt) {
        return claudeClient.execute(prompt);
    }
}
```

## REST API

Enable the REST adapter to expose Claude functionality via HTTP:

```yaml
claude:
  code:
    rest:
      enabled: true
      base-path: /api/claude
```

### Endpoints

| Method |            Path            |          Description          |
|--------|----------------------------|-------------------------------|
| `POST` | `/api/claude/prompt`       | Execute prompt synchronously  |
| `POST` | `/api/claude/prompt/async` | Execute prompt asynchronously |
| `POST` | `/api/claude/stream`       | Stream response via SSE       |
| `GET`  | `/api/claude/health`       | Health check                  |

### Example Request

```bash
curl -X POST http://localhost:8080/api/claude/prompt \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Write a hello world in Rust",
    "outputFormat": "TEXT",
    "timeout": "PT30S"
  }'
```

### SSE Streaming

```bash
curl -N http://localhost:8080/api/claude/stream \
  -H "Content-Type: application/json" \
  -d '{"text": "Explain microservices architecture"}'
```

## Kafka Integration

Enable request-reply messaging over Kafka:

```yaml
claude:
  code:
    kafka:
      enabled: true
      request-topic: claude-requests
      reply-topic: claude-replies
      group-id: claude-processor
      reply-timeout: 5m
```

### Producer (Request Side)

```java
@Service
public class KafkaPromptService {

    private final ClaudeKafkaProducer producer;

    public CompletableFuture<String> sendPrompt(String text) {
        return producer.sendRequest(text);
    }
}
```

### Consumer (Processing Side)

The `ClaudeKafkaListener` automatically:
1. Consumes messages from `request-topic`
2. Executes prompts via `ClaudeClient`
3. Sends responses to `reply-topic` with correlation ID

## Module Structure

```
claudecode4j/
├── claudecode4j-bom/                 # Bill of Materials
├── claudecode4j-api/                 # Interfaces & DTOs
│   ├── client/                       # ClaudeClient, ClaudeSession
│   ├── model/                        # Prompt, Response records
│   ├── exception/                    # Sealed exception hierarchy
│   └── spi/                          # Extension points
├── claudecode4j-core/                # Pure Java implementation
│   ├── client/                       # DefaultClaudeClient
│   ├── process/                      # VirtualThreadExecutor
│   ├── parser/                       # StreamJsonParser
│   └── resolver/                     # Binary resolvers
├── claudecode4j-spring-boot-starter/ # Spring Boot integration
│   ├── autoconfigure/                # Auto-configuration
│   ├── properties/                   # ConfigurationProperties
│   ├── health/                       # HealthIndicator
│   └── metrics/                      # Micrometer metrics
├── claudecode4j-rest-adapter/        # REST API
│   ├── controller/                   # ClaudeController
│   └── dto/                          # Request/Response DTOs
└── claudecode4j-kafka-adapter/       # Kafka messaging
    ├── listener/                     # Message consumer
    ├── producer/                     # Request producer
    └── correlation/                  # Correlation ID manager
```

## Exception Handling

All exceptions extend the sealed `ClaudeException`:

```java
try {
    ClaudeResponse response = client.execute(prompt);
} catch (ClaudeException e) {
    switch (e) {
        case ClaudeBinaryNotFoundException ex ->
            log.error("Claude CLI not found: {}", ex.getMessage());
        case ClaudeExecutionException ex ->
            log.error("Execution failed: {}", ex.getMessage());
        case ClaudeTimeoutException ex ->
            log.error("Timeout after: {}", ex.getTimeout());
        case ClaudeConfigurationException ex ->
            log.error("Configuration error: {}", ex.getMessage());
    }
}
```

## Observability

### Health Check

```json
{
  "status": "UP",
  "components": {
    "claudeCode": {
      "status": "UP",
      "details": {
        "binaryPath": "/usr/local/bin/claude",
        "version": "1.0.0"
      }
    }
  }
}
```

### Metrics (Micrometer)

|              Metric              |  Type   |     Description     |
|----------------------------------|---------|---------------------|
| `claude.code.executions`         | Counter | Total executions    |
| `claude.code.executions.active`  | Gauge   | Currently running   |
| `claude.code.execution.duration` | Timer   | Execution time      |
| `claude.code.errors`             | Counter | Error count by type |

## Security

The library includes built-in security measures:

- **Input Sanitization** - Prevents command injection attacks
- **Concurrency Limiting** - Protects against resource exhaustion
- **Permission Control** - `dangerously-skip-permissions` must be explicitly enabled

## Building from Source

```bash
git clone https://github.com/sudoit/claudecode4j.git
cd claudecode4j
mvn clean install
```

## Running Tests

```bash
# Unit tests
mvn test

# Integration tests (requires Docker for Testcontainers)
mvn verify -Pintegration-tests
```

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Anthropic](https://www.anthropic.com/) for Claude and Claude Code
- [Spring Team](https://spring.io/) for Spring Boot 4
- [Project Loom](https://openjdk.org/projects/loom/) for Virtual Threads

