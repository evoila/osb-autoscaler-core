package de.evoila.cf.autoscaler.core.controller;

import de.evoila.cf.autoscaler.api.binding.Binding;
import de.evoila.cf.autoscaler.core.model.ScalableApp;
import de.evoila.cf.autoscaler.core.model.ScalableAppService;
import de.evoila.cf.autoscaler.core.controller.response.ResponseApplication;
import de.evoila.cf.autoscaler.core.controller.scaling.AutoscalerScalingEngineService;
import de.evoila.cf.autoscaler.core.manager.ScalableAppManager;
import de.evoila.cf.autoscaler.core.properties.AutoscalerPropertiesBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Controller to handle incoming bindings and unbindings.
 *
 * @author Marius Berger
 */
@Controller
public class BindingController extends BaseController {

    /**
     * {@code ScalableAppManager} to get, bind or unbind model.
     */
    @Autowired
    private ScalableAppManager scalableAppManager;

    @Autowired
    private AutoscalerPropertiesBean autoscalerPropertiesBean;

    @Autowired
    private AutoscalerScalingEngineService autoscalerScalingEngineService;

    /**
     * Handles incoming request to get information about service instance specific existing bindings.
     *
     * @param secret    {@code String} to authorize with
     * @param serviceId {@code String} of the service instance you want to get the bindings of
     * @return the response in form of a {@code ResponseEntity}
     */
    @RequestMapping(value = "/bindings/serviceInstance/{serviceId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> infosAboutSpecificBindings(@RequestHeader(value = "secret") String secret,
                                                        @PathVariable("serviceId") String serviceId) {
        List<Binding> bindings = new LinkedList<>();

        for (Binding binding : scalableAppManager.getListOfBindings()) {
            if (binding.getServiceId().equals(serviceId)) {
                bindings.add(binding);
            }
        }

        Map<String, List<Binding>> map = new HashMap<>();
        map.put("bindings", bindings);
        return new ResponseEntity<>(map, HttpStatus.OK);

    }

    /**
     * Handles incoming requests to bind a new application.
     *
     * @param secret  {@code String} to authorize with
     * @param binding information about the binding via a {@linkplain Binding} object
     * @return the response in form of a {@code ResponseEntity} with a related statuscode and either information about the new application or an other JSON String
     * @see ResponseEntity
     */
    @RequestMapping(value = "/bindings", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> bindApp(@RequestHeader(value = "secret") String secret, @RequestBody Binding binding) {

        if (binding.isValidWithReason() != null) {
            return processErrorResponse(binding.isValidWithReason(), HttpStatus.BAD_REQUEST);
        }

        ScalableApp newApp = scalableAppManager.getNewApp(binding);
        if (scalableAppManager.contains(binding.getId())) {
            if (scalableAppManager.get(binding.getId()).getBinding().equals(newApp.getBinding())) {
                return ResponseEntity.status(HttpStatus.OK).body("{}");
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    "{ \"error\" : \"An other binding was found with the same id.\" }");
        }

        if (autoscalerPropertiesBean.isGetAppNameFromScalingEngineAtBinding()) {
            newApp.getBinding().setResourceName(ScalableAppService.getNameForScalableApp(newApp.getBinding(), autoscalerScalingEngineService));
        }

        ResponseApplication responseApp = ScalableAppService.getSerializationObjectWithLock(newApp);
        scalableAppManager.add(newApp, false);
        return new ResponseEntity<>(responseApp, HttpStatus.CREATED);

    }

    /**
     * Handles incoming requests to unbind a existing application.
     *
     * @param secret {@code String} to authorize with
     * @param appId  ID of the application
     * @return the response in form of a {@code ResponseEntity} with an empty JSON String and a related statuscode
     * @see ResponseEntity
     */
    @RequestMapping(value = "/bindings/{appId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> unbindApp(@RequestHeader(value = "secret") String secret, @PathVariable("appId") String appId) {
        if (scalableAppManager.contains(appId)) {
            scalableAppManager.remove(appId);
            return ResponseEntity.status(HttpStatus.OK).body("{}");
        }
        return ResponseEntity.status(HttpStatus.GONE).body("{}");

    }

}
