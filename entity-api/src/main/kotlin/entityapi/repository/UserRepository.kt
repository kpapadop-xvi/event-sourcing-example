package entityapi.repository

import entityapi.model.User
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class UserRepository {

    private val users = ConcurrentHashMap<String, User>()

    fun findById(userId: String): User? {
        return users[userId]
    }

    fun findAll(): List<User> {
        return users.values
                .sortedBy { it.id }
                .toList()
    }

    fun updateField(userId: String, fieldName: String, value: Any?) {
        val applyUpdate = updates[fieldName] ?: { u, _ -> u }
        val user = users[userId] ?: User(userId)
        val modifiedUser = applyUpdate(user, value)
        users[userId] = modifiedUser
    }

    fun delete(userId: String) {
        users.remove(userId)
    }

    private val updates: Map<String, (user: User, value: Any?) -> User> = mapOf(
            "name" to { u, value -> u.copy(name = (value as String?)) },
            "stringValue" to { u, value -> u.copy(stringValue = (value as String?)) },
            "intValue" to { u, value -> u.copy(intValue = (value as Int?)) },
            "floatValue" to { u, value -> u.copy(floatValue = (value as Double?)) },
            "booleanValue" to { u, value -> u.copy(booleanValue = (value as Boolean?)) }
    )

}