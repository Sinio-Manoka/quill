# Contributing to Quill

Thank you for your interest in contributing to Quill! This document provides guidelines for contributing to the project.

## Getting Started

### Prerequisites

- Java 21 or later
- Git
- Gradle 8.0+ (the project uses the Gradle Wrapper)

### Building the Project

```bash
git clone <repository-url>
cd quill
./gradlew build
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "LoggerTest"

# Run with coverage
./gradlew test jacocoTestReport
```

## Development Workflow

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass (`./gradlew test`)
6. Commit your changes with a descriptive message
7. Push to your fork (`git push origin feature/my-feature`)
8. Open a pull request

## Code Style

- Use 4 spaces for indentation
- Follow Java naming conventions
- Add Javadoc comments for public APIs
- Keep methods focused and small
- Write unit tests for new functionality

## Making Changes

### Adding a New Feature

1. Create an issue to discuss the feature first
2. Implement the feature
3. Add comprehensive tests
4. Update documentation (README.md)
5. Submit a pull request

### Fixing a Bug

1. Create an issue describing the bug
2. Write a failing test that reproduces the bug
3. Fix the bug
4. Ensure all tests pass
5. Submit a pull request

## Testing

We use JUnit 5 for testing. Tests are located in `src/test/java/com/quill/`.

### Test Structure

```
src/test/java/com/quill/
├── api/           # Tests for Logger API
├── appender/      # Tests for appenders
├── config/        # Tests for configuration
├── context/       # Tests for LogContext
└── model/         # Tests for models
```

### Writing Tests

- Write descriptive test names
- Use `@BeforeEach` for setup
- Use `@AfterEach` for cleanup (especially to reset Logging configuration)
- Test both success and failure cases
- Mock external dependencies

Example test structure:

```java
class MyFeatureTest {

    @BeforeEach
    void setUp() {
        Logging.reset();
        LoggerFactory.reset();
        // Other setup
    }

    @AfterEach
    void tearDown() {
        Logging.reset();
        LoggerFactory.reset();
    }

    @Test
    void testSomething() {
        // Given
        // When
        // Then
        assertEquals(expected, actual);
    }
}
```

## Documentation

- Public APIs must have Javadoc comments
- Update README.md for user-facing features
- Update CLAUDE.md for development-related changes

## Pull Request Guidelines

- Provide a clear description of the changes
- Reference related issues
- Keep changes focused and minimal
- Ensure all tests pass
- Update documentation as needed

## Project Structure

```
com.quill/
├── api/           # Public logging API
├── model/         # Data models (LogEvent, Level)
├── appender/      # Output appenders
├── config/        # Configuration
├── context/       # Thread-local context (MDC)
└── util/          # Utilities (JSON writer)
```

## Questions?

Feel free to open an issue for questions or discussion.
