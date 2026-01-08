<div dir="rtl">
<div align="center">

# ClaudeCode4J

**قدرت عاملی Claude Code را به جاوا بیاورید.**

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
  <b>یک کتابخانه مدرن جاوا برای ادغام با <a href="https://docs.anthropic.com/en/docs/claude-code">Claude Code CLI</a> — ابزار کدنویسی عاملی Anthropic.</b>
</p>

## فهرست مطالب

- [ویژگی‌ها](#ویژگی‌ها)
- [پیش‌نیازها](#پیش‌نیازها)
- [نصب](#نصب)
  - [Maven BOM (توصیه شده)](#maven-bom-توصیه-شده)
  - [کتابخانه هسته (بدون Spring)](#کتابخانه-هسته-بدون-spring)
  - [Spring Boot Starter](#spring-boot-starter)
  - [آداپتور REST](#آداپتور-rest)
  - [آداپتور Kafka](#آداپتور-kafka)
  - [آداپتور WebSocket](#آداپتور-websocket)
  - [سرور MCP](#سرور-mcp)
  - [ماژول کانتکست (بهینه‌سازی توکن)](#ماژول-کانتکست-بهینه‌سازی-توکن)
- [شروع سریع](#شروع-سریع)
  - [استفاده مستقل (بدون Spring)](#استفاده-مستقل-بدون-spring)
  - [اجرای غیرهمگام (Async)](#اجرای-غیرهمگام-async)
  - [استریمینگ (Streaming)](#استریمینگ-streaming)
  - [مدیریت نشست (Session)](#مدیریت-نشست-session)
- [ادغام با Spring Boot](#ادغام-با-spring-boot)
  - [پیکربندی](#پیکربندی)
  - [استفاده با تزریق وابستگی (Auto-wired)](#استفاده-با-تزریق-وابستگی-auto-wired)
  - [محدودسازی همزمانی با AOP](#محدودسازی-همزمانی-با-aop)
- [REST API](#rest-api)
  - [اندپوینت‌ها](#اندپوینت‌ها)
  - [نمونه درخواست](#نمونه-درخواست)
  - [استریمینگ با SSE](#استریمینگ-با-sse)
- [API سازگار با OpenAI](#api-سازگار-با-openai)
  - [پیکربندی](#پیکربندی-1)
  - [اندپوینت‌ها](#اندپوینت‌ها-1)
  - [نمونه درخواست](#نمونه-درخواست-1)
  - [نمونه استریمینگ](#نمونه-استریمینگ)
- [API سازگار با Anthropic](#api-سازگار-با-anthropic)
  - [پیکربندی](#پیکربندی-2)
  - [اندپوینت‌ها](#اندپوینت‌ها-2)
  - [نمونه درخواست](#نمونه-درخواست-2)
  - [نمونه استریمینگ](#نمونه-استریمینگ-1)
- [ادغام با Kafka](#ادغام-با-kafka)
  - [پیکربندی](#پیکربندی-3)
  - [تولیدکننده (سمت درخواست)](#تولیدکننده-سمت-درخواست)
  - [مصرف‌کننده (سمت پردازش)](#مصرف‌کننده-سمت-پردازش)
- [ساختار ماژول‌ها](#ساختار-ماژول‌ها)
- [مدیریت خطاها](#مدیریت-خطاها-exception-handling)
- [قابلیت مشاهده (Observability)](#قابلیت-مشاهده-observability)
  - [بررسی سلامت (Health Check)](#بررسی-سلامت-health-check)
  - [متریک‌ها (Micrometer)](#متریک‌ها-micrometer)
- [امنیت](#امنیت)
- [ساخت از سورس کد](#ساخت-از-سورس-کد)
- [اجرای تست‌ها](#اجرای-تست‌ها)
- [مشارکت](#مشارکت)
- [لایسنس](#لایسنس)
- [تقدیر و تشکر](#تقدیر-و-تشکر)

---

## ویژگی‌ها

* **API خالص جاوا** - رابط‌های تمیز با استفاده از sealed types و records
* **Virtual Threads** - اجرای همزمان کارآمد با استفاده از Project Loom
* **Structured Concurrency** - مدیریت ایمن پردازش‌های موازی
* **ادغام با Spring Boot 4** - تنظیمات خودکار (Auto-configuration)، بررسی سلامت (Health checks) و متریک‌ها
* **آداپتور REST API** - اندپوینت‌های HTTP با پشتیبانی از SSE streaming
* **آداپتور Kafka** - الگوی درخواست-پاسخ با استفاده از correlation IDs
* **ترمینال WebSocket** - نشست‌های تعاملی با پشتیبانی از انسان-در-حلقه (human-in-the-loop)
* **سرور MCP** - ارائه متدهای جاوا به عنوان ابزارهای Claude از طریق Model Context Protocol
* **کانتکست هوشمند** - بهینه‌سازی کانتکست با آگاهی از توکن‌ها با استفاده از JTokkit
* **تاب‌آوری (Resilience)** - تلاش مجدد داخلی با الگوی exponential backoff
* **آماده برای JPMS** - پشتیبانی کامل از سیستم ماژولار پلتفرم جاوا
* **Null-Safe** - استفاده سراسری از انوتیشن‌های JSpecify

## پیش‌نیازها

* Java 25+
* نصب بودن Claude Code CLI (`npm install -g @anthropic-ai/claude-code`)
* Maven 3.9+

## نصب

### Maven BOM (توصیه شده)

<div dir="ltr">

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

</div>

### کتابخانه هسته (بدون Spring)

<div dir="ltr">

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-core</artifactId>
</dependency>

```

</div>

### Spring Boot Starter

<div dir="ltr">

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-spring-boot-starter</artifactId>
</dependency>

```

</div>

### آداپتور REST

<div dir="ltr">

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-rest-adapter</artifactId>
</dependency>

```

</div>

### آداپتور Kafka

<div dir="ltr">

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-kafka-adapter</artifactId>
</dependency>

```

</div>

### آداپتور WebSocket

<div dir="ltr">

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-websocket-adapter</artifactId>
</dependency>

```

</div>

### سرور MCP

<div dir="ltr">

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-mcp-server</artifactId>
</dependency>

```

</div>

### ماژول کانتکست (بهینه‌سازی توکن)

<div dir="ltr">

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-context</artifactId>
</dependency>

```

</div>

## شروع سریع

### استفاده مستقل (بدون Spring)

<div dir="ltr">

```java
import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.core.client.DefaultClaudeClientFactory;

// ساخت کلاینت با استفاده از SPI
ClaudeClient client = new DefaultClaudeClientFactory().create();

// اجرای یک پرامپت
Prompt prompt = Prompt.of("Explain what this code does");
ClaudeResponse response = client.execute(prompt);

// مدیریت پاسخ با استفاده از pattern matching
switch (response) {
    case TextResponse text -> System.out.println(text.content());
    case StreamResponse stream -> stream.events().forEach(System.out::println);
    case ErrorResponse error -> System.err.println(error.message());
}

```

</div>

### اجرای غیرهمگام (Async)

<div dir="ltr">

```java
CompletableFuture<ClaudeResponse> future = client.executeAsync(prompt);
future.thenAccept(response -> {
    if (response instanceof TextResponse text) {
        System.out.println(text.content());
    }
});

```

</div>

### استریمینگ (Streaming)

<div dir="ltr">

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

</div>

### مدیریت نشست (Session)

<div dir="ltr">

```java
// ایجاد یک نشست گفتگو
ClaudeSession session = client.createSession();

// ادامه گفتگو با حفظ کانتکست
ClaudeResponse response1 = session.send(Prompt.of("Create a Java class for User"));
ClaudeResponse response2 = session.send(Prompt.of("Add validation annotations"));

// نشست تاریخچه گفتگو را حفظ می‌کند
session.close();

```

</div>

## ادغام با Spring Boot

### پیکربندی

<div dir="ltr">

```yaml
claude:
  code:
    binary-path: /usr/local/bin/claude  # اختیاری: تشخیص خودکار
    concurrency-limit: 4                 # حداکثر اجراهای همزمان
    default-timeout: 5m                  # زمان انتظار (Timeout) اجرا
    dangerously-skip-permissions: false  # پرچم امنیتی
    health:
      enabled: true                      # فعال‌سازی شاخص سلامت
      cache-duration: 30s                # مدت کش بررسی سلامت
    metrics:
      enabled: true                      # فعال‌سازی متریک‌های Micrometer

```

</div>

### استفاده با تزریق وابستگی (Auto-wired)

<div dir="ltr">

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

</div>

### محدودسازی همزمانی با AOP

<div dir="ltr">

```java
@Service
public class RateLimitedService {

    private final ClaudeClient claudeClient;

    @ConcurrencyLimit(permits = 2)  // حداکثر ۲ فراخوانی همزمان برای این متد
    public ClaudeResponse processWithLimit(Prompt prompt) {
        return claudeClient.execute(prompt);
    }
}

```

</div>

## REST API

آداپتور REST را فعال کنید تا قابلیت‌های Claude را از طریق HTTP ارائه دهید:

<div dir="ltr">

```yaml
claude:
  code:
    rest:
      enabled: true
      base-path: /api/claude

```

</div>

### اندپوینت‌ها

|  متد   |            مسیر            |         توضیحات         |
|--------|----------------------------|-------------------------|
| `POST` | `/api/claude/prompt`       | اجرای همگام پرامپت      |
| `POST` | `/api/claude/prompt/async` | اجرای غیرهمگام پرامپت   |
| `POST` | `/api/claude/stream`       | استریم پاسخ از طریق SSE |
| `GET`  | `/api/claude/health`       | بررسی سلامت سرویس       |

### نمونه درخواست

<div dir="ltr">

```bash
curl -X POST http://localhost:8080/api/claude/prompt \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Write a hello world in Rust",
    "outputFormat": "TEXT",
    "timeout": "PT30S"
  }'

```

</div>

### استریمینگ با SSE

<div dir="ltr">

```bash
curl -N http://localhost:8080/api/claude/stream \
  -H "Content-Type: application/json" \
  -d '{"text": "Explain microservices architecture"}'

```

</div>

## API سازگار با OpenAI

آداپتور REST شامل یک اندپوینت `/v1/chat/completions` **سازگار با OpenAI** است که به ابزارها و برنامه‌های طراحی شده برای API‌های OpenAI اجازه می‌دهد با Claude Code CLI کار کنند.

### پیکربندی

<div dir="ltr">

```yaml
claude:
  code:
    rest:
      openai:
        enabled: true                    # فعال‌سازی اندپوینت سازگار با OpenAI (پیش‌فرض: true)
        base-path: /v1                   # مسیر پایه اندپوینت‌های OpenAI (پیش‌فرض: /v1)

```

</div>

### اندپوینت‌ها

|  متد   |          مسیر          |                      توضیحات                       |
|--------|------------------------|----------------------------------------------------|
| `POST` | `/v1/chat/completions` | API تکمیل گفتگو (Chat Completion) سازگار با OpenAI |

### نمونه درخواست

<div dir="ltr">

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

</div>

### نمونه استریمینگ

<div dir="ltr">

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

</div>

## API سازگار با Anthropic

آداپتور REST همچنین شامل یک اندپوینت `/v1/messages` **سازگار با Anthropic** است که از مشخصات API پیام‌های Anthropic پیروی می‌کند.

### پیکربندی

<div dir="ltr">

```yaml
claude:
  code:
    rest:
      anthropic:
        enabled: true                    # فعال‌سازی اندپوینت سازگار با Anthropic (پیش‌فرض: true)
        base-path: /v1                   # مسیر پایه اندپوینت‌های Anthropic (پیش‌فرض: /v1)

```

</div>

### اندپوینت‌ها

|  متد   |      مسیر      |                     توضیحات                     |
|--------|----------------|-------------------------------------------------|
| `POST` | `/v1/messages` | API پیام‌های (Messages API) سازگار با Anthropic |

### نمونه درخواست

<div dir="ltr">

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

</div>

### نمونه استریمینگ

<div dir="ltr">

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

</div>

## ادغام با Kafka

فعال‌سازی پیام‌رسانی درخواست-پاسخ بر بستر Kafka:

<div dir="ltr">

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

</div>

### تولیدکننده (سمت درخواست)

<div dir="ltr">

```java
@Service
public class KafkaPromptService {

    private final ClaudeKafkaProducer producer;

    public CompletableFuture<String> sendPrompt(String text) {
        return producer.sendRequest(text);
    }
}

```

</div>

### مصرف‌کننده (سمت پردازش)

کلاس `ClaudeKafkaListener` به صورت خودکار:

1. پیام‌ها را از `request-topic` دریافت می‌کند
2. پرامپت‌ها را از طریق `ClaudeClient` اجرا می‌کند
3. پاسخ‌ها را به `reply-topic` (همراه با شناسه Correlation) ارسال می‌کند

## ساختار ماژول‌ها

<div dir="ltr">

```
claudecode4j/
├── claudecode4j-bom/                 # Bill of Materials
├── claudecode4j-api/                 # رابط‌ها و DTOها
│   ├── client/                       # ClaudeClient, ClaudeSession
│   ├── model/                        # Prompt, Response records
│   ├── exception/                    # سلسله‌مراتب استثناهای sealed
│   └── spi/                          # نقاط گسترش (Extension points)
├── claudecode4j-core/                # پیاده‌سازی خالص جاوا
│   ├── client/                       # DefaultClaudeClient
│   ├── process/                      # VirtualThreadExecutor
│   ├── parser/                       # StreamJsonParser
│   └── resolver/                     # Binary resolvers
├── claudecode4j-context/             # بهینه‌سازی کانتکست با آگاهی از توکن
│   ├── spi/                          # TokenCounter, ContextOptimizer
│   ├── model/                        # ContextBudget, ModelTokenLimits
│   ├── tokenizer/                    # پیاده‌سازی JTokkit
│   └── optimizer/                    # DefaultContextOptimizer
├── claudecode4j-spring-boot-starter/ # ادغام با Spring Boot
│   ├── autoconfigure/                # پیکربندی خودکار
│   ├── properties/                   # مشخصات پیکربندی
│   ├── health/                       # شاخص سلامت
│   ├── metrics/                      # متریک‌های Micrometer
│   └── resilience/                   # تلاش مجدد با backoff
├── claudecode4j-rest-adapter/        # REST API
│   ├── controller/                   # ClaudeController
│   └── dto/                          # Request/Response DTOs
├── claudecode4j-kafka-adapter/       # پیام‌رسانی Kafka
│   ├── listener/                     # مصرف‌کننده پیام
│   ├── producer/                     # تولیدکننده درخواست
│   └── correlation/                  # مدیریت شناسه Correlation
├── claudecode4j-websocket-adapter/   # ترمینال WebSocket
│   ├── handler/                      # هندلر WebSocket
│   ├── session/                      # مدیریت نشست
│   └── message/                      # انواع پیام‌های sealed
└── claudecode4j-mcp-server/          # پشتیبانی سرور MCP
    ├── annotation/                   # @ClaudeTool, @ToolParam
    ├── registry/                     # کشف ابزار
    └── server/                       # فراخوانی ابزار

```

</div>

## مدیریت خطاها (Exception Handling)

تمام استثناها از کلاس sealed با نام `ClaudeException` ارث‌بری می‌کنند:

<div dir="ltr">

```java
try {
    ClaudeResponse response = client.execute(prompt);
} catch (ClaudeException e) {
    switch (e) {
        case ClaudeBinaryNotFoundException ex -> log.error("Claude CLI not found: {}", ex.getMessage());
        case ClaudeExecutionException ex -> log.error("Execution failed: {}", ex.getMessage());
        case ClaudeTimeoutException ex -> log.error("Timeout after: {}", ex.getTimeout());
        case ClaudeConfigurationException ex -> log.error("Configuration error: {}", ex.getMessage());
    }
}

```

</div>

## قابلیت مشاهده (Observability)

### بررسی سلامت (Health Check)

<div dir="ltr">

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

</div>

### متریک‌ها (Micrometer)

|              متریک               |   نوع   |          توضیحات           |
|----------------------------------|---------|----------------------------|
| `claude.code.executions`         | Counter | تعداد کل اجراها            |
| `claude.code.executions.active`  | Gauge   | تعداد اجراهای در حال انجام |
| `claude.code.execution.duration` | Timer   | زمان اجرا                  |
| `claude.code.errors`             | Counter | تعداد خطاها بر اساس نوع    |

## امنیت

این کتابخانه شامل تدابیر امنیتی داخلی است:

* **پاکسازی ورودی (Input Sanitization)** - جلوگیری از حملات تزریق دستور (command injection)
* **محدودسازی همزمانی** - محافظت در برابر اتمام منابع
* **کنترل دسترسی** - پرچم `dangerously-skip-permissions` باید به صورت صریح فعال شود

## ساخت از سورس کد

<div dir="ltr">

```bash
git clone [https://github.com/sudoit/claudecode4j.git](https://github.com/sudoit/claudecode4j.git)
cd claudecode4j
mvn clean install

```

</div>

## اجرای تست‌ها

### پروفایل‌های تست

<div dir="ltr">

```bash
# تست‌های واحد فقط (پیش‌فرض - سریع، بدون وابستگی خارجی)
mvn test

# تست‌های یکپارچه (نیاز به Docker برای Testcontainers)
mvn verify -Pintegration

# تمام تست‌ها شامل E2E (نیاز به نصب Claude CLI و احراز هویت)
mvn verify -Pall

# اجرای کلاس تست خاص
mvn test -Dtest=ClaudeRestIntegrationTest

# اجرای متد تست خاص
mvn test -Dtest=ClaudeRestIntegrationTest#shouldExecutePromptAndReturnResponse

```

</div>

### دسته‌بندی تست‌ها

* **تست‌های واحد** (`*Test.java`): تست‌های سریع و ایزوله با وابستگی‌های ماک شده
* **تست‌های یکپارچه** (`*IntegrationTest.java`): تست‌ها با کانتینرهای واقعی (Testcontainers)
* **تست‌های E2E** (`*E2ETest.java`): تست‌های کامل سمت کاربر که به Claude CLI نیاز دارند

**توجه**: تست‌های E2E باید فقط به صورت دستی اجرا شوند. آنها نیاز دارند به:

* نصب بودن Claude CLI: `npm install -g @anthropic-ai/claude-code`
* احراز هویت شده بودن Claude CLI (یکبار اجرای `claude` برای احراز هویت)

## مشارکت

مشارکت‌ها استقبال می‌شوند! لطفاً برای جزئیات بیشتر [راهنمای مشارکت](https://www.google.com/search?q=CONTRIBUTING.md) را مطالعه کنید.

## لایسنس

این پروژه تحت مجوز MIT منتشر شده است - برای جزئیات فایل [LICENSE](https://www.google.com/search?q=LICENSE) را ببینید.

## تقدیر و تشکر

* [Anthropic](https://www.anthropic.com/) برای Claude و Claude Code
* [تیم Spring](https://spring.io/) برای Spring Boot 4
* [Project Loom](https://openjdk.org/projects/loom/) برای Virtual Threads

</div>

