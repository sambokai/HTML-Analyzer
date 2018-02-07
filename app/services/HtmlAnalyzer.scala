package services

import java.net.{MalformedURLException, URI}
import javax.inject.{Inject, Singleton}
import javax.net.ssl.SSLHandshakeException

import com.google.inject.ImplementedBy
import domain.{HTMLVersion, WebPage}
import org.jsoup.nodes.{Document, DocumentType}
import org.jsoup.{Jsoup, UnsupportedMimeTypeException}
import services.LinkCheckerImpl.AvailabilitiesByLinkTarget

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
    case e: MalformedURLException => Left("Only HTTP and HTTPS websites are supported.")
  }
}

@Singleton
class HtmlAnalyzer @Inject()(documentRetriever: DocumentRetriever, linkChecker: LinkChecker) {

  def analyze(location: String): Future[Either[String, WebPage]] =
    documentRetriever.get(location) match {
      case Left(e) => Future.successful(Left(e))
      case Right(doc) => getHyperlinks(doc).map { avail =>
        Right(WebPage(doc.location(), doc.title(), getDomainName(doc), getHeadings(doc), avail, checkForLoginForm(doc), getHtmlVersion(doc)))
      }
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

  private[services] def getHyperlinks(doc: Document): Future[AvailabilitiesByLinkTarget] = {
    val allHyperLinks: Seq[String] = doc
      .body
      .select("a")
      .asScala
      .map(link =>
        link.attributes.get("href")
      )

    linkChecker.getAvailabilityForLinks(allHyperLinks, getDomainName(doc))
  }


}