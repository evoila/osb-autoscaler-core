package de.cf.autoscaler.tests.kafka;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;

import com.google.protobuf.InvalidProtocolBufferException;

import de.cf.autoscaler.kafka.AutoScalerConsumer;
import de.cf.autoscaler.kafka.messages.ContainerMetric;
import de.cf.autoscaler.kafka.messages.HttpMetric;
import de.cf.autoscaler.kafka.producer.MetricProducer;
import de.cf.autoscaler.kafka.protobuf.ProtobufContainerMetricWrapper.ProtoContainerMetric;
import de.cf.autoscaler.kafka.protobuf.ProtobufHttpMetricWrapper.ProtoHttpMetric;
import de.cf.autoscaler.kafka.protobuf.ProtobufPredictionWrapper.ProtoPrediction;
import de.cf.autoscaler.scaling.prediction.Prediction;

public class KafkaTest {
	
	private static String host = "";
	private static final int PORT = 9092;
	private static final int SECONDS_TO_SLEEP = 3;
	
	private static final String TOPIC_CONTAINER = "autoscaler_tests";
	private static final String TOPIC_HTTP = "autoscaler_tests";
	private static final String TOPIC_PREDICTION = "autoscaler_tests";
	
	private byte[] input;
	private static MetricProducer producer;
	
	@Before
	public void init() throws IOException {
		
		Properties properties = new Properties();
		String s = File.separator;
		File file = new File("." + s + "src" + s + "main" + s + "resources" + s + "application.properties");
		if (file.exists()) {
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
			properties.load(stream);
			stream.close();
			host = properties.getProperty("kafka.host");
		}
		producer = new MetricProducer("autoscaler_test_metricProducer", host, PORT);
	}
	
	//@Test
	public void testContainerMetric() throws InvalidProtocolBufferException {
		AutoScalerConsumer consumer = new KafkaTestConsumer(TOPIC_CONTAINER, host, PORT, this);
		consumer.startConsumer();
		
		int cpu = (int) (Math.random() * 100);
		long ram = (long) (500 * Math.random() * 1024 * 1024);
		int instanceIndex = (int) (Math.random() * 10);
		String appId = "testAppId";
		String desc = "This is a test container metric.";
		
		try {
			Thread.sleep(SECONDS_TO_SLEEP * 1000);
		} catch (InterruptedException ex) {}
		
		ContainerMetric metric = new ContainerMetric(System.currentTimeMillis(), "testContainerMetric", appId, cpu , ram, instanceIndex, desc);
		producer.produce(metric, TOPIC_CONTAINER);
		
		try {
			Thread.sleep(SECONDS_TO_SLEEP * 1000);
		} catch (InterruptedException ex) {}
		
		assertNotNull(input);
		ContainerMetric inputMetric = new ContainerMetric(ProtoContainerMetric.parseFrom(input));
		assertTrue(inputMetric.equals(metric));
		
		consumer.stopConsumer();
	}
	
	//@Test
	public void testHTTPMetric() throws InvalidProtocolBufferException {
		AutoScalerConsumer consumer = new KafkaTestConsumer(TOPIC_HTTP, host, PORT, this);
		consumer.startConsumer();
		
		int requests = (int) (Math.random() * 100);
		int latency = (int) (Math.random() * 50);
		String appId = "testAppId";
		String desc = "This is a test http metric.";
		
		try {
			Thread.sleep(SECONDS_TO_SLEEP * 1000);
		} catch (InterruptedException ex) {}
		
		HttpMetric metric = new HttpMetric(System.currentTimeMillis(), "testHttpMetric", appId, requests, latency, desc);
		producer.produce(metric, TOPIC_HTTP);
		
		try {
			Thread.sleep(SECONDS_TO_SLEEP * 1000);
		} catch (InterruptedException ex) {}
		
		assertNotNull(input);
		HttpMetric inputMetric = new HttpMetric(ProtoHttpMetric.parseFrom(input));
		assertTrue(inputMetric.equals(metric));
		
		consumer.stopConsumer();
	}
	
	//@Test
	public void testPrediction() throws InvalidProtocolBufferException {
		AutoScalerConsumer consumer = new KafkaTestConsumer(TOPIC_PREDICTION, host, PORT, this);
		consumer.startConsumer();
		
		int instanceCount = (int) (Math.random() * 10);
		long intervalStart = System.currentTimeMillis() + (int) (Math.random() * 50 * 1000);
		long intervalEnd = intervalStart + (int) (Math.random() * 50 * 1000);
		String predictorId = "testPredictorId";
		String appId = "testAppId";
		String desc = "This is a test prediction.";
		
		try {
			Thread.sleep(SECONDS_TO_SLEEP * 1000);
		} catch (InterruptedException ex) {}
		
		Prediction pre = new Prediction(instanceCount, System.currentTimeMillis(), intervalStart, intervalEnd, predictorId, appId, desc);
//		producer.produce(pre, TOPIC_PREDICTION); //needs to be implemented first, although it is not used other than this test
		
		try {
			Thread.sleep(SECONDS_TO_SLEEP * 1000);
		} catch (InterruptedException ex) {}
		
		assertNotNull(input);
		Prediction inputPre = new Prediction(ProtoPrediction.parseFrom(input));
		assertTrue(inputPre.equals(pre));
		
		consumer.stopConsumer();
	}
	
	public void setInput(byte[] bytes) {
		input = bytes;
	}

}
