package services

import javax.inject.{Inject, Singleton}

import akka.http.scaladsl.model.StatusCode
import client.LinkCheckClient
import com.google.inject.ImplementedBy
import services.LinkCheckerImpl.AvailabilitiesByLinkTarget

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex

case class Availability(link: String, statusCode: StatusCode)

@ImplementedBy(classOf[LinkCheckerImpl])
trait LinkChecker {
  def getAvailabilityForLinks(links: Seq[String], domain: String): Future[AvailabilitiesByLinkTarget]
}

@Singleton
class LinkCheckerImpl @Inject()(linkCheckClient: LinkCheckClient) extends LinkChecker {

  import LinkCheckerImpl._

  override def getAvailabilityForLinks(links: Seq[String], domain: String): Future[AvailabilitiesByLinkTarget] = {

    val sortedLinks: Map[LinkTargetDomain, Seq[String]] = links.groupBy(link => getLinkTargetDomain(link, domain)).filterKeys(_ != NoLink)

    val result: Map[LinkTargetDomain, Future[Seq[Availability]]] = sortedLinks.map {
      case (linkTargetDomain, urls) => (linkTargetDomain, Future.sequence(urls.map {
        case link if link.startsWith("/") => resolve(s"http://$domain$link")
        case link => resolve(link)
      }))
    }

    Future.traverse(result) {
      case (k, fv) => fv.map(k -> _)
    }.map(_.toMap)
  }

  def resolve(link: String): Future[Availability] = linkCheckClient.forUrl(link)

  def getLinkTargetDomain(link: String, domainName: String): LinkTargetDomain = {
    val regexCompatibleDomainname = domainName.replace(".", "\\.") //Escape the dot in "xyz.com"

    val internalLink: Regex = s"(?<rootpath>^\\/.*$$)|(^(?<protocol>https?:\\/\\/)?(?<predomain>[a-zA-Z0-9]*\\.)*(?<domain>$regexCompatibleDomainname)(?<path>\\/.*)?$$)".r
    val externalLink: Regex = s"^(?<protocol>https?:\\/\\/)?(?<predomain>[a-zA-Z0-9]*\\.)*(?<domain>[a-zA-Z0-9]*\\.[a-zA-Z0-9]*)(?<!$regexCompatibleDomainname)(\\/.*)?$$".r

    link match {
      case internalLink(_*) => InternalLink
      case externalLink(_*) => ExternalLink
      case _ => NoLink
    }
  }
}

object LinkCheckerImpl {
  type LinkTargetDomain = String

  val InternalLink = "Internal"
  val ExternalLink = "External"
  val NoLink = "NoLink"

  type StatusCode = Int

  type AvailabilitiesByLinkTarget = Map[LinkTargetDomain, Seq[Availability]]
}


