package services

import domain.HTMLVersion._
import domain.{HTMLVersion, WebPage}
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import services.HtmlAnalyzer._
import utils.TestDocumentRetriever._

import scala.collection.mutable.ArrayBuffer

class HtmlAnalyzerSpec extends WordSpec with FutureAwaits with DefaultAwaitTimeout {

  import TestWebsite._
  import utils.TestDocumentRetriever

  trait WithHtmlAnalyzer {
    val testDocumentRetriever = new TestDocumentRetriever
    val analyzer = new HtmlAnalyzer(testDocumentRetriever)
  }

  "HtmlAnalyzer" can {

    "provide an analyze method which" should {

      "analyze an html page and return the result inside a WebPage object" in new WithHtmlAnalyzer {
        val testPage = WebPage(
            location = "http://www.test.com/testfolder/testfile?testquery=123",
            title = "Sign in to GitHub · GitHub",
            domainName = "test.com",
            headings = List(("h1", 1)),
            hyperlinks = Map(
              false -> ArrayBuffer("https://github.com/", "https://github.com/site/terms", "https://github.com/site/privacy", "https://github.com/security", "https://github.com/contact"),
              true -> ArrayBuffer("#start-of-content", "/password_reset", "/join?source=login", "", "")),
            hasLoginForm = true,
            html_version = HTML5
          )

        await(analyzer.analyze(gitHubLogin.filePath)) shouldBe testPage
      }

      "analyze an html5 file that has no content and return the result inside a WebPage object" in new WithHtmlAnalyzer {
        val emptyPage = WebPage("http://www.test.com/testfolder/testfile?testquery=123",
          "",
          "test.com",
          List(),
          Map(),
          hasLoginForm = false,
          HTMLVersion.HTML5
        )

        await(analyzer.analyze(noContentHtmlFile.filePath)) shouldBe emptyPage
      }

      "analyze an empty file and return the result inside a WebPage object" in new WithHtmlAnalyzer {
        val emptyFilePage = WebPage(
          "http://www.test.com/testfolder/testfile?testquery=123",
          "",
          "test.com",
          List(),
          Map(),
          hasLoginForm = false,
          HTMLVersion.Unknown
        )
        await(analyzer.analyze(emptyFile.filePath)) shouldBe emptyFilePage
      }

      "analyze an invalid/corrupted html file and return the result inside a WebPage object" in new WithHtmlAnalyzer {
        val corruptedFilePage = WebPage(
          "http://www.test.com/testfolder/testfile?testquery=123",
          "",
          "test.com",
          List(),
          Map(),
          hasLoginForm = false,
          HTMLVersion.Unknown
        )
        await(analyzer.analyze(corruptedHtmlFile.filePath)) shouldBe corruptedFilePage
      }

    }

    "provide a getHtmlVersion method which" should {
      "detect if a website has an unknown html type" in new WithHtmlAnalyzer {
        analyzer.getHtmlVersion(testDocumentRetriever.get(ieIsEvil_html4_00.filePath)) shouldBe Unknown
      }

      "detect an HTML5 Page" in new WithHtmlAnalyzer {
        analyzer.getHtmlVersion(testDocumentRetriever.get(gitHubLogin.filePath)) shouldBe HTML5
      }


      "detect an HTML4.01 Page" in new WithHtmlAnalyzer {
        analyzer.getHtmlVersion(testDocumentRetriever.get(w3c_html4_01_spec.filePath)) shouldBe HTML4_01
      }
    }

    "provide a checkForLoginForm method which" should {
      "detect that a page does NOT have a Login Form" in new WithHtmlAnalyzer {
        analyzer.checkForLoginForm(testDocumentRetriever.get(obama_wiki.filePath)) shouldBe false
      }

      "detect a page that only contains a single login form" in new WithHtmlAnalyzer {
        analyzer.checkForLoginForm(testDocumentRetriever.get(gitHubLogin.filePath)) shouldBe true
      }

      "detect a page that contains both login form and register form" in new WithHtmlAnalyzer {
        analyzer.checkForLoginForm(testDocumentRetriever.get(linkedin_loginAndSignup.filePath)) shouldBe true
      }
    }

    "provide a getDomainName method which" should {
      "detect the domainname of a webpage" in new WithHtmlAnalyzer {
        analyzer.getDomainName(testDocumentRetriever.get(gitHubLogin.filePath)) shouldBe testBaseDomainName
      }
    }

    "provide a getHeadings method which" should {
      "count occurence of html-headings grouped by heading-level" in new WithHtmlAnalyzer {
        analyzer.getHeadings(testDocumentRetriever.get(obama_wiki.filePath)) shouldBe List(
          ("h1", 1),
          ("h2", 12),
          ("h3", 31),
          ("h4", 26),
          ("h5", 2)
        )
      }

      //TODO: test no link
    }

    "provide a getHyperlinks which" should {
      "return all hyperlinks in the webpage, grouped by whether they link to an internal or external location" in new WithHtmlAnalyzer {
        analyzer.getHyperlinks(testDocumentRetriever.get(gitHubLogin.filePath)) shouldBe Map(
          ExternalLink -> ArrayBuffer(
            "https://github.com/",
            "https://github.com/site/terms",
            "https://github.com/site/privacy",
            "https://github.com/security",
            "https://github.com/contact"
          ),
          InternalLink -> ArrayBuffer(
            "#start-of-content",
            "/password_reset",
            "/join?source=login",
            "",
            "")
        )
      }
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
  val corruptedHtmlFile = TestWebsite("/corruptedHtmlFile.html", "")
  val emptyFile = TestWebsite("/emptyFile.html", "")
  val noContentHtmlFile = TestWebsite("/noContentHtmlFile.html", "")
}

