package services

import java.io.File

import domain.HTMLVersion._
import domain.{HTMLVersion, WebPage}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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

    def testDocument(location: String): Document = {
      val path = getClass.getResource(location).toURI
      val testFile = new File(path)
      Jsoup.parse(testFile, null, testBaseUri)
    }
  }

  "HtmlAnalyzer" must {

    "provide an analyze method which" should {
      "return an error if document could not be retrieved" in {
        val errorMessage = "This is a bad error"
        val documentRetrieverWithErrors = new DocumentRetriever {
          override def get(url: String): Either[String, Document] = Left(errorMessage)
        }

        await(new HtmlAnalyzer(documentRetrieverWithErrors).analyze("valid url")) shouldBe Left(errorMessage)
      }

      "analyzes an html page and return the result inside a WebPage object" in new WithHtmlAnalyzer {
        val testPage: Either[Nothing, WebPage] =
          Right(
            WebPage(
              location = "http://www.test.com/testfolder/testfile?testquery=123",
              title = "Sign in to GitHub · GitHub",
              domainName = "test.com",
              headings = List(("h1", 1)),
              hyperlinks = Map(
                NoLink -> ArrayBuffer(
                  "#start-of-content",
                  "",
                  ""),
                InternalLink -> ArrayBuffer(
                  "https://test.com/",
                  "/password_reset",
                  "/join?source=login",
                  "https://test.com/site/terms",
                  "https://test.com/site/privacy",
                  "https://test.com/security",
                  "https://test.com/contact")
              ),
              hasLoginForm = true,
              html_version = HTML5
            )
          )

        await(analyzer.analyze(gitHubLogin.filePath)) shouldBe testPage
      }

      "analyzes an html5 file that has no content and return the result inside a WebPage object" in new WithHtmlAnalyzer {
        val emptyPage =
          Right(
            WebPage("http://www.test.com/testfolder/testfile?testquery=123",
              "",
              "test.com",
              List(),
              Map(),
              hasLoginForm = false,
              HTMLVersion.HTML5
            )
          )

        await(analyzer.analyze(noContentHtmlFile.filePath)) shouldBe emptyPage
      }

      "analyzes an empty file and return the result inside a WebPage object" in new WithHtmlAnalyzer {
        val emptyFilePage =
          Right(
            WebPage(
              "http://www.test.com/testfolder/testfile?testquery=123",
              "",
              "test.com",
              List(),
              Map(),
              hasLoginForm = false,
              HTMLVersion.Unknown
            ))
        await(analyzer.analyze(emptyFile.filePath)) shouldBe emptyFilePage
      }

      "analyzes an invalid/corrupted html file and return the result inside a WebPage object" in new WithHtmlAnalyzer {
        val corruptedFilePage =
          Right(
            WebPage(
              "http://www.test.com/testfolder/testfile?testquery=123",
              "",
              "test.com",
              List(),
              Map(),
              hasLoginForm = false,
              HTMLVersion.Unknown
            ))
        await(analyzer.analyze(corruptedHtmlFile.filePath)) shouldBe corruptedFilePage
      }

    }

    "provide a getHtmlVersion method which" should {
      "detects if a website has an unknown html type" in new WithHtmlAnalyzer {
        analyzer.getHtmlVersion(testDocument(ieIsEvil_html4_00.filePath)) shouldBe Unknown
      }

      "detects an HTML5 Page" in new WithHtmlAnalyzer {
        analyzer.getHtmlVersion(testDocument(gitHubLogin.filePath)) shouldBe HTML5
      }


      "detects an HTML4.01 Page" in new WithHtmlAnalyzer {
        analyzer.getHtmlVersion(testDocument(w3c_html4_01_spec.filePath)) shouldBe HTML4_01
      }
    }

    "provide a checkForLoginForm method which" should {
      "detects that a page does NOT have a Login Form" in new WithHtmlAnalyzer {
        analyzer.checkForLoginForm(testDocument(obama_wiki.filePath)) shouldBe false
      }

      "detects a page that only contains a single login form" in new WithHtmlAnalyzer {
        analyzer.checkForLoginForm(testDocument(gitHubLogin.filePath)) shouldBe true
      }

      "detects a page that contains both login form and register form" in new WithHtmlAnalyzer {
        analyzer.checkForLoginForm(testDocument(linkedin_loginAndSignup.filePath)) shouldBe true
      }
    }

    "provide a getDomainName method which" should {
      "detects the domainname of a webpage" in new WithHtmlAnalyzer {
        analyzer.getDomainName(testDocument(gitHubLogin.filePath)) shouldBe testBaseDomainName
      }
    }

    "provide a getHeadings method which" should {
      "counts occurence of html-headings grouped by heading-level" in new WithHtmlAnalyzer {
        analyzer.getHeadings(testDocument(obama_wiki.filePath)) shouldBe List(
          ("h1", 1),
          ("h2", 12),
          ("h3", 31),
          ("h4", 26),
          ("h5", 2)
        )
      }

    }

    "provide a getHyperlinks which" should {
      "returns all hyperlinks in the webpage, grouped by whether they link to an internal or external location" in new WithHtmlAnalyzer {
        val hyperLinks: Map[LinkType, Seq[String]] = analyzer.getHyperlinks(testDocument(gitHubLogin.filePath))
        hyperLinks shouldBe Map(
          NoLink -> ArrayBuffer(
            "#start-of-content",
            "",
            ""),
          InternalLink -> ArrayBuffer(
            "https://test.com/",
            "/password_reset",
            "/join?source=login",
            "https://test.com/site/terms",
            "https://test.com/site/privacy",
            "https://test.com/security",
            "https://test.com/contact")
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

