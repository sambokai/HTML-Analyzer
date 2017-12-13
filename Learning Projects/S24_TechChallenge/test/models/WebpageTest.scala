package models

import java.io.InputStream

import models.HTMLVersion._
import org.scalatestplus.play.PlaySpec
import play.api.Environment


private[models] object TestWebsites {

  case class TestWebsite(filePath: String, baseUri: String)

  val gitHubLogin = TestWebsite("WebsiteSnapshots/github_login_08122017.htm", "https://www.github.com/login")
  val spiegelLogin = TestWebsite("WebsiteSnapshots/spiegel-online_login_08122017.htm", "https://www.spiegel.de/meinspiegel/login.html")
  val w3c_html4_01_spec = TestWebsite("WebsiteSnapshots/W3C_recommendation_HTML4-01Specification_12122017.htm", "https://www.w3.org/TR/1999/REC-html401-19991224/")
  val ieIsEvil_html4_00 = TestWebsite("WebsiteSnapshots/InternetExplorer-Is-Evil_12122017.htm", "http://toastytech.com/evil/")
  val obama_wiki = TestWebsite("WebsiteSnapshots/BarackObama_Wikipedia_13122017.htm", "https://en.wikipedia.org/wiki/Barack_Obama")
  val linkedin_loginAndSignup = TestWebsite("WebsiteSnapshots/linkedin_loginandsignup_13122017.htm", "https://www.linkedin.com")

  def webpageFromResource(website: TestWebsite): Webpage = {
    val testpath: InputStream = Environment.simple().classLoader.getResource(website.filePath).openStream()
    val testpage = new Webpage(testpath, website.baseUri)
    testpath.close()
    testpage
  }
}

class WebpageTest extends PlaySpec {


  "A Webpage" must {
    import TestWebsites._

    "retrieve a webpage document from a local html file" in {
      webpageFromResource(gitHubLogin)
    }

    "Retrieve a webpage document from network via full protocol-prefixed URL" in {
      val testurl = new UrlWrapper("http://example.com/")
      new Webpage(testurl)
    }

    "retrieve a webpage document from network via shortened, non-protocol URL" in {
      val testurl = new UrlWrapper("example.com")
      new Webpage(testurl)
    }

    "retrieve the page title of a webpage" in {
      val testpage = webpageFromResource(gitHubLogin)

      testpage.title mustBe "Sign in to GitHub · GitHub"
    }

    "detect if a page contains a login form" in {
      val testpage = webpageFromResource(gitHubLogin)
      val falsetest = webpageFromResource(ieIsEvil_html4_00)

      testpage.hasLoginForm mustBe true
      falsetest.hasLoginForm mustBe false
    }

    "detect if a page with signup AND login contains a login" in {
      val signupAndLoginPage = webpageFromResource(linkedin_loginAndSignup)

      signupAndLoginPage.hasLoginForm mustBe true
    }

    "retrieve the resolved full url from where the document is served" in {
      val testpage = webpageFromResource(gitHubLogin)

      testpage.location mustBe gitHubLogin.baseUri
    }

    "retrieve the domain name from where the document is served" in {
      val testpage = webpageFromResource(spiegelLogin)

      testpage.domainName mustBe "spiegel.de"
    }

    "detect # of internal links and # of external links in a webpage" in {
      val spiegelPage = webpageFromResource(spiegelLogin)
      val wikiPage = webpageFromResource(obama_wiki)

      spiegelPage.hyperlinks(true).size mustBe 245
      spiegelPage.hyperlinks(false).size mustBe 76


      wikiPage.hyperlinks(true).size mustBe 3994
      wikiPage.hyperlinks(false).size mustBe 1092
    }

    "determine the count of html-heading-tags by heading level " in {
      val testpage = webpageFromResource(obama_wiki)
      testpage.headings("h1") mustBe 1
      testpage.headings("h2") mustBe 12
      testpage.headings("h3") mustBe 31
      testpage.headings("h4") mustBe 26
      testpage.headings("h5") mustBe 2
    }

    "detect html version 5 in a webpage" in {
      val testpage = webpageFromResource(gitHubLogin)
      testpage.html_version mustBe HTML5
    }

    "detect html version 4.1 in a webpage" in {
      val testpage = webpageFromResource(w3c_html4_01_spec)
      testpage.html_version mustBe HTML4_01
    }

    "detect unknown html version / missing version-string in a webpage" in {
      val testpage = webpageFromResource(ieIsEvil_html4_00)
      testpage.html_version mustBe Unknown
    }

  }
}
