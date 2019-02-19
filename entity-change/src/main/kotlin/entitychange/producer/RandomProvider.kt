package entitychange.producer

import org.kohsuke.randname.RandomNameGenerator
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import kotlin.random.Random

@Component
class RandomProvider {

    private val rndSeed = Instant.now().toEpochMilli()
    private val rnd = Random(rndSeed)
    private val rndNames = RandomNameGenerator(rndSeed.toInt())

    fun nextBoolean() = rnd.nextBoolean()
    fun nextFloat() = rnd.nextFloat()
    fun nextInt() = rnd.nextInt()
    fun randomString() = listOf("abc", "def", "ghi", "jkl", "mno", "pqr", "stu", "vwx", "yz!").random()
    fun randomUuids(numberOfIds: Int) = (0..numberOfIds).map { UUID.randomUUID().toString() }
    fun randomName() = rndNames.next()
    fun <T> randomDynamicSubset(items: List<T>) = RandomItemSelection(items)
}

class RandomItemSelection<T>(private val items: List<T>) {

    fun randomSelection(): List<T> {
        val maxNoOfRandomFields = (1..items.size).random()
        return (1..maxNoOfRandomFields).map { items.random() }
    }

}