package entityapi.repository

import entityapi.model.User
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class UserRepository {

    private val users = ConcurrentHashMap<String, Any?>()

    fun updateField(userId: String, fieldName: String, value: Any?) {
        val fieldKey = "$userId/$fieldName"
        users.put(fieldKey, value)
    }

    fun findById(userId: String): User? {
        return null
    }

    fun findAll(): List<User> {
        return users.asSequence()
                .groupBy {
                    // the first part of the every field is the userId
                    it.key.split("/")[0]
                }
                .map { (userId, fieldEntries) ->
                    val fields = fieldEntries
                            .map { it.toPair() }
                            .toMap()
                    User(
                            id = userId,
                            firstName = fields["$userId/firstName"] as String,
                            lastName = fields["$userId/lastName"] as String,
                            stringValue = fields["$userId/stringValue"] as String?,
                            intValue = fields["$userId/intValue"] as Int?,
                            floatValue = fields["$userId/firstName"] as Float?,
                            booleanValue = fields["$userId/firstName"] as Boolean
                    )
                }
                .toList()
    }

}