import cats.effect._
import com.twitter.finagle.Thrift
import com.twitter.util.Future
import org.http4s._
import org.http4s.dsl.io._
import com.dr0l3.thrift.generated._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import cats.effect._
import fs2.{ Stream, StreamApp }
import fs2.StreamApp.ExitCode

import io.circe._
import org.http4s._
import org.http4s.dsl.io._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.server.blaze._

object Web extends StreamApp[IO] {
  import Conversions._
  val userClient = Thrift.client.build[UserService.MethodPerEndpoint]("user:80")
  val orderClient = Thrift.client.build[OrderService.MethodPerEndpoint]("order:80")

  implicit val orderJson = new Encoder[Order] {
    override def apply(a: Order): Json = {
      Json.obj(
        "item" -> a.items.asJson,
        "total_price" -> a.totalPrice.asJson)
    }
  }

  implicit val orderResponseJson = new Encoder[OrderResponse] {
    override def apply(a: OrderResponse): Json = {
      a.orders.asJson
    }
  }

  implicit val preferencesJson = new Encoder[UserPreferences] {
    override def apply(a: UserPreferences): Json = {
      Json.obj(
        "favorite_color" -> a.favoriteColor.asJson,
        "payment_method" -> a.paymentMethod.originalName.toLowerCase.asJson)
    }
  }

  implicit val userJson = new Encoder[User] {
    override def apply(a: User): Json = {
      Json.obj(
        "id" -> a.id.asJson,
        "name" -> a.name.asJson,
        "preferences" -> a.preferences.asJson)
    }
  }

  implicit val userResponseJson = new Encoder[UserResponse] {
    override def apply(a: UserResponse): Json = {
      a.user.asJson
    }
  }

  case class UserInput(name: String)
  case class OrderInput(userId: String, item: List[String], totalPrice: Double)

  implicit val userInputDecoder = jsonOf[IO, UserInput]
  implicit val orderInput = jsonOf[IO, OrderInput]

  val service = HttpService[IO] {
    case GET -> Root / "orders" / id =>
      println("Orders reached")
      Ok(orderClient.getOrdersForUser(id).toIO.map(_.asJson))
    case GET -> Root / "users" / id =>
      println("Users reached")
      Ok(userClient.getUserById(id).toIO.map(_.asJson))

    case req @ POST -> Root / "users" => {
      println("Users post reached")
      for {
        input <- req.as[UserInput]
        resp <- userClient.createUser(input.name).toIO
        response <- Ok(resp.user.asJson)
      } yield response
    }

    case req @ POST -> Root / "orders" => {
      println("Orders post reached")
      for {
        input <- req.as[OrderInput]
        resp <- orderClient.addOrderToUser(input.userId, Order(input.item, input.totalPrice)).toIO
        response <- Ok(resp.orders.asJson)
      } yield response
    }
  }

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {
    BlazeBuilder[IO].bindHttp(80, "0.0.0.0").mountService(service, "/").serve
  }

}

object Conversions {
  def twitterToIO[A](tf: Future[A]): IO[A] = {
    val promise = Promise[A]()

    tf.respond {
      case com.twitter.util.Throw(e) => promise.failure(e)
      case com.twitter.util.Return(value) => promise.success(value)
    }

    IO.fromFuture(IO(promise.future))
  }

  implicit class TOps[A](val fut: Future[A]) {
    def toIO = twitterToIO(fut)
  }
}