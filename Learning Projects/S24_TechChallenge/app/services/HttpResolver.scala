package services

import javax.inject.{Inject, Singleton}

import play.api.libs.ws.WSClient

@Singleton
class HttpResolver @Inject()(ws: WSClient) {

  def resolve(url: String): Boolean = ???


  def validateAllLinks(links: Iterator[String]) = {
    // TODO: check if a page exists
    ???
  }

}
