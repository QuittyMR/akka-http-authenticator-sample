package authenticator.core.services

import authenticator.core.storage.Persistence

object UserPersistenceService {

    import Persistence.context._

    private type Users = Persistence.Users

    def getAll: List[Users] = transaction {
        run(query[Users])
    }

    def getByEmail(userEmail: String): Option[Users] = transaction {
        run(query[Users].filter(_.userName == lift(userEmail))).headOption
    }

    def updateUser(userName: String, changeMap: Map[String, Either[String, Boolean]]): String = {
        val originalUser: Users = getByEmail(userName).get
        val newUser: Users = originalUser.copy(
            displayName = changeMap.get("displayName") match {
                case Some(displayName) => displayName.left.get
                case None => originalUser.displayName
            },
            password = changeMap.get("password") match {
                case Some(password) => UserSessionService.getEncryptedPassword(password.left.get)
                case None => originalUser.password
            },
            isActive = changeMap.get("isActive") match {
                case Some(isActive) => isActive.right.get
                case None => originalUser.isActive
            }
        )

        transaction {
            run(query[Users].filter(_.userName == lift(userName)).update(lift(newUser)))
            "User updated"
        }
    }

    def createUser(user: Users): String = transaction {
        run(query[Users].insert(lift(user.copy(
            password = UserSessionService.getEncryptedPassword(user.password)
        ))))
        "User created"
    }
}
