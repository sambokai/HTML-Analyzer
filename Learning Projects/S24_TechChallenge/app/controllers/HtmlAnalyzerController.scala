package controllers

import javax.inject.{Inject, Singleton}

import controllers.WebsiteForm._
import models.{UrlWrapper, Webpage}
import play.api.data.Form
import play.api.mvc._

@Singleton
class HtmlAnalyzerController @Inject()(messagesAction: MessagesActionBuilder, components: ControllerComponents) extends AbstractController(components) {
  private val postUrl = routes.HtmlAnalyzerController.post()

  private var websiteAnalysis: Option[Webpage] = None
  // TODO: save list of analyzed websites, in order to show the history of analyzed sites as a list on the frontend

  def index = messagesAction { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.HtmlAnalyzer(websiteAnalysis, form, postUrl))
  }

  def post = messagesAction { implicit request: MessagesRequest[AnyContent] =>

    val errorFunction = { formWithErrors: Form[Data] =>
      println("Error: " + formWithErrors) // Debug
      websiteAnalysis = None
      BadRequest(views.html.HtmlAnalyzer(websiteAnalysis, formWithErrors, postUrl))
    }

    val successFunction = { data: Data =>
      websiteAnalysis = Some(new Webpage(new UrlWrapper(data.url)))
      Redirect(routes.HtmlAnalyzerController.index())
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}
