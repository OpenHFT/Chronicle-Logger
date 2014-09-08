Chronicle-Logger
================

### Example for :
* [slf4j] (https://github.com/OpenHFT/Chronicle-Logger/wiki/logger-slf4j)
* [logger-logback](https://github.com/OpenHFT/Chronicle-Logger/wiki/logger-logback)
* [logger-slf4j] (https://github.com/OpenHFT/Chronicle-Logger/wiki/logger-slf4j)


### What is it ?
Chronicle Logger is an extremely fast java logger. We feel logging should not slow down your system.

Chronicle logger is able to aggregate all your logs to a central store. It has built in resilience, so you will never loose messages.

Chronicle logger supports most of the standard logging API’s including slf4j, sun logging, commons logging, log4j.

Today most programs require the logging of large amounts of data, especially in trading systems where this is a regulatory requirement. Loggers can affect your system performance, therefore logging is sometimes kept to a minimum, With chronicle we aim to eliminate this added overhead, freeing your system to focus on the business logic.

# How it works

Chronicle logger is built on Chronicle Queue. It provides multiple Chronicle Queue adapters and is a low latency, high throughput synchronous writer. Unlike asynchronous writers, you will always see the last message before the application dies.  As the last message is often the most valuable.
