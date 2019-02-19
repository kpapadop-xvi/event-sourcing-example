package entityapi.web

import entityapi.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(private val users: UserRepository) {

    @GetMapping("/users/{user-id}")
    fun findById(@PathVariable("user-id") userId: String): ResponseEntity<*> {
        return userId
                .takeIf { it.isNotBlank() }
                ?.let { it.trim() }
                ?.let { users.findById(it) }
                ?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity
                        .notFound()
                        .build<Any>()
    }

    @GetMapping("/users")
    fun findAll() = users.findAll()

}
