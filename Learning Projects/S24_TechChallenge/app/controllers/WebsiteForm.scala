package controllers

import java.net.URL

import play.api.data.Form
import play.api.data.Forms._


object WebsiteForm {

  case class Data(url: String)

  val form = Form(
    mapping(
      "url" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )


}

