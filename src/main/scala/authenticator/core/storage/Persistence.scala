package authenticator.core.storage

import authenticator.AuthenticatorGlobal.log
import io.getquill._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.util.{Failure, Success, Try}

object Persistence extends PersistenceModels {
    lazy val context: MysqlJdbcContext[SnakeCase] =
        Try(new MysqlJdbcContext[SnakeCase]("database")) match {
            case Success(instance) =>
                instance
            case Failure(error) =>
                log.error(s"Error instantiating persistence: \n${error.getMessage}")
                throw error
        }

    def isConnected: Boolean = {
        Try(context.probe("show databases;")) match {
            case Success(queryAttempt) =>
                queryAttempt.isSuccess
            case Failure(error) =>
                false
        }
    }
}

trait PersistenceModels extends DefaultJsonProtocol {

    case class Users(lastModified: Option[String] = None,
                     userName: String,
                     displayName: String,
                     password: String,
                     isActive: Boolean = true)

    implicit val jsonUsers: RootJsonFormat[Users] = jsonFormat5(Users)
}
