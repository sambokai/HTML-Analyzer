package services

import java.io.File

import domain.{HTMLVersion, WebPage}
import org.jsoup.nodes.Document
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

class HtmlAnalyzerTest extends PlaySpec with MockitoSugar {

  import HtmlAnalyzerTest._

  "HtmlAnalyzerTest" should {

    "detect the Html Version of a document" in {
      val analyzer = new HtmlAnalyzer(httpResolver)
      analyzer.getHtmlVersion(documentFromResource(gitHubLogin)) mustBe HTMLVersion.HTML5
      analyzer.getHtmlVersion(documentFromResource(w3c_html4_01_spec)) mustBe HTMLVersion.HTML4_01
      analyzer.getHtmlVersion(documentFromResource(ieIsEvil_html4_00)) mustBe HTMLVersion.Unknown
    }

    "determine the count of html-heading-tags by heading level" in {
      val analyzer = new HtmlAnalyzer(httpResolver)

      analyzer.getHeadings(documentFromResource(obama_wiki))("h1") mustBe 1
      analyzer.getHeadings(documentFromResource(obama_wiki))("h2") mustBe 12
      analyzer.getHeadings(documentFromResource(obama_wiki))("h3") mustBe 31
      analyzer.getHeadings(documentFromResource(obama_wiki))("h4") mustBe 26
      analyzer.getHeadings(documentFromResource(obama_wiki))("h5") mustBe 2
    }

    "detect # of internal links and # of external links in a webpage" in {
      val analyzer = new HtmlAnalyzer(httpResolver)

      val spiegelPage = documentFromResource(spiegelLogin)
      val wikiPage = documentFromResource(obama_wiki)

      analyzer.getHyperlinks(spiegelPage)(true).size mustBe 245
      analyzer.getHyperlinks(spiegelPage)(false).size mustBe 76


      analyzer.getHyperlinks(wikiPage)(true).size mustBe 3994
      analyzer.getHyperlinks(wikiPage)(false).size mustBe 1092
    }

    "detect the domain name from where the document is served" in {
      val analyzer = new HtmlAnalyzer(httpResolver)

      analyzer.getDomainName(documentFromResource(spiegelLogin)) mustBe "spiegel.de"
    }

    "detect if a page contains a login form" in {
      val analyzer = new HtmlAnalyzer(httpResolver)

      analyzer.checkForLoginForm(documentFromResource(gitHubLogin)) mustBe true
      analyzer.checkForLoginForm(documentFromResource(ieIsEvil_html4_00)) mustBe false
    }

    "detect if a page with signup AND login contains a login" in {
      val analyzer = new HtmlAnalyzer(httpResolver)

      analyzer.checkForLoginForm(documentFromResource(linkedin_loginAndSignup)) mustBe true

    }

    "detect if a link directs to an internal location / the same domain" in {
      val analyzer = new HtmlAnalyzer(httpResolver)

      analyzer.isInternal("https://www.w3.org/TR/1999/REC-html401-19991224/", "w3.org") mustBe true
      analyzer.isInternal("https://www.w3.org/TR/1999/REC-html401-19991224/", "google.com") mustBe false
    }


  }
}

object HtmlAnalyzerTest {
  private val httpResolver = MockitoSugar.mock[HttpResolver]

  case class TestWebsite(filePath: String, baseUri: String)

  val gitHubLogin = TestWebsite("/WebsiteSnapshots/github_login_08122017.htm", "https://www.github.com/login")
  val spiegelLogin = TestWebsite("/WebsiteSnapshots/spiegel-online_login_08122017.htm", "https://www.spiegel.de/meinspiegel/login.html")
  val w3c_html4_01_spec = TestWebsite("/WebsiteSnapshots/W3C_recommendation_HTML4-01Specification_12122017.htm", "https://www.w3.org/TR/1999/REC-html401-19991224/")
  val ieIsEvil_html4_00 = TestWebsite("/WebsiteSnapshots/InternetExplorer-Is-Evil_12122017.htm", "http://toastytech.com/evil/")
  val obama_wiki = TestWebsite("/WebsiteSnapshots/BarackObama_Wikipedia_13122017.htm", "https://en.wikipedia.org/wiki/Barack_Obama")
  val linkedin_loginAndSignup = TestWebsite("/WebsiteSnapshots/linkedin_loginandsignup_13122017.htm", "https://www.linkedin.com")

  def webpageFromResource(website: TestWebsite): WebPage = {
    val path = getClass.getResource(website.filePath).toURI
    val testFile = new File(path)

    new HtmlAnalyzer(httpResolver).analyze(testFile, website.baseUri)
  }

  def documentFromResource(website: TestWebsite): Document = {
    val path = getClass.getResource(website.filePath).toURI
    val testFile = new File(path)

    val analyzer = new HtmlAnalyzer(httpResolver)

    analyzer.getDocument(testFile, website.baseUri)
  }
}