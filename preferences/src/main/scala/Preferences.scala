import com.twitter.finagle.Thrift
import com.twitter.util.{ Await, Future }
import com.dr0l3.thrift.generated.{ UserPreferenceResponse, UserPreferenceService, UserPreferences }

import scala.collection.mutable

object Run extends com.twitter.app.App {
  val service = new PreferencesServiceImpl()
  val server = Thrift.server.serveIface("0.0.0.0:80", service)
  closeOnExit(server)
  Await.ready(server)
}

class PreferencesServiceImpl extends UserPreferenceService[Future] {
  val userPreferences = mutable.Map[String, UserPreferences]()
  override def getPreferencesForUser(id: String): Future[UserPreferenceResponse] = {
    Future(
      UserPreferenceResponse(userPreferences.get(id)))
  }

  override def updatePreferenceForUser(id: String, preferences: UserPreferences): Future[UserPreferenceResponse] = {
    Future(UserPreferenceResponse(userPreferences.put(id, preferences)))
  }
}
