import com.dr0l3.thrift.generated.{ Order, OrderResponse, OrderService }
import com.twitter.finagle.Thrift
import com.twitter.util.{ Await, Future }

import scala.collection.mutable

object Run extends com.twitter.app.App {
  val service = new OrdersImpl()
  val server = Thrift.server.serveIface("0.0.0.0:80", service)
  closeOnExit(server)
  Await.ready(server)
}

class OrdersImpl extends OrderService.MethodPerEndpoint {
  val store = mutable.Map[String, List[Order]]()
  override def getOrdersForUser(id: String): Future[OrderResponse] = {
    Future(OrderResponse(store.get(id).toList.flatten))
  }

  override def addOrderToUser(id: String, order: Order): Future[OrderResponse] = Future {
    val current = store.get(id)
    val withNewOrder = order :: current.toList.flatten
    store.put(id, withNewOrder)
    OrderResponse(withNewOrder)
  }
}
