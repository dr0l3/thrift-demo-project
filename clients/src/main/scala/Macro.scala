import cats.effect.IO
import com.dr0l3.thrift.generated.UserService
import com.twitter.util.{ Future => TFuture, Promise => TPromise }
import scala.concurrent.{ Promise, Future }
import scala.util.{ Failure, Success }

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros
import scala.util.matching._
import scala.concurrent.ExecutionContext.Implicits.global

trait Test[A[_]] {
  def hello(): A[String]
  def hello2(): A[String]
}

class FutT extends Test[Future] {
  import scala.concurrent.ExecutionContext.Implicits.global
  override def hello(): Future[String] = Future {
    "Hello"
  }

  override def hello2(): Future[String] = Future {
    "HELLOOO!"
  }
}

object hmm {
  val f: Test[Future] = new FutT
  val w = new Test[IO] {
    override def hello(): IO[String] = {
      IO.fromFuture(IO(f.hello()))
    }

    override def hello2(): IO[String] = {
      IO.fromFuture(IO(f.hello2()))
    }
  }
}

object TwitterConverters {

  implicit def scalaFutureToTwitterFuture[A](future: Future[A]): TFuture[A] = {
    val promise = TPromise[A]()
    future.onComplete {
      case Success(value) => promise.setValue(value)
      case Failure(e: Throwable) => promise.setException(e)
    }

    promise
  }

  implicit def twitterFutureToScalaFuture[A](tf: TFuture[A]): Future[A] = {
    val promise = Promise[A]()

    tf.respond {
      case com.twitter.util.Throw(e) => promise.failure(e)
      case com.twitter.util.Return(value) => promise.success(value)
    }

    promise.future
  }
}

object LibraryMacros {
  def greeting[A](a: List[A]): Unit = macro greetingMacro[A]

  def greetingMacro[A](c: Context)(a: c.Expr[List[A]]): c.Tree = {
    import c.universe._

    q"""
        println(${a})
        println("Hello from macro")
     """
  }

  def convert(a: Test[Future]): Test[IO] = macro convertImpl

  def convertImpl(c: Context)(a: c.Expr[Test[Future]]): c.Tree = {
    import c.universe._
    val tpe = typeOf[Test[Future]]

    val paramsByMethod = tpe.members.filter(_.isMethod).filter(_.asMethod.returnType.typeSymbol.name.toString.startsWith("A")).map { member =>
      val methodName = member.name.decodedName.toTermName
      val params = member.asMethod.paramLists.flatten.map { param =>
        (param.name.decodedName.toString, param.typeSignature.resultType.finalResultType.toString)
      }
      val resultType = member.asMethod.returnType.typeArgs.head
      (methodName, params, resultType)
    }

    val futureMethods = paramsByMethod.map {
      case (methodName, params, resultType) =>
        val paramDefinitions = params.map {
          case (name, paramType) =>
            q"$name : $paramType"
        }
        val paramList = params.map {
          case (name, _) =>
            q"$name"
        }
        q"""override def $methodName(...$paramDefinitions): IO[$resultType] = IO.fromFuture(IO($a.$methodName(...$paramList)))"""
    }

    q"""
       new Test[IO] {
            ..$futureMethods
       }
     """
  }
}