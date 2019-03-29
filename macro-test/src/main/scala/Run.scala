import java.util.UUID

import cats.effect.IO
import com.dr0l3.thrift.generated.{ User, UserResponse, UserService }
import com.twitter.util.Future

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

  val test = LibraryMacros.convert(fut)

  val io = for {
    first <- test.hello()
    second <- test.hello2()
  } yield (first, second)

  println(io.unsafeRunSync())
}
