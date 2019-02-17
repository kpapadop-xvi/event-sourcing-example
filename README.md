# Event Sourcing Example

## About

This project attempts to provide a simple example of the concept of 
[Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html). The 
implementation is based on the following tech stack:

- [Kotlin 1.3](https://kotlinlang.org/docs/reference/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Apache Kafka](https://kafka.apache.org/)
- [Docker/Docker Compose](https://www.docker.com/get-started)

The build tool of choise is [Gradle](https://gradle.org/)

## Overview

The example demonstrates how event sourcing is used in producing and 
capturing changes in the state of some entities, and then querying 
the current state of those.

It is split in three modules:

- `entity-state-change-producer`. Simulates an event source that changes the
  the state of entities. The events can also imply that a new entity
  is created or an existing one is deleted.
  
- `event-bus`. Essentially an instance of Kafka. Stores all entity state change
  events in the order they have been received.

- `entity-api`. This is the component that provides the current state of an 
  entity, after consuming all of the events related to it.
  