Chronicle-Logger
================

An extremely fast java logger. We feel logging should not slow down your system.

![Chronicle](http://openhft.net/wp-content/uploads/2014/07/ChronicleLogger_200px_ver2.png)

#### Contents
* [Overview](https://github.com/OpenHFT/Chronicle-Logger#overview)
* [How it works](https://github.com/OpenHFT/Chronicle-Logger#How it works)
* [Bindings](https://github.com/OpenHFT/Chronicle-Logger#bindings)
*   [slf4j](https://github.com/OpenHFT/Chronicle-Logger#slf4j)
*   [logback](https://github.com/OpenHFT/Chronicle-Logger#logback)
*   [Apache log4j 1.2](https://github.com/OpenHFT/Chronicle-Logger#log4j-1)
*   [Apache log4j 2](https://github.com/OpenHFT/Chronicle-Logger#log4j-2)
*   [Java Util Logging](https://github.com/OpenHFT/Chronicle-Logger#jul)
*   [Apache Common Logging](https://github.com/OpenHFT/Chronicle-Logger#jcl)


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

### Bindings
## slf4j
## logback
## log4j-1
## log4j-2
## jul
## jcl

