import java.util.UUID

import com.twitter.finagle.Thrift
import com.twitter.util.{ Await, Future }
import com.dr0l3.thrift.generated.{ User, UserPreferenceService, UserResponse, UserService }

import scala.collection.mutable.ListBuffer

object Main extends com.twitter.app.App {
  val preferencesClient = Thrift.client.build[UserPreferenceService.MethodPerEndpoint]("user-preferences:80")
  val service = new UserServiceImpl(preferencesClient)
  val server = Thrift.server.serveIface("0.0.0.0:80", service)
  closeOnExit(server)
  Await.ready(server)
}

class UserServiceImpl(userPrefs: UserPreferenceService.MethodPerEndpoint) extends UserService[Future] {
  val repo = new ListBuffer[User]()

  override def getUserById(id: String): Future[UserResponse] = {
    val user = repo.find(_.id == id)

    for {
      userPreferences <- user match {
        case Some(value) =>
          userPrefs.getPreferencesForUser(value.id).map(_.preference)
        case None =>
          Future.value(None)
      }
    } yield UserResponse(user.map(us => us.copy(preferences = userPreferences)))
  }

  override def createUser(name: String): Future[UserResponse] = {
    val user = User(UUID.randomUUID().toString, name)
    repo.append(user)
    Future(UserResponse(Option(user)))
  }
}
