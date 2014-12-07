chronicle-logger-slf4j
======================

To configure this sl4j binding you need to specify the location of a properties files via system properties:
```
-Dchronicle.logger.properties=${pathOfYourPropertiesFile}
```

The following properties are supported to configure the behavior of the logger:

 **Property** | **Description**                          | **Values**                       | **Per-Logger**
--------------|------------------------------------------|----------------------------------|----------------
type          | the type of the underlying Chronicle     | indexed, vanilla                 | no
path          | the base directory of a Chronicle        |                                  | yes
level         | the default log level                    | trace, debug, info, warn, error  | yes
append        |                                          | true, false                      | yes
format        | write log as text or binary              | binary, text                     | yes
binaryFormat  | format or serialize log arguments        | formatted, serialized            | no
dateFormat    | the date format for text loggers         |                                  | no 
synchronous   | synchronous mode                         | true, false                      | yes

The default configuration needs chronicle.logger. as prefix but you can set per-logger settings using chronicle.logger.logger as prefix, here an example:

```properties
# default
chronicle.logger.base         = ${java.io.tmpdir}/chronicle/${today}/${pid}

# logger : root
chronicle.logger.type         = vanilla
chronicle.logger.path         = ${chronicle.logger.base}/main
chronicle.logger.level        = debug
chronicle.logger.shortName    = false
chronicle.logger.append       = false
chronicle.logger.format       = binary
chronicle.logger.binaryFormat = formatted

# logger : Logger1
chronicle.logger.logger.Logger1.path           = ${chronicle.logger.base}/logger_1
chronicle.logger.logger.Logger1.level          = info

# logger : TextLogger
chronicle.logger.logger.TextLogger.path        = ${chronicle.logger.base}/text
chronicle.logger.logger.TextLogger.level       = debug
chronicle.logger.logger.TextLogger.format      = text
chronicle.logger.logger.TextLogger.dateFormat  = yyyyMMdd-HHmmss-S
```


The configuration of chronicle-slf4j supports variable interpolation where the variables are replaced with the corresponding values from the same configuration file, the system properties and from some predefined values. System properties have the precedence in placeholder replacement so one can override a value via system properties.

Predefined values are:
  * pid which will replaced by the process id
  * today wich will be replaced by the current date (yyyyMMdd)

###Notes
  * Loggers are not hierarchical grouped so my.domain.package.MyClass1 and my.domain are two distinct entities.
  * The _path_ is used to track the underlying VanillaChronicle so two loggers configured with the same _path_ will share the same Chronicle  
