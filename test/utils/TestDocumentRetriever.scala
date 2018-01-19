package utils

import java.io.File

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import services.DocumentRetriever

class TestDocumentRetriever extends DocumentRetriever {

  import TestDocumentRetriever._

  override def get(location: String): Document = {
    val path = getClass.getResource(location).toURI
    val testFile = new File(path)
    Jsoup.parse(testFile, null, testBaseUri)
  }
}

object TestDocumentRetriever {
  val testBaseDomainName: String = "test.com"
  val testBaseUri: String = s"http://www.$testBaseDomainName/testfolder/testfile?testquery=123"
}
