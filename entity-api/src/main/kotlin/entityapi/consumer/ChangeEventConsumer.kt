package entityapi.consumer

import entityapi.consumer.KafkaConsumerConfiguration.Companion.USER_ENTITY_CHANGE_TOPIC
import entityapi.repository.UserRepository
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders.RECEIVED_MESSAGE_KEY
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class ChangeEventConsumer(
        private val users: UserRepository,
        meters: MeterRegistry
) {

    private val consumedMsgCounter = meters.counter("change-events.consumed.total")
    private val successfulMsgCounter = meters.counter("change-events.consumed.successful")
    private val errorsMsgCounter = meters.counter("change-events.consumed.errors")

    @KafkaListener(clientIdPrefix = "change-event-listener", topics = [USER_ENTITY_CHANGE_TOPIC])
    fun consumeChangeEvent(
            @Header(RECEIVED_MESSAGE_KEY) userId: String,
            @Header(OPERATION_HEADER) operation: String,
            @Payload updatedFields: Map<String, Any?>
    ) {
        try {
            consumedMsgCounter.increment()

            updatedFields.forEach { e ->
                val fieldName = e.key
                val fieldValue = e.value

                users.updateField(userId, fieldName, fieldValue)
            }

            successfulMsgCounter.increment()
        } catch (e: Exception) {
            errorsMsgCounter.increment()
        }
    }

    companion object {
        private const val OPERATION_HEADER = "X-Operation"
    }

}