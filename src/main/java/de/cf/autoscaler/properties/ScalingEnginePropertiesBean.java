package de.cf.autoscaler.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * A bean for storing properties dedicated to the Scaling Engine.
 * Spring fills the fields at the start of the Autoscaler with values out of the properties file.
 * @author Marius Berger
 *
 */
@Service
public class ScalingEnginePropertiesBean {

	/**
	 * IP or URL of the Scaling Engine
	 */
	@Value("${engine.host}")
	private String host;
	
	/**
	 * Port to connect to the Scaling Engine
	 */
	@Value("${engine.port}")
	private int port;
	
	/**
	 * Endpoint to communicate with the Scaling Engine
	 */
	@Value("${engine.endpoint.scaling}")
	private String scalingEndpoint;
	
	/**
	 * Secret String to authenticate at the Scaling Engine
	 */
	@Value("${engine.secret}")
	private String secret;

	/**
	 * Constructor for Spring to inject the bean.
	 */
	public ScalingEnginePropertiesBean() { }

	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getScalingEndpoint() {
		return scalingEndpoint;
	}

	public void setScalingEndpoint(String scalingEndpoint) {
		this.scalingEndpoint = scalingEndpoint;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
}
