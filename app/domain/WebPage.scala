package domain

import services.LinkCheckerImpl.AvailabilitiesByLinkTarget

case class WebPage(
                    location: String,

                    title: String,

                    domainName: String,

                    headings: Seq[(String, Int)],

                    hyperlinks: AvailabilitiesByLinkTarget,

                    hasLoginForm: Boolean,

                    html_version: HTMLVersion
                  )