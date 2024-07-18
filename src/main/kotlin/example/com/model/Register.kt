package example.com.model

import kotlinx.serialization.Serializable

@Serializable
data class Register(
    var id: Long = 0L,
    var profile_image : String = "",
    var name : String = "",
    var email : String = "",
    var password : String = "",
    var profile_bio : String = "",
    var created_at : Long = System.currentTimeMillis()
)
