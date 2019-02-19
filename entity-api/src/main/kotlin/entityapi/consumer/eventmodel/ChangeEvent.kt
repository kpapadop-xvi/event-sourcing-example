package entityapi.consumer.eventmodel

data class ChangeEvent(
        val operation: Operation,
        val fields: Map<String, Any?>?
)