package utils

import java.net.URL

class UrlWrapper(path: String) {
  private val prefixedPath = if ("^(https?:\\/\\/)".r.findPrefixOf(path).isEmpty) "http://" + path else path
  val url: URL = new URL(prefixedPath)
}