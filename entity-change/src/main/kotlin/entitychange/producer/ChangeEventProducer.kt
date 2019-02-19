package entitychange.producer

import entitychange.producer.KafkaProducerConfiguration.Companion.USER_ENTITY_CHANGE_TOPIC
import entitychange.producer.eventmodel.ChangeEvent
import entitychange.producer.eventmodel.Operation
import entitychange.producer.eventmodel.Operation.*
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.support.SendResult
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.util.concurrent.ListenableFutureCallback

@Component
class ChangeEventProducer(
        private val kafka: KafkaOperations<String, ChangeEvent>, meters: MeterRegistry,
        private val rnd: RandomProvider,
        @Value("\${producer.max-distinct-ids:100}") val maxDistinctIds: Int
) {
    private val successMsgCounter = meters.counter("change-events.successful")
    private val failedMsgCounter = meters.counter("change-events.failed")
    private val errorsMsgCounter = meters.counter("change-events.errors")

    private val randomIds = rnd.randomUuids(maxDistinctIds)
    private val randomFieldsSubset = rnd.randomDynamicSubset(MUTABLE_USER_FIELDS)

    @Scheduled(fixedDelayString = "\${producer.sending-period-ms:10}")
    fun produceRandomChangeEvent() {
        val objId = randomIds.random()
        val operation = Operation.values().random()
        val fields = randomFields(operation)
        val event = ChangeEvent(operation, fields)

        try {
            kafka.send(USER_ENTITY_CHANGE_TOPIC, objId, event)
                    .addCallback(MessageCallback())
        } catch (e: Exception) {
            errorsMsgCounter.increment()
            throw e
        }
    }

    private fun randomFields(operation: Operation): Map<String, Any?> {
        return when (operation) {
            CREATION -> {
                val rndName = rnd.randomName()
                mutableMapOf<String, Any?>(NAME to rndName)
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
        return randomFieldsSubset.randomSelection()
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

    private inner class MessageCallback : ListenableFutureCallback<SendResult<String, ChangeEvent>> {
        override fun onSuccess(result: SendResult<String, ChangeEvent>?) {
            successMsgCounter.increment()
        }

        override fun onFailure(ex: Throwable) {
            failedMsgCounter.increment()
        }
    }

    companion object {
        private const val NAME = "name"
        private val MUTABLE_USER_FIELDS = listOf("stringValue", "intValue", "floatValue", "booleanValue")
    }
}
