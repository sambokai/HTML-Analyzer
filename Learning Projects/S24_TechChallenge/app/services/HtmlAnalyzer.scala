package services

import java.io.File
import java.net.URI
import javax.inject.{Inject, Singleton}

import domain.{HTMLVersion, WebPage}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, DocumentType}
import utils.UrlWrapper

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap
import scala.collection.mutable

@Singleton
class HtmlAnalyzer @Inject()(httpResolver: HttpResolver) {


  def analyze(url: UrlWrapper): WebPage = analyzeDocument(Jsoup.connect(url.url.toString).get)

  def analyze(file: File, baseUri: String): WebPage = analyzeDocument(getDocument(file, baseUri))

  def analyzeDocument(doc: Document): WebPage = {
    WebPage(doc.location(), doc.title(), getDomainName(doc), getHeadings(doc), getHyperlinks(doc), checkForLoginForm(doc), getHtmlVersion(doc))
  }

  def getDocument(file: File, baseUri: String): Document = Jsoup.parse(file, null, baseUri)

  def getHtmlVersion(doc: Document): HTMLVersion = {
    val doctypeInDOM = {
      doc
        .childNodes()
        .asScala
        .find(documentType => documentType.isInstanceOf[DocumentType])
        .map(node => node.asInstanceOf[DocumentType])
    }

    doctypeInDOM match {
      case Some(doctype) => doctype.attributes.get("publicId") match {
        case "" => HTMLVersion.HTML5
        case doctype_string if doctype_string contains "4.01" => HTMLVersion.HTML4_01
        case doctype_string if doctype_string contains "XHTML 1.0" => HTMLVersion.XHTML1_0
        case doctype_string if doctype_string contains "DTD XHTML 1.1" => HTMLVersion.XHTMLDTD1_1
        case doctype_string if doctype_string contains "XHTML Basic 1.1" => HTMLVersion.XHTMLBasic1_1
        case _ => HTMLVersion.Unknown
      }
      case None => HTMLVersion.Unknown
    }
  }

  def checkForLoginForm(doc: Document): Boolean = doc.select("form").asScala.exists(elem => elem.select("[type=password]").size == 1)

  def resolveAllLinks(doc: Document) = {
    val allHyperlinks: Iterator[String] = getHyperlinks(doc).values.flatten.iterator

    ???
  }


  def getDomainName(doc: Document): String = {
    val uri = new URI(doc.location)
    val domain = uri.getHost
    if (domain.startsWith("www.")) domain.substring(4) else domain
  }

  def getHeadings(doc: Document): ListMap[String, Int] = {
    val allHeadings = doc
      .select("h0, h1, h2, h3, h4, h5, h6")
      .asScala
      .map(_.tag().getName)

    ListMap(allHeadings.groupBy(identity).mapValues(_.size).toSeq.sortWith(_._1 < _._1): _*)
  }

  def getHyperlinks(doc: Document): Map[Boolean, mutable.Buffer[String]] = {
    val domainName = getDomainName(doc)

    doc
      .body
      .select("a")
      .asScala
      .map(_.attributes.get("href"))
      .groupBy(link => isInternal(link, domainName))
  }

  def isInternal(link: String, domainName: String): Boolean = {
    //TODO: IP adresses are not considered in this implementation. e.g. 124.512.152.124 may be an external link
    val httpPrefix = s"^(https?:\\/\\/)".r
    val fullPattern = s"^(https?:\\/\\/)?(www.)?($domainName)(\\/.*)?$$".r

    if (httpPrefix.findPrefixOf(link).isDefined) fullPattern.findFirstIn(link).isDefined else true
  }


}