chronology-logback
==================

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <Pattern>%d %contextName [%t] %level %logger{36} - %msg%n</Pattern>
        </encoder>
    </appender>

    <appender name="VANILLA-CHRONICLE" class="com.higherfrequencytrading.chronology.logback.VanillaChronicleAppender">
        <path>${java.io.tmpdir}/chronicle-logback/vanilla-chronicle</path>
        <includeCallerData>true</includeCallerData>
        <includeMappedDiagnosticContext>true</includeMappedDiagnosticContext>
    </appender>

    <logger name="vanilla-chronicle" level="TRACE" additivity="false">
        <appender-ref ref="VANILLA-CHRONICLE"/>
    </logger>

    <root level="DEBUG">
        <appender-ref ref="STDOUT" />
    </root>

</configuration>
``
