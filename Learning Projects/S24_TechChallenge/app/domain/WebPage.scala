package domain

case class WebPage(
                    location: String,

                    title: String,

                    domainName: String,

                    headings: scala.collection.immutable.ListMap[String, Int],

                    hyperlinks: Map[Boolean, scala.collection.mutable.Buffer[String]],

                    hasLoginForm: Boolean,

                    html_version: HTMLVersion
                  )