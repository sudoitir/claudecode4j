<div align="center">

# ClaudeCode4J

**Bring the agentic power of Claude Code to Java.**

[English](README.md) • [中文](README_CN.md) • [فارسی](README_FA.md)

<br />

<img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" />
<img src="https://img.shields.io/badge/Spring_Boot-4.0.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4.0.1" />
<img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="License" />

<br />
<br />

[![CI](https://img.shields.io/github/actions/workflow/status/sudoitir/claudecode4j/ci.yml?branch=main&style=flat-square&label=Build)](https://github.com/sudoitir/claudecode4j/actions/workflows/ci.yml)
[![Quality Gate Status](https://img.shields.io/sonar/quality_gate/sudoitir_claudecode4j?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)](https://sonarcloud.io/summary/new_code?id=sudoitir_claudecode4j)
[![Coverage](https://img.shields.io/sonar/coverage/sudoitir_claudecode4j?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)](https://sonarcloud.io/summary/new_code?id=sudoitir_claudecode4j)
[![Bugs](https://img.shields.io/sonar/bugs/sudoitir_claudecode4j?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)](https://sonarcloud.io/summary/new_code?id=sudoitir_claudecode4j)

</div>

---

<p align="center">
  <b>A modern Java library for integrating with <a href="https://docs.anthropic.com/en/docs/claude-code">Claude Code CLI</a> — Anthropic's agentic coding tool.</b>
</p>

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
  - [Maven BOM (Recommended)](#maven-bom-recommended)
  - [Core Library (No Spring)](#core-library-no-spring)
  - [Spring Boot Starter](#spring-boot-starter)
  - [REST Adapter](#rest-adapter)
  - [Kafka Adapter](#kafka-adapter)
  - [WebSocket Adapter](#websocket-adapter)
  - [MCP Server](#mcp-server)
  - [Context Module (Token Optimization)](#context-module-token-optimization)
- [Quick Start](#quick-start)
  - [Standalone Usage (No Spring)](#standalone-usage-no-spring)
  - [Async Execution](#async-execution)
  - [Streaming](#streaming)
  - [Session Management](#session-management)
- [Spring Boot Integration](#spring-boot-integration)
  - [Configuration](#configuration)
  - [Auto-wired Usage](#auto-wired-usage)
  - [Concurrency Limiting with AOP](#concurrency-limiting-with-aop)
- [REST API](#rest-api)
  - [Endpoints](#endpoints)
  - [Example Request](#example-request)
  - [SSE Streaming](#sse-streaming)
- [OpenAI-Compatible API](#openai-compatible-api)
  - [Configuration](#configuration-1)
  - [Endpoints](#endpoints-1)
  - [Example Request](#example-request-1)
  - [Streaming Example](#streaming-example)
  - [Response Format (Non-Streaming)](#response-format-non-streaming)
  - [Streaming Format](#streaming-format)
- [Anthropic-Compatible API](#anthropic-compatible-api)
  - [Configuration](#configuration-2)
  - [Endpoints](#endpoints-2)
  - [Example Request](#example-request-2)
  - [Streaming Example](#streaming-example-1)
  - [Response Format (Non-Streaming)](#response-format-non-streaming-1)
  - [Streaming Format](#streaming-format-1)
- [Kafka Integration](#kafka-integration)
  - [Configuration](#configuration-3)
  - [Producer (Request Side)](#producer-request-side)
  - [Consumer (Processing Side)](#consumer-processing-side)
- [Module Structure](#module-structure)
- [Exception Handling](#exception-handling)
- [Observability](#observability)
  - [Health Check](#health-check)
  - [Metrics (Micrometer)](#metrics-micrometer)
- [Security](#security)
- [Building from Source](#building-from-source)
- [Running Tests](#running-tests)
  - [Test Profiles](#test-profiles)
  - [Test Categories](#test-categories)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgments](#acknowledgments)

---

- **Pure Java API** - Clean interfaces with sealed types and records
- **Virtual Threads** - Efficient concurrent execution using Project Loom
- **Structured Concurrency** - Safe parallel process management
- **Spring Boot 4 Integration** - Auto-configuration, health checks, and metrics
- **REST API Adapter** - HTTP endpoints with SSE streaming support
- **Kafka Adapter** - Request-reply pattern with correlation IDs
- **WebSocket Terminal** - Interactive sessions with human-in-the-loop support
- **MCP Server** - Expose Java methods as Claude tools via Model Context Protocol
- **Smart Context** - Token-aware context optimization using JTokkit
- **Resilience** - Built-in retry with exponential backoff
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
            <groupId>io.github.sudoitir</groupId>
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
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-core</artifactId>
</dependency>
```

### Spring Boot Starter

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-spring-boot-starter</artifactId>
</dependency>
```

### REST Adapter

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-rest-adapter</artifactId>
</dependency>
```

### Kafka Adapter

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-kafka-adapter</artifactId>
</dependency>
```

### WebSocket Adapter

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-websocket-adapter</artifactId>
</dependency>
```

### MCP Server

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-mcp-server</artifactId>
</dependency>
```

### Context Module (Token Optimization)

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-context</artifactId>
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

## OpenAI-Compatible API

The REST adapter includes an **OpenAI-compatible** `/v1/chat/completions` endpoint, allowing tools and applications designed for OpenAI's API to work with Claude Code CLI.

### Configuration

```yaml
claude:
  code:
    rest:
      openai:
        enabled: true                    # Enable OpenAI-compatible endpoint (default: true)
        base-path: /v1                   # Base path for OpenAI endpoints (default: /v1)
```

### Endpoints

| Method |          Path          |              Description               |
|--------|------------------------|----------------------------------------|
| `POST` | `/v1/chat/completions` | OpenAI-compatible chat completions API |

### Example Request

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "claude-3-5-sonnet-20241022",
    "messages": [
      {"role": "system", "content": "You are a helpful assistant."},
      {"role": "user", "content": "Write a hello world in Rust"}
    ],
    "max_tokens": 1000,
    "temperature": 0.7,
    "stream": false
  }'
```

### Streaming Example

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "claude-3-5-sonnet-20241022",
    "messages": [
      {"role": "user", "content": "Explain quantum computing"}
    ],
    "stream": true
  }'
```

### Response Format (Non-Streaming)

```json
{
  "id": "chatcmpl-abc123",
  "object": "chat.completion",
  "created": 1234567890,
  "model": "claude-3-5-sonnet-20241022",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "Hello, World! in Rust:\n\nfn main() {\n    println!(\"Hello, World!\");\n}"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 20,
    "completion_tokens": 30,
    "total_tokens": 50
  }
}
```

### Streaming Format

```
data: {"id":"chatcmpl-abc123","object":"chat.completion.chunk","created":1234567890,"model":"claude-3-5-sonnet-20241022","choices":[{"index":0,"delta":{"content":"Hello"},"finish_reason":null}]}

data: {"id":"chatcmpl-abc123","object":"chat.completion.chunk","created":1234567890,"model":"claude-3-5-sonnet-20241022","choices":[{"index":0,"delta":{"content": " World!"},"finish_reason":null}]}

data: [DONE]
```

## Anthropic-Compatible API

The REST adapter also includes an **Anthropic-compatible** `/v1/messages` endpoint, following Anthropic's Messages API specification.

### Configuration

```yaml
claude:
  code:
    rest:
      anthropic:
        enabled: true                    # Enable Anthropic-compatible endpoint (default: true)
        base-path: /v1                   # Base path for Anthropic endpoints (default: /v1)
```

### Endpoints

| Method |      Path      |            Description            |
|--------|----------------|-----------------------------------|
| `POST` | `/v1/messages` | Anthropic-compatible Messages API |

### Example Request

```bash
curl -X POST http://localhost:8080/v1/messages \
  -H "Content-Type: application/json" \
  -H "x-api-key: not-required" \
  -H "anthropic-version: 2023-06-01" \
  -d '{
    "model": "claude-3-5-sonnet-20241022",
    "max_tokens": 1000,
    "system": "You are a helpful assistant.",
    "messages": [
      {"role": "user", "content": "Write a hello world in Rust"}
    ],
    "stream": false
  }'
```

### Streaming Example

```bash
curl -X POST http://localhost:8080/v1/messages \
  -H "Content-Type: application/json" \
  -d '{
    "model": "claude-3-5-sonnet-20241022",
    "max_tokens": 1000,
    "messages": [
      {"role": "user", "content": "Explain reactive programming"}
    ],
    "stream": true
  }'
```

### Response Format (Non-Streaming)

```json
{
  "id": "msg_abc123",
  "type": "message",
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "Here is a hello world in Rust:\n\nfn main() {\n    println!(\"Hello, World!\");\n}"
    }
  ],
  "model": "claude-3-5-sonnet-20241022",
  "stop_reason": "end_turn",
  "usage": {
    "input_tokens": 15,
    "output_tokens": 25
  }
}
```

### Streaming Format

```
event: message_start
data: {"type":"message_start","message":{"id":"msg_abc123","type":"message","role":"assistant","content":[]}}

event: content_block_start
data: {"type":"content_block_start","index":0}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"Here is"}}

event: content_block_stop
data: {"type":"content_block_stop","index":0}

event: message_delta
data: {"type":"message_delta","delta":{"stop_reason":"end_turn"},"usage":{"output_tokens":25}}

event: message_stop
data: {"type":"message_stop"}
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
├── claudecode4j-context/             # Token-aware context optimization
│   ├── spi/                          # TokenCounter, ContextOptimizer
│   ├── model/                        # ContextBudget, ModelTokenLimits
│   ├── tokenizer/                    # JTokkit implementation
│   └── optimizer/                    # DefaultContextOptimizer
├── claudecode4j-spring-boot-starter/ # Spring Boot integration
│   ├── autoconfigure/                # Auto-configuration
│   ├── properties/                   # ConfigurationProperties
│   ├── health/                       # HealthIndicator
│   ├── metrics/                      # Micrometer metrics
│   └── resilience/                   # Retry with backoff
├── claudecode4j-rest-adapter/        # REST API
│   ├── controller/                   # ClaudeController
│   └── dto/                          # Request/Response DTOs
├── claudecode4j-kafka-adapter/       # Kafka messaging
│   ├── listener/                     # Message consumer
│   ├── producer/                     # Request producer
│   └── correlation/                  # Correlation ID manager
├── claudecode4j-websocket-adapter/   # WebSocket terminal
│   ├── handler/                      # WebSocket handler
│   ├── session/                      # Session management
│   └── message/                      # Sealed message types
└── claudecode4j-mcp-server/          # MCP Server support
    ├── annotation/                   # @ClaudeTool, @ToolParam
    ├── registry/                     # Tool discovery
    └── server/                       # Tool invocation
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

### Test Profiles

```bash
# Unit tests only (default - fast, no external dependencies)
mvn test

# Integration tests (requires Docker for Testcontainers)
mvn verify -Pintegration

# All tests including E2E (requires Claude CLI installation and authentication)
mvn verify -Pall

# Run specific test class
mvn test -Dtest=ClaudeRestIntegrationTest

# Run specific test method
mvn test -Dtest=ClaudeRestIntegrationTest#shouldExecutePromptAndReturnResponse
```

### Test Categories

- **Unit Tests** (`*Test.java`): Fast, isolated tests with mocked dependencies
- **Integration Tests** (`*IntegrationTest.java`): Tests with real containers (Testcontainers)
- **E2E Tests** (`*E2ETest.java`): Full end-to-end tests requiring Claude CLI

**Note**: E2E tests should only be run manually. They require:
- Claude CLI installed: `npm install -g @anthropic-ai/claude-code`
- Claude CLI authenticated (run `claude` once to authenticate)

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Anthropic](https://www.anthropic.com/) for Claude and Claude Code
- [Spring Team](https://spring.io/) for Spring Boot 4
- [Project Loom](https://openjdk.org/projects/loom/) for Virtual Threads

