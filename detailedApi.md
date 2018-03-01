# Detailed API #

## Get information about all bindings ##

Receive a list of all current bindings with basic information about every single one.

### Request ###

```GET /bindings```

Example body:
```
No body needed / possible.
```

### Response ###

| Code | Description |
| ----- | ----- |
| 200 OK | successful request with a list of bindings with basic information |
| 401 UNAUTHORIZED | unauthorized request, might be caused by wrong secret string. |


Example body for 200:
```json
{
    "bindings": [
        {
            "id": "exampleBinding",
            "resourceId": "1234567890",
            "scalerId": "0",
            "creationTime": 1511429355735,
            "context": {
                "platform": "cloudfoundry",
                "space_guid": "example_space",
                "organization_guid": "example_org"
            }
        },
        {
            "id": "anotherExampleBinding",
            "resourceId": "0987654321",
            "scalerId": "0",
            "creationTime": 1511952821309,
            "context": {
                "platform": "cloudfoundry",
                "space_guid": "example_space",
                "organization_guid": "example_org"
            }
        }
    ]
}
```
## Create a new binding ##

Creates a new binding based on the given parameters and the default values of the Core.

### Request ###

```POST /bindings```

You can use the [body class](https://github.com/evoila/osb-autoscaler-api/blob/develop/src/main/java/de/cf/autoscaler/api/binding/Binding.java) in the api repository for easy creation of a body.

Example body:
```
{
	"id" : "exampleBinding",
	"resourceId" : "0123456789",
	"scalerId" : "0",
	"context" : {
		"platform" : "cloudfoundry",
		"organization_guid" : "example_org",
		"space_guid" : "example_space"
	}
}
```

### Response ###

| Code | Description |
| ----- | ----- |
| 200 OK | found an identic binding |
| 201 CREATED | successfully created a new binding |
| 400 BAD_REQUEST | invalid or empty parameters or body |
| 401 UNAUTHORIZED | unauthorized request, might be caused by wrong secret string. |
| 409 CONFLICT | ID is already in use and existing binding differs from given parameters |

Example body for 201:
```json
{
    "binding": {
        "id": "exampleBinding",
        "resourceId": "0123456789",
        "scalerId": "0",
        "creationTime": 1512050206563,
        "context": {
            "platform": "cloudfoundry",
            "space_guid": "example_space",
            "organization_guid": "example_org"
        }
    },
    "scaling": {
        "scalingEnabled": true,
        "predictionScalingEnabled": false,
        "billingIntervalEnabled": false,
        "scalingIntervalMultiplier": 1,
        "currentIntervalState": 0,
        "minInstances": 1,
        "maxInstances": 2147483647,
        "cooldownTime": 30000
    },
    "cpu": {
        "upperLimit": 100,
        "lowerLimit": 0,
        "thresholdPolicy": "mean",
        "cpuScalingEnabled": false
    },
    "ram": {
        "upperLimit": 2147483647,
        "lowerLimit": 0,
        "thresholdPolicy": "mean",
        "ramScalingEnabled": false
    },
    "latency": {
        "upperLimit": 2147483647,
        "lowerLimit": 0,
        "thresholdPolicy": "mean",
        "latencyScalingEnabled": false
    },
    "requests": {
        "quotient": 0,
        "minQuotient": 0,
        "thresholdPolicy": "mean",
        "quotientScalingEnabled": false
    },
    "learning": {
        "learningEnabled": false,
        "learningTimeMultiplier": 1,
        "learningStartTime": 0,
        "learningStartTimeAsString": "Thu Jan 01 00:00:00 UTC 1970"
    }
}
```

## Get information about a specific binding ##

Gathers all available information about a binding.

### Request ###

```GET /bindings/{bindingId}```

Example body:
```
No body needed / possible.
```

### Response ###

| Code | Description |
| ----- | ----- |
| 200 OK | successful request with detailed information about the binding |
| 401 UNAUTHORIZED | unauthorized request, might be caused by wrong secret string. |
| 410 GONE | no binding found with the given id |

Example body for 200:
```json
See "create new binding" example body.
```

## Delete a binding ##

Deletes a binding matching the given ID from the Core.

### Request ###

```DELETE /bindings/{bindingId}```

Example body:
```json
No body needed.
```

### Response ###

| Code | Description |
| ----- | ----- |
| 200 OK | successfully deleted the binding |
| 401 UNAUTHORIZED | unauthorized request, might be caused by wrong secret string. |
| 410 GONE | no binding found with the given id |

Example body for 200:
```json
{}
```

## Update policies of a binding ##

Updates the policies of a binding with the given parameters. Updating some of the policies have further consequences:
- setting learning enabled from false to true - reset the learning start time
- setting the minQuotient - setting the quotient to the minimum quotient, if it is smaller

### Request ###

```PATCH /bindings/{bindingId}```

Example body:
```json
{ 
	"scaling" : {
		"scalingEnabled" : true,
		"predictionScalingEnabled" : false,
		"billingIntervalConsidered" : false,
		"scalingIntervalMultiplier" : 1,
		"minInstances" : 1,
		"maxInstances" : 35,
		"cooldownTime" : 10000
	},
	"cpu" : {
		"thresholdPolicy" : "mean",
		"upperLimit" : 90,
		"lowerLimit" : 50,
		"cpuScalingEnabled" : true
	},
	"ram" : {
		"thresholdPolicy" : "mean",
		"upperLimit" : 734003200,
		"lowerLimit" : 536870912,
		"ramScalingEnabled" : true
	},
	"latency" : {
		"thresholdPolicy" : "mean",
		"latencyScalingEnabled" : false,
		"upperLimit" : 5000,
		"lowerLimit" : 25
	},
	"requests" : {
		"thresholdPolicy" : "mean",
		"quotientScalingEnabled" : true,
		"minRequestQuotient" : 80
	},
	"learning" : {
		"learningEnabled" : false,
		"learningTimeMultiplier" : 1
	}
}	
```

### Response ###

| Code | Description |
| ----- | ----- |
| 200 OK | successfully upated the binding |
| 400 BAD_REQUEST | invalid parameter names or policies |
| 401 UNAUTHORIZED | unauthorized request, might be caused by wrong secret string. |
| 410 GONE | no binding found with the given id |

Example body for 200:
```json
See "create new binding" example body.
```

## Reset the quotient ##

Resets the quotient of a binding to its minimum quotient.

### Request ###

```PATCH /bindings/{bindingId}/resetQuotient```

Example body:
```json
No body needed.
```

### Response ###

| Code | Description |
| ----- | ----- |
| 200 OK | successfully resetted the quotient of the binding |
| 401 UNAUTHORIZED | unauthorized request, might be caused by wrong secret string. |
| 410 GONE | no binding found with the given id |

Example body for 200:
```json
See "create new binding" example body.
```

## Reset the learning start time ##

Resets the learning start time of a binding to enable a new learning process. 

### Request ###

```PATCH /bindings/{bindingId}/resetLST```

Example body:
```json
No body needed.
```

### Response ###

| Code | Description |
| ----- | ----- |
| 200 OK | successfully resetted the learning start time of the binding |
| 401 UNAUTHORIZED | unauthorized request, might be caused by wrong secret string. |
| 410 GONE | no binding found with the given id |

Example body for 200:
```json
See "create new binding" example body.
```

## Update the resource name ##

Requests the descriptive resource name the platform has for the resource behind the binding and updates the current value.

### Request ###

```PATCH /bindings/{bindingId}/updateName```

Example body:
```json
No body needed.
```

### Response ###

| Code | Description |
| ----- | ----- |
| 200 OK | successfully contacted the scaling engine and updated the name |
| 401 UNAUTHORIZED | unauthorized request, might be caused by wrong secret string. |
| 410 GONE | no binding found with the given id |

Example body for 200:
```json
See "create new binding" example body.
```