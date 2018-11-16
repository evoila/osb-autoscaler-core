package de.evoila.cf.autoscaler.core.controller;

import de.evoila.cf.autoscaler.api.binding.InvalidBindingException;
import de.evoila.cf.autoscaler.api.update.UpdateRequest;
import de.evoila.cf.autoscaler.core.controller.response.ResponseApplication;
import de.evoila.cf.autoscaler.core.controller.scaling.AutoscalerScalingEngineService;
import de.evoila.cf.autoscaler.core.exception.*;
import de.evoila.cf.autoscaler.core.manager.ScalableAppManager;
import de.evoila.cf.autoscaler.core.model.ScalableApp;
import de.evoila.cf.autoscaler.core.model.ScalableAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to handle requests to manage existing {@code ScalableApps}.
 *
 * @author Marius Berger
 * @see ScalableApp
 */
@Controller
public class ManagingController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(ManagingController.class);

    @Autowired
    private ScalableAppManager scalableAppManager;

    @Autowired
    private AutoscalerScalingEngineService autoscalerScalingEngineService;

    /**
     * Handles incoming request to update an application.
     *
     * @param appId       ID of the application
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
    @PatchMapping(value = "/manage/{appId}")
    public ResponseEntity patch(@PathVariable("appId") String appId, @RequestBody UpdateRequest requestBody) throws LimitException,
            InvalidPolicyException, SpecialCharacterException, TimeException, InvalidWorkingSetException, InvalidBindingException {

        ScalableApp app = scalableAppManager.get(appId);

        if (app == null) {
            return new ResponseEntity(HttpStatus.GONE);
        }

        if (requestBody.getAllSetElements().size() == 0) {
            return new ResponseEntity("{ \"error\" : \"Not one valid policy was found.\"}", HttpStatus.BAD_REQUEST);
        }

        ResponseApplication responseApp;
        try {
            app.acquire();
            app.update(requestBody);
            scalableAppManager.updateInDatabase(app);
            responseApp = ScalableAppService.getSerializationObjectWithoutLock(app);
        } catch (InterruptedException ex) {
            return new ResponseEntity("{ \"error\" : \"Request was interrupted.\" }", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        app.release();
        return new ResponseEntity<>(responseApp, HttpStatus.OK);

    }

    /**
     * Handles incoming request to get information about an application.
     *
     * @param appId  ID of the application
     * @return the response in form of a {@code ResponseEntity}
     * @see ResponseEntity
     */
    @GetMapping(value = "/manage/{appId}")
    public ResponseEntity get(@PathVariable("appId") String appId) {

        ScalableApp app = scalableAppManager.get(appId);
        if (app != null) {
            ResponseApplication responseApp = ScalableAppService.getSerializationObjectWithLock(app);
            if (responseApp == null)
                return new ResponseEntity("{ \"error\" : \"Request was interrupted.\" }", HttpStatus.INTERNAL_SERVER_ERROR);

            return new ResponseEntity(responseApp, HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.GONE);

    }

    /**
     * Handles incoming requests to reset the quotient of an application.
     *
     * @param appId  ID of the application
     * @return the response in form of a {@code ResponseEntity}
     */
    @PatchMapping(value = "/manage/{appId}/resetQuotient")
    public ResponseEntity quotient(@PathVariable("appId") String appId) {

        ScalableApp app = scalableAppManager.get(appId);
        if (app != null) {
            ResponseApplication responseApp = null;
            try {
                app.acquire();
                app.getRequest().resetQuotient();
                scalableAppManager.updateInDatabase(app);
                responseApp = ScalableAppService.getSerializationObjectWithoutLock(app);
                log.info("Reset quotient for " + app.getIdentifierStringForLogs());
            } catch (InterruptedException e) {
            }
            app.release();
            return new ResponseEntity(responseApp, HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.GONE);

    }

    /**
     * Handles incoming requests to reset the learning start time of an application.
     *
     * @param appId  ID of the application
     * @return the response in form of a {@code ResponseEntity}
     */
    @PatchMapping(value = "/manage/{appId}/resetLST")
    public ResponseEntity reset(@PathVariable("appId") String appId) {
        ScalableApp app = scalableAppManager.get(appId);
        if (app != null) {
            ResponseApplication responseApp;
            try {
                app.acquire();
                app.setLearningStartTime(System.currentTimeMillis());
                scalableAppManager.updateInDatabase(app);
                responseApp = ScalableAppService.getSerializationObjectWithoutLock(app);
                log.info("Reset learning start time for " + app.getIdentifierStringForLogs());
            } catch (InterruptedException ex) {
                return new ResponseEntity("{ \"error\" : \"Request was interrupted.\" }", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            app.release();
            return new ResponseEntity(responseApp, HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.GONE);
    }

    /**
     * Handles incoming requests to update the name of a resource by requesting it from the scaling engine.
     *
     * @return the response in form of a {@code ResponseEntity}
     */
    @PatchMapping(value = "/manage/{bindingId}/updateName")
    public ResponseEntity<?> updateName(@PathVariable("bindingId") String bindingId) {
        ScalableApp app = scalableAppManager.get(bindingId);
        if (app != null) {
            ResponseApplication responseApp;
            try {
                app.acquire();
                String resourceName = ScalableAppService.getNameForScalableApp(app.getBinding(), autoscalerScalingEngineService);
                if (resourceName != null && !resourceName.isEmpty()) {
                    log.info("Updating resource name of " + app.getBinding().getIdentifierStringForLogs() + " to '" + resourceName + "'.");
                    app.getBinding().setResourceName(resourceName);
                } else {
                    log.info("Could not update resource name of " + app.getBinding().getIdentifierStringForLogs() + ", because the retrieved name is empty or null.");
                    app.release();
                    return new ResponseEntity("{\"message\" : \"Could not update the name. This might be caused by corrupt binding information"
                            + " or the application is not findable by the scaling engine. \"}", HttpStatus.NOT_FOUND);
                }
                responseApp = ScalableAppService.getSerializationObjectWithoutLock(app);
            } catch (InterruptedException ex) {
                return new ResponseEntity("{ \"error\" : \"Request was interrupted.\" }", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            app.release();
            return new ResponseEntity<>(responseApp, HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({LimitException.class, InvalidPolicyException.class, SpecialCharacterException.class, TimeException.class,
            InvalidWorkingSetException.class, InvalidBindingException.class})
    public ResponseEntity<ErrorMessage> handleInputException(Exception ex) {
        log.warn(ex.getClass().getSimpleName(), ex);
        return processErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<ErrorMessage> handleException(InterruptedException ex) {
        log.warn("Acquiring a mutex was interrupted.", ex);
        return processErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
