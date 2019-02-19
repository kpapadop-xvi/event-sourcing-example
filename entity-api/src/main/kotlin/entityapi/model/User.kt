package entityapi.model

data class User(
        val id: String,
        val firstName: String,
        val lastName: String,
        val stringValue: String?,
        val intValue: Int?,
        val floatValue: Float?,
        val booleanValue: Boolean?
)