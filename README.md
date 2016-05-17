Chronicle-Logger
================

An extremely fast java logger. We feel logging should not slow down your system.

![Chronicle](http://openhft.net/wp-content/uploads/2014/07/ChronicleLogger_200px_ver2.png)

#### Contents
* [Overview](https://github.com/OpenHFT/Chronicle-Logger#overview)
* [How it works](https://github.com/OpenHFT/Chronicle-Logger#How it works)
* [Bindings](https://github.com/OpenHFT/Chronicle-Logger#bindings)
  * [SLF4j](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-slf4j)
  * [Logback](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-logback)
  * [Apache log4j 1.2](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-log4j-1)
  * [Apache log4j 2](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-log4j-2)
  * [Java Util Logging](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-jul)
  * [Apache Common Logging](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-jcl)
* [Tools](https://github.com/OpenHFT/Chronicle-Logger#tools)

### Overview
Today most programs require the logging of large amounts of data, especially in trading systems where this is a regulatory requirement. Loggers can affect your system performance, therefore logging is sometimes kept to a minimum, With chronicle we aim to eliminate this added overhead, freeing your system to focus on the business logic.

Chronicle logger supports most of the standard logging API’s including: 
  * [SLF4j](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-slf4j)
  * [Logback](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-logback)
  * [Apache log4j 1.2](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-log4j-1)
  * [Apache log4j 2](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-log4j-2)
  * [Java Util Logging](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-jul)
  * [Apache Common Logging](https://github.com/OpenHFT/Chronicle-Logger#chronicle-logger-jcl)

Chronicle logger is able to aggregate all your logs to a central store. It has built in resilience, so you will never lose messages.

We also have some very helpfull [tools] (https://github.com/OpenHFT/Chronicle-Logger#tools)

### How it works
Chronicle logger is built on Chronicle Queue. It provides multiple Chronicle Queue adapters and is a low latency, high throughput synchronous writer. Unlike asynchronous writers, you will always see the last message before the application dies.  As the last message is often the most valuable.

###Bindings

#### chronicle-logger-slf4j
The chronicle-logger-slf4j is an implementation of SLF4J API > 1.7.x with [Chronicle-Queue](https://github.com/OpenHFT/Chronicle-Queue) as persistence engine.

To configure this sl4j binding you need to specify the location of a properties files (file-system or classpath) via system properties:
```
-Dchronicle.logger.properties=${pathOfYourPropertiesFile}
```

The following properties are supported:

 **Property** | **Description**                      | **Values**                       | **Per-Logger**
--------------|--------------------------------------|----------------------------------|----------------
type          | the type of the underlying Chronicle | indexed, vanilla                 | no
path          | the base directory of a Chronicle    |                                  | yes
level         | the default log level                | trace, debug, info, warn, error  | yes
append        |                                      | true, false                      | yes (if a specific path is defined)
format        | write log as text or binary          | binary, text                     | yes (if a specific path is defined)
dateFormat    | the date format for text loggers     |                                  | no 

The default configuration is build using properties with chronicle.logger.root as prefix but you can also set per-logger settings i.e. chronicle.logger.L1, an example:

```properties
# shared properties
chronicle.base                        = ${java.io.tmpdir}/chronicle/${today}/${pid}

# logger : default
chronicle.logger.root.type            = vanilla
chronicle.logger.root.path            = ${slf4j.chronicle.base}/main
chronicle.logger.root.level           = debug
chronicle.logger.root.shortName       = false
chronicle.logger.root.append          = false
chronicle.logger.root.format          = binary

# logger : L1 (binary)
chronicle.logger.L1.path              = ${slf4j.chronicle.base}/L1
chronicle.logger.L1.level             = info

# logger : T1 (text)
chronicle.logger.T1.path              = ${slf4j.chronicle.base}/T1
chronicle.logger.T1.level             = debug
chronicle.logger.T1.format            = text
chronicle.logger.T1.dateFormat        = yyyyMMdd-HHmmss-S
```

The configuration of chronicle-slf4j supports variable interpolation where the variables are replaced with the corresponding values from the same configuration file, the system properties and from some predefined values. System properties have the precedence in placeholder replacement so they can be overriden.

Predefined values are:
  * pid which will replaced by the process id
  * today which will be replaced by the current date (yyyyMMdd)


By default the underlying Chronicle is set up using the default configuration but you can tweak it via chronicle.logger.${name}.cfg prefix where the name after the prefix should be the name of the related ChronicleQueueBuilder setter (depending of the value set for chronicle.logger.${name}.type) i.e:

```properties
chronicle.logger.root.cfg.indexFileCapacity = 128
chronicle.logger.root.cfg.dataBlockSize     = 256
chronicle.logger.root.cfg.synchronous       = true
```

The parameters will change those defined by the default configuration.


##Notes
  * Loggers are not hierarchical grouped so my.domain.package.MyClass1 and my.domain are two distinct entities.
  * The _path_ is used to track the underlying Chronicle so two loggers configured with the same _path_ will share the same Chronicle  


## chronicle-logger-logback
The chronicle-logger-logback module provides appenders for Logback targeting [Chronicle-Queue](https://github.com/OpenHFT/Chronicle-Queue) as underlying persistence framework:

  * BinaryIndexedChronicleAppender
  * TextIndexedChronicleAppender
  * BinaryVanillaChronicleAppender
  * TextVanillaChronicleAppender

### Configuration

* BinaryIndexedChronicleAppender

  This appender writes log entries to an IndexedChronicle as binary
  
  ```xml
  <appender name  = "BinaryIndexedAppender"
            class = "net.openhft.chronicle.logger.logback.BinaryIndexedChronicleAppender">
      
      <!-- Path used by the underlying IndexedChronicle -->
      <path>${java.io.tmpdir}/BinaryIndexedAppender</path>

      <!--
      Configure the underlying IndexedChronicle, for a list of the options have
      a look at net.openhft.chronicle.ChronicleQueueBuilder 
      -->
      <chronicleConfig>
          <indexFileCapacity>128</indexFileCapacity>
      </chronicleConfig>
  </appender>
  ```

* TextIndexedChronicleAppender

  This appender writes log entries to an IndexedChronicle as text

  ```xml
  <appender name  = "TextIndexedAppender"
            class = "net.openhft.chronicle.logger.logback.TextIndexedChronicleAppender">
      
      <!-- Path used by the underlying IndexedChronicle -->
      <path>${java.io.tmpdir}/TextIndexedAppender</path>

      <!--
      Configure the underlying IndexedChronicle, for a list of the options have
      a look at net.openhft.chronicle.ChronicleQueueBuilder 
      -->
      <chronicleConfig>
          <indexFileCapacity>128</indexFileCapacity>
      </chronicleConfig>
  </appender>
  ```

* BinaryVanillaChronicleAppender

  This appender writes log entries to a VanillaChronicle as binary

  ```xml
  <appender name  = "BinaryVanillaAppender"
            class = "net.openhft.chronicle.logger.logback.BinaryVanillaChronicleAppender">

      <!-- Path used by the underlying VanillaChronicle -->
      <path>${java.io.tmpdir}/BinaryVanillaAppender</path>

      <!--
      Configure the underlying VanillaChronicle, for a list of the options have
      a look at net.openhft.chronicle.ChronicleQueueBuilder 
      -->
      <chronicleConfig>
          <dataCacheCapacity>128</dataCacheCapacity>
      </chronicleConfig>
  </appender>
  ```

* TextVanillaChronicleAppender

  This appender writes log entries to a VanillaChronicle as text

  ```xml
  <appender name  = "TextVanillaAppender"
            class = "net.openhft.chronicle.logger.logback.TextVanillaChronicleAppender">
      
      <!-- Path used by the underlying VanillaChronicle -->
      <path>${java.io.tmpdir}/TextVanillaAppender</path>

      <!--
      Configure the underlying VanillaChronicle, for a list of the options have
      a look at net.openhft.chronicle.ChronicleQueueBuilder 
      -->
      <chronicleConfig>
          <dataCacheCapacity>128</dataCacheCapacity>
      </chronicleConfig>

  </appender>
  ```
  
## chronicle-logger-log4j-1

```xml
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
<log4j:configuration xmlns:log4j='http://jakarta.apache.org/log4j/'>

    <!-- ******************************************************************* -->
    <!--                                                                     -->
    <!-- ******************************************************************* -->

    <appender name  = "BINARY-VANILLA-CHRONICLE"
              class = "net.openhft.chronicle.logger.log4j1.BinaryVanillaChronicleAppender">
        <param name="path" value="${java.io.tmpdir}/chronicle-log4j1/binary-vanilla-chronicle"/>
        <param name="includeCallerData" value="false"/>
        <param name="includeMappedDiagnosticContext" value="false"/>
    </appender>

    <appender name  = "TEXT-VANILLA-CHRONICLE"
              class = "net.openhft.chronicle.logger.log4j1.TextVanillaChronicleAppender">
        <param name="path" value="${java.io.tmpdir}/chronicle-log4j1/text-vanilla-chronicle"/>
        <param name="dateFormat" value="yyyy.MM.dd-HH:mm:ss.SSS"/>
        <param name="stackTradeDepth" value="3"/>
    </appender>

    <appender name  = "BINARY-INDEXED-CHRONICLE"
              class = "net.openhft.chronicle.logger.log4j1.BinaryIndexedChronicleAppender">
        <param name="path" value="${java.io.tmpdir}/chronicle-log4j1/binary-indexed-chronicle"/>
        <param name="includeCallerData" value="false"/>
        <param name="includeMappedDiagnosticContext" value="false"/>
    </appender>

    <appender name  = "TEXT-INDEXED-CHRONICLE"
              class = "net.openhft.chronicle.logger.log4j1.TextIndexedChronicleAppender">
        <param name="path" value="${java.io.tmpdir}/chronicle-log4j1/text-indexed-chronicle"/>
        <param name="dateFormat" value="yyyy.MM.dd-HH:mm:ss.SSS"/>
        <param name="stackTradeDepth" value="3"/>
    </appender>

    <!-- ******************************************************************* -->
    <!-- STDOUT                                                              -->
    <!-- ******************************************************************* -->

    <appender name  = "STDOUT"
              class = "org.apache.log4j.ConsoleAppender">
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%-4r [%t] %-5p %c %x - %m%n" />
        </layout>
    </appender>

    <!-- ******************************************************************* -->
    <!--                                                                     -->
    <!-- ******************************************************************* -->

    <logger name="binary-vanilla-chronicle" additivity="false">
        <level value="trace"/>
        <appender-ref ref="BINARY-VANILLA-CHRONICLE"/>
    </logger>

    <logger name="text-vanilla-chronicle" additivity="false">
        <level value="trace"/>
        <appender-ref ref="TEXT-VANILLA-CHRONICLE"/>
    </logger>

    <logger name="binary-indexed-chronicle" additivity="false">
        <level value="trace"/>
        <appender-ref ref="BINARY-INDEXED-CHRONICLE"/>
    </logger>

    <logger name="text-indexed-chronicle" additivity="false">
        <level value="trace"/>
        <appender-ref ref="TEXT-INDEXED-CHRONICLE"/>
    </logger>

    <!-- ******************************************************************* -->
    <!--                                                                     -->
    <!-- ******************************************************************* -->

    <logger name="net.openhft" additivity="false">
        <level value="warn"/>
        <appender-ref ref="STDOUT"/>
    </logger>

    <!-- ******************************************************************* -->
    <!--                                                                     -->
    <!-- ******************************************************************* -->

    <root>
        <level value="debug" />
        <appender-ref ref="STDOUT" />
    </root>

</log4j:configuration>
```

## chronicle-logger-log4j-2

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration packages="net.openhft.chronicle.logger,net.openhft.chronicle.logger.log4j2">

    <!-- ******************************************************************* -->
    <!-- APPENDERS                                                           -->
    <!-- ******************************************************************* -->

    <appenders>

        <Console name="STDOUT" target="SYSTEM_OUT">
            <PatternLayout pattern="[CHRONOLOGY] [%-5p] %c - %m%n%throwable{none}"/>
        </Console>

        <!-- *************************************************************** -->
        <!-- VANILLA                                                         -->
        <!-- *************************************************************** -->

        <BinaryVanillaChronicle name="BINARY-VANILLA-CHRONICLE">
            <path>${sys:java.io.tmpdir}/chronicle-log4j2/binary-vanilla-chronicle</path>
            <includeCallerData>false</includeCallerData>
            <includeMappedDiagnosticContext>false</includeMappedDiagnosticContext>
        </BinaryVanillaChronicle>

        <TextVanillaChronicle name="TEXT-VANILLA-CHRONICLE">
            <path>${sys:java.io.tmpdir}/chronicle-log4j2/text-vanilla-chronicle</path>
            <dateFormat>yyyy.MM.dd-HH:mm:ss.SSS</dateFormat>
            <stackTraceDepth>3</stackTraceDepth>
        </TextVanillaChronicle>

        <!-- *************************************************************** -->
        <!-- INDEXED                                                         -->
        <!-- *************************************************************** -->

        <BinaryIndexedChronicle name="BINARY-INDEXED-CHRONICLE">
            <path>${sys:java.io.tmpdir}/chronicle-log4j2/binary-indexed-chronicle</path>
            <includeCallerData>false</includeCallerData>
            <includeMappedDiagnosticContext>false</includeMappedDiagnosticContext>
        </BinaryIndexedChronicle>

        <TextIndexedChronicle name="TEXT-INDEXED-CHRONICLE">
            <path>${sys:java.io.tmpdir}/chronicle-log4j2/text-indexed-chronicle</path>
            <dateFormat>yyyy.MM.dd-HH:mm:ss.SSS</dateFormat>
            <stackTraceDepth>3</stackTraceDepth>
        </TextIndexedChronicle>

    </appenders>

    <!-- ******************************************************************* -->
    <!-- LOGGERS                                                             -->
    <!-- ******************************************************************* -->

    <loggers>

        <root level="all">
            <appender-ref ref="STDOUT"/>
        </root>

        <!-- *************************************************************** -->
        <!-- VANILLA                                                         -->
        <!-- *************************************************************** -->

        <logger name="binary-vanilla-chronicle" level="trace" additivity="false">
            <appender-ref ref="BINARY-VANILLA-CHRONICLE"/>
        </logger>
        <logger name="text-vanilla-chronicle" level="trace" additivity="false">
            <appender-ref ref="TEXT-VANILLA-CHRONICLE"/>
        </logger>

        <!-- *************************************************************** -->
        <!-- INDEXED                                                         -->
        <!-- *************************************************************** -->

        <logger name="binary-indexed-chronicle" level="trace" additivity="false">
            <appender-ref ref="BINARY-INDEXED-CHRONICLE"/>
        </logger>
        <logger name="text-indexed-chronicle" level="trace" additivity="false">
            <appender-ref ref="TEXT-INDEXED-CHRONICLE"/>
        </logger>

        <!-- *************************************************************** -->
        <!--                                                                 -->
        <!-- *************************************************************** -->

        <logger name="net.openhft" level="warn"/>

    </loggers>

</configuration>
```

## chronicle-logger-jul

```properties
handlers=java.util.logging.ConsoleHandler, net.openhft.chronicle.logger.jul.BinaryVanillaChronicleHandler

.level=ALL

java.util.logging.ConsoleHandler.level=ALL
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter

net.openhft.level=WARNING
net.openhft.handlers=java.util.logging.ConsoleHandler

################################################################################
# BINARY VANILLA
################################################################################

net.openhft.chronicle.logger.jul.BinaryVanillaChronicleHandler.path = ${java.io.tmpdir}/chronicle-jul/vanilla
net.openhft.chronicle.logger.jul.BinaryVanillaChronicleHandler.level = ALL

binary-vanilla-cfg.level=INFO
binary-vanilla-cfg.handlers=net.openhft.chronicle.logger.jul.BinaryVanillaChronicleHandler
binary-vanilla-cfg.useParentHandlers=false
```

## chronicle-logger-jcl

### Tools

  * net.openhft.chronicle.logger.tools.ChroniTail
  ```
    ChroniTail [-t|-i] path
        -t = text chronicle, default binary
        -i = IndexedCronicle, default VanillaChronicle

    mvn exec:java -Dexec.mainClass="net.openhft.chronicle.logger.tools.ChroniTail" -Dexec.args="..."
  ```

  * net.openhft.chronicle.logger.tools.ChroniCat
  ```
      ChroniCat [-t|-i] path
        -t = text chronicle, default binary
        -i = IndexedCronicle, default VanillaChronicle

      mvn exec:java -Dexec.mainClass="net.openhft.chronicle.logger.tools.ChroniCat" -Dexec.args="..."
  ```

  * net.openhft.chronicle.logger.tools.ChroniGrep
  ```
      ChroniCat [-t|-i] regexp1 ... regexpN path
        -t = text chronicle, default binary
        -i = IndexedCronicle, default VanillaChronicle

      mvn exec:java -Dexec.mainClass="net.openhft.chronicle.logger.tools.ChroniCat" -Dexec.args="..."
  ```

### Writing a simple LogSearch with Groovy and Grape

  * Binary log search
  ```groovy
  import net.openhft.chronicle.ChronicleQueueBuilder
  import net.openhft.chronicle.logger.ChronicleLogProcessor
  import net.openhft.chronicle.logger.tools.ChroniTool

  @Grapes([
     @Grab(group='net.openhft', module='chronicle'              , version='3.3.5'),
     @Grab(group='net.openhft', module='chronicle-logger-tools' , version='1.1.0-SNAPSHOT' ),
  ])
  class LogSearch {
      static def main(String[] args) {
          try {
              if(args.length == 1) {
                  ChroniTool.process(
                      ChronicleQueueBuilder.vanilla(args[0]).build(),
                      new ChroniTool.BinaryProcessor() {
                          @Override
                          public void process(ChronicleLogEvent event) {
                              if(event.message =~ '.*n.*') {
                                  printf("%s => %s\n",ts,msg)
                              }
                          }
                      }
                      false,
                      false)
              }
          } catch(Exception e) {
              e.printStackTrace(System.err);
          }
      }
  }
  ```

  * Text log search
  ```groovy
  import net.openhft.chronicle.ChronicleQueueBuilder
  import net.openhft.chronicle.logger.ChronicleLogProcessor
  import net.openhft.chronicle.logger.tools.ChroniTool

  @Grapes([
     @Grab(group='net.openhft', module='chronicle'             , version='3.3.5'),
     @Grab(group='net.opemhft', module='chronicle-logger-tools', version='1.1.0-SNAPSHOT' ),
  ])
  class LogSearch {
      static def main(String[] args) {
          try {
              if(args.length == 1) {
                  ChroniTool.process(
                      ChronicleQueueBuilder.vanilla(args[0]).build(),
                      new ChroniTool.TextProcessor() {
                          @Override
                          public void process(ChronicleLogEvent event) {
                              if(msg =~ '.*n.*') {
                                  printf("%s => %s\n",ts,msg)
                              }
                          }
                      }
                      ChroniTool.binaryReader(processor as ChronicleLogProcessor),
                      false,
                      false)
              }
          } catch(Exception e) {
              e.printStackTrace(System.err);
          }
      }
  }
  ```
  
