package de.evoila.cf.autoscaler.core.controller;

import de.evoila.cf.autoscaler.api.binding.Binding;
import de.evoila.cf.autoscaler.core.controller.response.ResponseApplication;
import de.evoila.cf.autoscaler.core.controller.scaling.AutoscalerScalingEngineService;
import de.evoila.cf.autoscaler.core.manager.ScalableAppManager;
import de.evoila.cf.autoscaler.core.model.ScalableApp;
import de.evoila.cf.autoscaler.core.model.ScalableAppService;
import de.evoila.cf.autoscaler.core.properties.AutoscalerPropertiesBean;
import org.springframework.http.HttpStatus;
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


    private ScalableAppManager scalableAppManager;

    private AutoscalerPropertiesBean autoscalerPropertiesBean;

    private AutoscalerScalingEngineService autoscalerScalingEngineService;

    public BindingController(ScalableAppManager scalableAppManager, AutoscalerPropertiesBean autoscalerPropertiesBean,
                             AutoscalerScalingEngineService autoscalerScalingEngineService) {
        this.scalableAppManager = scalableAppManager;
        this.autoscalerPropertiesBean = autoscalerPropertiesBean;
        this.autoscalerScalingEngineService = autoscalerScalingEngineService;
    }

    /**
     * Handles incoming request to get information about service instance specific existing bindings.
     *
     * @param serviceId {@code String} of the service instance you want to get the bindings of
     * @return the response in form of a {@code ResponseEntity}
     */
    @GetMapping(value = "/bindings/serviceInstance/{serviceId}")
    public ResponseEntity bindings(@PathVariable("serviceId") String serviceId) {
        List<Binding> bindings = new LinkedList<>();

        for (Binding binding : scalableAppManager.getListOfBindings()) {
            if (binding.getServiceId().equals(serviceId)) {
                bindings.add(binding);
            }
        }

        Map<String, List<Binding>> map = new HashMap<>();
        map.put("bindings", bindings);
        return new ResponseEntity(map, HttpStatus.OK);
    }

    /**
     * Handles incoming requests to bind a new application.
     *
     * @param binding information about the binding via a {@linkplain Binding} object
     * @return the response in form of a {@code ResponseEntity} with a related statuscode and either information about the new application or an other JSON String
     * @see ResponseEntity
     */
    @PostMapping(value = "/bindings")
    public ResponseEntity bind(@RequestBody Binding binding) {

        if (binding.isValidWithReason() != null) {
            return processErrorResponse(binding.isValidWithReason(), HttpStatus.BAD_REQUEST);
        }

        ScalableApp newApp = scalableAppManager.getNewApp(binding);
        if (scalableAppManager.contains(binding.getId())) {
            if (scalableAppManager.get(binding.getId()).getBinding().equals(newApp.getBinding())) {
                return new ResponseEntity(HttpStatus.OK);
            }
            return new ResponseEntity("{ \"error\" : \"An other binding was found with the same id.\" }", HttpStatus.CONFLICT);
        }

        if (autoscalerPropertiesBean.isUpdateAppNameAtBinding()) {
            newApp.getBinding().setResourceName(ScalableAppService.getNameForScalableApp(newApp.getBinding(), autoscalerScalingEngineService));
        }

        ResponseApplication responseApp = ScalableAppService.getSerializationObjectWithLock(newApp);
        scalableAppManager.add(newApp, false);
        return new ResponseEntity(responseApp, HttpStatus.CREATED);
    }

    /**
     * Handles incoming requests to unbind a existing application.
     *
     * @param appId  ID of the application
     * @return the response in form of a {@code ResponseEntity} with an empty JSON String and a related statuscode
     * @see ResponseEntity
     */
    @DeleteMapping(value = "/bindings/{appId}")
    public ResponseEntity<String> unbind(@PathVariable("appId") String appId) {
        if (scalableAppManager.contains(appId)) {
            scalableAppManager.remove(appId);
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.GONE);
    }

}
