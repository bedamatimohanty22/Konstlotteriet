####
# This Dockerfile is used to build a container that runs the Quarkus application in JVM mode
####
FROM eclipse-temurin:21-jre
ENV LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 target/quarkus-app/*.jar /deployments/
COPY --chown=185 target/quarkus-app/app/ /deployments/app/
COPY --chown=185 target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m ${JAVA_OPTS_APPEND}"

ENTRYPOINT [ "java", "-jar", "/deployments/quarkus-run.jar" ]