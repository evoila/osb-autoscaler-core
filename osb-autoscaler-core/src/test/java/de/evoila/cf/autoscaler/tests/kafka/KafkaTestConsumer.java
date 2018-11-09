package de.evoila.cf.autoscaler.tests.kafka;


import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer;
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread;

public class KafkaTestConsumer implements AutoScalerConsumer {

	public static final int CONTAINER_METRIC = 1;
	public static final int HTTP_METRIC = 2;
	public static final int PREDICTION = 4;
	
	private ByteConsumerThread cThread;
	private KafkaTest consumer;
	private String groupId;
	
	
	KafkaTestConsumer(String topic, String hostname, int port, KafkaTest test) {
		consumer = test;
		groupId = "kafka_test_consumer" + (int) (Math.random()* 100000);
		cThread = new ByteConsumerThread(topic, groupId, hostname, port, this);
	}
	
	@Override
	public void consume(byte[] bytes) {
		consumer.setInput(bytes);
	}
	
	@Override
	public void startConsumer() {
		cThread.start();
	}
	
	@Override
	public void stopConsumer() {
		cThread.getKafkaConsumer().wakeup();
	}

	@Override
	public String getType() {
		return "kafka_test";
	}
}
