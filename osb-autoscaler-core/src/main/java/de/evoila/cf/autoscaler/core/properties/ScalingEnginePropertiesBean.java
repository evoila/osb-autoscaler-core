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

	private String endpoint;

	private String secret;

	public ScalingEnginePropertiesBean() { }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
