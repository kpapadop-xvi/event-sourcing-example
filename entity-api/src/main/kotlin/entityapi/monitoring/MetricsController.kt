package entityapi.monitoring

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MetricsController(meters: MeterRegistry) {

    private val consumedMsgCounter = meters.counter("change-events.consumed.total")
    private val successfulMsgCounter = meters.counter("change-events.consumed.successful")
    private val errorsMsgCounter = meters.counter("change-events.consumed.errors")

    @GetMapping("/metrics")
    fun getAll(): Map<String, Any?> {
        return listOf(consumedMsgCounter, successfulMsgCounter, errorsMsgCounter)
                .map { it.id.name to it.count() }
                .toMap()
    }

}