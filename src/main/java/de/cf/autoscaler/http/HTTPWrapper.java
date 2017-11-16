package de.cf.autoscaler.http;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import de.cf.autoscaler.api.ScalingRequest;
import de.cf.autoscaler.api.binding.BindingContext;

/**
 * Wrapper class for outgoing HTTP Communication of the Autoscaler.
 * @author Marius Berger
 *
 */
public class HTTPWrapper {
	
	/**
	 * Hide constructor because there is no need for instance of this class.
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
	public static HttpResponse<String> scale(String host, int port, String endpoint
			, String resourceId, BindingContext context, int newInstances, String secret) throws UnirestException {
		
		HttpResponse<String> response;
		String myEndpoint = endpoint;
		ScalingRequest scalingOrder = new ScalingRequest(newInstances, context);
		if (!endpoint.isEmpty())
			myEndpoint += "/";
		
		if (port == 8080) {
			response = Unirest.patch(
					"http://"+host+"/"+myEndpoint+resourceId+"/"+newInstances)
					.header("secret", secret)
					.header("Content-Type", "application/json")
					.body(scalingOrder.getJSON())
					.asString();
		} else {
			response = Unirest.patch(
					"http://"+host+":"+port+"/"+myEndpoint+resourceId+"/"+newInstances)
					.header("secret", secret)
					.header("Content-Type", "application/json")
					.body(scalingOrder.getJSON())
					.asString();
		}
		return response;
	}
}
