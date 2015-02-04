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
  * [slf4j](https://github.com/OpenHFT/Chronicle-Logger#slf4j)
  * [logback](https://github.com/OpenHFT/Chronicle-Logger#logback)
  * [Apache log4j 1.2](https://github.com/OpenHFT/Chronicle-Logger#log4j-1)
  * [Apache log4j 2](https://github.com/OpenHFT/Chronicle-Logger#log4j-2)
  * [Java Util Logging](https://github.com/OpenHFT/Chronicle-Logger#jul)
  * [Apache Common Logging](https://github.com/OpenHFT/Chronicle-Logger#jcl)

Chronicle logger is able to aggregate all your logs to a central store. It has built in resilience, so you will never loose messages.

We also have some very helpfull [tools] (https://github.com/OpenHFT/Chronicle-Logger/wiki/logger-tools)

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
## chronicle-logger-log4j-2
## chronicle-logger-jul
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
          def processor = { event ->
              if(event.message =~ '.*n.*') {
                  printf("%s => %s\n",ts,msg)
              }
          }

          try {
              if(args.length == 1) {
                  ChroniTool.process(
                      ChronicleQueueBuilder.vanilla(args[0]),
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
          def processor = { msg ->
              if(msg =~ '.*n.*') {
                  printf("%s => %s\n",ts,msg)
              }
          }

          try {
              if(args.length == 1) {
                  ChroniTool.process(
                      ChronicleQueueBuilder.vanilla(args[0]),
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
  
