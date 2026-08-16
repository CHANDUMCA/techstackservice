# Spring Boot + Redis + Kubernetes + Log4j2: Master Reference Guide

This document serves as an end-to-end master reference guide for integrating Redis into a Spring Boot application, configuring Log4j2 logging, Dockerizing it, deploying it to a local Kubernetes (Minikube) cluster, and externalizing application logs to your Windows host machine (`D:\GIT\logs`).

---

## 🗺️ Complete Roadmap Overview

### 🚀 Phase 1: Local Spring Boot & Redis Setup
1. Create the Spring Boot maven project with Web, H2, and JPA dependencies.
2. Add the Redis Starter dependency in `pom.xml`.
3. Configure Redis connection details in `application.properties`.
4. Create the Java package `com.sunrize.it.redis`.
5. Implement the Java configuration class `RedisConfig.java`.
6. Implement the utility service class `RedisService.java`.
7. Create the REST controller `RedisController.java` to test set/get APIs.

### 🪵 Phase 2: Log4j2 Logging & Local Externalization
8. Exclude default logging (Logback) from the starters in `pom.xml` and add Log4j2.
9. Create `log4j2-spring.xml` to write logs to a dynamic directory (`LOG_PATH`).
10. Configure annotation processing in STS for Lombok and add `@Slf4j` to `RedisController.java`.
11. Verify local log output in `D:\GIT\logs` by running the app with `-DLOG_PATH`.

### 🐳 Phase 3: Dockerization & Kubernetes Deployment
12. Create a `Dockerfile` that passes `-DLOG_PATH=/app/logs`.
13. Build the Docker image locally and load it into Minikube's cache.
14. Configure Kubernetes manifests for Redis (`redis-k8s.yaml`).
15. Configure Kubernetes manifests for the application (<code>app-k8s.yaml</code>) with volume mounts.
16. Mount the Windows directory to Minikube, deploy the cluster, and test APIs.

---

## 📋 Prerequisites
1. **Java JDK 21** installed and configured.
2. **Maven 3.x** (or Maven Wrapper `mvnw` included in the project).
3. **Docker Desktop for Windows** installed and running.
4. **Minikube** and **kubectl** installed.
5. **Lombok plugin** installed and active in STS/Eclipse.

---

## 🛠️ Step-by-Step Files & Setup

### Step 1: Maven Dependencies (`pom.xml`)
This is the final, complete version of the `pom.xml` dependencies block, including the exclusions for default logging:

```xml
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-h2console</artifactId>
		</dependency>
		
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
			<exclusions>
				<exclusion>
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-starter-logging</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
		
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc</artifactId>
			<exclusions>
				<exclusion>
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-starter-logging</artifactId>
				</exclusion>
			</exclusions>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-redis</artifactId>
			<exclusions>
				<exclusion>
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-starter-logging</artifactId>
				</exclusion>
			</exclusions>
		</dependency>

		<!-- Add Log4j2 Starter -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-log4j2</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>com.oracle.database.jdbc</groupId>
			<artifactId>ojdbc11</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
			<scope>provided</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
```

---

### Step 2: Application Properties
`src/main/resources/application.properties`
```properties
spring.application.name=techstackservice
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

---

### Step 3: Log4j2 Layout Config
`src/main/resources/log4j2-spring.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Properties>
        <Property name="LOG_DIR">${sys:LOG_PATH:-./logs}</Property>
        <Property name="LOG_FILE_NAME">techstackservice</Property>
    </Properties>
    
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        
        <RollingFile name="RollingFile" 
                     fileName="${LOG_DIR}/${LOG_FILE_NAME}.log" 
                     filePattern="${LOG_DIR}/${LOG_FILE_NAME}-%d{yyyy-MM-dd}-%i.log.gz">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
                <SizeBasedTriggeringPolicy size="10 MB"/>
            </Policies>
            <DefaultRolloverStrategy max="10"/>
        </RollingFile>
    </Appenders>
    
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="RollingFile"/>
        </Root>
        
        <Logger name="com.sunrize.it" level="DEBUG" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="RollingFile"/>
        </Logger>
    </Loggers>
</Configuration>
```

> [!WARNING]
> **Spring Tool Suite (STS) Lombok Setup:** If STS displays red markers on `@Slf4j` or `log` variables, run the Lombok jar installer in cmd:
> `java -jar C:\Users\DHRUVA\.m2\repository\org\projectlombok\lombok\1.18.46\lombok-1.18.46.jar`
> Select your STS location, run the install, restart the IDE, and click **Project -> Clean...**.

---

### Step 4: Java configuration (`RedisConfig.java`)
`src/main/java/com/sunrize/it/redis/RedisConfig.java`
```java
package com.sunrize.it.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        Thread template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        GenericJacksonJsonRedisSerializer serializer = GenericJacksonJsonRedisSerializer.builder().build();
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
```

---

### Step 5: Service Helper (`RedisService.java`)
`src/main/java/com/sunrize/it/redis/RedisService.java`
```java
package com.sunrize.it.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeoutInSeconds) {
        redisTemplate.opsForValue().set(key, value, timeoutInSeconds, TimeUnit.SECONDS);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
```

---

### Step 6: REST Controller (`RedisController.java`)
`src/main/java/com/sunrize/it/redis/RedisController.java`
```java
package com.sunrize.it.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/redis")
public class RedisController {

    @Autowired
    private RedisService redisService;

    @PostMapping("/set")
    public String setKey(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(required = false) Long ttl) {
        
        log.info("Received POST request to set key: '{}' with value: '{}', TTL: {}", key, value, ttl);
        
        if (ttl != null) {
            redisService.set(key, value, ttl);
            log.debug("Successfully saved key: '{}' with TTL: {}s", key, ttl);
            return "Key '" + key + "' set with value '" + value + "' and TTL " + ttl + " seconds successfully!";
        } else {
            redisService.set(key, value);
            log.debug("Successfully saved key: '{}' without TTL", key);
            return "Key '" + key + "' set with value '" + value + "' successfully!";
        }
    }

    @GetMapping("/get/{key}")
    public Object getKey(@PathVariable String key) {
        log.info("Received GET request to retrieve key: '{}'", key);
        
        Object value = redisService.get(key);
        if (value == null) {
            log.warn("Key '{}' was not found in Redis.", key);
            return "Key '" + key + "' not found in Redis.";
        }
        
        log.debug("Retrieved value for key '{}': '{}'", key, value);
        return value;
    }

    @DeleteMapping("/delete/{key}")
    public String deleteKey(@PathVariable String key) {
        log.info("Received DELETE request for key: '{}'", key);
        
        boolean deleted = redisService.delete(key);
        if (deleted) {
            log.debug("Successfully deleted key: '{}'", key);
            return "Key '" + key + "' deleted successfully.";
        } else {
            log.warn("Failed to delete key '{}'. Key might not exist.", key);
            return "Key '" + key + "' not found or could not be deleted.";
        }
    }
}
```

---

## 🐳 Phase 4: Dockerization

### 1. Dockerfile
`Dockerfile`
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/techstackservice-0.0.1.war app.war
EXPOSE 8080
# Override the LOG_PATH system property inside the container
ENTRYPOINT ["java", "-DLOG_PATH=/app/logs", "-jar", "app.war"]
```

### 2. Build local Docker Image
```cmd
mvnw clean package -DskipTests
docker build -t techstackservice:1.0 .
```

---

## ☸️ Phase 5: Kubernetes Manifests

### 1. Redis Manifest
`k8s/redis-k8s.yaml`
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-deployment
  labels:
    app: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:alpine
        ports:
        - containerPort: 6379
---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
spec:
  selector:
    app: redis
  ports:
    - protocol: TCP
      port: 6379
      targetPort: 6379
```

### 2. Application Manifest with volumeMounts
`k8s/app-k8s.yaml`
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: techstackservice-deployment
  labels:
    app: techstackservice
spec:
  replicas: 1
  selector:
    matchLabels:
      app: techstackservice
  template:
    metadata:
      labels:
        app: techstackservice
    spec:
      containers:
      - name: techstackservice
        image: techstackservice:1.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATA_REDIS_HOST
          value: "redis-service"
        - name: SPRING_DATA_REDIS_PORT
          value: "6379"
        volumeMounts:
        - name: log-volume
          mountPath: /app/logs
      volumes:
      - name: log-volume
        hostPath:
          path: /data/logs
          type: DirectoryOrCreate
---
apiVersion: v1
kind: Service
metadata:
  name: techstackservice-service
spec:
  type: NodePort
  selector:
    app: techstackservice
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
      nodePort: 30080
```

---

## 🏃‍♀️ Startup & Deployment Workflow (From Stopped State)

### Step 1: Start Docker Desktop & Minikube
1. Open **Docker Desktop** and wait for the green status.
2. In your terminal, start Minikube:
   ```cmd
   minikube start
   ```

### Step 2: Clear Minikube Cache & Reload Image (Important)
To ensure Minikube updates to your new Docker configuration:
```cmd
# 1. Delete old app deployment so the image is not in use
kubectl delete -f k8s/app-k8s.yaml

# 2. Remove the old image cache from Minikube
minikube image rm techstackservice:1.0

# 3. Load the new image
minikube image load techstackservice:1.0

# 4. Apply the fresh configuration
kubectl apply -f k8s/app-k8s.yaml
kubectl apply -f k8s/redis-k8s.yaml
```

### Step 3: Start directory mounting (Terminal 1)
Open a **new command prompt** and run:
```cmd
minikube mount "D:\GIT\logs:/data/logs"
```

> [!IMPORTANT]
> **Mount Ordering Gotcha:** If the pod starts *before* the mount tunnel is up, you must restart the pod to allow mounting to sync:
> `kubectl rollout restart deployment/techstackservice-deployment`

### Step 4: Open Service Tunnel (Terminal 2)
Open **another new command prompt** and run:
```cmd
minikube service techstackservice-service
```
Note the URL generated (e.g. `http://127.0.0.1:61225`).

---

## 🧪 Testing and Logs Verification

### 1. Trigger POST Request (PowerShell)
```powershell
Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:PORT/api/redis/set?key=k8sKey&value=HelloFromKubernetes"
```

### 2. Trigger GET Request (Browser)
Open: `http://127.0.0.1:PORT/api/redis/get/k8sKey`

### 3. Stream Windows Log File (PowerShell Terminal 3)
```powershell
Get-Content -Path "D:\GIT\logs\techstackservice.log" -Wait -Tail 15
```
