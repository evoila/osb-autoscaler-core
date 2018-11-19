package de.evoila.cf.autoscaler.core.controller.scaling;

import de.evoila.cf.autoscaler.api.ApplicationNameRequest;
import de.evoila.cf.autoscaler.api.ScalingRequest;
import de.evoila.cf.autoscaler.api.binding.BindingContext;
import de.evoila.cf.autoscaler.core.properties.ScalingEnginePropertiesBean;
import de.evoila.cf.autoscaler.core.utils.EnvironmentUtils;
import de.evoila.cf.security.AcceptSelfSignedClientHttpRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * Wrapper class for outgoing HTTP Communication of the Autoscaler Scaling Engine
 * @author Marius Berger
 *
 */
@Service
public class AutoscalerScalingEngineService {
	
	private static final Logger log = LoggerFactory.getLogger(AutoscalerScalingEngineService.class);

	private static final String CONTENT_TYPE = "Content-Type";

	private static final String X_AUTH_TOKEN_HEADER = "X-Auth-Token";

	private static final String APPLICATION_JSON = "application/json";
	
	private ScalingEnginePropertiesBean scalingEnginePropertiesBean;

	private Environment environment;
	
	private RestTemplate restTemplate;

    private HttpHeaders headers;

	public AutoscalerScalingEngineService(ScalingEnginePropertiesBean scalingEnginePropertiesBean,
                                          Environment environment) {
        this.scalingEnginePropertiesBean = scalingEnginePropertiesBean;
        this.environment = environment;
		headers = new HttpHeaders();

        if (EnvironmentUtils.isTestEnvironment(environment))
            scalingEnginePropertiesBean.setEndpoint(EnvironmentUtils
                    .replaceUrl(scalingEnginePropertiesBean.getEndpoint()));
    }
	
	@PostConstruct
	private void init() {
	    restTemplate = new RestTemplate();
        headers.add(X_AUTH_TOKEN_HEADER, scalingEnginePropertiesBean.getSecret());
        headers.add(CONTENT_TYPE, APPLICATION_JSON);
	}

    @ConditionalOnBean(AcceptSelfSignedClientHttpRequestFactory.class)
    @Autowired(required = false)
    private void selfSignedRestTemplate(AcceptSelfSignedClientHttpRequestFactory requestFactory) {
        restTemplate.setRequestFactory(requestFactory);
    }
	
	/**
	 * Send a scaling order to the scaling engine.
	 * @param resourceId ID of the resource
	 * @param context BindingContext of the application
	 * @param newInstances instance count to scale to
	 * @return the response in form of a {@code ResponseEntity}
	 * @throws HttpServerErrorException
	 */
	public ResponseEntity<String> scale(String resourceId, BindingContext context, int newInstances) throws HttpServerErrorException {
	    String url = scalingEnginePropertiesBean.getEndpoint() + "/resources/" + resourceId;
		ScalingRequest scalingOrder = new ScalingRequest(newInstances, context);
		HttpEntity<?> request = new HttpEntity<>(scalingOrder, headers);
		log.debug("Sending scaling request to " + url + " - " + scalingOrder.toString());
		
		return restTemplate.postForEntity(url, request, String.class);
	}
	
	/**
	 * Send a name request to the scaling engine
	 * @param resourceId ID of the resource
	 * @param context {@code BindingContext} of the application
	 * @return the response in form of a {@code ResponseEntity}
	 * @throws HttpServerErrorException
	 */
	public ResponseEntity<ApplicationNameRequest> getNameFromScalingEngine(String resourceId, BindingContext context) throws HttpServerErrorException {
	    String url = scalingEnginePropertiesBean.getEndpoint() + "/namefromid/" + resourceId;
		ApplicationNameRequest nameRequest = new ApplicationNameRequest(resourceId, "", context);
		HttpEntity<?> request = new HttpEntity<>(nameRequest, headers);
		log.debug("Sending name request to " + url + " - " + nameRequest.toString());
		
		ResponseEntity<ApplicationNameRequest> response = null;
		try {
			response = restTemplate.postForEntity(url, request, ApplicationNameRequest.class);
		} catch (RestClientException ex) {
			log.warn("Exception during a name request for '" + resourceId + "' to scaling engine at '" + url + "'."
					+ " Maybe the scaling engine is not reachable or could not find an application for the given resourceId"
					+ " or an invalid ApplicationNameRequest was used as the body for the request.");
		}
		return response;
	}
}
