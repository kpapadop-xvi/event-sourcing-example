package entityapi.model

data class User(
        val id: String,
        val name: String? = null,
        val stringValue: String? = null,
        val intValue: Int? = null,
        val floatValue: Double? = null,
        val booleanValue: Boolean? = null
)