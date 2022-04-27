FROM openjdk:8-alpine

COPY target/uberjar/clj-url-shortener.jar /clj-url-shortener/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/clj-url-shortener/app.jar"]
