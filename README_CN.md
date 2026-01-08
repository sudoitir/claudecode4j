<div align="center">

# ClaudeCode4J

**将 Claude Code 的代理能力引入 Java。**

[English](README.md) • [中文](README_CN.md) • [فارسی](README_FA.md) • [Español](README_ES.md)

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
  <b>一个用于集成 <a href="https://docs.anthropic.com/en/docs/claude-code">Claude Code CLI</a> — Anthropic 代理编码工具的现代 Java 库。</b>
</p>

## 目录

- [功能特性](#功能特性)
- [环境要求](#环境要求)
- [安装](#安装)
  - [Maven BOM (推荐)](#maven-bom-推荐)
  - [核心库 (无 Spring 依赖)](#核心库-无-spring-依赖)
  - [Spring Boot Starter](#spring-boot-starter)
  - [REST 适配器](#rest-适配器)
  - [Kafka 适配器](#kafka-适配器)
  - [WebSocket 适配器](#websocket-适配器)
  - [MCP 服务器](#mcp-服务器)
  - [上下文模块 (Token 优化)](#上下文模块-token-优化)
- [快速开始](#快速开始)
  - [独立使用 (无 Spring)](#独立使用-无-spring)
  - [异步执行](#异步执行)
  - [流式传输 (Streaming)](#流式传输-streaming)
  - [会话管理](#会话管理)
- [Spring Boot 集成](#spring-boot-集成)
  - [配置](#配置)
  - [自动装配使用](#自动装配使用)
  - [使用 AOP 限制并发](#使用-aop-限制并发)
- [REST API](#rest-api)
  - [端点](#端点)
  - [示例请求](#示例请求)
  - [SSE 流式传输](#sse-流式传输)
- [OpenAI 兼容 API](#openai-兼容-api)
  - [配置](#配置-1)
  - [端点](#端点-1)
  - [示例请求](#示例请求-1)
  - [流式传输示例](#流式传输示例)
- [Anthropic 兼容 API](#anthropic-兼容-api)
  - [配置](#配置-2)
  - [端点](#端点-2)
  - [示例请求](#示例请求-2)
  - [流式传输示例](#流式传输示例-1)
- [Kafka 集成](#kafka-集成)
  - [配置](#配置-3)
  - [生产者 (请求端)](#生产者-请求端)
  - [消费者 (处理端)](#消费者-处理端)
- [模块结构](#模块结构)
- [异常处理](#异常处理)
- [可观测性](#可观测性)
  - [健康检查](#健康检查)
  - [指标 (Micrometer)](#指标-micrometer)
- [安全性](#安全性)
- [从源码构建](#从源码构建)
- [运行测试](#运行测试)
- [贡献](#贡献)
- [许可证](#许可证)
- [致谢](#致谢)

---

## 功能特性

- **纯 Java API** - 使用密封类型 (Sealed Types) 和记录 (Records) 的清晰接口
- **虚拟线程 (Virtual Threads)** - 基于 Project Loom 的高效并发执行
- **结构化并发 (Structured Concurrency)** - 安全的并行进程管理
- **Spring Boot 4 集成** - 自动配置、健康检查和指标监控
- **REST API 适配器** - 支持 SSE 流式传输的 HTTP 端点
- **Kafka 适配器** - 支持 Correlation ID 的请求-响应模式
- **WebSocket 终端** - 支持人机交互 (Human-in-the-loop) 的会话
- **MCP 服务器** - 通过 Model Context Protocol 将 Java 方法暴露为 Claude 工具
- **智能上下文** - 使用 JTokkit 进行 Token 感知的上下文优化
- **弹性设计** - 内置带指数退避的重试机制
- **JPMS 就绪** - 完全支持 Java 平台模块系统
- **空安全 (Null-Safe)** - 全面使用 JSpecify 注解

## 环境要求

- Java 25+
- 已安装 Claude Code CLI (`npm install -g @anthropic-ai/claude-code`)
- Maven 3.9+

## 安装

### Maven BOM (推荐)

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

### 核心库 (无 Spring 依赖)

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

### REST 适配器

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-rest-adapter</artifactId>
</dependency>

```

### Kafka 适配器

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-kafka-adapter</artifactId>
</dependency>

```

### WebSocket 适配器

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-websocket-adapter</artifactId>
</dependency>

```

### MCP 服务器

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-mcp-server</artifactId>
</dependency>

```

### 上下文模块 (Token 优化)

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-context</artifactId>
</dependency>

```

## 快速开始

### 独立使用 (无 Spring)

```java
import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.core.client.DefaultClaudeClientFactory;

// 使用 SPI 创建客户端
ClaudeClient client = new DefaultClaudeClientFactory().create();

// 执行提示词
Prompt prompt = Prompt.of("Explain what this code does");
ClaudeResponse response = client.execute(prompt);

// 使用模式匹配处理响应
switch (response) {
    case TextResponse text -> System.out.println(text.content());
    case StreamResponse stream -> stream.events().forEach(System.out::println);
    case ErrorResponse error -> System.err.println(error.message());
}

```

### 异步执行

```java
CompletableFuture<ClaudeResponse> future = client.executeAsync(prompt);
future.thenAccept(response -> {
    if (response instanceof TextResponse text) {
        System.out.println(text.content());
    }
});

```

### 流式传输 (Streaming)

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

### 会话管理

```java
// 创建会话
ClaudeSession session = client.createSession();

// 在上下文中继续对话
ClaudeResponse response1 = session.send(Prompt.of("Create a Java class for User"));
ClaudeResponse response2 = session.send(Prompt.of("Add validation annotations"));

// 会话将保留对话历史
session.close();

```

## Spring Boot 集成

### 配置

```yaml
claude:
  code:
    binary-path: /usr/local/bin/claude  # 可选：自动检测
    concurrency-limit: 4                 # 最大并发执行数
    default-timeout: 5m                  # 执行超时时间
    dangerously-skip-permissions: false  # 安全标志
    health:
      enabled: true                      # 启用健康检查指标
      cache-duration: 30s                # 健康检查缓存时长
    metrics:
      enabled: true                      # 启用 Micrometer 指标

```

### 自动装配使用

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

### 使用 AOP 限制并发

```java
@Service
public class RateLimitedService {

    private final ClaudeClient claudeClient;

    @ConcurrencyLimit(permits = 2)  // 该方法最多允许 2 个并发调用
    public ClaudeResponse processWithLimit(Prompt prompt) {
        return claudeClient.execute(prompt);
    }
}

```

## REST API

启用 REST 适配器以通过 HTTP 暴露 Claude 功能：

```yaml
claude:
  code:
    rest:
      enabled: true
      base-path: /api/claude

```

### 端点

|   方法   |             路径             |      描述       |
|--------|----------------------------|---------------|
| `POST` | `/api/claude/prompt`       | 同步执行提示词       |
| `POST` | `/api/claude/prompt/async` | 异步执行提示词       |
| `POST` | `/api/claude/stream`       | 通过 SSE 流式传输响应 |
| `GET`  | `/api/claude/health`       | 健康检查          |

### 示例请求

```bash
curl -X POST http://localhost:8080/api/claude/prompt \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Write a hello world in Rust",
    "outputFormat": "TEXT",
    "timeout": "PT30S"
  }'

```

### SSE 流式传输

```bash
curl -N http://localhost:8080/api/claude/stream \
  -H "Content-Type: application/json" \
  -d '{"text": "Explain microservices architecture"}'

```

## OpenAI 兼容 API

REST 适配器包含一个 **OpenAI 兼容** 的 `/v1/chat/completions` 端点，允许为 OpenAI API 设计的工具和应用程序与 Claude Code CLI 配合使用。

### 配置

```yaml
claude:
  code:
    rest:
      openai:
        enabled: true                    # 启用 OpenAI 兼容端点 (默认: true)
        base-path: /v1                   # OpenAI 端点的基础路径 (默认: /v1)

```

### 端点

|   方法   |           路径           |         描述         |
|--------|------------------------|--------------------|
| `POST` | `/v1/chat/completions` | OpenAI 兼容的聊天补全 API |

### 示例请求

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

### 流式传输示例

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

## Anthropic 兼容 API

REST 适配器还包含一个 **Anthropic 兼容** 的 `/v1/messages` 端点，遵循 Anthropic 的 Messages API 规范。

### 配置

```yaml
claude:
  code:
    rest:
      anthropic:
        enabled: true                    # 启用 Anthropic 兼容端点 (默认: true)
        base-path: /v1                   # Anthropic 端点的基础路径 (默认: /v1)

```

### 端点

|   方法   |       路径       |             描述             |
|--------|----------------|----------------------------|
| `POST` | `/v1/messages` | Anthropic 兼容的 Messages API |

### 示例请求

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

### 流式传输示例

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

## Kafka 集成

启用基于 Kafka 的请求-响应消息传递：

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

### 生产者 (请求端)

```java
@Service
public class KafkaPromptService {

    private final ClaudeKafkaProducer producer;

    public CompletableFuture<String> sendPrompt(String text) {
        return producer.sendRequest(text);
    }
}

```

### 消费者 (处理端)

`ClaudeKafkaListener` 自动执行以下操作：

1. 从 `request-topic` 消费消息
2. 通过 `ClaudeClient` 执行提示词
3. 将响应发送到 `reply-topic`（附带 Correlation ID）

## 模块结构

```
claudecode4j/
├── claudecode4j-bom/                 # Bill of Materials (BOM)
├── claudecode4j-api/                 # 接口与 DTO
│   ├── client/                       # ClaudeClient, ClaudeSession
│   ├── model/                        # Prompt, Response records
│   ├── exception/                    # 密封异常层级
│   └── spi/                          # 扩展点
├── claudecode4j-core/                # 纯 Java 实现
│   ├── client/                       # DefaultClaudeClient
│   ├── process/                      # VirtualThreadExecutor
│   ├── parser/                       # StreamJsonParser
│   └── resolver/                     # Binary resolvers
├── claudecode4j-context/             # Token 感知的上下文优化
│   ├── spi/                          # TokenCounter, ContextOptimizer
│   ├── model/                        # ContextBudget, ModelTokenLimits
│   ├── tokenizer/                    # JTokkit 实现
│   └── optimizer/                    # DefaultContextOptimizer
├── claudecode4j-spring-boot-starter/ # Spring Boot 集成
│   ├── autoconfigure/                # 自动配置
│   ├── properties/                   # ConfigurationProperties
│   ├── health/                       # HealthIndicator
│   ├── metrics/                      # Micrometer 指标
│   └── resilience/                   # 带退避的重试机制
├── claudecode4j-rest-adapter/        # REST API
│   ├── controller/                   # ClaudeController
│   └── dto/                          # Request/Response DTOs
├── claudecode4j-kafka-adapter/       # Kafka 消息传递
│   ├── listener/                     # 消息消费者
│   ├── producer/                     # 请求生产者
│   └── correlation/                  # Correlation ID 管理器
├── claudecode4j-websocket-adapter/   # WebSocket 终端
│   ├── handler/                      # WebSocket 处理器
│   ├── session/                      # 会话管理
│   └── message/                      # 密封消息类型
└── claudecode4j-mcp-server/          # MCP 服务器支持
    ├── annotation/                   # @ClaudeTool, @ToolParam
    ├── registry/                     # 工具发现
    └── server/                       # 工具调用

```

## 异常处理

所有异常均继承自密封类 `ClaudeException`：

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

## 可观测性

### 健康检查

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

### 指标 (Micrometer)

|                指标                |   类型    |     描述     |
|----------------------------------|---------|------------|
| `claude.code.executions`         | Counter | 总执行次数      |
| `claude.code.executions.active`  | Gauge   | 当前正在运行的执行数 |
| `claude.code.execution.duration` | Timer   | 执行耗时       |
| `claude.code.errors`             | Counter | 按类型统计的错误数  |

## 安全性

本库包含内置的安全措施：

* **输入清理** - 防止命令注入攻击
* **并发限制** - 防止资源耗尽
* **权限控制** - 必须显式启用 `dangerously-skip-permissions`

## 从源码构建

```bash
git clone [https://github.com/sudoit/claudecode4j.git](https://github.com/sudoit/claudecode4j.git)
cd claudecode4j
mvn clean install

```

## 运行测试

### 测试 Profiles

```bash
# 仅单元测试 (默认 - 快速，无外部依赖)
mvn test

# 集成测试 (需要 Docker 以支持 Testcontainers)
mvn verify -Pintegration

# 所有测试，包含 E2E (需要安装 Claude CLI 并进行认证)
mvn verify -Pall

# 运行特定测试类
mvn test -Dtest=ClaudeRestIntegrationTest

# 运行特定测试方法
mvn test -Dtest=ClaudeRestIntegrationTest#shouldExecutePromptAndReturnResponse

```

### 测试分类

* **单元测试** (`*Test.java`): 快速、隔离的测试，使用 Mock 依赖
* **集成测试** (`*IntegrationTest.java`): 使用真实容器 (Testcontainers) 的测试
* **E2E 测试** (`*E2ETest.java`): 需要 Claude CLI 的完整端到端测试

**注意**: E2E 测试应仅手动运行。它们要求：

* 已安装 Claude CLI: `npm install -g @anthropic-ai/claude-code`
* Claude CLI 已认证 (运行一次 `claude` 进行认证)

## 贡献

欢迎贡献！详情请阅读我们的 [贡献指南](https://www.google.com/search?q=CONTRIBUTING.md)。

## 许可证

本项目基于 MIT 许可证开源 - 详情请参阅 [LICENSE](https://www.google.com/search?q=LICENSE) 文件。

## 致谢

* [Anthropic](https://www.anthropic.com/) 提供 Claude 和 Claude Code
* [Spring Team](https://spring.io/) 提供 Spring Boot 4
* [Project Loom](https://openjdk.org/projects/loom/) 提供虚拟线程支持

