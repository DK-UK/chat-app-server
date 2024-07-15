package example.com.model


enum class Priority {
    Low, Medium, High, Vital
}

@kotlinx.serialization.Serializable
data class Task(
    var name : String,
    var description : String,
    var priority : Priority
)
