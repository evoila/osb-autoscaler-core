package de.cf.autoscaler.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.cf.autoscaler.api.binding.InvalidBindingException;
import de.cf.autoscaler.api.update.UpdateRequest;
import de.cf.autoscaler.applications.ScalableApp;
import de.cf.autoscaler.applications.ScalableAppService;
import de.cf.autoscaler.exception.ErrorMessage;
import de.cf.autoscaler.exception.InvalidPolicyException;
import de.cf.autoscaler.exception.InvalidWorkingSetException;
import de.cf.autoscaler.exception.LimitException;
import de.cf.autoscaler.exception.SpecialCharacterException;
import de.cf.autoscaler.exception.TimeException;
import de.cf.autoscaler.http.response.ResponseApplication;
import de.cf.autoscaler.manager.ScalableAppManager;

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
