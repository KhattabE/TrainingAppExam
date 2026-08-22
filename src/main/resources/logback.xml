<configuration>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%msg%n</pattern>
        </encoder>
    </appender>

    <!-- Default level for everything -->
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>

    <!-- Silence noisy frameworks -->
    <logger name="org.hibernate" level="WARN"/>
    <logger name="org.testcontainers" level="WARN"/>
    <logger name="com.github.dockerjava" level="WARN"/>

</configuration>