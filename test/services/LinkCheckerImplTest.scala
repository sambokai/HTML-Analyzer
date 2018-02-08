package services

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.mockito.MockitoSugar
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import services.LinkCheckerImpl._

import scala.concurrent.Future

class TestLinkCheckClient extends LinkCheckClient {

  import TestLinkCheckClient._

  override def forUrl(url: String): Future[LinkAvailability] = Future.successful(testAvailability(url))

}

object TestLinkCheckClient {
  def testAvailability(url: String): LinkAvailability = LinkAvailability(url, StatusCode.int2StatusCode(200))
}

trait WithLinkCheckClient {
  private val linkCheckClient = new TestLinkCheckClient
  val linkChecker = new LinkCheckerImpl(linkCheckClient)
}

class LinkCheckerImplTest extends WordSpec with MockitoSugar with FutureAwaits with DefaultAwaitTimeout {

  import TestLinkCheckClient._

  "LinkCheckerImplTest" should {

    "provide a getAvailabilityForLinks method " should {

      "check the http response-codes of a sequence of valid links and return them sorted by whether the links are internal or external " in new WithLinkCheckClient {
        val internalTestLinks = Seq(
          "https://www.test.com",
          "https://www.test.com/login"
        )

        val externalTestLinks = Seq(
          "https://en.wikipedia.org/wiki/Madonna",
          "http://google.com/search"
        )

        val result: Future[AvailabilitiesByLinkTarget] = linkChecker.getAvailabilityForLinks(internalTestLinks ++ externalTestLinks, "test.com")

        await(result) shouldBe Map(InternalLink -> internalTestLinks.map(testAvailability), ExternalLink -> externalTestLinks.map(testAvailability))
      }

      "check the http response-codes of a valid link-sequence containing relative links (i.e. /login) and return them sorted by whether the links are internal or external " in new WithLinkCheckClient {

        val relativeTestLinks = Seq(
          "/login",
          "/"
        )

        val absoluteTestLinks = Seq(
          "http://test.com/login",
          "http://test.com/"
        )

        val result: Future[AvailabilitiesByLinkTarget] = linkChecker.getAvailabilityForLinks(relativeTestLinks, "test.com")

        await(result) shouldBe Map(InternalLink -> absoluteTestLinks.map(testAvailability))
      }

      "check the http response-codes of a sequence containing invalid links and return them sorted by whether the links are internal or external " in new WithLinkCheckClient {

        val invalidTestLinks = Seq(
          "!@#",
          "#anchor",
          ""
        )

        val result: Future[AvailabilitiesByLinkTarget] = linkChecker.getAvailabilityForLinks(invalidTestLinks, "test.com")

        await(result) shouldBe Map.empty
      }

      "check links that occur multiple times only once" in new WithLinkCheckClient {
        val duplicateInternalLinks = Seq(
          "https://www.test.com",
          "https://www.test.com",
          "https://www.test.com",
          "https://www.test.com"
        )

        val duplicateExternalLinks = Seq(
          "https://en.wikipedia.org/wiki/Madonna",
          "https://en.wikipedia.org/wiki/Madonna",
          "https://en.wikipedia.org/wiki/Madonna",
          "https://en.wikipedia.org/wiki/Madonna",
          "https://en.wikipedia.org/wiki/Madonna"
        )

        val result: Future[AvailabilitiesByLinkTarget] = linkChecker.getAvailabilityForLinks(duplicateInternalLinks ++ duplicateExternalLinks, "test.com")

        await(result) shouldBe Map(InternalLink -> Seq(testAvailability("https://www.test.com")), ExternalLink -> Seq(testAvailability("https://en.wikipedia.org/wiki/Madonna")))
      }
    }


    "provide a resolve method" should {
      "get the http response-code of a link and return it as an Availability" in new WithLinkCheckClient {
        val testUrl = "https://test.com/123"
        val testdomain = "test.com"
        await(linkChecker.resolve(testUrl, testdomain)) shouldBe(InternalLink, LinkAvailability(testUrl, StatusCodes.OK))
      }
    }


    "provide an isInternal method" should {
      "detect that a link directs to the same domain as a predefined base-domain" in new WithLinkCheckClient {
        linkChecker.getLinkTargetDomain("http://login.mail.test.com/de?username=123", "test.com") shouldBe InternalLink
      }

      "detect that a link directs to a domain that is different than the predefined base-domain" in new WithLinkCheckClient {
        linkChecker.getLinkTargetDomain("http://de.google.com/mail/de/login", "test.com") shouldBe ExternalLink
      }

      "return NoLink for html anchors " in new WithLinkCheckClient {
        linkChecker.getLinkTargetDomain("#about-us", "test.com") shouldBe NoLink
      }
    }

  }
}
