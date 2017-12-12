package models

import java.io.InputStream
import java.net.{URI, URL}

import org.jsoup.Jsoup
import org.jsoup.nodes._

import scala.collection.JavaConverters._

class Webpage(val doc: Document) {

  def this(url: URL) = this(Jsoup.connect(url.toString).get())

  def this(input: InputStream, baseUri: String) = this(Jsoup.parse(input, null, baseUri))

  val location: String = this.doc.location()

  val title: String = this.doc.title()

  val domainName: String = {
    val uri = new URI(this.location)
    val domain = uri.getHost
    if (domain.startsWith("www.")) domain.substring(4) else domain
  }

  val headings: Map[String, Int] = {
    val allHeadings = doc
      .select("h0, h1, h2, h3, h4, h5, h6")
      .asScala
      .map(_.tag().getName)

    allHeadings.groupBy(identity).mapValues(_.size)
  }

  val html_version: HTMLVersion = {
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

}