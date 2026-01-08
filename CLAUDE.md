# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Full build with tests (capture logs for review)
mkdir -p logs
mvn verify -l logs/mvn-verify.log
rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/mvn-verify.log

# Build a single module (with dependencies)
mvn -pl logger-core -am verify -l logs/mvn-verify.log

# Run a single test class
mvn test -pl logger-core -Dtest=ChronicleLogConfigTest

# Skip tests
mvn install -DskipTests

# Code review profile (runs CheckStyle, SpotBugs, PMD, JaCoCo)
mvn -Pcode-review verify
```

Do not commit the `logs/` directory.

## Project Architecture

Chronicle-Logger is a high-performance logging framework built on Chronicle Queue that provides adapters for standard Java logging APIs.

### Module Structure

```
chronicle-logger (parent pom)
├── logger-core          # Core logging abstraction and Chronicle Queue writer
├── logger-slf4j         # SLF4J 1.x binding
├── logger-slf4j-2       # SLF4J 2.x binding
├── logger-log4j-2       # Log4j 2 appender
├── logger-jul           # java.util.logging handler
├── logger-jcl           # Apache Commons Logging binding
├── logger-tools         # CLI tools (ChroniCat, ChroniTail) for reading logs
└── benchmark            # JMH performance benchmarks
```

### Core Components

- **ChronicleLogWriter** (`logger-core`): Interface for writing structured log events to Chronicle Queue
- **DefaultChronicleLogWriter**: Serialises log entries using Wire format with reentrancy protection
- **ChronicleLogConfig**: Loads configuration from properties files with `${placeholder}` interpolation (including `${pid}`)
- **ChronicleLogManager**: Singleton that manages writer instances and configuration

### Logging Adapter Pattern

Each adapter module (slf4j, log4j-2, jul, jcl) implements its respective logging API and delegates to `ChronicleLogWriter`. Example flow for SLF4J:
1. `StaticLoggerBinder` provides `ChronicleLoggerFactory`
2. `ChronicleLoggerFactory.getLogger()` returns `ChronicleLogger` instances
3. `ChronicleLogger` delegates to `ChronicleLogWriter.write()`

### Log Entry Format

Log entries are written to Chronicle Queue with these fields:
- `ts` (long): timestamp in epoch millis
- `level` (enum): ChronicleLogLevel
- `threadName`, `loggerName`, `message` (text)
- `throwable` (optional): serialised exception
- `args` (optional): sequence of argument objects

### Reading Logs

Use CLI tools from logger-tools module:
```bash
# Dump all logs
mvn exec:java -pl logger-tools -Dexec.mainClass="net.openhft.chronicle.logger.tools.ChroniCat" -Dexec.args="/path/to/logs"

# Tail logs (waits for new data)
mvn exec:java -pl logger-tools -Dexec.mainClass="net.openhft.chronicle.logger.tools.ChroniTail" -Dexec.args="/path/to/logs"
```

## Constraints

- **Java 8 baseline**: Do not use newer language features (no `var`, no records, no text blocks)
- **No extra allocations or synchronisation in hot paths**
- **Preserve public APIs** unless explicitly requested
- **Treat warnings as defects**: Keep build logs clean

## Code Style

- **British English** spelling in prose (`organisation`, `licence`)
- **ISO-8859-1** for source files (code points 0-255); prefer ASCII, avoid smart quotes and non-breaking spaces
- **UTF-8** for application I/O
- Javadoc must add behaviour, edge cases, thread safety, units, or performance notes beyond what the method signature provides

## Configuration

Configuration is loaded from `chronicle-logger.properties` (or via `-Dchronicle.logger.properties`):
```properties
chronicle.logger.base            = ${java.io.tmpdir}/chronicle-logs/${pid}
chronicle.logger.root.path       = ${chronicle.logger.base}/main
chronicle.logger.root.level      = debug
chronicle.logger.root.cfg.bufferCapacity = 128
chronicle.logger.root.cfg.blockSize      = 256
```

## Documentation

- Key docs: `README.adoc`, `src/main/docs/` (requirements, decision log, code review playbook)
- Keep AsciiDoc, tests, and code in sync; update docs when behaviour changes
- For large mechanical changes, declare the transformation rule and keep it consistent
