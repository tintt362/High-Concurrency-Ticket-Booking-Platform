########## BUILD STAGE ##########
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

########## RUNTIME STAGE ##########
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -m appuser

# Tạo thư mục logs và cấp quyền cho appuser
# Không có bước này → appuser không ghi được log → Logback lỗi
RUN mkdir -p /app/logs && chown -R appuser:appuser /app/logs

USER appuser

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]