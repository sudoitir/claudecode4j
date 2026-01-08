# ClaudeCode4J MCP Server

Model Context Protocol (MCP) server support for exposing Java methods as Claude tools.

## Features

- **Annotation-Based Tools**: Use `@ClaudeTool` to expose methods as MCP tools
- **Automatic Discovery**: Spring beans are automatically scanned for tool annotations
- **Type-Safe Parameters**: Parameter descriptions and validation via `@ToolParam`
- **JSON Schema Generation**: Automatic JSON schema generation for tool parameters

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>ir.sudoit</groupId>
    <artifactId>claudecode4j-mcp-server</artifactId>
</dependency>
```

## Configuration

```yaml
claude:
  code:
    mcp:
      enabled: true
      transport: stdio
      server-name: my-mcp-server
      scan-packages:
        - com.example.tools
```

## Usage

### Creating Tools

Use the `@ClaudeTool` annotation to expose methods as Claude tools:

```java
@Component
public class FileTools {

    @ClaudeTool(
        name = "read_file",
        description = "Reads the contents of a file",
        categories = {"filesystem", "read"}
    )
    public String readFile(
            @ToolParam(description = "Path to the file to read") String path
    ) throws IOException {
        return Files.readString(Path.of(path));
    }

    @ClaudeTool(
        name = "list_directory",
        description = "Lists files in a directory",
        requiresPermission = true
    )
    public List<String> listDirectory(
            @ToolParam(description = "Directory path") String path,
            @ToolParam(
                description = "File pattern to match",
                required = false,
                defaultValue = "*"
            ) String pattern
    ) throws IOException {
        return Files.list(Path.of(path))
            .map(Path::getFileName)
            .map(Path::toString)
            .filter(name -> name.matches(pattern.replace("*", ".*")))
            .toList();
    }
}
```

### Tool Annotations

**@ClaudeTool**
| Attribute | Type | Description |
|-----------|------|-------------|
| `name` | String | Tool name (defaults to method name) |
| `description` | String | Tool description for Claude |
| `categories` | String[] | Tool categories for organization |
| `requiresPermission` | boolean | Whether tool requires user permission |

**@ToolParam**
| Attribute | Type | Description |
|-----------|------|-------------|
| `name` | String | Parameter name (defaults to Java parameter name) |
| `description` | String | Parameter description for Claude |
| `required` | boolean | Whether parameter is required (default: true) |
| `defaultValue` | String | Default value if not provided |

### Accessing the Registry

```java
@Autowired
private AnnotatedToolRegistry toolRegistry;

// Get all tools
Collection<ToolDefinition> tools = toolRegistry.getAllTools();

// Get specific tool
Optional<ToolDefinition> tool = toolRegistry.getTool("read_file");

// Check if tool exists
boolean exists = toolRegistry.contains("read_file");
```

### Manual Tool Invocation

```java
@Autowired
private ToolInvocationHandler invocationHandler;

ToolDefinition tool = toolRegistry.getTool("read_file").orElseThrow();
Map<String, Object> args = Map.of("path", "/etc/hostname");

try {
    Object result = invocationHandler.invoke(tool, args);
    System.out.println("Result: " + result);
} catch (ToolInvocationException e) {
    System.err.println("Tool failed: " + e.getMessage());
}
```

## Supported Parameter Types

|      Java Type       | JSON Type |
|----------------------|-----------|
| `String`             | `string`  |
| `int`, `Integer`     | `integer` |
| `long`, `Long`       | `integer` |
| `double`, `Double`   | `number`  |
| `boolean`, `Boolean` | `boolean` |
| `List<?>`            | `array`   |
| `Map<?, ?>`          | `object`  |
| Records              | `object`  |

## Integration with Spring AI MCP

For full MCP server functionality, include the optional Spring AI MCP dependency:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp</artifactId>
</dependency>
```

This enables:
- stdio/SSE/HTTP transport protocols
- Full MCP protocol compliance
- Integration with Claude Desktop and other MCP clients

## Thread Safety

The tool registry and invocation handler are thread-safe. Tools can be invoked concurrently from multiple threads.
