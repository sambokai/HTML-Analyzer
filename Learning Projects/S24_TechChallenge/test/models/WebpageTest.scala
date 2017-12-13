package models

import java.io.InputStream
import java.net.URL

import models.HTMLVersion._
import org.scalatestplus.play.PlaySpec
import play.api.Environment


private[models] object testWebsites {

  case class testWebsite(filePath: String, baseUri: String)

  val gitHubLogin = testWebsite("WebsiteSnapshots/github_login_08122017.htm", "https://www.github.com/login")
  val spiegelLogin = testWebsite("WebsiteSnapshots/spiegel-online_login_08122017.htm", "https://www.spiegel.de/meinspiegel/login.html")
  val w3c_html4_01_spec = testWebsite("WebsiteSnapshots/W3C_recommendation_HTML4-01Specification_12122017.htm", "https://www.w3.org/TR/1999/REC-html401-19991224/")
  val ieIsEvil_html4_00 = testWebsite("WebsiteSnapshots/InternetExplorer-Is-Evil_12122017.htm", "http://toastytech.com/evil/")
  val obama_wiki = testWebsite("WebsiteSnapshots/BarackObama_Wikipedia_13122017.htm", "https://en.wikipedia.org/wiki/Barack_Obama")

  def webpageFromResource(website: testWebsite): Webpage = {
    val testpath: InputStream = Environment.simple().classLoader.getResource(website.filePath).openStream()
    val testpage = new Webpage(testpath, website.baseUri)
    testpath.close()
    testpage
  }
}

class WebpageTest extends PlaySpec {


  "A Webpage" must {
    import testWebsites._

    "retrieve a webpage document from a local html file" in {
      webpageFromResource(gitHubLogin)
    }

    "Retrieve a webpage document from network via URL" in {
      val testurl = new URL("http://example.com/")
      new Webpage(testurl)
    }


    "retrieve the page title of a webpage" in {
      val testpage = webpageFromResource(gitHubLogin)

      testpage.title mustBe "Sign in to GitHub · GitHub"
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

      spiegelPage.hyperlinks(true).size mustBe 245
      spiegelPage.hyperlinks(false).size mustBe 76

      val wikiPage = webpageFromResource(obama_wiki)

      wikiPage.hyperlinks(true).size mustBe 3994
      wikiPage.hyperlinks(false).size mustBe 1092
    }

    "determine the count of html-heading-tags by heading level " in {
      val testpage = webpageFromResource(gitHubLogin)
      testpage.headings("h1") mustBe 1
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
