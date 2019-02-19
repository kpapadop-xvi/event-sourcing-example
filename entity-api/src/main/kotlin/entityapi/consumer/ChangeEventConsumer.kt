package entityapi.consumer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import entityapi.consumer.KafkaConsumerConfiguration.Companion.USER_ENTITY_CHANGE_TOPIC
import entityapi.consumer.eventmodel.ChangeEvent
import entityapi.consumer.eventmodel.Operation.*
import entityapi.repository.UserRepository
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ChangeEventConsumer(
        private val users: UserRepository,
        meters: MeterRegistry
) {
    private val consumedMsgCounter = meters.counter("change-events.consumed.total")
    private val successfulMsgCounter = meters.counter("change-events.consumed.successful")
    private val errorsMsgCounter = meters.counter("change-events.consumed.errors")

    private val objectMapper = jacksonObjectMapper()

    @KafkaListener(clientIdPrefix = "change-event-listener", topics = [USER_ENTITY_CHANGE_TOPIC])
    fun consumeChangeEvent(message: ConsumerRecord<String, String>) {
        try {
            val userId = message.key()
            val changeEventRaw = message.value()

            log.info("Received raw: {}", changeEventRaw)

            // Manual parsing is required since the Spring JsonDeserializer
            // cannot be used out-of-the-box with Kotlin
            val changeEvent = objectMapper.readValue<ChangeEvent>(changeEventRaw)

            val operation = changeEvent.operation
            val updatedFields = changeEvent.fields

            when (operation) {
                CREATION, UPDATE -> updatedFields?.forEach { field ->
                    users.updateField(userId, field.key, field.value)
                }
                DELETION -> users.delete(userId)
            }

            successfulMsgCounter.increment()
        } catch (e: Exception) {
            errorsMsgCounter.increment()
            throw e
        } finally {
            consumedMsgCounter.increment()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ChangeEventConsumer::class.java)
    }
}