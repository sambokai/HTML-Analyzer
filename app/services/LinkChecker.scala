package services

import javax.inject.{Inject, Singleton}

import akka.http.scaladsl.model.StatusCode
import com.google.inject.ImplementedBy
import play.api.libs.ws.WSClient
import play.shaded.ahc.org.asynchttpclient.handler.MaxRedirectException
import services.LinkCheckerImpl.AvailabilitiesByLinkTarget

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex

case class LinkAvailability(link: String, statusCode: Either[String, StatusCode])


@ImplementedBy(classOf[LinkCheckClientImpl])
trait LinkCheckClient {
  def forUrl(url: String): Future[LinkAvailability]
}

@Singleton
class LinkCheckClientImpl @Inject()(ws: WSClient) extends LinkCheckClient {
  def forUrl(url: String): Future[LinkAvailability] =
    ws.url(url).withFollowRedirects(true).get.map { response =>
      LinkAvailability(url, Right(StatusCode.int2StatusCode(response.status)))
    }.recoverWith {
      case e: MaxRedirectException => Future.successful(LinkAvailability(url, Left("Too many redirects. (Max. 5)")))
    }
}

@ImplementedBy(classOf[LinkCheckerImpl])
trait LinkChecker {
  def getAvailabilityForLinks(links: Seq[String], domain: String): Future[AvailabilitiesByLinkTarget]
}

@Singleton
class LinkCheckerImpl @Inject()(linkCheckClient: LinkCheckClient) extends LinkChecker {

  import LinkCheckerImpl._

  override def getAvailabilityForLinks(links: Seq[String], domain: String): Future[AvailabilitiesByLinkTarget] = {
    val linkAvailabilitiesWithLinkTarget: Seq[Future[(LinkTargetDomain, LinkAvailability)]] = links.distinct
      .filter(getLinkTargetDomain(_, domain) != NoLink)
      .map { link =>
        resolve(link, domain).map((getLinkTargetDomain(link, domain), _))
      }

    val eventuallinkAvailabilitiesWithLinkTarget: Future[Seq[(LinkTargetDomain, LinkAvailability)]] = Future.sequence(linkAvailabilitiesWithLinkTarget)

    eventuallinkAvailabilitiesWithLinkTarget.map { availabilitiesWithLinkType: Seq[(LinkTargetDomain, LinkAvailability)] =>
      availabilitiesWithLinkType
        .groupBy {
          case (linkTarget, _) => linkTarget
        }
        .map {
          case (linkTarget, availabilities) => (linkTarget, availabilities.map(_._2))
        }
    }
  }

  def resolve(link: String, domain: String): Future[LinkAvailability] = link match {
    case relativeLink if link.startsWith("/") => linkCheckClient.forUrl(s"http://$domain$relativeLink")
    case absoluteLink => linkCheckClient.forUrl(link)
  }

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

  type AvailabilitiesByLinkTarget = Map[LinkTargetDomain, Seq[LinkAvailability]]
}


