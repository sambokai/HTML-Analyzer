package services

import domain.HTMLVersion
import org.scalatest.Matchers._
import org.scalatest.WordSpec

import scala.collection.mutable.ArrayBuffer

class HtmlAnalyzerSpec extends WordSpec {

  import TestWebsite._
  import utils.TestDocumentRetriever

  trait WithHtmlAnalyzer {
    val testDocumentRetriever = new TestDocumentRetriever
    val analyzer = new HtmlAnalyzer(testDocumentRetriever)
  }

  "getHtmlVersion()" should {
    "detect if a website has an unknown html type" in new WithHtmlAnalyzer {
      analyzer.getHtmlVersion(testDocumentRetriever.get(ieIsEvil_html4_00.filePath)) shouldBe HTMLVersion.Unknown
    }

    "detect an HTML5 Page" in new WithHtmlAnalyzer {
      analyzer.getHtmlVersion(testDocumentRetriever.get(gitHubLogin.filePath)) shouldBe HTMLVersion.HTML5
    }


    "detect an HTML4.01 Page" in new WithHtmlAnalyzer {
      analyzer.getHtmlVersion(testDocumentRetriever.get(w3c_html4_01_spec.filePath)) shouldBe HTMLVersion.HTML4_01
    }
  }

  "checkForLoginForm()" should {
    "detect that a page does NOT have a Login Form" in new WithHtmlAnalyzer {
      analyzer.checkForLoginForm(testDocumentRetriever.get(obama_wiki.filePath)) shouldBe false
    }

    "detect a login-only page" in new WithHtmlAnalyzer {
      analyzer.checkForLoginForm(testDocumentRetriever.get(gitHubLogin.filePath)) shouldBe true
    }

    "detect a login-AND-register page" in new WithHtmlAnalyzer {
      analyzer.checkForLoginForm(testDocumentRetriever.get(linkedin_loginAndSignup.filePath)) shouldBe true
    }
  }

  "getDomainName()" should {
    "detect the domainname of a webpage" in new WithHtmlAnalyzer {
      analyzer.getDomainName(testDocumentRetriever.get(gitHubLogin.filePath)) shouldBe TestDocumentRetriever.testBaseDomainName
    }
  }

  "getHeadings()" should {
    "count occurence of html-headings grouped by heading-level" in new WithHtmlAnalyzer {
      analyzer.getHeadings(testDocumentRetriever.get(obama_wiki.filePath)) shouldBe List(
        ("h1", 1),
        ("h2", 12),
        ("h3", 31),
        ("h4", 26),
        ("h5", 2)
      )
    }
  }

  "getHyperlinks()" should {
    "return all hyperlinks in the webpage, grouped by whether they link to an internal or external location" in new WithHtmlAnalyzer {
      analyzer.getHyperlinks(testDocumentRetriever.get(gitHubLogin.filePath)) shouldBe Map(
        false -> ArrayBuffer(
          "https://github.com/",
          "https://github.com/site/terms",
          "https://github.com/site/privacy",
          "https://github.com/security",
          "https://github.com/contact"
        ),
        true -> ArrayBuffer(
          "#start-of-content",
          "/password_reset",
          "/join?source=login",
          "",
          "")
      )
    }
  }

}


object TestWebsite {

  case class TestWebsite(filePath: String, baseUri: String)

  val gitHubLogin = TestWebsite("/WebsiteSnapshots/github_login_08122017.htm", "https://www.github.com/login")
  val spiegelLogin = TestWebsite("/WebsiteSnapshots/spiegel-online_login_08122017.htm", "https://www.spiegel.de/meinspiegel/login.html")
  val w3c_html4_01_spec = TestWebsite("/WebsiteSnapshots/W3C_recommendation_HTML4-01Specification_12122017.htm", "https://www.w3.org/TR/1999/REC-html401-19991224/")
  val ieIsEvil_html4_00 = TestWebsite("/WebsiteSnapshots/InternetExplorer-Is-Evil_12122017.htm", "http://toastytech.com/evil/")
  val obama_wiki = TestWebsite("/WebsiteSnapshots/BarackObama_Wikipedia_13122017.htm", "https://en.wikipedia.org/wiki/Barack_Obama")
  val linkedin_loginAndSignup = TestWebsite("/WebsiteSnapshots/linkedin_loginandsignup_13122017.htm", "https://www.linkedin.com")
}

