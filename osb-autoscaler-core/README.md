# osb-autoscaler-core #

`osb-autoscaler-core` is a spring boot application and part of the osb-autoscaler-framework. It is the backbone of the framework and is responsible for its logic.

## Requirements ##

- MongoDB for binding persistence
- Apache Kafka as the message broker for communication between the components of the osb-autoscaler-framework

## Dependencies ##
This project uses Apache Maven as build management tool. You should download the following repositories and build them before building the Core.
- [osb-autoscaler-api](https://github.com/evoila/osb-autoscaler-api/)
- [osb-autoscaler-messaging](https://github.com/evoila/osb-autoscaler-messaging/)

## Installation ##
Download this repository, edit the config file according to your setup and build it via Apache Maven. To start the `osb-autoscaler-core` run the created .jar file, there are no parameters needed.

You can run the `KafkaTest.java` unit test to test the connection with your Apache Kafka instance.

*NOTE*: At the moment you might have to run the test twice before completing it successfully, when starting it for the very first time.

## Configuration ##

Remember to create a properties file [here](src/main/resources) with following valid properties. You can use the example file to get an idea of how it could look like.

**Autoscaler Configs**

| Property | Description |
| ------ | ------ |
|  server.port| port of the API |
|  scaler.maxMetricListSize| maximum count of metrics saved at once for a resource |
| scaler.maxMetricAge | maximum for the age of a metric in milliseconds (should not be smaller than a scaling interval which is normaly 30s); metrics older than this value will be ignored and removed |
|scaler.appname.get_from_scaling_engine_at_binding| boolean value, whether to request the resource name directly after creating a new binding (set this to 'false' for testing the autoscaler without a running scaling engine|

**Service Broker Information**

| Property | Description |
| ------ | ------ |
| broker.secret | secret string needed for authorization of the service broker at the API of the Core |

**Kafka Information**

| Property | Description |
| ------ | ------ |
| kafka.host | IP / URL of the Apache Kafka host |
| kafka.port | port of the Apache Kafka host |
| kafka.metric_http_topic | topic name for HTTP metrics |
| kafka.metric_container_topic | topic name for instance / container metrics |
| kafka.metric_application_topic | topic name for application metrics |
| kafka.scaling_topic | topic name for scaling logs |
| kafka.predic_topic | topic name for predictions |
| kafka.binding_topic | topic name for binding logs |
| kafka.producer_groupId | ID of the producer group the Kafka producers join |
| kafka.container_consumer_groupId | ID of the consumer group the container metric Kafka consumers join |
| kafka.http_consumer_groupId | ID of the consumer group the HTTP metric Kafka consumers join |
| kafka.prediction_consumer_groupId | ID of the consumer group the prediction Kafka consumers join |
| kafka.container_consumer_count | number of container metric consumer threads to start |
| kafka.request_consumer_count | number of controller metric consumer threads to start |

**Scaling Engine Information**

| Property | Description |
| ------ | ------ |
| engine.host | IP / URL of the Scaling Engine |
| engine.port | port of the Scaling Engine |
| engine.endpoint.scaling | endpoint for scaling requests |
| engine.secret | secret string for authorization for communication between the Core and the Scaling Engine |

**MongoDB Information**

| Property | Description |
| ------ | ------ |
| spring.data.repositories.host | IP / URL of the repositories host |
| spring.data.repositories.port | port of the repositories host |
| spring.data.repositories.username | username to use when connecting to repositories |
| spring.data.repositories.password | password to use when connection to repositories |
| spring.data.repositories.database | name of the repositories database to use |
| spring.data.repositories.authentication-database | name of the authentication database |

**Default Values**

These default values are given to a newly created binding.

| Property | Description |
| ------ | ------ |
| default.scalingEnabled | boolean - allowed to send scaling requests |
| default.cpuScalingEnabled | boolean - use CPU as a metric for scaling |
| default.ramScalingEnabled | boolean - use RAM as a metric for scaling |
| default.latencyScalingEnabled | boolean -  use latency as a metric for scaling |
| default.quotientScalingEnabled | boolean - use the quotient to dynamicly calculate the new instance count |
| default.predictionScalingEnalbed | boolean - listen to incoming predictions |
| default.scalingIntervalMultiplier | int - number of intervals to pass before checking the resource (1 - 2147483647) |
| default.minInstances | int - minimum number of running instances (1 - maxInstances) |
| default.maxInstances | int - maximum number of running instances (minInstances - 2147483647) |
| default.cooldownTime | long - time in milliseconds to ignore incoming metrics and therefore disabling further scaling after an executed scaling request (0 - 9223372036854775807)|
| default.learningEnabled | boolean - allowed to automatically update to Quotient |
| default.billingIntervalConsidered | boolean - considering the billing interval when scaling (not implemented yet) |
| default.learningTimeMultiplier | int - number of intervals to only use the static scaling approach after enable learning |
| default.thresholdPolicy | string - default policy to use to aggregate metrics (max / min / mean) |
| default.cpuUpperLimit | int - upper threshold for cpu based scaling (cpuLowerLimit+1 - 100) |
| default.cpuLowerLimit | int - lower threshold for cpu based scaling (0 - cpuUpperLimit-1) |
| default.ramUpperLimit | long - upper threshold for ram based scaling (ramLowerLimit+1 - 9223372036854775807) |
| default.ramLowerLimit | long - lower threshold for ram based scaling (0 - ramUpperLimit-1) |
| default.latencyUpperLimit | int - upper threshold for latency based scaling (latencyLowerLimit+1 - 2147483647) |
| default.latencyLowerLimit | int - upper threshold for latency based scaling (0 - latencyUpperLimit-1) |
| default.minQuotient | int - minimum value the quotient has to be (0 - 2147483647) |


## API ##

Every call to the Core needs a secret header with the prearranged secret for authorization for now. You can set the secret of the Core in the config file via the scaler.secret property.

### Overview ###

| Endpoint | Body Class | Description |
| ------ | ------ | ------ |
| POST /bindings | [body](https://github.com/evoila/osb-autoscaler-api/blob/develop/src/main/java/de.evoila.cf.autoscaler.core/api/binding/Binding.java) | bind a new application |
| GET /bindings/{bindingId} | - | get information about a specific binding |
| DELETE /bindings/{bindingId} | - | delete a binding |
| GET /manage/serviceInstance/{serviceId}/bindings | - | Returns all Bindings to a give Service Instance
| PATCH /manage/{appId} | [body](https://github.com/evoila/osb-autoscaler-api/blob/develop/src/main/java/de.evoila.cf.autoscaler.core/api/update/UpdateRequest.java) | update the policies of a binding |
| GET /manage/{appId} | - | Get the policies of an AppId |
| PATCH /manage/{appId}/resetQuotient | - | reset the quotient of a binding to its minimum quotient
| PATCH /manage/{appId}/resetLST | - | reset the learning start time and therefore enabling a new learning process, if learning is enabled |
| PATCH /manage/{appId}/updateName | - |request the descriptive name of the resource on the platform and updates the current value (only used for easy visual identification of bindings) |

For example bodies and responses see [detailed API](detailedApi.md).



## Version ##
v1.2
