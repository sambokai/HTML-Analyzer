package services

import java.net.URI
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import domain.{HTMLVersion, WebPage}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, DocumentType}
import services.HtmlAnalyzer._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
    WebPage(doc.location(), doc.title(), getDomainName(doc), getHeadings(doc), getHyperlinks(doc), checkForLoginForm(doc), getHtmlVersion(doc))
  }

  private[services] def getHtmlVersion(doc: Document): HTMLVersion = {

    val doctypeInDOM: Option[DocumentType] = {
      doc
        .childNodes()
        .asScala
        .find(documentType => documentType.isInstanceOf[DocumentType])
        .map(node => node.asInstanceOf[DocumentType])
    }

    val html5pattern = "".r
    val html401pattern = ".*4\\.01.*".r
    val xhtml10pattern = ".*XHTML\\ss1\\.0.*".r
    val dtdxhtml11pattern = ".*DTD\\sXHTML\\ss1\\.1.*".r
    val basicxhtml11pattern = ".*XHTML\\sBasic\\ss1\\.1.*".r

    doctypeInDOM.map {
      documentType =>
        val attributes = documentType.attributes
        attributes.get("name") match {
          case "html" => attributes.get("publicId") match {
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

  private[services] def getDomainName(doc: Document): String = {
    val uri = new URI(doc.location)
    val domain = uri.getHost
    if (domain.startsWith("www.")) domain.substring(4) else domain
  }

  private[services] def getHeadings(doc: Document): Seq[(String, Int)] = {
    val allHeadings = doc
      .select("h0, h1, h2, h3, h4, h5, h6")
      .asScala
      .map(_.tag().getName)

    Seq(allHeadings.groupBy(identity).mapValues(_.size).toSeq.sortWith(_._1 < _._1): _*)
  }

  private[services] def getHyperlinks(doc: Document): Map[LinkType, Seq[String]] = {
    val domainName = getDomainName(doc)

    doc
      .body
      .select("a")
      .asScala
      .map(_.attributes.get("href"))
      .groupBy(link => isInternal(link, domainName))
  }

  private def isInternal(link: String, domainName: String): Boolean = {
    //TODO: IP adresses are not considered in this implementation. e.g. 124.512.152.124 may be an external link
    val httpPrefix = s"^(https?:\\/\\/)".r
    val fullPattern = s"^(https?:\\/\\/)?(www.)?($domainName)(\\/.*)?$$".r

    if (httpPrefix.findPrefixOf(link).isDefined) fullPattern.findFirstIn(link).isDefined else true
  }
}

object HtmlAnalyzer {
  type LinkType = Boolean

  val InternalLink = true
  val ExternalLink = false
}