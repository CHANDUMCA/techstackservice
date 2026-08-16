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

	/**
	 * Publish a message to the configured Kafka topic.
	 */
	public void sendMessage(String message) {
		log.info("Sending message to Kafka Topic '{}': {}", topicName, message);
		kafkaTemplate.send(topicName, message);
		log.debug("Message sent successfully to topic: {}", topicName);
	}
}
