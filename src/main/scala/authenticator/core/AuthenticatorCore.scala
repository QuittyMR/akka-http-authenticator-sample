package authenticator.core

import akka.actor.Actor
import authenticator.AuthenticatorGlobal
import authenticator.core.services.{UserPersistenceService, UserSessionService}
import authenticator.core.storage.{Persistence, Volatile}

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

class AuthenticatorCore extends Actor {

    import AuthenticatorGlobal._

    implicit def userRequestToUser(userRequest: NewUserRequest): Persistence.Users =
        Persistence.Users(
            userName = userRequest.userName,
            displayName = userRequest.displayName.get,
            password = userRequest.password.get
        )

    override def receive: Receive = {

        case DiagnosticsRequest =>
            log.info("Running diagnostics")
            val diagnosticsMap = Map[String, Boolean](
                "Actors" -> true,
                "Persistence" -> Persistence.isConnected,
                "Volatile" -> Volatile.isConnected
            )
            sender() ! diagnosticsMap

        case request: UserRequest =>
            log.info("Retrieving user/s")
            if (request.userName.isDefined) {
                UserPersistenceService.getByEmail(request.userName.get) match {
                    case Some(user) =>
                        sender() ! user
                    case _ =>
                        sender() ! new NoSuchElementException("User not found")
                }
            } else {
                sender() ! UserPersistenceService.getAll
            }

        case newUserRequest: NewUserRequest =>
            log.info("Generating user")
            Try(UserPersistenceService.createUser(newUserRequest)) match {
                case Success(value) =>
                    sender() ! value
                case Failure(error) =>
                    log.error(error.getMessage)
                    sender() ! new IllegalArgumentException("Unable to create user. Check log for details")
            }

        case updateUserRequest: UpdateUserRequest =>
            log.info("Updating user")
            Try((UserPersistenceService.updateUser _).tupled(UpdateUserRequest.unapply(updateUserRequest).get)) match {
                case Success(value) =>
                    sender() ! value
                case Failure(error) =>
                    log.error(error.getMessage)
                    sender() ! new IllegalArgumentException("Unable to update user. Check log for details")
            }

        case loginRequest: LoginRequest =>
            log.info("Authenticating user")
            UserPersistenceService.getByEmail(loginRequest.userName) match {
                case Some(user) =>
                    if (UserSessionService.authenticate(user, loginRequest.password)) {
                        sender() ! UserSessionService.createSession(loginRequest.appId, user)
                    } else {
                        sender() ! false
                    }
                case _ =>
                    sender() ! new NoSuchElementException("User not found")
            }

        case logoutRequest: LogoutRequest =>
            log.info("Terminating session")
            UserSessionService.deleteSession(logoutRequest.sessionHash)
            sender() ! true

        case _ =>
            val errorMessage = "Invalid message received"
            log.error(errorMessage)
            sender() ! new Exception(errorMessage)
    }
}
