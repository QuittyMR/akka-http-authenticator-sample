package authenticator

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import authenticator.core.AuthenticatorCore
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.language.{implicitConversions, postfixOps}

object AuthenticatorGlobal extends GlobalActorMembers with GlobalConfiguration with GlobalMessages {
    val log: LoggingAdapter = Logging(system, getClass)
    val core: ActorRef = system.actorOf(Props[AuthenticatorCore], "authenticator-core")
}

trait GlobalActorMembers {
    implicit val system: ActorSystem = ActorSystem("authenticator-router")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
}

trait GlobalConfiguration {
    implicit var config: Config = ConfigFactory.load()
    System.getenv().asScala.filterKeys(_.startsWith("CONFIG__")).foreach(envVariable => {
        val confMap = envVariable._1.replace("CONFIG__", "").split("__").mkString(".") -> envVariable._2
        printf(s"${confMap._1} => ${confMap._2}\n")
        config = config.withValue(confMap._1, ConfigValueFactory.fromAnyRef(confMap._2))
    })

    implicit val timeout: Timeout = Timeout(config.getInt("akka.actors.core.timeout") milliseconds)
}

trait GlobalMessages {

    final case class LoginRequest(userName: String,
                                  password: String,
                                  appId: String)

    implicit val jsonLoginRequest: RootJsonFormat[LoginRequest] = jsonFormat3(LoginRequest)

    final case class LogoutRequest(sessionHash: String)

    final case class UserRequest(userName: Option[String] = None)

    final case class NewUserRequest(userName: String,
                                    displayName: Option[String],
                                    password: Option[String],
                                    isActive: Boolean = true)

    implicit val jsonNewUserRequest: RootJsonFormat[NewUserRequest] = jsonFormat4(NewUserRequest)

    final case class UpdateUserRequest(userName: String,
                                       changes: Map[String, Either[String, Boolean]])

    implicit val jsonUpdateUserRequest: RootJsonFormat[UpdateUserRequest] = jsonFormat2(UpdateUserRequest)

    final case class AuthenticationRequest(userEmail: String,
                                           userPassword: String)

    final case class DiagnosticsRequest()

}