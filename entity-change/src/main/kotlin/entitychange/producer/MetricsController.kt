package entitychange.producer

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MetricsController(meters: MeterRegistry) {

    private val successMsgCounter = meters.counter("change-events.successful")
    private val failedMsgCounter = meters.counter("change-events.failed")
    private val errorsMsgCounter = meters.counter("change-events.errors")

    @GetMapping("/metrics")
    fun getAll(): Map<String, Any?> {
        return listOf(successMsgCounter, failedMsgCounter, errorsMsgCounter)
                .map { it.id.name to it.count() }
                .toMap()
    }

}