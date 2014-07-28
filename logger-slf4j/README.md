chronology-slf4j
================

To configure this sl4j binding you need to specify the location of a properties files via system properties:
```
-Dslf4j.chronology.properties=${pathOfYourPropertiesFile}
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

The default configuration needs slf4j.chronicle as prefix but you can set per-logger settings using slf4j.chronology.logger as prefix, here an example:

```properties
# default
slf4j.chronology.base         = ${java.io.tmpdir}/chronicle/${today}/${pid}

# logger : root
slf4j.chronology.type         = vanilla
slf4j.chronology.path         = ${slf4j.chronology.base}/main
slf4j.chronology.level        = debug
slf4j.chronology.shortName    = false
slf4j.chronology.append       = false
slf4j.chronology.format       = binary
slf4j.chronology.binaryFormat = formatted

# logger : Logger1
slf4j.chronology.logger.Logger1.path           = ${slf4j.chronology.base}/logger_1
slf4j.chronology.logger.Logger1.level          = info

# logger : TextLogger
slf4j.chronology.logger.TextLogger.path        = ${slf4j.chronology.base}/text
slf4j.chronology.logger.TextLogger.level       = debug
slf4j.chronology.logger.TextLogger.format      = text
slf4j.chronology.logger.TextLogger.dateFormat  = yyyyMMdd-HHmmss-S
```


The configuration of chronicle-slf4j supports variable interpolation where the variables are replaced with the corresponding values from the same configuration file, the system properties and from some predefined values. System properties have the precedence in placeholder replacement so one can override a value via system properties.

Predefined values are:
  * pid which will replaced by the process id
  * today wich will be replaced by the current date (yyyyMMdd)

###Notes
  * Loggers are not hierarchical grouped so my.domain.package.MyClass1 and my.domain are two distinct entities.
  * The _path_ is used to track the underlying VanillaChronicle so two loggers configured with the same _path_ will share the same Chronicle  
