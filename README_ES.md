<div align="center">

# ClaudeCode4J

**Lleva el poder agéntico de Claude Code a Java.**

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
  <b>Una biblioteca Java moderna para integrar con <a href="https://docs.anthropic.com/en/docs/claude-code">Claude Code CLI</a> — la herramienta de codificación agéntica de Anthropic.</b>
</p>

## Tabla de Contenidos

- [Características](#características)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
  - [Maven BOM (Recomendado)](#maven-bom-recomendado)
  - [Biblioteca Principal (Sin Spring)](#biblioteca-principal-sin-spring)
  - [Spring Boot Starter](#spring-boot-starter)
  - [Adaptador REST](#adaptador-rest)
  - [Adaptador Kafka](#adaptador-kafka)
  - [Adaptador WebSocket](#adaptador-websocket)
  - [Servidor MCP](#servidor-mcp)
  - [Módulo de Contexto (Optimización de Tokens)](#módulo-de-contexto-optimización-de-tokens)
- [Inicio Rápido](#inicio-rápido)
  - [Uso Independiente (Sin Spring)](#uso-independiente-sin-spring)
  - [Ejecución Asíncrona](#ejecución-asíncrona)
  - [Streaming](#streaming)
  - [Gestión de Sesiones](#gestión-de-sesiones)
- [Integración con Spring Boot](#integración-con-spring-boot)
  - [Configuración](#configuración)
  - [Uso con Auto-wired](#uso-con-auto-wired)
  - [Límite de Concurrencia con AOP](#límite-de-concurrencia-con-aop)
- [API REST](#api-rest)
  - [Endpoints](#endpoints)
  - [Ejemplo de Petición](#ejemplo-de-petición)
  - [Streaming SSE](#streaming-sse)
- [API Compatible con OpenAI](#api-compatible-con-openai)
  - [Configuración](#configuración-1)
  - [Endpoints](#endpoints-1)
  - [Ejemplo de Petición](#ejemplo-de-petición-1)
  - [Ejemplo de Streaming](#ejemplo-de-streaming)
- [API Compatible con Anthropic](#api-compatible-con-anthropic)
  - [Configuración](#configuración-2)
  - [Endpoints](#endpoints-2)
  - [Ejemplo de Petición](#ejemplo-de-petición-2)
  - [Ejemplo de Streaming](#ejemplo-de-streaming-1)
- [Integración con Kafka](#integración-con-kafka)
  - [Configuración](#configuración-3)
  - [Productor (Lado de la Petición)](#productor-lado-de-la-petición)
  - [Consumidor (Lado del Procesamiento)](#consumidor-lado-del-procesamiento)
- [Estructura del Módulo](#estructura-del-módulo)
  - [Manejo de Excepciones](#manejo-de-excepciones)
- [Observabilidad](#observabilidad)
  - [Health Check](#health-check)
  - [Métricas (Micrometer)](#métricas-micrometer)
- [Seguridad](#seguridad)
- [Construcción desde el Código Fuente](#construcción-desde-el-código-fuente)
- [Ejecución de Pruebas](#ejecución-de-pruebas)
- [Contribuir](#contribuir)
- [Licencia](#licencia)
- [Agradecimientos](#agradecimientos)

---

## Características

- **API Java Pura** - Interfaces limpias con tipos sellados (sealed types) y records
- **Hilos Virtuales (Virtual Threads)** - Ejecución concurrente eficiente usando Project Loom
- **Concurrencia Estructurada** - Gestión segura de procesos paralelos
- **Integración con Spring Boot 4** - Autoconfiguración, comprobaciones de salud y métricas
- **Adaptador API REST** - Endpoints HTTP con soporte para streaming SSE
- **Adaptador Kafka** - Patrón petición-respuesta con IDs de correlación
- **Terminal WebSocket** - Sesiones interactivas con soporte "human-in-the-loop"
- **Servidor MCP** - Exponer métodos Java como herramientas de Claude vía Model Context Protocol
- **Contexto Inteligente** - Optimización de contexto consciente de tokens usando JTokkit
- **Resiliencia** - Reintentos integrados con espera exponencial (exponential backoff)
- **Listo para JPMS** - Soporte completo para el Sistema de Módulos de la Plataforma Java
- **Null-Safe** - Anotaciones JSpecify en todo el código

## Requisitos

- Java 25+
- Claude Code CLI instalado (`npm install -g @anthropic-ai/claude-code`)
- Maven 3.9+

## Instalación

### Maven BOM (Recomendado)

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

### Biblioteca Principal (Sin Spring)

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

### Adaptador REST

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-rest-adapter</artifactId>
</dependency>

```

### Adaptador Kafka

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-kafka-adapter</artifactId>
</dependency>

```

### Adaptador WebSocket

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-websocket-adapter</artifactId>
</dependency>

```

### Servidor MCP

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-mcp-server</artifactId>
</dependency>

```

### Módulo de Contexto (Optimización de Tokens)

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-context</artifactId>
</dependency>

```

## Inicio Rápido

### Uso Independiente (Sin Spring)

```java
import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.core.client.DefaultClaudeClientFactory;

// Crear cliente usando SPI
ClaudeClient client = new DefaultClaudeClientFactory().create();

// Ejecutar un prompt
Prompt prompt = Prompt.of("Explain what this code does");
ClaudeResponse response = client.execute(prompt);

// Manejar la respuesta usando coincidencia de patrones (pattern matching)
switch (response) {
    case TextResponse text -> System.out.println(text.content());
    case StreamResponse stream -> stream.events().forEach(System.out::println);
    case ErrorResponse error -> System.err.println(error.message());
}

```

### Ejecución Asíncrona

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

### Gestión de Sesiones

```java
// Crear una sesión de conversación
ClaudeSession session = client.createSession();

// Continuar la conversación con contexto
ClaudeResponse response1 = session.send(Prompt.of("Create a Java class for User"));
ClaudeResponse response2 = session.send(Prompt.of("Add validation annotations"));

// La sesión mantiene el historial de la conversación
session.close();

```

## Integración con Spring Boot

### Configuración

```yaml
claude:
  code:
    binary-path: /usr/local/bin/claude  # Opcional: auto-detectado
    concurrency-limit: 4                 # Máx ejecuciones concurrentes
    default-timeout: 5m                  # Tiempo de espera (Timeout)
    dangerously-skip-permissions: false  # Bandera de seguridad
    health:
      enabled: true                      # Habilitar indicador de salud
      cache-duration: 30s                # Caché de comprobación de salud
    metrics:
      enabled: true                      # Habilitar métricas de Micrometer

```

### Uso con Auto-wired

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
            default -> "Respuesta inesperada";
        };
    }
}

```

### Límite de Concurrencia con AOP

```java
@Service
public class RateLimitedService {

    private final ClaudeClient claudeClient;

    @ConcurrencyLimit(permits = 2)  // Máx 2 llamadas concurrentes a este método
    public ClaudeResponse processWithLimit(Prompt prompt) {
        return claudeClient.execute(prompt);
    }
}

```

## API REST

Habilita el adaptador REST para exponer la funcionalidad de Claude vía HTTP:

```yaml
claude:
  code:
    rest:
      enabled: true
      base-path: /api/claude

```

### Endpoints

| Método |            Ruta            |          Descripción           |
|--------|----------------------------|--------------------------------|
| `POST` | `/api/claude/prompt`       | Ejecutar prompt síncronamente  |
| `POST` | `/api/claude/prompt/async` | Ejecutar prompt asíncronamente |
| `POST` | `/api/claude/stream`       | Stream de respuesta vía SSE    |
| `GET`  | `/api/claude/health`       | Comprobación de salud          |

### Ejemplo de Petición

```bash
curl -X POST http://localhost:8080/api/claude/prompt \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Write a hello world in Rust",
    "outputFormat": "TEXT",
    "timeout": "PT30S"
  }'

```

### Streaming SSE

```bash
curl -N http://localhost:8080/api/claude/stream \
  -H "Content-Type: application/json" \
  -d '{"text": "Explain microservices architecture"}'

```

## API Compatible con OpenAI

El adaptador REST incluye un endpoint `/v1/chat/completions` **compatible con OpenAI**, permitiendo que herramientas y aplicaciones diseñadas para la API de OpenAI funcionen con Claude Code CLI.

### Configuración

```yaml
claude:
  code:
    rest:
      openai:
        enabled: true                    # Habilitar endpoint compatible con OpenAI (defecto: true)
        base-path: /v1                   # Ruta base para endpoints OpenAI (defecto: /v1)

```

### Endpoints

| Método |          Ruta          |                  Descripción                  |
|--------|------------------------|-----------------------------------------------|
| `POST` | `/v1/chat/completions` | API de chat completions compatible con OpenAI |

### Ejemplo de Petición

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

### Ejemplo de Streaming

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

## API Compatible con Anthropic

El adaptador REST también incluye un endpoint `/v1/messages` **compatible con Anthropic**, siguiendo la especificación de la API Messages de Anthropic.

### Configuración

```yaml
claude:
  code:
    rest:
      anthropic:
        enabled: true                    # Habilitar endpoint compatible con Anthropic (defecto: true)
        base-path: /v1                   # Ruta base para endpoints Anthropic (defecto: /v1)

```

### Endpoints

| Método |      Ruta      |              Descripción              |
|--------|----------------|---------------------------------------|
| `POST` | `/v1/messages` | API Messages compatible con Anthropic |

### Ejemplo de Petición

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

### Ejemplo de Streaming

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

## Integración con Kafka

Habilita mensajería petición-respuesta sobre Kafka:

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

### Productor (Lado de la Petición)

```java
@Service
public class KafkaPromptService {

    private final ClaudeKafkaProducer producer;

    public CompletableFuture<String> sendPrompt(String text) {
        return producer.sendRequest(text);
    }
}

```

### Consumidor (Lado del Procesamiento)

El `ClaudeKafkaListener` automáticamente:

1. Consume mensajes de `request-topic`
2. Ejecuta prompts vía `ClaudeClient`
3. Envía respuestas a `reply-topic` con ID de correlación

## Estructura del Módulo

```
claudecode4j/
├── claudecode4j-bom/                 # Bill of Materials
├── claudecode4j-api/                 # Interfaces y DTOs
│   ├── client/                       # ClaudeClient, ClaudeSession
│   ├── model/                        # Records de Prompt, Response
│   ├── exception/                    # Jerarquía de excepciones selladas
│   └── spi/                          # Puntos de extensión
├── claudecode4j-core/                # Implementación pura en Java
│   ├── client/                       # DefaultClaudeClient
│   ├── process/                      # VirtualThreadExecutor
│   ├── parser/                       # StreamJsonParser
│   └── resolver/                     # Resolvers de binarios
├── claudecode4j-context/             # Optimización de contexto
│   ├── spi/                          # TokenCounter, ContextOptimizer
│   ├── model/                        # ContextBudget, ModelTokenLimits
│   ├── tokenizer/                    # Implementación JTokkit
│   └── optimizer/                    # DefaultContextOptimizer
├── claudecode4j-spring-boot-starter/ # Integración Spring Boot
│   ├── autoconfigure/                # Auto-configuración
│   ├── properties/                   # ConfigurationProperties
│   ├── health/                       # HealthIndicator
│   ├── metrics/                      # Métricas Micrometer
│   └── resilience/                   # Reintentos con backoff
├── claudecode4j-rest-adapter/        # API REST
│   ├── controller/                   # ClaudeController
│   └── dto/                          # DTOs Petición/Respuesta
├── claudecode4j-kafka-adapter/       # Mensajería Kafka
│   ├── listener/                     # Consumidor de mensajes
│   ├── producer/                     # Productor de peticiones
│   └── correlation/                  # Gestor de IDs de correlación
├── claudecode4j-websocket-adapter/   # Terminal WebSocket
│   ├── handler/                      # Manejador WebSocket
│   ├── session/                      # Gestión de sesiones
│   └── message/                      # Tipos de mensajes sellados
└── claudecode4j-mcp-server/          # Soporte servidor MCP
    ├── annotation/                   # @ClaudeTool, @ToolParam
    ├── registry/                     # Descubrimiento de herramientas
    └── server/                       # Invocación de herramientas

```

## Manejo de Excepciones

Todas las excepciones extienden de la clase sellada `ClaudeException`:

```java
try {
    ClaudeResponse response = client.execute(prompt);
} catch (ClaudeException e) {
    switch (e) {
        case ClaudeBinaryNotFoundException ex ->
            log.error("Claude CLI no encontrado: {}", ex.getMessage());
        case ClaudeExecutionException ex ->
            log.error("Falló la ejecución: {}", ex.getMessage());
        case ClaudeTimeoutException ex ->
            log.error("Tiempo de espera agotado tras: {}", ex.getTimeout());
        case ClaudeConfigurationException ex ->
            log.error("Error de configuración: {}", ex.getMessage());
    }
}

```

## Observabilidad

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

### Métricas (Micrometer)

|             Métrica              |  Tipo   |     Descripción     |
|----------------------------------|---------|---------------------|
| `claude.code.executions`         | Counter | Total ejecuciones   |
| `claude.code.executions.active`  | Gauge   | Ejecuciones activas |
| `claude.code.execution.duration` | Timer   | Tiempo de ejecución |
| `claude.code.errors`             | Counter | Errores por tipo    |

## Seguridad

La biblioteca incluye medidas de seguridad integradas:

* **Sanitización de Entradas** - Previene ataques de inyección de comandos
* **Límite de Concurrencia** - Protege contra el agotamiento de recursos
* **Control de Permisos** - `dangerously-skip-permissions` debe ser habilitado explícitamente

## Construcción desde el Código Fuente

```bash
git clone [https://github.com/sudoit/claudecode4j.git](https://github.com/sudoit/claudecode4j.git)
cd claudecode4j
mvn clean install

```

## Ejecución de Pruebas

### Perfiles de Prueba

```bash
# Solo pruebas unitarias (por defecto - rápido, sin dependencias externas)
mvn test

# Pruebas de integración (requiere Docker para Testcontainers)
mvn verify -Pintegration

# Todas las pruebas incluyendo E2E (requiere instalación y autenticación de Claude CLI)
mvn verify -Pall

# Ejecutar clase de prueba específica
mvn test -Dtest=ClaudeRestIntegrationTest

# Ejecutar método de prueba específico
mvn test -Dtest=ClaudeRestIntegrationTest#shouldExecutePromptAndReturnResponse

```

## Contribuir

¡Las contribuciones son bienvenidas! Por favor lee nuestra [Guía de Contribución](https://www.google.com/search?q=CONTRIBUTING.md) para más detalles.

## Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo [LICENSE](https://www.google.com/search?q=LICENSE) para más detalles.

## Agradecimientos

* [Anthropic](https://www.anthropic.com/) por Claude y Claude Code
* [Spring Team](https://spring.io/) por Spring Boot 4
* [Project Loom](https://openjdk.org/projects/loom/) por los Hilos Virtuales

