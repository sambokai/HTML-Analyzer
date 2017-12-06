package controllers

import javax.inject.{Inject, Singleton}

import controllers.WebsiteForm._
import play.api.data.Form
import play.api.mvc._

// TODO: learn more about Play Forms
// TODO: learn about implicit parameters


@Singleton
class HttpAnalyzerController @Inject()(messagesAction: MessagesActionBuilder, components: ControllerComponents) extends AbstractController(components) {
  private val postUrl = routes.HttpAnalyzerController.post()


  def index = messagesAction { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.HttpAnalyzer(form, postUrl))
  }

  def post = messagesAction { implicit request: MessagesRequest[AnyContent] =>

    val errorFunction = { formWithErrors: Form[Data] =>
      println("Error: " + formWithErrors) // Debug
      BadRequest(views.html.HttpAnalyzer(formWithErrors, postUrl))
    }

    val successFunction = { data: Data =>
      println("Success: " + data) // Debug
      Redirect(routes.HttpAnalyzerController.index()).flashing("info" -> "Website analyzed.")
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}
