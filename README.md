# Quill

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build](https://github.com/Sinio-Manoka/quill/actions/workflows/build.yml/badge.svg)](https://github.com/Sinio-Manoka/quill/actions/workflows/build.yml)

A modern, structured logging library for Java 21+. Built from scratch with JSON-first output, zero dependencies, and
virtual thread support.

## Why Quill?

Java logging has been stuck in the past. SLF4J and Logback were designed for an era of XML configuration,
thread-per-request architectures, and plain text logs. Quill brings logging into the modern era:

* **JSON-first** — Every log event is structured data from the start
* **Zero XML** — Pure Java configuration, compile-time safe
* **Virtual Thread Ready** — Context propagation that works with Project Loom
* **Zero Dependencies** — Single JAR, no dependency hell
* **Built for the Cloud** — JSON output ready for Datadog, Loki, ELK, CloudWatch

## Table of Contents

- [Quick Start](#quick-start)
- [Installation](#installation)
- [Basic Usage](#basic-usage)
- [Structured Logging](#structured-logging)
- [Log Levels](#log-levels)
- [Configuration](#configuration)
- [Appenders](#appenders)
- [Contextual Logging](#contextual-logging)
- [Advanced Configuration](#advanced-configuration)
- [Examples](#examples)
- [API Reference](#api-reference)
- [Development](#development)

## Quick Start

### Dependency

#### Gradle (Kotlin)

```kotlin
dependencies {
    implementation("com.github.Sinio-Manoka:quill:1.0.0")
}
```

#### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.github.Sinio-Manoka:quill:1.0.0'
}
```

#### Maven

```xml

<dependency>
    <groupId>com.github.Sinio-Manoka</groupId>
    <artifactId>quill</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Minimal Example

```java
import com.quill.api.Logger;
import com.quill.api.LoggerFactory;
import com.quill.config.Logging;
import com.quill.model.Level;

public class MyApp {
    private static final Logger log = LoggerFactory.get(MyApp.class);

    public static void main(String[] args) {
        // Configure logging
        Logging.configure(config -> config
                .level(Level.INFO)
                .appender(Appenders.jsonConsole())
        );

        // Simple log
        log.infoEmit("Application started");

        // Log with fields
        log.info("User logged in", "userId", 42, "ip", "192.168.1.1");

        // Fluent API
        log.error("Database connection failed")
                .field("host", "db.example.com")
                .field("port", 5432)
                .emit();
    }
}
```

**Output (JSON):**

```json
{
  "timestamp": "2024-01-15T09:23:41.000Z",
  "level": "INFO",
  "logger": "MyApp",
  "thread": "main",
  "message": "Application started"
}
{
  "timestamp": "2024-01-15T09:23:42.000Z",
  "level": "INFO",
  "logger": "MyApp",
  "thread": "main",
  "message": "User logged in",
  "userId": 42,
  "ip": "192.168.1.1"
}
{
  "timestamp": "2024-01-15T09:23:43.000Z",
  "level": "ERROR",
  "logger": "MyApp",
  "thread": "main",
  "message": "Database connection failed",
  "host": "db.example.com",
  "port": 5432
}
```

## Installation

Quill requires **Java 21 or later**.

### Adding to Your Project

**Option 1: Build from source**

```bash
git clone <repository-url>
cd quill
./gradlew build
# JAR will be in build/libs/
```

**Option 2: Manual installation**

After building, add the JAR to your project:

**Gradle:**

```kotlin
dependencies {
    implementation(files("libs/quill-1.0.0.jar"))
}
```

**Maven:**

```xml

<dependency>
    <groupId>com.github.Sinio-Manoka</groupId>
    <artifactId>quill</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/quill-1.0.0.jar</systemPath>
</dependency>
```

## Basic Usage

### Creating a Logger

```java
import com.quill.api.Logger;
import com.quill.api.LoggerFactory;

// Get a logger by Class
Logger log = LoggerFactory.get(MyService.class);

        // Get a logger by name
        Logger log = LoggerFactory.get("MyService");
```

### Logging Messages

```java
// Simple message (no fields)
log.infoEmit("Processing request");

// With key-value pairs (auto-emits)
log.

info("User action","userId",42,"action","login");
log.

error("Payment failed","amount",99.99,"currency","USD");

// Fluent API for complex cases
log.

warn("Cache miss")
   .

field("key","user_123")
   .

field("ttl",3600)
   .

emit();
```

### Log Levels

| Level   | Severity | Use Case                       |
|---------|----------|--------------------------------|
| `TRACE` | 0        | Most detailed diagnostic info  |
| `DEBUG` | 1        | Debugging information          |
| `INFO`  | 2        | General informational messages |
| `WARN`  | 3        | Warning conditions             |
| `ERROR` | 4        | Error conditions               |

```java
log.traceEmit("Entering method");  // Very detailed
log.

debugEmit("Cache miss for key");  // Debug info
log.

infoEmit("Request processed");   // Normal operation
log.

warnEmit("High memory usage");   // Warning
log.

errorEmit("Connection lost");    // Error
```

## Structured Logging

### Adding Fields

```java
// Single field
log.info("Payment received","amount",100.00);

// Multiple fields
log.

info("Order created",
             "orderId","ORD-123",
             "amount",50.00,
             "currency","USD");

// Nested objects
log.

info("User profile",
             "userId",42,
             "profile",Map.of(
             "name", "John Doe",
             "email","john@example.com",
             "age",30
));
```

### Fluent API

```java
log.info("Complex event")
   .

field("eventId",UUID.randomUUID())
        .

field("userId",42)
   .

field("tags",List.of("important", "billing"))
        .

field("metadata",Map.of("source", "api"))
        .

emit();
```

## Configuration

### Basic Configuration

```java
import com.quill.config.Logging;
import com.quill.model.Level;
import com.quill.appender.Appenders;

// Configure logging (usually done at application startup)
Logging.configure(config ->config
        .

level(Level.INFO)
    .

appender(Appenders.jsonConsole())
        );
```

### Output Formats

#### JSON Output (Production)

```java
Logging.configure(config ->config
        .

appender(Appenders.jsonConsole())
        );
```

```json
{
  "timestamp": "2024-01-15T09:23:41.000Z",
  "level": "INFO",
  "logger": "PaymentService",
  "thread": "main",
  "message": "Payment received",
  "amount": 100.0
}
```

#### Console Output (Development)

```java
Logging.configure(config ->config
        .

appender(Appenders.console(true))  // true = ANSI colors
        );
```

```
[09:23:41.000] [INFO] [PaymentService] Payment received amount=100.0
```

## Appenders

### Console Appender

```java
// Human-readable, no colors
Appenders.console()

// Human-readable, with ANSI colors
Appenders.

console(true)
```

### JSON Console Appender

```java
// Single-line JSON to stdout (ideal for containers/cloud)
Appenders.jsonConsole()
```

### File Appender

```java
// Write to file (overwrites existing)
Appenders.file("logs/application.log")

// Append to existing file
Appenders.

file("logs/application.log",true)
```

### Rolling File Appender

```java
// Rotate when file exceeds 10MB (default)
Appenders.rollingFile("logs/app.log")

// Custom size limit (5MB)
Appenders.

rollingFile("logs/app.log",5_000_000)
```

Rolled files are named with a timestamp: `app_2024-01-15_14-30-00.log`

### Async Appender

```java
// Wrap any appender to run asynchronously
Appenders.async(Appenders.file("logs/app.log"))

// With custom queue size and behavior
        Appenders.

async(
        Appenders.file("logs/app.log"),
    5000,    // queue size
            true      // drop logs when full (false = block)
            )
            );
```

### Chaining Appenders

```java
// Output to both console and file
Logging.configure(config ->config
        .

appender(Appenders.chain(
        Appenders.jsonConsole(),    // JSON to stdout
        Appenders.

file("logs/app.log")  // JSON to file
    ))
            );
```

## Contextual Logging

Quill supports scoped context that automatically propagates to all log statements within a code block. This is
especially useful for request-scoped data like request IDs, user IDs, or trace IDs.

### Basic Context

```java
import com.quill.context.LogContext;

LogContext.bind("requestId","req-abc-123")
   .

and("userId",42)
   .

run(() ->{
        // All logs here automatically include requestId and userId
        log.

infoEmit("Processing request");
       log.

info("Querying database","table","users");
   });
```

**Output:**

```json
{
  "timestamp": "2024-01-15T09:23:41.000Z",
  "level": "INFO",
  "logger": "MyService",
  "thread": "main",
  "message": "Processing request",
  "_requestId": "req-abc-123",
  "_userId": "42"
}
```

Note: Context keys are prefixed with `_` in JSON output to avoid collision with structured fields.

### Nested Contexts

Inner contexts inherit and can override outer context:

```java
LogContext.bind("requestId","outer-123")
   .

run(() ->{
        log.

infoEmit("Outer scope");  // Has _requestId

       LogContext.

bind("userId",999)
           .

run(() ->{
        // Has both _requestId (from outer) and _userId (from inner)
        log.

info("Inner scope").

emit();
           });
                   });
```

### Context from a Map

```java
Map<String, Object> ctx = Map.of(
        "traceId", "trace-xyz",
        "spanId", "span-abc"
);

LogContext.

bind(ctx)
   .

run(() ->{
        log.

infoEmit("Request with trace information");
   });
```

### Combining Context with Other Features

```java
// Context + Sampling + Async File
Logging.configure(config ->config
        .

level(Level.DEBUG)
        .

sampling(0.1)  // Sample 90% of DEBUG/TRACE
        .

appender(Appenders.async(
        Appenders.rollingFile("logs/app.log", 10_000_000)
        ))
                );

                LogContext.

bind("correlationId","abc-123")
   .

and("userId",9999)
   .

run(() ->{
        log.

debug("Processing payment");  // May be sampled
       log.

info("Payment successful");    // Never sampled
   });
```

## Advanced Configuration

### Sampling

Reduce log volume by sampling DEBUG/TRACE logs. INFO and above are never sampled.

```java
// Log only 10% of DEBUG/TRACE logs
Logging.configure(config ->config
        .

sampling(0.1)
);
```

**When to use sampling:**

- Production environments with high DEBUG/TRACE volume
- Reducing log volume during traffic spikes
- Performance-sensitive paths where logging overhead matters

### Package-Level Filtering

Configure different log levels for specific packages:

```java
Logging.configure(config ->config
        .

level(Level.ERROR)  // Global ERROR
        .

packageLevel("com.example.api",Level.WARN)  // API logs at WARN
        .

packageLevel("com.example.db",Level.DEBUG)   // DB logs at DEBUG
);
```

Subpackages inherit their parent's level:

```java
.packageLevel("com.example",Level.INFO)

// com.example, com.example.api, and com.example.db all use INFO
// Unless a more specific level is set
```

### Complete Example

```java
import com.quill.api.Logger;
import com.quill.api.LoggerFactory;
import com.quill.appender.Appenders;
import com.quill.config.Logging;
import com.quill.context.LogContext;
import com.quill.model.Level;

public class PaymentService {
    private static final Logger log = LoggerFactory.get(PaymentService.class);

    static {
        Logging.configure(config -> config
                .level(Level.INFO)
                .packageLevel("com.example.db", Level.DEBUG)
                .sampling(0.1)
                .appender(Appenders.chain(
                        Appenders.jsonConsole(),
                        Appenders.async(Appenders.rollingFile("logs/payment.log"))
                ))
        );
    }

    public void processPayment(PaymentRequest request) {
        LogContext.bind("requestId", request.requestId())
                .and("userId", request.userId())
                .run(() -> {
                    log.info("Processing payment", "amount", request.amount());

                    try {
                        chargePayment(request);
                        log.info("Payment successful", "transactionId", request.transactionId());
                    } catch (PaymentException e) {
                        log.error("Payment failed", "errorCode", e.getCode());
                    }
                });
    }
}
```

## API Reference

### Logger

| Method                             | Description                           |
|------------------------------------|---------------------------------------|
| `traceEmit(String)`                | Log TRACE level message               |
| `debugEmit(String)`                | Log DEBUG level message               |
| `infoEmit(String)`                 | Log INFO level message                |
| `warnEmit(String)`                 | Log WARN level message                |
| `errorEmit(String)`                | Log ERROR level message               |
| `trace(String, String, Object...)` | Log TRACE with fields (auto-emits)    |
| `debug(String, String, Object...)` | Log DEBUG with fields (auto-emits)    |
| `info(String, String, Object...)`  | Log INFO with fields (auto-emits)     |
| `warn(String, String, Object...)`  | Log WARN with fields (auto-emits)     |
| `error(String, String, Object...)` | Log ERROR with fields (auto-emits)    |
| `log(Level, String)`               | Create a LogBuilder for custom fields |
| `name()`                           | Get the logger name                   |

### LogBuilder

| Method                  | Description                  |
|-------------------------|------------------------------|
| `field(String, Object)` | Add a field to the log entry |
| `emit()`                | Emit the log event           |

### Logging Configuration

| Method                        | Description                  | Default               |
|-------------------------------|------------------------------|-----------------------|
| `level(Level)`                | Set minimum log level        | `INFO`                |
| `appender(Appender)`          | Add an appender              | `JsonConsoleAppender` |
| `async(boolean)`              | Enable async mode (reserved) | `false`               |
| `sampling(double)`            | Set sampling rate (0.0-1.0)  | `1.0`                 |
| `packageLevel(String, Level)` | Set level for a package      | -                     |
| `packageLevels(Map)`          | Set multiple package levels  | -                     |

### Appenders Factory

| Method                      | Description                               |
|-----------------------------|-------------------------------------------|
| `console()`                 | Human-readable output, no colors          |
| `console(boolean)`          | Human-readable, with optional ANSI colors |
| `jsonConsole()`             | Single-line JSON to stdout                |
| `file(String)`              | Write JSON logs to file                   |
| `file(String, boolean)`     | Write JSON logs to file (append mode)     |
| `rollingFile(String)`       | Rolling file appender (10MB default)      |
| `rollingFile(String, long)` | Rolling file with custom size             |
| `async(Appender)`           | Wrap appender to run asynchronously       |
| `chain(Appender...)`        | Combine multiple appenders                |

## Examples

### Web Service

```java
import com.quill.api.Logger;
import com.quill.api.LoggerFactory;
import com.quill.config.Logging;
import com.quill.model.Level;
import com.quill.context.LogContext;

public class PaymentService {
    private static final Logger log = LoggerFactory.get(PaymentService.class);

    static {
        Logging.configure(config -> config
                .level(Level.INFO)
                .appender(Appenders.jsonConsole())
        );
    }

    public PaymentResult processPayment(PaymentRequest request) {
        return LogContext.bind("requestId", generateRequestId())
                .run(() -> {
                    log.info("Payment started", "amount", request.amount());

                    try {
                        Transaction txn = charge(request);
                        log.info("Payment completed", "transactionId", txn.getId());
                        return new PaymentResult(txn.getId());
                    } catch (PaymentException e) {
                        log.error("Payment failed", "errorCode", e.getCode(),
                                "errorMessage", e.getMessage());
                        throw e;
                    }
                });
    }
}
```

### Microservice with Context Propagation

```java
import com.quill.context.LogContext;

public class RequestFilter {
    private final Logger log = LoggerFactory.get(RequestFilter.class);

    public void filter(Request request, Response response) {
        String requestId = UUID.randomUUID().toString();

        LogContext.bind("requestId", requestId)
                .and("userId", request.getUserId())
                .and("path", request.getPath())
                .run(() -> {
                    // All logs in this scope (including downstream services)
                    // automatically include requestId, userId, and path
                    log.infoEmit("Incoming request");

                    try {
                        response.send(processRequest(request));
                        log.infoEmit("Request completed", "status", response.getStatus());
                    } catch (Exception e) {
                        log.error("Request failed", "error", e.getMessage());
                    }
                });
    }
}
```

### Async Logging

```java
import com.quill.appender.Appenders;

Logging.configure(config ->config
        .

level(Level.DEBUG)
// Non-blocking async logging
        .

appender(Appenders.async(
        Appenders.rollingFile("logs/app.log", 50_000_000)
        ))
                );
```

### Package-Level Configuration

```java
import com.quill.config.Logging;
import com.quill.model.Level;

// Third-party libraries at WARN
// Our API at INFO
// Our database layer at DEBUG
Logging.configure(config ->config
        .

level(Level.ERROR)
        .

packageLevel("com.thirdparty",Level.WARN)
        .

packageLevel("com.example.api",Level.INFO)
        .

packageLevel("com.example.db",Level.DEBUG)
        .

appender(Appenders.jsonConsole())
        );
```

### Development vs Production Configuration

```java
if(isProduction()){
        // Production: JSON to stdout, minimal noise
        Logging.

configure(config ->config
        .

level(Level.WARN)
            .

appender(Appenders.jsonConsole())
        );
        }else{
        // Development: Human-readable, full detail
        Logging.

configure(config ->config
        .

level(Level.DEBUG)
            .

appender(Appenders.console(true))
        );
        }
```

## Development

### Building from Source

```bash
git clone <repository-url>
cd quill
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Code Coverage

```bash
./gradlew test jacocoTestReport
```

Coverage reports are generated in `build/reports/jacoco/test/html/index.html`.

## Requirements

* Java 21 or later
* No external dependencies

## License

```
MIT License

Copyright (c) 2024 Quill Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Changelog

### 1.0.0 (2026-02-19)

#### Features

- Structured logging with JSON-first output
- Fluent Logger API with convenience methods
- Console appenders (plain and ANSI-colored)
- JSON console appender
- File appender with append mode
- Rolling file appender with size-based rotation
- Async appender wrapper
- Appender chaining/composition
- Scoped context with automatic propagation
- Virtual thread support via InheritableThreadLocal
- Package-level log filtering
- Sampling for TRACE/DEBUG logs
- Zero external dependencies

#### Requirements

- Java 21 or later

---

Built with ❤️ for the modern Java ecosystem
