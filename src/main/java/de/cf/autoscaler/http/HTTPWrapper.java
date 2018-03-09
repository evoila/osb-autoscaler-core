package de.cf.autoscaler.http;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import de.cf.autoscaler.api.ApplicationNameRequest;
import de.cf.autoscaler.api.ScalingRequest;
import de.cf.autoscaler.api.binding.BindingContext;
import de.cf.autoscaler.properties.ScalingEnginePropertiesBean;

/**
 * Wrapper class for outgoing HTTP Communication of the Autoscaler.
 * @author Marius Berger
 *
 */
@Service
public class HTTPWrapper {
	
	private static final Logger log = LoggerFactory.getLogger(HTTPWrapper.class);
	
	@Autowired
	private ScalingEnginePropertiesBean engineProps;
	
	private RestTemplate restTemplate;
	
	/**
	 * Hide constructor because there is no need for an instance of this class.
	 */
	public HTTPWrapper() {}
	
	@PostConstruct
	private void init() {
		restTemplate = new RestTemplate();
	}
	
	/**
	 * Send a scaling order to the scaling engine.
	 * @param host IP or URL of the scaling engine
	 * @param port port of the scaling engine
	 * @param endpoint endpoint of the scaling engine to call
	 * @param resourceId ID of the resource
	 * @param context BindingContext of the application
	 * @param newInstances instance count to scale to
	 * @param secret {@code String} to authorize to the scaling engine
	 * @return the response in form of a {@code ResponseEntity}
	 * @throws HttpServerErrorException
	 */
	public ResponseEntity<String> scale(String resourceId, BindingContext context, int newInstances) throws HttpServerErrorException {
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("secret", engineProps.getSecret());
		headers.add("Content-Type", "application/json");
		String url = "http://"+engineProps.getHost()+"/"+engineProps.getScalingEndpoint()+"/"+resourceId;
		ScalingRequest scalingOrder = new ScalingRequest(newInstances, context);
		HttpEntity<?> request = new HttpEntity<>(scalingOrder, headers);
		log.debug("Sending scaling request to " + url + " - " + scalingOrder.toString());
		
		return restTemplate.postForEntity(url, request, String.class);
	}
	
	/**
	 * Send a name rquest to the scaling engine
	 * @param host IP or the URL of the scaling engine
	 * @param endpoint endpoint of the scaling engine to call
	 * @param resourceId ID of the resource
	 * @param context {@code BindingContext} of the application
	 * @param secret {@code String} to authorize to the scaling engine
	 * @return the response in form of a {@code ResponseEntity}
	 * @throws HttpServerErrorException
	 */
	public  ResponseEntity<ApplicationNameRequest> getNameFromScalingEngine(String resourceId, BindingContext context) throws HttpServerErrorException {
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("secret", engineProps.getSecret());
		headers.add("Content-Type", "application/json");
		String url = "http://"+ engineProps.getHost() +"/"+ engineProps.getNameEndpoint() +"/"+resourceId;
		ApplicationNameRequest nameRequest = new ApplicationNameRequest(resourceId, "", context);
		HttpEntity<?> request = new HttpEntity<>(nameRequest, headers);
		log.debug("Sending name request to " + url + " - " + nameRequest.toString());
		
		ResponseEntity<ApplicationNameRequest> response = null;
		try {
			response = restTemplate.postForEntity(url, request, ApplicationNameRequest.class);
		} catch (RestClientException ex) {
			log.warn("Exception during a name request for '" + resourceId + "' to scaling engine at '" + url + "'."
					+ " Maybe the scaling engine could not find an application for the given resourceId"
					+ " or an invalid ApplicationNameRequest was used as the body for the request.");
		}
		return response;
	}
}
