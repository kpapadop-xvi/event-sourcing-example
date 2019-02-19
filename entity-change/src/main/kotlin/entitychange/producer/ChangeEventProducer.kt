package entitychange.producer

import entitychange.producer.KafkaProducerConfiguration.Companion.USER_ENTITY_CHANGE_TOPIC
import entitychange.producer.Operation.*
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.SendResult
import org.springframework.messaging.support.MessageBuilder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.util.concurrent.ListenableFutureCallback

@Component
class ChangeEventProducer(
        private val kafka: KafkaOperations<String, Map<String, Any?>>, meters: MeterRegistry,
        private val rnd: RandomProvider
) {
    private val successMsgCounter = meters.counter("change-events.successful")
    private val failedMsgCounter = meters.counter("change-events.failed")
    private val errorsMsgCounter = meters.counter("change-events.errors")

    private val randomIds = rnd.randomUuids(100)
    private val randomMutableFieldSelect = rnd.randomSubset(MUTABLE_USER_FIELDS)

    @Scheduled(fixedDelay = 1000)
    fun produceRandomChangeEvent() {
        val objId = randomIds.random()
        val operation = Operation.values().random()
        val userEntityFields = randomFields(operation)

        val msg = MessageBuilder
                .withPayload(userEntityFields)
                .setHeader(KafkaHeaders.TOPIC, USER_ENTITY_CHANGE_TOPIC)
                .setHeader(KafkaHeaders.MESSAGE_KEY, objId)
                .setHeader(OPERATION_HEADER, operation)
                .build()

        try {
            kafka.send(msg)
                    .addCallback(MessageCallback())
        } catch (e: Exception) {
            errorsMsgCounter.increment()
            throw e
        }
    }

    private fun randomFields(operation: Operation): Map<String, Any?> {
        return when (operation) {
            CREATION -> {
                val rndName = rnd.randomFirstLastName()
                mutableMapOf<String, Any?>(FIRST_NAME to rndName.first, LAST_NAME to rndName.second)
                        .also { it.putAll(randomMutableFields()) }
                        .let { it.toMap() }
            }
            UPDATE -> {
                mutableMapOf<String, Any?>()
                        .also { it.putAll(randomMutableFields()) }
                        .let { it.toMap() }
            }
            DELETION -> emptyMap()
        }
    }

    private fun randomMutableFields(): Map<String, Any?> {
        return randomMutableFieldSelect.randomSelection()
                .map { fieldName -> fieldName to randomValueForEntityField(fieldName) }
                .toMap()
    }

    private fun randomValueForEntityField(fieldName: String): Any? {
        return when (fieldName) {
            MUTABLE_USER_FIELDS[0] -> rnd.randomString()
            MUTABLE_USER_FIELDS[1] -> rnd.nextInt()
            MUTABLE_USER_FIELDS[2] -> rnd.nextFloat()
            MUTABLE_USER_FIELDS[3] -> rnd.nextBoolean()
            else -> null
        }
    }

    private inner class MessageCallback : ListenableFutureCallback<SendResult<String, Map<String, Any?>>> {
        override fun onSuccess(result: SendResult<String, Map<String, Any?>>?) {
            successMsgCounter.increment()
        }

        override fun onFailure(ex: Throwable) {
            failedMsgCounter.increment()
        }
    }

    companion object {
        private const val OPERATION_HEADER = "X-Operation"
        private const val FIRST_NAME = "firstName"
        private const val LAST_NAME = "lastName"
        private val MUTABLE_USER_FIELDS = listOf("stringValue", "intValue", "floatValue", "booleanValue")
    }
}
