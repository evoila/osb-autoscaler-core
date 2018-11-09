package de.evoila.cf.autoscaler.core.manager;

import de.evoila.cf.autoscaler.api.binding.Binding;
import de.evoila.cf.autoscaler.api.binding.InvalidBindingException;
import de.evoila.cf.autoscaler.core.applications.AppBlueprint;
import de.evoila.cf.autoscaler.core.applications.ScalableApp;
import de.evoila.cf.autoscaler.core.applications.ScalableAppService;
import de.evoila.cf.autoscaler.core.data.mongodb.AppBlueprintRepository;
import de.evoila.cf.autoscaler.core.exception.*;
import de.evoila.cf.autoscaler.core.kafka.producer.ProtobufProducer;
import de.evoila.cf.autoscaler.core.properties.AutoscalerPropertiesBean;
import de.evoila.cf.autoscaler.core.properties.DefaultValueBean;
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean;
import de.evoila.cf.autoscaler.kafka.model.BindingInformation;
import de.evoila.cf.autoscaler.kafka.producer.KafkaJsonProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Manager for adding, deleting and getting {@linkplain ScalableApp}.
 * @author Marius Berger
 *
 */
@Service
public class ScalableAppManager {

	/**
	 * Logger of this class.
	 */
	private Logger log = LoggerFactory.getLogger(ScalableAppManager.class);
	
	/**
	 * Property Bean for Kafka Settings.
	 */
	private KafkaPropertiesBean kafkaProperties;
	
	/**
	 * Property Bean for default values.
	 */
	@Autowired
	private DefaultValueBean defaults;
	
	/**
	 * Properties for settings for the Autoscaler.
	 */
	@Autowired
	private AutoscalerPropertiesBean autoscalerProperties;
	
	/**
	 * Repository for connection to the database.
	 */
	@Autowired
	private AppBlueprintRepository appRepository;
	
	/**
	 * Producer to publish protobuf messages on Kafka.
	 */
	@Autowired
	private ProtobufProducer protobufProducer;
	
	/**
	 * Producer to publish JSON messages on Kafka.
	 * This will be the case when binding or unbinding.
	 */
	@Autowired
	private KafkaJsonProducer jsonProducer;
	
	/**
	 * Internal list of all {@linkplain ScalableApp} objects bound to the Autoscaler.
	 */
	private List<ScalableApp> apps;
	
	/**
	 * Basic constructor for setting up the manager.
	 */
	public ScalableAppManager(KafkaPropertiesBean kafkaProperties) {
		this.kafkaProperties = kafkaProperties;
		apps = new ArrayList<ScalableApp>();
	}

	/**
	 * Connects to the database and loads the stored {@linkplain AppBlueprint} object
	 * to create {@linkplain ScalableApp} objects.
	 * This method will not overwrite existing objects with equal IDs or remove the current {@linkplain ScalableApp} objects.
	 */
	@PostConstruct
	public void loadFromDatabase() {
		log.info("Importing from database ...");
		List<AppBlueprint> appsFromDb = appRepository.findAll();
		
		for (int i = 0; i < appsFromDb.size(); i++) {		
			AppBlueprint bp = appsFromDb.get(i);
		
			try {
				if (ScalableAppService.isValid(bp)) {
					ScalableApp app = new ScalableApp(bp, kafkaProperties, autoscalerProperties, protobufProducer);
					if (!contains(app)) {
						add(app,true);
						log.info("Imported app from database: "+app.getIdentifierStringForLogs());
					} else {
						log.debug("Found an already existing binding with the same ID while trying to import " + bp.getBinding().getIdentifierStringForLogs());
					}
				} else {
					log.error("Found an invalid AppBlueprint while trying to synch with the database: " + bp.getBinding().getIdentifierStringForLogs() + " : could not determine the cause.");
				}
			} catch (LimitException | InvalidPolicyException | TimeException
                    | InvalidWorkingSetException | InvalidBindingException ex) {
				log.error("Found an invalid AppBlueprint while trying to synch with the database: "
							+bp.getBinding().getIdentifierStringForLogs()+" : "+ex.getMessage());
			}
		}
		log.info("Imports from database complete.");
	}
	
	/**
	 * Adds a {@linkplain ScalableApp} to the list and the database, if its ID is not already taken.
	 * @param app {@linkplain ScalableApp} to add
	 * @param loadedFromDatabase boolean indicator to signal, whether this ScalableApp was loaded from the database.
	 * @return true if the application was successfully added
	 */
	public boolean add(ScalableApp app, boolean loadedFromDatabase) {
		if (!contains(app)) {
			apps.add(app);
			String action = BindingInformation.ACTION_LOAD;
			log.debug("Added following app to ScalableAppManager: "+app.getIdentifierStringForLogs());
			if (!loadedFromDatabase) {
				appRepository.save(app.getCopyOfBlueprint());
				action = BindingInformation.ACTION_BIND;
				log.info("Bound following app: "+app.getIdentifierStringForLogs());
			}
			jsonProducer.produceKafkaMessage(kafkaProperties.getBindingTopic(), new BindingInformation(app.getBinding().getResourceId(),
					action, BindingInformation.SOURCE_AUTOSCALER));
			return true;
		}
		return false;
	}
	
	/**
	 * Removes a {@linkplain ScalableApp} from the list and the database, if an application with the same ID is found.
	 * @param app {@linkplain ScalableApp} to remove
	 * @return true if the application was successfully removed
	 */
	public boolean remove(ScalableApp app) {
		if (contains(app)) {
			apps.remove(app);
			appRepository.deleteById(app.getBinding().getId());
			jsonProducer.produceKafkaMessage(kafkaProperties.getBindingTopic(), new BindingInformation(app.getBinding().getResourceId(),
					BindingInformation.ACTION_UNBIND, BindingInformation.SOURCE_AUTOSCALER));
			log.info("Removed following app from ScalableAppManager: "+app.getIdentifierStringForLogs());
			return true;
		}
		return false;
	}
	
	/**
	 * See {@linkplain #remove(ScalableApp)}
	 * @param bindingId id of the application to remove
	 * @return true if an application with the given binding id was successfully removed
	 */
	public boolean remove(String bindingId) {
		return remove(get(bindingId));
	}
	
	/**
	 * See {@linkplain #contains(ScalableApp)}
	 * @param bindingId ID of the binding to look for
	 * @return true if the list contains an application with a binding id equal to the given one 
	 */
	public boolean contains(String bindingId) {
		for (int i = 0; i < apps.size(); i++) {
			if (apps.get(i).getBinding().getId().equals(bindingId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * See {@linkplain #contains(ScalableApp)}
	 * @param resourceId ID of the resource to look for
	 * @return true if the list contains an application with a resource id equal to the given one 
	 */
	public boolean containsResourceId(String resourceId) {
		for (int i = 0; i < apps.size(); i++) {
			if (apps.get(i).getBinding().getResourceId().equals(resourceId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Looks for an equal {@linkplain ScalableApp} in the list.
	 * @param app {@linkplain ScalableApp} to look for.
	 * @return true if the list contains an application equal to the given one
	 */
	public boolean contains(ScalableApp app) {
		return apps.contains(app);
	}
	
	/**
	 * Returns a {@linkplain ScalableApp} if a matching one was found
	 * @param bindingId ID of the application to look for
	 * @return {@linkplain ScalableApp} that matches the search criteria
	 */
	public ScalableApp get(String bindingId) {
		for (int i = 0 ; i < apps.size(); i++) {
			if (apps.get(i).getBinding().getId().equals(bindingId)) {
				return apps.get(i);
			}
		}
		return null;
	}
	
	public ScalableApp getByResourceId(String resourceId) {
		for (int i = 0 ; i < apps.size(); i++) {
			if (apps.get(i).getBinding().getResourceId().equals(resourceId)) {
				return apps.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Returns a default {@linkplain ScalableApp} with the given binding information.
	 * @param binding binding information for the new {@linkplain ScalableApp}
	 * @return the new {@linkplain ScalableApp}
	 */
	public ScalableApp getNewApp(Binding binding) {
		return new ScalableApp(binding, kafkaProperties, defaults, autoscalerProperties, protobufProducer);
	}
	
	/**
	 * Returns the count of managed applications.
	 * @return count of managed applications.
	 */
	public int size() {
		return apps.size();
	}
	
	/**
	 * Creates and returns a new {@linkplain List} with the managed applications as a flat copy.
	 * @return flat copy of the managed applications as a {@linkplain List}
	 */
	public List<ScalableApp> getFlatCopyOfApps() {
		return new LinkedList<ScalableApp>(apps);
	}
	
	/**
	 * Creates and returns a {@linkplain List} with the identifier Strings of all managed applications.
	 * @return {@linkplain List} with the identifier Strings of all managed applications.
	 */
	public List<String> getListOfIdentifierStrings() {
		ScalableApp current;
		List<String> list = new LinkedList<String>();
		for (int i = 0; i < apps.size(); i++) {
			current = apps.get(i);
			try {
				current.acquire();
				list.add(current.getIdentifierStringForLogs());
			} catch (InterruptedException ex) {}
			current.release();
		}
		return list;
	}
	
	/**
	 * Creates and returns a {@linkplain List} with the basic information Strings of all managed applications.
	 * @return {@linkplain List} with the basic information Strings of all managed applications.
	 */
	public List<Binding> getListOfBindings() {
		ScalableApp current;
		List<Binding> list = new LinkedList<Binding>();
		
		for (int i = 0; i < apps.size(); i++) {
			current = apps.get(i);
			try {
				current.acquire();
				list.add(current.getBinding());
			} catch (InterruptedException ex) {}
			current.release();
		}
		
		return list;
	}

	/**
	 * Updates the {@linkplain AppBlueprint} of a {@linkplain ScalableApp} in the database.
	 * @param app {@linkplain ScalableApp} to update.
	 */
	public void updateInDatabase(ScalableApp app) {
		appRepository.save(app.getCopyOfBlueprint());
	}
}
