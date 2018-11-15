package de.evoila.cf.autoscaler.core.model.repositories;

import de.evoila.cf.autoscaler.api.binding.Binding;
import de.evoila.cf.autoscaler.core.model.AppBlueprint;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Interface for connection with MongoDB
 * @author Marius Berger
 *
 */
public interface AppBlueprintRepository extends MongoRepository<AppBlueprint, String> {

	/**
	 * Returns the {@code AppBlueprint} from the database which responds the given {@code appId}
	 * @param binding binding information of the {@code ScalableApp} to look for
	 * @return {@code AppBlueprint} found in the database
	 */
	AppBlueprint findByBinding(Binding binding);
	
}
