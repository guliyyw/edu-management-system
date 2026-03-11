FROM eclipse-temurin:17-jdk-alpine AS builder

# 安装 Maven
RUN apk add --no-cache maven

WORKDIR /app

# 先复制 pom.xml，利用 Docker 缓存层
COPY pom.xml .

# 下载依赖（这一层会被缓存，只要 pom.xml 不变就不会重新下载）
RUN mvn dependency:go-offline -B

# 再复制源代码
COPY src ./src

# 编译打包（跳过测试）
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine

# 安装 PostgreSQL 客户端（包含 pg_dump 和 psql）
RUN apk add --no-cache postgresql-client

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
