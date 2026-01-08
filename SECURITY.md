# Security Policy

## Supported Versions

We actively support the latest stable version of ClaudeCode4J. Security updates are prioritized for the following
versions:

| Version |     Supported      |
|---------|--------------------|
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability in ClaudeCode4J, please follow these steps:

1. **Do NOT create a public GitHub issue.** This allows us to patch the vulnerability before it can be exploited.
2. Email our security team at `dev@sudoit.ir` (or the maintainer's email if specific security email is not yet
   established).
3. Include a detailed description of the vulnerability, steps to reproduce it, and any relevant code snippets.

### Response Timeline

* **Acknowledgment:** We will acknowledge your report within 48 hours.
* **Assessment:** We will assess the severity and impact within 5 business days.
* **Fix:** A patch will be released as soon as the vulnerability is confirmed and a fix is tested.

## Security Features

ClaudeCode4J includes several built-in security features that users should be aware of:

* **Input Sanitization:** The library sanitizes inputs to prevent command injection attacks.
* **Permission Control:** By default, the library respects permissions. The `dangerously-skip-permissions` flag must be
  explicitly enabled to bypass this.
* **Concurrency Limits:** To prevent resource exhaustion (DoS), use the provided `concurrency-limit` configuration in
  Spring Boot or the rate-limiting AOP annotations.

## Best Practices

* Always run the application with the least privilege necessary.
* Do not expose the `dangerously-skip-permissions` flag in production environments unless strictly secured.
* Keep your `claudecode4j` dependency up to date.

