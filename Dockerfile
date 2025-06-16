FROM eclipse-temurin:17-jre
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-Dvaadin.productionMode=true", "-jar", "app.jar"] 