# syntax=docker/dockerfile:1.7

# =============================================================================
# ClaudeCode4J Runtime Dockerfile
#
# Sample Dockerfile for applications that use ClaudeCode4J library.
# Provides Java 25 runtime + Node.js + Claude Code CLI pre-installed.
#
# Usage:
#   docker build -f docker/Dockerfile -t myapp --target runtime .
#   docker build -f docker/Dockerfile -t myapp-dev --target development .
#
# Or with docker-compose:
#   cd docker && docker-compose up app
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build stage (for building Java applications using this library)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk-noble AS builder

WORKDIR /build

# Copy Maven wrapper and pom files first for better layer caching
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY claudecode4j-bom/pom.xml claudecode4j-bom/
COPY claudecode4j-api/pom.xml claudecode4j-api/
COPY claudecode4j-core/pom.xml claudecode4j-core/
COPY claudecode4j-spring-boot-starter/pom.xml claudecode4j-spring-boot-starter/
COPY claudecode4j-rest-adapter/pom.xml claudecode4j-rest-adapter/
COPY claudecode4j-kafka-adapter/pom.xml claudecode4j-kafka-adapter/

# Download dependencies (cached unless pom.xml changes)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -B

# Copy source code
COPY . .

# Build the project
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean install -DskipTests -B

# -----------------------------------------------------------------------------
# Stage 2: Runtime stage
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-noble AS runtime

LABEL maintainer="Mahdi Amirabdollahi"
LABEL org.opencontainers.image.title="ClaudeCode4J Runtime"
LABEL org.opencontainers.image.description="Java 25 runtime with Claude Code CLI for ClaudeCode4J applications"
LABEL org.opencontainers.image.source="https://github.com/sudoit/claudecode4j"
LABEL org.opencontainers.image.licenses="MIT"

# Environment variables
ENV NODE_VERSION=22 \
    CLAUDE_HOME=/home/claude \
    JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational --enable-preview" \
    PATH="/home/claude/.npm-global/bin:$PATH"

# Install system dependencies
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    --mount=type=cache,target=/var/lib/apt,sharing=locked \
    apt-get update && apt-get install -y --no-install-recommends \
    # Required for Node.js installation
    curl \
    ca-certificates \
    gnupg \
    # Git for Claude Code operations
    git \
    # Process utilities
    procps \
    # For healthchecks
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Install Node.js LTS
RUN curl -fsSL https://deb.nodesource.com/setup_${NODE_VERSION}.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && rm -rf /var/lib/apt/lists/* \
    && node --version \
    && npm --version

# Create non-root user with home directory
RUN groupadd --gid 1000 claude \
    && useradd --uid 1000 --gid claude --shell /bin/bash --create-home claude \
    && mkdir -p /app /home/claude/.npm-global \
    && chown -R claude:claude /app /home/claude

# Switch to non-root user
USER claude
WORKDIR /home/claude

# Configure npm to use user directory for global packages
RUN npm config set prefix '/home/claude/.npm-global'

# Install Claude Code CLI globally
RUN npm install -g @anthropic-ai/claude-code \
    && claude --version

# Set working directory for application
WORKDIR /app

# Copy application JAR (when used with an actual application)
# COPY --from=builder --chown=claude:claude /build/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD claude --version || exit 1

# Default command (override in docker-compose or k8s)
ENTRYPOINT ["java"]
CMD ["--enable-preview", "-jar", "app.jar"]

# -----------------------------------------------------------------------------
# Stage 3: Development stage (includes build tools)
# -----------------------------------------------------------------------------
FROM runtime AS development

USER root

# Install development tools
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    --mount=type=cache,target=/var/lib/apt,sharing=locked \
    apt-get update && apt-get install -y --no-install-recommends \
    # Build tools
    maven \
    # Editors
    vim \
    # Debug tools
    strace \
    htop \
    && rm -rf /var/lib/apt/lists/*

# Install JDK instead of JRE for development
COPY --from=eclipse-temurin:25-jdk-noble /opt/java/openjdk /opt/java/openjdk

USER claude
WORKDIR /app

# Override entrypoint for development
ENTRYPOINT ["/bin/bash"]
CMD ["-c", "echo 'Development container ready. Run: mvn clean install'"]
