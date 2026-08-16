# Spring Boot + Apache Kafka: Event-Driven Microservices Reference Guide

This document serves as an end-to-end reference guide for configuring a decoupled, multi-service communication flow using Apache Kafka in Spring Boot. It details how the `techstackservice` project (Producer) publishes events to Kafka, and the `techstackservicecon` project (Consumer) consumes them.

---

## 🗺️ Kafka Project Architecture
* **Broker (Kafka Cluster):** Runs in a local Docker container on port `9092`.
* **Producer (`techstackservice`):** Runs on port `8080`. Exposes an API endpoint to accept string payloads and publishes them to a Kafka topic.
* **Consumer (`techstackservicecon`):** Runs on port `8081`. Subscribes to the Kafka topic using a listener, consuming and logging messages automatically.

---

## 📋 Prerequisites
* **Docker Desktop** running on Windows.
* **Apache Kafka Container** running in Docker on port `9092`:
  ```cmd
  docker run -d --name local-kafka -p 9092:9092 apache/kafka:latest
  ```

---

## 🚀 Part 1: Producer Application (techstackservice)

### 1. Maven Dependency
Include the Spring Boot Kafka Starter (with Logback logging excluded to prevent conflicts with Log4j2):

```xml
		<!-- Add Kafka Starter (with logging excluded for Log4j2) -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-kafka</artifactId>
			<exclusions>
				<exclusion>
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-starter-logging</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
```

### 2. Producer Configurations (`application.properties`)
```properties
spring.application.name=techstackservice

# Kafka Broker address
spring.kafka.bootstrap-servers=localhost:9092

# Serializers for converting key/value strings into bytes
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

# Custom Topic name
app.kafka.topic=practice-topic
```

### 3. Producer Service Class
`src/main/java/com/sunrize/it/kafka/KafkaProducerService.java`
```java
package com.sunrize.it.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topicName;

    public void sendMessage(String message) {
        log.info("Sending message to Kafka Topic '{}': {}", topicName, message);
        kafkaTemplate.send(topicName, message);
        log.debug("Message sent successfully to topic: {}", topicName);
    }
}
```

### 4. REST Controller
`src/main/java/com/sunrize/it/kafka/KafkaController.java`
```java
package com.sunrize.it.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/kafka")
public class KafkaController {

    @Autowired
    private KafkaProducerService producerService;

    @PostMapping("/publish")
    public String publishMessage(@RequestParam String message) {
        log.info("Received POST request to publish message to Kafka: '{}'", message);
        producerService.sendMessage(message);
        return "Message '" + message + "' published to Kafka successfully!";
    }

    @GetMapping("/publish")
    public String publishMessageGet(@RequestParam String message) {
        log.info("Received GET request to publish message to Kafka: '{}'", message);
        producerService.sendMessage(message);
        return "Message '" + message + "' published to Kafka successfully!";
    }
}
```

---

## 🛰️ Part 2: Consumer Application (techstackservicecon)

### 1. Maven Dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-kafka</artifactId>
</dependency>
```

### 2. Consumer Configurations (`application.properties`)
```properties
# Run on a different port to avoid conflicts
server.port=8081

# Kafka Broker address
spring.kafka.bootstrap-servers=localhost:9092

# Consumer Group ID
spring.kafka.consumer.group-id=practice-group

# Deserializers to convert bytes back to String objects
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer

# Matching Topic Name
app.kafka.topic=practice-topic
```

### 3. Listener Service
```java
package com.sunrize.it.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(String message) {
        log.info(">>>> [KAFKA CONSUMER] Successfully received message: '{}'", message);
    }
}
```

---

## 🧪 Verification & Testing

### 1. Command-Line Consumer Verification (Direct Docker CLI)
Verify the producer by streaming messages directly from the running container:
```cmd
docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic practice-topic --from-beginning
```

### 2. Decoupled Service Integration Testing
1. Start your **Producer** app on port `8080` (`techstackservice`).
2. Start your **Consumer** app on port `8081` (`techstackservicecon`).
3. Trigger the HTTP GET publishing URL in your browser:
   👉 **[http://localhost:8080/api/kafka/publish?message=HelloToConsumerService](http://localhost:8080/api/kafka/publish?message=HelloToConsumerService)**
4. Inspect the console logs of the **Consumer** application on port `8081`. You should see:
   `>>>> [KAFKA CONSUMER] Successfully received message: 'HelloToConsumerService'`
