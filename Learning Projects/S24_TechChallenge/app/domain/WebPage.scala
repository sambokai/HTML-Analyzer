package domain

case class WebPage(
                    location: String,

                    title: String,

                    domainName: String,

                    headings: Seq[(String, Int)],

                    hyperlinks: Map[Boolean, Seq[String]],

                    hasLoginForm: Boolean,

                    html_version: HTMLVersion
                  )