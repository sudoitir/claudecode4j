# ClaudeCode4J Context Module

Token-aware context optimization for ClaudeCode4J using JTokkit.

## Features

- **Token Counting**: Accurate token counting using `cl100k_base` encoding (same as Claude models)
- **Context Optimization**: Automatically prioritize and trim context files to fit within model limits
- **Caching**: Optional token count caching for improved performance
- **Model Awareness**: Built-in token limits for Claude 3, 3.5, and 4 models

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>ir.sudoit</groupId>
    <artifactId>claudecode4j-context</artifactId>
</dependency>
```

## Configuration

```yaml
claude:
  code:
    context:
      enabled: true
      model: claude-3.5-sonnet
      max-context-ratio: 0.8  # Use 80% of available context window
      ranking-strategy: recency  # recency, size, or custom
```

## Usage

### Token Counting

```java
@Autowired
private TokenCounter tokenCounter;

// Count tokens in a string
int tokens = tokenCounter.count("Hello, world!");

// Count tokens in a file
int fileTokens = tokenCounter.count(Path.of("src/main/java/MyClass.java"));
```

### Context Optimization

```java
@Autowired
private ContextOptimizer contextOptimizer;

// Create a budget for the model
ContextBudget budget = ContextBudget.forModel("claude-3.5-sonnet");

// Optimize context files
Prompt prompt = Prompt.builder()
    .text("Analyze these files")
    .contextFiles(List.of(file1, file2, file3))
    .build();

OptimizationResult result = contextOptimizer.optimize(prompt, budget);

// Get included and excluded files
List<Path> included = result.includedFiles();
List<Path> excluded = result.excludedFiles();
int tokensUsed = result.totalTokensUsed();
```

### Custom Ranking Strategy

```java
RankingStrategy customStrategy = files -> {
    // Sort by file extension (prioritize .java files)
    return files.stream()
        .sorted((a, b) -> {
            boolean aIsJava = a.toString().endsWith(".java");
            boolean bIsJava = b.toString().endsWith(".java");
            return Boolean.compare(bIsJava, aIsJava);
        })
        .toList();
};

DefaultContextOptimizer optimizer = DefaultContextOptimizer.builder()
    .rankingStrategy(customStrategy)
    .build();
```

## Model Token Limits

|       Model       | Context Window | Max Output |
|-------------------|----------------|------------|
| Claude 3 Opus     | 200K           | 4K         |
| Claude 3 Sonnet   | 200K           | 4K         |
| Claude 3 Haiku    | 200K           | 4K         |
| Claude 3.5 Sonnet | 200K           | 8K         |
| Claude 4          | 200K           | 16K        |

## SPI Extension Points

- `TokenCounter` - Implement custom token counting
- `ContextOptimizer` - Implement custom optimization strategies
- `RankingStrategy` - Implement custom file ranking

## Thread Safety

All components are thread-safe and suitable for concurrent use in multi-threaded applications.
