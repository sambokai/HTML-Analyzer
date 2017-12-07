package models

case class HTMLVersion(value: String)

object HTMLVersion {

  object HTML5 extends HTMLVersion("HTML5")

  object HTML4_01 extends HTMLVersion("HTML 4.01")

  object XHTML1_0 extends HTMLVersion("XHTML 1.0")

  object XHTMLDTD1_1 extends HTMLVersion("XHTML 1.1 - DTD")

  object XHTMLBasic1_1 extends HTMLVersion("XHTML Basic 1.1")

  object Unknown extends HTMLVersion("Unknown HTML Version")

  val values = Seq(HTML5, HTML4_01, XHTML1_0, XHTMLDTD1_1, XHTMLBasic1_1, Unknown)
}