package client

import javax.inject.Singleton

import akka.http.scaladsl.model.StatusCode
import com.google.inject.{ImplementedBy, Inject}
import play.api.libs.ws.WSClient
import services.Availability
import scala.concurrent.ExecutionContext.Implicits.global


import scala.concurrent.Future

@ImplementedBy(classOf[LinkCheckClientImpl])
trait LinkCheckClient {
  def forUrl(url: String): Future[Availability]
}

@Singleton
class LinkCheckClientImpl @Inject()(ws: WSClient) extends LinkCheckClient {
  def forUrl(url: String): Future[Availability] = ws.url(url).withFollowRedirects(true).get().map { response =>
    Availability(url, StatusCode.int2StatusCode(response.status))
  }
}
