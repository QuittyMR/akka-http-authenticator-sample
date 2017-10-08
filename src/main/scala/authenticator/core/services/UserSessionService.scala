package authenticator.core.services

import authenticator.core.storage.{Persistence, Volatile}
import com.redis.RedisClient
import org.joda.time._
import org.mindrot.jbcrypt.BCrypt

import scala.util.hashing._

object UserSessionService {

    private type Users = Persistence.Users

    private val client: RedisClient = Volatile.client

    def authenticate(user: Users, password: String): Boolean = {
        BCrypt.checkpw(password, user.password)
    }

    def getEncryptedPassword(password: String): String = {
        BCrypt.hashpw(password, BCrypt.gensalt())
    }

    def createSession(appId: String, user:Users, ttl: Option[Int] = None): String = {
        def hashMaker: String = {
            val timeStamp: String = DateTime.now().getMillis.toString
            s"session::${MurmurHash3.stringHash(s"$appId::$user.userName::$timeStamp")}"
        }

        hashMaker match {
            case hash if client.exists(hash) =>
                createSession(appId, user, ttl)
            case hash =>
                client.hmset(hash, List(
                    "display_name" -> user.displayName,
                    "user_name" -> user.userName
                ))
                client.expire(hash, Volatile.ttl)
                hash.toString
        }
    }

    def deleteSession(sessionHash: String): Unit = {
        client.del(sessionHash)
    }
}

