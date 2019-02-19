package entitychange.producer

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaProducerConfiguration {

    @Autowired
    private lateinit var props: KafkaProperties

    @Bean
    fun entityChangeTopic() = NewTopic(USER_ENTITY_CHANGE_TOPIC, 3, 1)

    @Bean
    fun producerConfigs() = mutableMapOf<String, Any>(
            BOOTSTRAP_SERVERS_CONFIG to props.bootstrapServers.joinToString(separator = ","),
            KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java)

    @Bean
    fun producerFactory() = DefaultKafkaProducerFactory<String, Map<String, Any?>>(producerConfigs())

    @Bean
    fun kafkaTemplate() = KafkaTemplate(producerFactory())

    companion object {
        const val USER_ENTITY_CHANGE_TOPIC = "user-entity-change"
    }

}