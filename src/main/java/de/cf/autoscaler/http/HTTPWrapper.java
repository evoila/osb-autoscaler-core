package de.cf.autoscaler.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import de.cf.autoscaler.api.ScalingRequest;
import de.cf.autoscaler.api.binding.BindingContext;

/**
 * Wrapper class for outgoing HTTP Communication of the Autoscaler.
 * @author Marius Berger
 *
 */
public class HTTPWrapper {
	
	private static final Logger log = LoggerFactory.getLogger(HTTPWrapper.class);
	
	private static RestTemplate restTemplate = new RestTemplate();
	
	/**
	 * Hide constructor because there is no need for an instance of this class.
	 */
	private HTTPWrapper() {}
	
	/**
	 * Send a scaling order to the scaling engine.
	 * @param host IP or URL of the scaling engine
	 * @param port port of the scaling engine
	 * @param endpoint endpoint to call of the scaling engine
	 * @param resourceId ID of the resource
	 * @param context BindingContext of the application
	 * @param newInstances instance count to scale to
	 * @param secret {@code String} to authorize to the scaling engine
	 * @return the response in form of a {@code ResponseEntity}
	 * @throws UnirestException - if Unirest throws an exception itself
	 * @see HttpResponse
	 */
	public static ResponseEntity<String> scale(String host, int port, String endpoint
			, String resourceId, BindingContext context, int newInstances, String secret) throws HttpServerErrorException {
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("secret", secret);
		headers.add("Content-Type", "application/json");
		HttpEntity<?> request = new HttpEntity<>(null, headers);
		String url = "http://"+host+":"+port+"/"+endpoint+"/"+resourceId;
		ScalingRequest scalingOrder = new ScalingRequest(newInstances, context);
		log.debug("Sending scaling request to " + url + " - " + scalingOrder.toString());
		
		return restTemplate.postForEntity(url, request, String.class);
	}
}
