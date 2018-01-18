package controllers

import play.api.data.Form
import play.api.data.Forms._

object WebsiteForm {

  case class Data(url: String)

  val urlAnalysisForm = Form(
    mapping(
      "url" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )


}

