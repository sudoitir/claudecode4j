# ClaudeCode4J

[![English](https://img.shields.io/badge/Language-English-blue)](README.md)
[![Chinese](https://img.shields.io/badge/Language-中文-red)](README_CN.md)
[![Persian](https://img.shields.io/badge/Language-فارسی-green)](README_FA.md)

A modern Java library for integrating with...

```

---

### 2. Chinese Translation

**File Name:** `README_CN.md`

```markdown
# ClaudeCode4J

[![Java](https://img.shields.io/badge/Java-25-blue.svg)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

一个用于集成 [Claude Code CLI](https://docs.anthropic.com/en/docs/claude-code)（Anthropic 的代理编码工具）的现代 Java 库。

## 功能特性

- **纯 Java API** - 使用密封类型（Sealed Types）和记录（Records）的清晰接口
- **虚拟线程 (Virtual Threads)** - 基于 Project Loom 的高效并发执行
- **结构化并发 (Structured Concurrency)** - 安全的并行进程管理
- **Spring Boot 4 集成** - 自动配置、健康检查和指标监控
- **REST API 适配器** - 支持 SSE 流式传输的 HTTP 端点
- **Kafka 适配器** - 支持 Correlation ID 的请求-响应模式
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
├── claudecode4j-spring-boot-starter/ # Spring Boot 集成
│   ├── autoconfigure/                # 自动配置
│   ├── properties/                   # ConfigurationProperties
│   ├── health/                       # HealthIndicator
│   └── metrics/                      # Micrometer metrics
├── claudecode4j-rest-adapter/        # REST API
│   ├── controller/                   # ClaudeController
│   └── dto/                          # Request/Response DTOs
└── claudecode4j-kafka-adapter/       # Kafka 消息传递
    ├── listener/                     # 消息消费者
    ├── producer/                     # 请求生产者
    └── correlation/                  # Correlation ID 管理器

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

```bash
# 单元测试
mvn test

# 集成测试 (需要 Docker 以支持 Testcontainers)
mvn verify -Pintegration-tests

```

## 贡献

欢迎贡献！详情请阅读我们的 [贡献指南](CONTRIBUTING.md)。

## 许可证

本项目基于 MIT 许可证开源 - 详情请参阅 [LICENSE](https://www.google.com/search?q=LICENSE) 文件。

## 致谢

* [Anthropic](https://www.anthropic.com/) 提供 Claude 和 Claude Code
* [Spring Team](https://spring.io/) 提供 Spring Boot 4
* [Project Loom](https://openjdk.org/projects/loom/) 提供虚拟线程支持

```
```

