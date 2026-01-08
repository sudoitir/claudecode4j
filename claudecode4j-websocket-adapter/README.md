# ClaudeCode4J WebSocket Adapter

Interactive WebSocket terminal for human-in-the-loop Claude workflows.

## Features

- **Bidirectional Communication**: Real-time streaming responses via WebSocket
- **Session Management**: Multiple concurrent sessions with automatic cleanup
- **Permission Handling**: Interactive permission request/response workflow
- **Stream Events**: Receive Claude responses as they are generated

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.sudoitir</groupId>
    <artifactId>claudecode4j-websocket-adapter</artifactId>
</dependency>
```

## Configuration

```yaml
claude:
  code:
    websocket:
      enabled: true
      endpoint: /ws/cli
      session-timeout: 30m
      max-sessions: 100
```

## WebSocket Protocol

### Client to Server Messages

**Prompt Message**

```json
{
  "type": "prompt",
  "sessionId": "abc123",
  "text": "List all files in the current directory",
  "systemPrompt": "You are a helpful assistant",
  "contextFiles": ["src/main/java/App.java"]
}
```

**Permission Response**

```json
{
  "type": "permission_response",
  "sessionId": "abc123",
  "requestId": "perm1",
  "granted": true
}
```

### Server to Client Messages

**Session Connected**

```json
{
  "sessionId": "abc123",
  "status": "connected",
  "timestamp": "2026-01-08T12:00:00Z"
}
```

**Stream Chunk**

```json
{
  "type": "stream_chunk",
  "sessionId": "abc123",
  "content": "Here are the files...",
  "sequence": 1
}
```

**Response Complete**

```json
{
  "type": "response",
  "sessionId": "abc123",
  "content": "",
  "isComplete": true
}
```

**Permission Request**

```json
{
  "type": "permission_request",
  "sessionId": "abc123",
  "requestId": "perm1",
  "toolName": "Bash",
  "description": "rm -rf /tmp/cache"
}
```

**Error**

```json
{
  "type": "error",
  "sessionId": "abc123",
  "code": "EXECUTION_ERROR",
  "message": "Command failed with exit code 1"
}
```

## Usage Example

### JavaScript Client

```javascript
const socket = new WebSocket('ws://localhost:8080/ws/cli');

socket.onopen = () => {
    // Send a prompt
    socket.send(JSON.stringify({
        type: 'prompt',
        text: 'What is 2+2?'
    }));
};

socket.onmessage = (event) => {
    const message = JSON.parse(event.data);

    switch (message.type) {
        case 'stream_chunk':
            console.log('Chunk:', message.content);
            break;
        case 'response':
            if (message.isComplete) {
                console.log('Response complete');
            }
            break;
        case 'permission_request':
            // Handle permission request
            const granted = confirm(`Allow ${message.toolName}: ${message.description}?`);
            socket.send(JSON.stringify({
                type: 'permission_response',
                sessionId: message.sessionId,
                requestId: message.requestId,
                granted: granted
            }));
            break;
        case 'error':
            console.error('Error:', message.message);
            break;
    }
};
```

## Session Management

Sessions are automatically cleaned up after the configured timeout. You can also manually manage sessions:

```java
@Autowired
private SessionRegistry sessionRegistry;

// Get session count
int count = sessionRegistry.size();

// Cleanup idle sessions
int removed = sessionRegistry.cleanup(Duration.ofMinutes(30));
```

## Security Considerations

- Sessions are isolated by WebSocket connection
- Permission requests require explicit user approval
- Session IDs are generated server-side
- Consider adding authentication before the WebSocket upgrade

## Thread Safety

The WebSocket handler uses virtual threads for efficient concurrent session handling. All session operations are thread-safe.
