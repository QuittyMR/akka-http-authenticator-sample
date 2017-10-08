package authenticator.api

import akka.http.scaladsl.Http
import authenticator.AuthenticatorGlobal

object AuthenticatorServer extends App with Router {

    import AuthenticatorGlobal._

    private val port = config.getInt("service.port")
    private val host = config.getString("service.host")
    val server = Http().bindAndHandle(routes, host, port)
    println(s"Serving on port $port; Press CTRL+C to terminate...\n\n")
}
