package example.com.plugins

import example.com.model.AuthUser
import example.com.model.Register
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

@Serializable
data class City(val name: String, val population: Int)


class ChatService(private val connection: Connection) {
    companion object {

        private const val SELECT_CITY_BY_ID = "SELECT name, population FROM cities WHERE id = ?"
        private const val INSERT_CITY = "INSERT INTO cities (name, population) VALUES (?, ?)"
        private const val UPDATE_CITY = "UPDATE cities SET name = ?, population = ? WHERE id = ?"
        private const val DELETE_CITY = "DELETE FROM cities WHERE id = ?"

        private const val CREATE_TABLE_REGISTER =
            "CREATE TABLE IF NOT EXISTS Register(id SERIAL PRIMARY KEY," +
                    "profile_img varchar(2048), name varchar(50) NOT NULL, email varchar(100) NOT NULL," +
                    "password varchar(50) NOT NULL, profile_bio varchar(250), created_at BIGINT NOT NULL DEFAULT 0)"

        private const val SELECT_USER = "SELECT * FROM Register"
        private const val REGISTER_USER = "INSERT INTO Register (profile_img, name, email, password, profile_bio, created_at) VALUES (?,?,?,?,?,?)"
        private const val IS_USER_EXISTS = "SELECT * FROM Register where email = ?"

        private const val DROP_TABLE_REGISTER = "DROP TABLE Register"

    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_REGISTER)
    }

    private var newCityId = 0

    suspend fun checkIfUserExists(email : String) : Boolean = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(IS_USER_EXISTS)
        statement.setString(1, email)
        val result = statement.executeQuery()
        if (result.next()){
            return@withContext true
        }
        else{
            return@withContext false
        }
    }

    suspend fun authenticateUser(authUser: AuthUser) : Register = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(IS_USER_EXISTS)
        statement.setString(1, authUser.email)
        val resultSet = statement.executeQuery()

        if (resultSet.next()){
            return@withContext Register(
                id = resultSet.getLong(1),
                profile_image = resultSet.getString(2),
                name = resultSet.getString(3),
                email = resultSet.getString(4),
                password = resultSet.getString(5),
                profile_bio = resultSet.getString(6),
                created_at = resultSet.getLong(7)
            )
        }
        else{
            return@withContext Register()
        }
    }

    suspend fun registerUser(register: Register) : Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(REGISTER_USER, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, register.profile_image)
        statement.setString(2, register.name)
        statement.setString(3, register.email)
        statement.setString(4, register.password)
        statement.setString(5, register.profile_bio)
        statement.setLong(6, register.created_at)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()){
            return@withContext generatedKeys.getInt(1)
        }
        else{
            return@withContext -1
        }
    }

    // Read a city
    suspend fun read(id: Int): City = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_CITY_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val name = resultSet.getString("name")
            val population = resultSet.getInt("population")
            return@withContext City(name, population)
        } else {
            throw Exception("Record not found")
        }
    }

    suspend fun getUsers() : List<Register> = withContext(Dispatchers.IO) {
        val users = mutableListOf<Register>()
        val statement = connection.prepareStatement(SELECT_USER)
        val resultSet = statement.executeQuery()
        while (resultSet.next()){
            users.add(Register(
                id = resultSet.getLong(1),
                profile_image = resultSet.getString(2),
                name = resultSet.getString(3),
                email = resultSet.getString(4),
                password = resultSet.getString(5),
                profile_bio = resultSet.getString(6),
                created_at = resultSet.getLong(7)
            ))
        }
        return@withContext users
    }

    // Update a city
    suspend fun update(id: Int, city: City) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_CITY)
        statement.setString(1, city.name)
        statement.setInt(2, city.population)
        statement.setInt(3, id)
        statement.executeUpdate()
    }

    // Delete a city
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_CITY)
        statement.setInt(1, id)
        statement.executeUpdate()
    }

    suspend fun dropTable() = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DROP_TABLE_REGISTER)
        statement.executeUpdate()
    }
}

