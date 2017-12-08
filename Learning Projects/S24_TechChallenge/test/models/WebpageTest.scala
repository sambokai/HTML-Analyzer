package models

import org.scalatestplus.play.PlaySpec

class WebpageTest extends PlaySpec {

  "A Webpage" must {

    // TODO: implement non-networking tests. Meaning that the webpages-to-be-tested should exist as html copy inside the project directory and not be pulled from the live website.

    "Retrieve a website document via URL" in {
      val testurl = "http://example.com/"
      new Webpage(testurl)
    }

    "retrieve the page title of a webpage" in {
      val testurl = "http://example.com/"
      val testpage = new Webpage(testurl)

      testpage.title mustBe "Example Domain"
    }


    "retrieve the resolved full url from where the document is served" in {
      val testurl = "http://example.com/"
      val testpage = new Webpage(testurl)

      testpage.location mustBe testurl
    }

    "retrieve the domain name from where the document is served" in {
      val testurl = "https://en.wikipedia.org/wiki/Shrek"
      val testpage = new Webpage(testurl)

      testpage.domainName mustBe "en.wikipedia.org"
    }

    "determine the count of html-heading-tags by heading level " in {
      val testurl = "http://example.com/"
      val testpage = new Webpage(testurl)

      val headings = testpage.headings

      headings("h1") mustBe 1
    }

    "detect html version 5 in a webpage" in {
      val testurl = "http://example.com/"
      val testpage = new Webpage(testurl)
      testpage.html_version mustBe HTMLVersion.HTML5
    }

    "detect html version 4.1 in a webpage" in {
      val testurl = "https://www.w3.org/TR/1999/REC-html401-19991224/"
      val testpage = new Webpage(testurl)
      testpage.html_version mustBe HTMLVersion.HTML4_01
    }

    "detect unknown html version / missing version-string in a webpage" in {
      val testurl = "http://www.lost-world.com:80/ingen/index.html"
      val testpage = new Webpage(testurl)
      testpage.html_version mustBe HTMLVersion.Unknown
    }

  }
}
