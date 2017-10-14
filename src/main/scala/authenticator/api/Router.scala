package authenticator.api

import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import authenticator.AuthenticatorGlobal
import authenticator.core.storage.Persistence
import spray.json.DefaultJsonProtocol._

import scala.concurrent.duration.DurationInt
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success}

// @formatter:off
trait Router {

    import AuthenticatorGlobal._

    val routes: Route = {
        logRequestResult("Authenticator.Request:", Logging.DebugLevel) {
            path("alive") {
                get {
                    complete("Alive")
                } ~
                post {
                    // Run diagnostics on required resources
                    onComplete(core ? DiagnosticsRequest) {
                        case Success(response) =>
                            response match {
                                case response: Map[String, Boolean] =>
                                    complete(response)
                            }
                        case Failure(error) =>
                            complete("Actors" -> false)
                    }
                }
            } ~
            path("session") {
                post {
                    // Authenticate user and create a new session
                    entity(as[LoginRequest]) { loginRequest =>
                        onSuccess(core ? loginRequest) {
                            case response: String =>
                                complete(response)
                            case _: Boolean =>
                                complete(Unauthorized -> "User unauthenticated")
                            case error: NoSuchElementException =>
                                complete(Unauthorized -> error.getMessage)
                        }
                    }
                } ~
                delete {
                    // Request termination of existing session
                    parameter("sessionId") { sessionHash =>
                        onSuccess(core ? LogoutRequest(sessionHash)) {
                            case true =>
                                complete(ResetContent -> "Session terminated")
                        }
                    }
                }
            } ~
            pathPrefix("users") {
                get {
                    path("get") {
                        // Get all users
                        implicit val timeout: Timeout = Timeout(10 seconds)
                        onSuccess(core ? UserRequest()) {
                            case response: List[Persistence.Users] =>
                                complete(response)
                            case _ =>
                                failWith(new Exception)
                        }
                    } ~
                    path("get" / Remaining) { userName =>
                        // Get specific user by username
                        onSuccess(core ? UserRequest(Some(userName))) {
                            case response: Persistence.Users =>
                                complete(response)
                            case error: NoSuchElementException =>
                                complete(NoContent -> error.getMessage)
                        }
                    }
                } ~
                post {
                    path("create") {
                        // Create a new user
                        entity(as[NewUserRequest]) { newUserRequest =>
                            onSuccess(core ? newUserRequest) {
                                case response: String =>
                                    if (response.contains("updated")) {
                                        complete(response)
                                    } else {
                                        complete(Created -> response)
                                    }
                                case error: IllegalArgumentException =>
                                    complete(Conflict -> error.getMessage)
                            }
                        }
                    } ~
                    path("update") {
                        // Update an existing user
                        entity(as[UpdateUserRequest]) { updateUserRequest =>
                            onSuccess(core ? updateUserRequest) {
                                case response: String =>
                                    if (response.contains("updated")) {
                                        complete(response)
                                    } else {
                                        complete(Created -> response)
                                    }
                                case error: IllegalArgumentException =>
                                    complete(Conflict -> error.getMessage)
                            }
                        }
                    }
                }
            }
        }
    }
}
// @formatter:on
