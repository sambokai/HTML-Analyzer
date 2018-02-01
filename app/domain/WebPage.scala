package domain

import services.HtmlAnalyzer

case class WebPage(
                    location: String,

                    title: String,

                    domainName: String,

                    headings: Seq[(String, Int)],

                    hyperlinks: Map[HtmlAnalyzer.LinkType, Seq[String]],

                    hasLoginForm: Boolean,

                    html_version: HTMLVersion
                  )