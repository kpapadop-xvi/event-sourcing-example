package entityapi.consumer

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer


@EnableKafka
@Configuration
class KafkaConsumerConfiguration {

    @Autowired
    private lateinit var props: KafkaProperties

    @Bean
    fun entityChangeTopic() = NewTopic(USER_ENTITY_CHANGE_TOPIC, 3, 1)

    @Bean
    fun consumerConfigs() = mutableMapOf<String, Any>(
            BOOTSTRAP_SERVERS_CONFIG to props.bootstrapServers.joinToString(separator = ","),
            KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            GROUP_ID_CONFIG to "json",
            AUTO_OFFSET_RESET_CONFIG to "earliest"
    )

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Map<String, Any?>> {
        return DefaultKafkaConsumerFactory<String, Map<String, Any?>>(
                consumerConfigs(),
                StringDeserializer(),
                JsonDeserializer(Map::class.java))
    }

    @Bean
    fun kafkaListenerContainerFactory() = ConcurrentKafkaListenerContainerFactory<String, Map<String, Any?>>()
            .also { it.consumerFactory = consumerFactory() }

    companion object {
        const val USER_ENTITY_CHANGE_TOPIC = "user-entity-change"
    }

}