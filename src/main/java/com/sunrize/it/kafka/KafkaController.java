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
