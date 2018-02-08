package de.cf.autoscaler.http;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.cf.autoscaler.api.binding.Binding;
import de.cf.autoscaler.applications.ScalableApp;
import de.cf.autoscaler.applications.ScalableAppService;
import de.cf.autoscaler.http.response.ResponseApplication;
import de.cf.autoscaler.manager.ScalableAppManager;

/**
 * Controller to handle incoming bindings and unbindings.
 * @author Marius Berger
 *
 */
@Controller
public class BindingController extends BaseController {
	
	
	/**
	 * {@code ScalableAppManager} to get, bind or unbind applications.
	 */
	@Autowired
	private ScalableAppManager appManager;
	
	/**
	 * {@code String} to check for equality with the secret of a request to authorize it.
	 */
	@Value("${broker.secret}")
	private String secret;
	
	/**
	 * Handles incoming requests to bind a new application.
	 * @param secret {@code String} to authorize with
	 * @param binding information about the binding via a {@linkplain Binding} object
	 * @return the response in form of a {@code ResponseEntity} with a related statuscode and either information about the new application or an other JSON String
	 * @see ResponseEntity
	 */
	@RequestMapping( value = "/bindings", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> bindApp(@RequestHeader(value="secret") String secret, @RequestBody Binding binding) {
		
		if (secret.equals(this.secret)) {
			if (binding.isValidWithReason() != null) {
				return processErrorResponse(binding.isValidWithReason(), HttpStatus.BAD_REQUEST);
			}
				
			ScalableApp newApp = appManager.getNewApp(binding);
			if (appManager.contains(binding.getId())) {
				if (appManager.get(binding.getId()).getBinding().equals(newApp.getBinding())) {
					return ResponseEntity.status(HttpStatus.OK).body("{}");
				}
				return ResponseEntity.status(HttpStatus.CONFLICT).body(
						"{ \"error\" : \"An other binding was found with the same id.\" }");
			}
			ResponseApplication responseApp = ScalableAppService.getSerializationObjectWithLock(newApp);
			appManager.add(newApp, false);
			return new ResponseEntity<ResponseApplication>(responseApp, HttpStatus.CREATED);
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
	}
	
	
	
	/**
	 * Handles incoming requests to unbind a existing application.
	 * @param secret {@code String} to authorize with
	 * @param appId ID of the application
	 * @return the response in form of a {@code ResponseEntity} with an empty JSON String and a related statuscode
	 * @see ResponseEntity
	 */
	@RequestMapping(value = "/bindings/{appId}", method = RequestMethod.DELETE,  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> unbindApp(@RequestHeader(value="secret") String secret, @PathVariable("appId") String appId) {
		if (secret.equals(this.secret)) {
			if (appManager.contains(appId)) {
				appManager.remove(appId);
				return ResponseEntity.status(HttpStatus.OK).body("{}");
			}
			return ResponseEntity.status(HttpStatus.GONE).body("{}");
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
	}
	
	/**
	 * Handles incoming request to get information about existing bindings.
	 * @param secret {@code String} to authorize with
	 * @return the response in form of a {@code ResponseEntity}
	 */
	@RequestMapping(value = "/bindings", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> infosAboutBindings(@RequestHeader(value="secret") String secret) {
		if (secret.equals(this.secret)) {
			List<Binding> bindings = appManager.getListOfBindings();
			Map<String, List<Binding>> map = new HashMap<String, List<Binding>>();
			map.put("bindings", bindings);
			return new ResponseEntity<Map<String, List<Binding>>>(map, HttpStatus.OK);
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
	}

	/**
	 * Handles incoming request to get information about service instance specific existing bindings.
	 * @param secret {@code String} to authorize with
	 * @param serviceId {@code String} of the service instance you want to get the bindings of
	 * @return the response in form of a {@code ResponseEntity}
	 */
	@RequestMapping(value = "/bindings/serviceInstance/{serviceId}", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> infosAboutSpecificBindings(@RequestHeader(value="secret") String secret,
														@PathVariable("serviceId") String serviceId) {
		if (secret.equals(this.secret)) {
			List<Binding> bindings = new LinkedList<>();

			for(Binding binding : appManager.getListOfBindings()) {
				if(binding.getServiceId().equals(serviceId)) {
					bindings.add(binding);
				}
			}

			Map<String, List<Binding>> map = new HashMap<String, List<Binding>>();
			map.put("bindings", bindings);
			return new ResponseEntity<Map<String, List<Binding>>>(map, HttpStatus.OK);
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
	}
}
