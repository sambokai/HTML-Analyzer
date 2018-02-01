package services

import java.net.URI
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import domain.{HTMLVersion, WebPage}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, DocumentType}
import play.api.Logger
import services.HtmlAnalyzer._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex

@ImplementedBy(classOf[UrlRetriever])
trait DocumentRetriever {
  def get(url: String): Document
}

@Singleton
class UrlRetriever extends DocumentRetriever {
  override def get(url: String): Document = Jsoup.connect(url).get
}

@Singleton
class HtmlAnalyzer @Inject()(documentRetriever: DocumentRetriever) {


  def analyze(location: String): Future[WebPage] = Future {
    val doc = documentRetriever.get(location)
    val result = WebPage(doc.location(), doc.title(), getDomainName(doc), getHeadings(doc), getHyperlinks(doc), checkForLoginForm(doc), getHtmlVersion(doc))
    Logger.info(s"Webpage analysis done for ${result.location} - (${result.title})")
    result
  }

  private[services] def getHtmlVersion(doc: Document): HTMLVersion = {
    import domain.HTMLVersion.HtmlVersionPatterns._

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
          case "html" => attributes.get("publicId") match { // TODO: delegate to HTMLVersion
            case html5pattern() => HTMLVersion.HTML5
            case html401pattern() => HTMLVersion.HTML4_01
            case xhtml10pattern() => HTMLVersion.XHTML1_0
            case dtdxhtml11pattern() => HTMLVersion.XHTMLDTD1_1
            case basicxhtml11pattern() => HTMLVersion.XHTMLBasic1_1
            case _ => HTMLVersion.Unknown
          }
          case _ => HTMLVersion.Unknown //TODO: Make method throw InvalidFileException("Not an HTML File") if necessary
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
      .map(_.attributes.get("href"))

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
}