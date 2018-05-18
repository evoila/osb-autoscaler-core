package de.evoila.cf.autoscaler.core.http;

import de.evoila.cf.autoscaler.api.binding.InvalidBindingException;
import de.evoila.cf.autoscaler.api.update.UpdateRequest;
import de.evoila.cf.autoscaler.core.applications.ScalableApp;
import de.evoila.cf.autoscaler.core.applications.ScalableAppService;
import de.evoila.cf.autoscaler.core.exception.*;
import de.evoila.cf.autoscaler.core.http.response.ResponseApplication;
import de.evoila.cf.autoscaler.core.manager.ScalableAppManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to handle requests to manage existing {@code ScalableApps}.
 * @author Marius Berger
 * @see ScalableApp
 */
@Controller
public class ManagingController extends BaseController{

	/**
	 * Logger of this class.
	 */
	private static Logger log = LoggerFactory.getLogger(ManagingController.class);
	
	/**
	 * {@code ScalableAppManager} to get applications from.
	 */
	@Autowired
	private ScalableAppManager appManager;
	
	@Autowired
	private HTTPWrapper httpWrapper;
	
	/**
	 * {@code String} to check for equality with the secret of a request to authorize it.
	 */
	@Value("${broker.secret}")
	private String secret;
	
	/**
	 * Handles incoming request to update an application.
	 * @param secret {@code String} to authorize with
	 * @param appId ID of the application
	 * @param requestBody body of the request
	 * @return the response in form of a {@code ResponseEntity}
	 * @throws InvalidWorkingSetException 
	 * @throws TimeException 
	 * @throws SpecialCharacterException 
	 * @throws InvalidPolicyException 
	 * @throws LimitException 
	 * @throws InvalidBindingException 
	 * @see ResponseEntity
	 */
	@RequestMapping(value = "/bindings/{appId}", method = RequestMethod.PATCH
			, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateApp(@RequestHeader(value="secret") String secret, @PathVariable("appId") String appId,
			@RequestBody UpdateRequest requestBody) throws LimitException, InvalidPolicyException, SpecialCharacterException, TimeException, InvalidWorkingSetException, InvalidBindingException {
		
		if (secret.equals(this.secret)) {
			ScalableApp app = appManager.get(appId);
			
			if (app == null) {
				return ResponseEntity.status(HttpStatus.GONE).body("{}");
			}
			
			if (requestBody.getAllSetElements().size() == 0 ) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
						"{ \"error\" : \"Not one valid policy was found.\"}");
			}
			
			ResponseApplication responseApp = null;
			try {
				app.acquire();
				app.update(requestBody);
				appManager.updateInDatabase(app);
				responseApp = ScalableAppService.getSerializationObjectWithoutLock(app);
			} catch (InterruptedException ex) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{ \"error\" : \"Request was interrupted.\" }");
			}
			
			app.release();
			return new ResponseEntity<ResponseApplication>(responseApp, HttpStatus.OK);
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");	
	}
	
	/**
	 * Handles incoming request to get information about an application.
	 * @param secret {@code String} to authorize with
	 * @param appId ID of the application
	 * @return the response in form of a {@code ResponseEntity}
	 * @see ResponseEntity
	 */
	@RequestMapping(value = "/bindings/{appId}", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> appInfo(@RequestHeader(value="secret") String secret, @PathVariable("appId") String appId) {
		if (secret.equals(this.secret)) {
			ScalableApp app = appManager.get(appId);
			if (app != null) {
				ResponseApplication responseApp = ScalableAppService.getSerializationObjectWithLock(app);
				if (responseApp == null)
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{ \"error\" : \"Request was interrupted.\" }");
				
				return new ResponseEntity<ResponseApplication>(responseApp, HttpStatus.OK);
			}
			return ResponseEntity.status(HttpStatus.GONE).body("{}");
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
	}
	
	/**
	 * Handles incoming requests to reset the quotient of an application.
	 * @param secret {@code String} to authorize with
	 * @param appId ID of the application
	 * @return the response in form of a {@code ResponseEntity}
	 */
	@RequestMapping(value = "/bindings/{appId}/resetQuotient", method = RequestMethod.PATCH,  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> resetQuotient(@RequestHeader(value="secret") String secret, @PathVariable("appId") String appId) {
		if (secret.equals(this.secret)) {
			ScalableApp app = appManager.get(appId);
			if (app != null) {
				ResponseApplication responseApp = null;
				try {
					app.acquire();
					app.getRequest().resetQuotient();
					appManager.updateInDatabase(app);
					responseApp = ScalableAppService.getSerializationObjectWithoutLock(app);
					log.info("Reset quotient for "+app.getIdentifierStringForLogs());
				} catch (InterruptedException e) {}
				app.release();
				return new ResponseEntity<ResponseApplication>(responseApp, HttpStatus.OK);
			}
			return ResponseEntity.status(HttpStatus.GONE).body("{}");
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
	}
	
	/**
	 * Handles incoming requests to reset the learning start time of an application.
	 * @param secret {@code String} to authorize with
	 * @param appId ID of the application
	 * @return the response in form of a {@code ResponseEntity}
	 */
	@RequestMapping (value = "/bindings/{appId}/resetLST", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> resetLearningStartTime(@RequestHeader(value = "secret") String secret, @PathVariable("appId") String appId) {
		if (secret.equals(this.secret)) {
			ScalableApp app = appManager.get(appId);
			if (app != null) {
				ResponseApplication responseApp = null;
				try {
					app.acquire();
					app.setLearningStartTime(System.currentTimeMillis());
					appManager.updateInDatabase(app);
					responseApp = ScalableAppService.getSerializationObjectWithoutLock(app);
					log.info("Reset learning start time for "+app.getIdentifierStringForLogs());
				} catch (InterruptedException ex) {  
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{ \"error\" : \"Request was interrupted.\" }");
				}
				app.release();
				return new ResponseEntity<ResponseApplication>(responseApp, HttpStatus.OK);
			}
			return ResponseEntity.status(HttpStatus.GONE).body("{}");
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
	}
	
	/**
	 * Handles incoming requests to update the name of a resource by requesting it from the scaling engine.
	 * @param secret secret {@code String} to authorize with
	 * @return the response in form of a {@code ResponseEntity}
	 */
	@RequestMapping(value  = "/bindings/{bindingId}/updateName", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateResourceName(@RequestHeader(value = "secret") String secret, @PathVariable("bindingId") String bindingId) {
		if (secret.equals(this.secret)) {
			ScalableApp app = appManager.get(bindingId);
			if (app != null) {
				ResponseApplication responseApp = null;
				try {
					app.acquire();
					String resourceName = ScalableAppService.getNameForScalableApp(app.getBinding(), httpWrapper);
					if (resourceName != null && !resourceName.isEmpty()) {
						log.info("Updating resource name of " + app.getBinding().getIdentifierStringForLogs() + " to '" + resourceName + "'.");
						app.getBinding().setResourceName(resourceName);
						appManager.updateInDatabase(app);
						log.debug("Updated app " + app.getBinding().getIdentifierStringForLogs() + " in the database.");
					} else {
						log.info("Could not update resource name of " + app.getBinding().getIdentifierStringForLogs() + ", because the retrieved name is empty or null.");
						app.release();
						return new ResponseEntity<String>("{\"message\" : \"Could not update the name. This might be caused by corrupt binding information"
								+ " or the application is not findable by the scaling engine. \"}",HttpStatus.NOT_FOUND);
					}
					responseApp = ScalableAppService.getSerializationObjectWithoutLock(app);
				} catch (InterruptedException ex) {
					return new ResponseEntity<String>("{ \"error\" : \"Request was interrupted.\" }",HttpStatus.INTERNAL_SERVER_ERROR);
				}
				app.release();
				return new ResponseEntity<ResponseApplication>(responseApp, HttpStatus.OK);
			}
			return new ResponseEntity<String>("{}",HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>("{}",HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler({LimitException.class, InvalidPolicyException.class, SpecialCharacterException.class, TimeException.class, InvalidWorkingSetException.class, InvalidBindingException.class})
	public ResponseEntity<ErrorMessage> handleInputException(Exception ex) {
		log.warn(ex.getClass().getSimpleName(), ex);
		return processErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(InterruptedException.class)
	public ResponseEntity<ErrorMessage> handleException(InterruptedException ex) {
		log.warn("Acquiring a mutex was interrupted.",ex);
		return processErrorResponse(ex.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
