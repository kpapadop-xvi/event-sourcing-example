# Event Sourcing Example

## About

This project attempts to provide a simple example of the concept of 
Event Sourcing. The implementation is based on the following tech stack:

- [Kotlin 1.3](https://kotlinlang.org/docs/reference/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Apache Kafka](https://kafka.apache.org/)
- [Docker/Docker Compose](https://www.docker.com/get-started)

The build tool of choise is [Gradle](https://gradle.org/)

## Overview

The example demonstrates how the current state of an entity can be 
reconstructed by "replaying" all of the change events that modify
its fields.

The project is split in three modules:

- `entity-change`. Simulates an event source that changes the the state of
  entities. The events can also imply that a new entity is created or an 
  existing one is deleted.
  
- `entity-api`. This is the component that provides the current state of an 
  entity as a web resource, after consuming all of the events related to it.
 
Two more are added in the docker compositions:

- `event-bus`. Essentially an instance of Kafka. Stores all entity state change
  events in the order they have been received.

- `zookeeper`. Required by Kafka.

## Running the demo

Assuming that the latest Docker and Docker Compose are installed, the demo
can started with the following command:

```bash
docker-compose up
```

After inspecting the port on which the `entity-api` component is listening,

```bash
$ docker-compose ps 
                 Name                               Command               State              Ports            
--------------------------------------------------------------------------------------------------------------
event-sourcing-example_entity-api_1      java -jar /app.jar               Up      0.0.0.0:32865->8080/tcp     
event-sourcing-example_entity-change_1   java -jar /app.jar               Up      0.0.0.0:32864->8080/tcp     
event-sourcing-example_event-bus_1       start-kafka.sh                   Up      0.0.0.0:32863->9092/tcp     
event-sourcing-example_zookeeper_1       /docker-entrypoint.sh zkSe ...   Up      2181/tcp, 2888/tcp, 3888/tcp
```

the `/users` JSON resource can be queried:

```bash
curl http://localhost:32865/users
```

The output should be something like:

```json
[
  {
    "id": "23b637d6-34bf-4944-92e5-dbae1b92a1d3",
    "name": "convenient_poison",
    "stringValue": "vwx",
    "intValue": null,
    "floatValue": null,
    "booleanValue": null
  },
  {
    "id": "60d7ddd9-705e-4fd1-8dac-fbc6acc0f700",
    "name": "portable_echo",
    "stringValue": null,
    "intValue": -32126484,
    "floatValue": null,
    "booleanValue": null
  },
  {
    "id": "763c04ec-af2f-47f5-8e30-df5b81115431",
    "name": "foolish_architecture",
    "stringValue": "stu",
    "intValue": 1810455317,
    "floatValue": null,
    "booleanValue": true
  },
  {
    "id": "7adfd90b-af69-485e-9bb8-fd2a5e40088b",
    "name": "abnormal_script",
    "stringValue": "jkl",
    "intValue": -56940000,
    "floatValue": 0.8843099,
    "booleanValue": null
  },
  {
    "id": "e6113a2c-22f2-423a-b96b-63a13830a48c",
    "name": "abundant_mathematics",
    "stringValue": "jkl",
    "intValue": null,
    "floatValue": 0.72102016,
    "booleanValue": null
  },
  {
    "id": "e91fc82c-7874-4c60-977f-725792adb64a",
    "name": "stable_pneumonia",
    "stringValue": null,
    "intValue": null,
    "floatValue": 0.33895004,
    "booleanValue": true
  },
  {
    "id": "f0a26cfd-cbcf-4e63-bda4-b7694e658e45",
    "name": "liquid_coalition",
    "stringValue": null,
    "intValue": 781488229,
    "floatValue": 0.18277454,
    "booleanValue": true
  }
]
```

The IDs of the user objects will remain the same (unless the event implies 
that the object will be deleted), but the rest of the fields will be modified
constantly.

A single user object can be view like this:

```bash
curl http://localhost:32865/users/f0a26cfd-cbcf-4e63-bda4-b7694e658e45
```

```json
{
    "id": "e91fc82c-7874-4c60-977f-725792adb64a",
    "name": "arbitrary_youth",
    "stringValue": "def",
    "intValue": -1797764096,
    "floatValue": 0.644067,
    "booleanValue": true
}
```

If the `entity-change` service is stopped:

```bash
docker-compose stop event-change
```

the objects won't be modified anymore.

The `entity-api` is built to keep those objects in memory. If the service 
should go down, the information is lost. However, since it is configured
to consume all the events from the beginning, calling the above `curl` 
command, while the `entity-change` is stopped, will return the same 
result as above eventually (once all events are consumed).


## Cleaning up

With the following command, all services will be stopped and all created and 
downloaded images will be removed.

```bash
docker-compose down -v --rmi all
```