package de.cf.autoscaler.data.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.cf.autoscaler.api.binding.Binding;
import de.cf.autoscaler.applications.AppBlueprint;

/**
 * Interface for connection with MongoDB
 * @author Marius Berger
 *
 */
public interface AppBlueprintRepository  extends MongoRepository<AppBlueprint, String> {

	/**
	 * Returns the {@code AppBlueprint} from the database which responds the given {@code appId}
	 * @param binding binding information of the {@code ScalableApp} to look for
	 * @return {@code AppBlueprint} found in the database
	 */
	public AppBlueprint findByBinding(Binding binding);
	
}
