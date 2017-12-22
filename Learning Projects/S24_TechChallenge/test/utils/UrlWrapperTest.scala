package utils

import java.net.URL

import org.scalatestplus.play.PlaySpec

class UrlWrapperTest extends PlaySpec {

  "UrlWrapperTest" should {

    "prefix an unprefixed url" in {
      val prefixed_wrapper = new UrlWrapper("example.com")
      val fullUrl = new URL("http://example.com")
      assert(prefixed_wrapper.url.equals(fullUrl))
    }
  }
}
