package authenticator.core.storage

import com.redis._

import scala.util.{Failure, Success, Try}

object Volatile {

    import authenticator.AuthenticatorGlobal._

    val sessionsKey: String = "authenticator_sessions"
    private val host = config.getString("redis.host")
    private val port = config.getInt("redis.port")
    private val database = config.getInt("redis.database")
    val ttl: Int = Try(config.getInt("redis.ttl")) match {
        case Success(configTtl) => configTtl
        case Failure(_) => 86400000
    }

    lazy val client: RedisClient = Try(new RedisClient(host, port, database)) match {
        case Success(instance) =>
            instance
        case Failure(error) =>
            log.error(s"Error instantiating volatile storage: \n${error.getMessage}")
            throw error
    }

    def isConnected: Boolean = {
        Try(client.keys()).isSuccess
    }
}
