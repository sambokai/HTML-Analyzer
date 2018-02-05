package services

import java.net.URI
import javax.inject.{Inject, Singleton}
import javax.net.ssl.SSLHandshakeException

import com.google.inject.ImplementedBy
import domain.{HTMLVersion, WebPage}
import org.jsoup.nodes.{Document, DocumentType}
import org.jsoup.{Jsoup, UnsupportedMimeTypeException}
import services.HtmlAnalyzer._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex

@ImplementedBy(classOf[UrlRetriever])
trait DocumentRetriever {
  def get(url: String): Either[String, Document]
}

@Singleton
class UrlRetriever extends DocumentRetriever {
  override def get(url: String): Either[String, Document] = try {
    Right(Jsoup.connect(url).get)
  } catch {
    case e: IllegalArgumentException => Left("The URL was wrong.")
    case e: UnsupportedMimeTypeException => Left("Unsupported Filetype. URL must link to an HTML or TXT file.")
    case e: SSLHandshakeException => Left("SSL Encryption Error. Is the domain using HTTPS?")
  }
}

@Singleton
class HtmlAnalyzer @Inject()(documentRetriever: DocumentRetriever) {

  def analyze(location: String): Future[Either[String, WebPage]] = Future {
    for {
      doc <- documentRetriever.get(location)
    } yield WebPage(doc.location(), doc.title(), getDomainName(doc), getHeadings(doc), getHyperlinks(doc), checkForLoginForm(doc), getHtmlVersion(doc))
  }

  private[services] def getHtmlVersion(doc: Document): HTMLVersion = {
    val doctypeInDOM: Option[DocumentType] = {
      doc
        .childNodes()
        .asScala
        .find(documentType => documentType.isInstanceOf[DocumentType])
        .map(node => node.asInstanceOf[DocumentType])
    }

    doctypeInDOM.map {
      documentType =>
        val attributes = documentType.attributes
        attributes.get("name") match {
          case "html" => HTMLVersion.parse(attributes.get("publicId"))
          case _ => HTMLVersion.Unknown
        }
    }.getOrElse(HTMLVersion.Unknown)
  }

  private[services] def checkForLoginForm(doc: Document): Boolean = doc.select("form").asScala.exists(elem => elem.select("[type=password]").size == 1)

  def getDomainName(doc: Document): String = {
    val uri = new URI(doc.location)
    val domain = uri.getHost
    if (domain.startsWith("www.")) domain.substring(4) else domain
  }

  private[services] def getHeadings(doc: Document): Seq[(String, Int)] = {
    val allHeadings: Seq[String] = doc
      .select("h1, h2, h3, h4, h5, h6")
      .asScala
      .map(_.tag.getName)


    allHeadings.groupBy(identity)
      .mapValues(_.size)
      .toSeq
      .sortBy {
        case (header, _) => header
      }
  }

  private[services] def getHyperlinks(doc: Document): Map[LinkType, Seq[String]] = {
    val domainName = getDomainName(doc)

    val allHyperLinks: Seq[String] = doc
      .body
      .select("a")
      .asScala
      .map(link =>
        link.attributes.get("href")
      )

    allHyperLinks.groupBy(isInternal(_, domainName))
  }

  private def isInternal(link: String, domainName: String): LinkType = {
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

object HtmlAnalyzer {
  type LinkType = String

  val InternalLink = "Internal"
  val ExternalLink = "External"
  val NoLink = "NoLink"

  type StatusCode = Int
}