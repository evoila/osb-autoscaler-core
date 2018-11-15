package de.evoila.cf.autoscaler.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * A bean for storing properties dedicated to the Scaling Engine.
 * Spring fills the fields at the start of the Autoscaler with values out of the properties file.
 * @author Marius Berger
 *
 */
@ConfigurationProperties(prefix = "scaling-engine")
public class ScalingEnginePropertiesBean {

    private String scheme;

	private String host;
	
	private int port;
	
	private String scalingEndpoint;
	
	private String nameEndpoint;
	
	private String secret;

	public ScalingEnginePropertiesBean() { }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

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

	public String getNameEndpoint() {
		return nameEndpoint;
	}

	public void setNameEndpoint(String nameEndpoint) {
		this.nameEndpoint = nameEndpoint;
	}
}
