package domain

import scala.util.matching.Regex

case class HTMLVersion(value: String)

object HTMLVersion {

  val HTML5 = HTMLVersion("HTML5")

  val HTML4_01 = HTMLVersion("HTML 4.01")

  val XHTML1_0 = HTMLVersion("XHTML 1.0")

  val XHTMLDTD1_1 = HTMLVersion("XHTML 1.1 - DTD")

  val XHTMLBasic1_1 = HTMLVersion("XHTML Basic 1.1")

  val Unknown = HTMLVersion("Unknown HTML Version")

  object HtmlVersionPatterns {
    val html5pattern: Regex = "".r
    val html401pattern: Regex = ".*4\\.01.*".r
    val xhtml10pattern: Regex = ".*XHTML\\ss1\\.0.*".r
    val dtdxhtml11pattern: Regex = ".*DTD\\sXHTML\\ss1\\.1.*".r
    val basicxhtml11pattern: Regex = ".*XHTML\\sBasic\\ss1\\.1.*".r
  }

  def parse(s: String): HTMLVersion = {
    import HtmlVersionPatterns._
    s match {
      case html5pattern() => HTMLVersion.HTML5
      case html401pattern() => HTMLVersion.HTML4_01
      case xhtml10pattern() => HTMLVersion.XHTML1_0
      case dtdxhtml11pattern() => HTMLVersion.XHTMLDTD1_1
      case basicxhtml11pattern() => HTMLVersion.XHTMLBasic1_1
      case _ => HTMLVersion.Unknown
    }
  }
}

