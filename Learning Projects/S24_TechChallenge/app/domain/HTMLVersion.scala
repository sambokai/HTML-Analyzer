package domain

case class HTMLVersion(value: String)

object HTMLVersion {

  val HTML5 = HTMLVersion("HTML5")

  val HTML4_01 = HTMLVersion("HTML 4.01")

  val XHTML1_0 = HTMLVersion("XHTML 1.0")

  val XHTMLDTD1_1 = HTMLVersion("XHTML 1.1 - DTD")

  val XHTMLBasic1_1 = HTMLVersion("XHTML Basic 1.1")

  val Unknown = HTMLVersion("Unknown HTML Version")

  val values = Seq(HTML5, HTML4_01, XHTML1_0, XHTMLDTD1_1, XHTMLBasic1_1, Unknown)
}