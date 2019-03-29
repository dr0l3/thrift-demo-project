import java.util.UUID

import cats.effect.IO
import com.dr0l3.thrift.generated.{ User, UserResponse, UserService }
import com.twitter.util.Future
import TwitterConverters._

object Run extends App {
  //  LibraryMacros.greeting(List("omgzomg"))

  val fut = new FutT()

  val omg: UserService[Future] = new UserService[Future] {
    override def getUserById(id: String): Future[UserResponse] = {
      Future {
        UserResponse(Option(User(UUID.randomUUID().toString, "Rune", None)))
      }
    }

    override def createUser(name: String): Future[UserResponse] = {
      Future {
        UserResponse(Option(User(UUID.randomUUID().toString, "Rune", None)))
      }
    }
  }

  val test = LibraryMacros.convert(omg)

  val io = for {
    first <- test.getUserById("OMG")
    second <- test.createUser("Rune")
  } yield (first, second)

  println(io.unsafeRunSync())
}
