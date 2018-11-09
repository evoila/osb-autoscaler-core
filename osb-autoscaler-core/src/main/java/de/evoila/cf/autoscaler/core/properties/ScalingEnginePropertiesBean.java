package de.evoila.cf.autoscaler.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * A bean for storing properties dedicated to the Scaling Engine.
 * Spring fills the fields at the start of the Autoscaler with values out of the properties file.
 * @author Marius Berger
 *
 */
@Service
@ConfigurationProperties(prefix = "engine")
public class ScalingEnginePropertiesBean {

	/**
	 * IP or URL of the Scaling Engine
	 */
	private String host;
	
	/**
	 * Port to connect to the Scaling Engine
	 */
	private int port;

	/**
	 * Secret String to authenticate at the Scaling Engine
	 */
	private String secret;

	private Endpoint endpoint;

	public static class Endpoint {
		private String scaling;

		private String name;

		public String getScaling() {
			return scaling;
		}

		public void setScaling(String scaling) {
			this.scaling = scaling;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}


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

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
	}
}
