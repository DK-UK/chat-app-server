package example.com.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthUser(
    var id : Long = 0L,
    var email : String = "",
    var password : String = ""
)
