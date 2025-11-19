# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

Chronicle Logger is a high-performance logging library built on Chronicle Queue. It provides adapters for major Java logging frameworks (SLF4J, Logback, Log4J 1/2, JUL, JCL) that write to binary Chronicle Queues rather than text files. This approach minimises logging overhead and enables sub-microsecond logging latency.

**Key architectural principle:** Chronicle Logger writes synchronously to off-heap queues, ensuring the last log message is always visible before application termination whilst maintaining low latency through zero-allocation design.

## Module Structure

This is a multi-module Maven project with the following modules:

* `logger-core` - Core API (`ChronicleLogWriter`, `ChronicleLogManager`) and configuration
* `logger-slf4j` - SLF4J 1.x binding
* `logger-slf4j-2` - SLF4J 2.x binding
* `logger-logback` - Logback appender
* `logger-log4j-1` - Log4J 1.2 appender
* `logger-log4j-2` - Log4J 2.x appender
* `logger-jul` - Java Util Logging handler
* `logger-jcl` - Apache Commons Logging binding
* `logger-tools` - CLI tools (`ChroniCat`, `ChroniTail`) and `ChronicleLogReader` API
* `benchmark` - JMH performance benchmarks

All binding modules depend on `logger-core` and translate framework-specific APIs into Chronicle Queue writes.

## Build Commands

### Full Build

```bash
# Clean build with tests
mvn clean verify

# Build without tests (faster)
mvn clean install -DskipTests

# Quiet build with tests
mvn -q clean verify
```

### Module-Specific Builds

```bash
# Build single module with dependencies
mvn -pl logger-slf4j -am clean install

# Build single module without tests
mvn -pl logger-core -am -DskipTests install
```

### Running Tests

```bash
# Run all tests in a module
mvn -pl logger-slf4j test

# Run single test class
mvn -pl logger-core -Dtest=DefaultChronicleLogWriterTest test

# Run specific test method
mvn -pl logger-core -Dtest=DefaultChronicleLogWriterTest#testWrite test
```

### Running Benchmarks

```bash
cd benchmark
mvn clean install
# Run JMH benchmarks
java -jar target/benchmarks.jar

# Run specific benchmark
java -jar target/benchmarks.jar <BenchmarkClassName>

# Run with custom parameters
java -jar target/benchmarks.jar -f 1 -wi 3 -i 5
```

### Code Quality

```bash
# Run Checkstyle (inherited from java-parent-pom)
mvn checkstyle:check

# Run SpotBugs
mvn spotbugs:check

# Run all quality checks with tests
mvn clean verify
```

## Architecture Overview

### Write Path

1. Logging framework (e.g., SLF4J) receives a log call
2. Binding checks log level against configured minimum
3. If enabled, binding forwards event to `ChronicleLogWriter`
4. `ChronicleLogWriter` serialises data into Chronicle Queue using configured wire type
5. Write is synchronous but zero-allocation on hot path for minimal overhead

### Configuration

* Properties-based bindings (SLF4J, JCL): Use `chronicle.logger.properties` (classpath or path specified via `-Dchronicle.logger.properties`)
* Native framework bindings (Logback, Log4J): Use XML configuration with `ChronicleAppender`/`ChronicleHandler`
* `ChronicleLogManager` caches writers per queue path
* Each logger requires distinct queue path; sharing paths between loggers is unsupported

### Reading Logs

Binary logs require tools to read:

* `ChroniCat` - Dump log contents to STDOUT
* `ChroniTail` - Stream log contents like Unix `tail -f`
* `ChronicleLogReader` - Programmatic API for custom log processing

Example:
```bash
mvn exec:java -Dexec.mainClass="net.openhft.chronicle.logger.tools.ChroniCat" \
  -Dexec.args="/tmp/chronicle-logs/my-logger"
```

## Dependencies

Chronicle Logger depends on:

* `chronicle-core` - Low-level utilities
* `chronicle-bytes` - Off-heap memory access
* `chronicle-queue` - Persisted message queue

Versions are managed through `chronicle-bom` imported in the parent POM.

## Documentation Structure

Documentation follows Chronicle standards:

* Location: `src/main/docs/` (AsciiDoc format)
* Key files:
  - `architecture-overview.adoc` - High-level architecture
  - `project-requirements.adoc` - Requirements catalogue with Nine-Box tags (e.g., `CLG-FN-001`)
  - `functional-requirements.adoc` - Functional requirements summary
  - `decision-log.adoc` - Architecture Decision Records
  - `testing-strategy.adoc` - Testing approach and traceability
  - `code-review-playbook.adoc` - Code review guidelines
* Format: British English, ISO-8859-1 character set
* Requirements use Nine-Box taxonomy: `FN` (Functional), `NF-P` (Performance), `NF-S` (Security), `NF-O` (Operability), etc.

See `AGENTS.md` for detailed documentation standards and contribution guidelines.

## Code Conventions

* Follow Chronicle coding standards in `AGENTS.md`
* British English spelling (except technical US terms like `synchronized`)
* ISO-8859-1 character set only
* Javadoc should explain _why_ and _how_, not restate obvious signatures
* Preserve Nine-Box requirement tags in comments (e.g., `// CLG-NF-P-001: Zero-allocation write path`)

## Common Development Tasks

### Adding Support for New Logging Framework

1. Create new module `logger-<framework>` following existing patterns
2. Implement framework-specific adapter/appender/handler
3. Delegate to `ChronicleLogWriter` from `logger-core`
4. Add configuration support (properties or framework-native)
5. Write tests extending appropriate base test class
6. Add module to parent `pom.xml` `<modules>` section

### Modifying Log Format

1. Changes to `ChronicleLogWriter` interface affect all bindings
2. Update serialisation in `DefaultChronicleLogWriter`
3. Update deserialisation in `ChronicleLogReader` (logger-tools)
4. Update CLI tools if record schema changes
5. Update relevant tests across modules

### Performance Testing

Benchmark changes that affect write path:

```bash
cd benchmark
mvn clean install
java -jar target/benchmarks.jar -f 1
```

Compare results with baseline in README.adoc.

## Important Notes

1. **Log4Shell Immunity**: `logger-log4j-2` does not suffer from log4shell vulnerability as it does not reuse Log4J2 message formatting machinery

2. **Queue Path Uniqueness**: Each logger must have a distinct Chronicle Queue path. Two loggers sharing the same path is unsupported and will cause errors

3. **Non-Hierarchical Loggers**: Logger names like `my.domain.package` and `my.domain` are distinct; no hierarchical grouping is applied

4. **Synchronous Writes**: All writes are synchronous to ensure last messages are visible before application termination. This differs from async appenders but maintains sub-microsecond latency through zero-allocation design

5. **Binary Format**: Logs are binary (not text). Use provided tools (`ChroniCat`, `ChroniTail`) or `ChronicleLogReader` API to read them

## Related Documentation

* `AGENTS.md` - Detailed contribution guidelines, build commands, documentation standards
* `README.adoc` - Project overview, configuration examples, performance metrics
* `src/main/docs/architecture-overview.adoc` - Detailed architecture documentation
* `src/main/docs/project-requirements.adoc` - Complete requirements catalogue
