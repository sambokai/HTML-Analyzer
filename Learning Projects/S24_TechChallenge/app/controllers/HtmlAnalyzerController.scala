package controllers

import javax.inject.{Inject, Singleton}

import controllers.WebsiteForm._
import play.api.data.Form
import play.api.mvc._
import services.HtmlAnalyzer
import utils.UrlWrapper

@Singleton
class HtmlAnalyzerController @Inject()(htmlAnalyzer: HtmlAnalyzer, messagesAction: MessagesActionBuilder, components: ControllerComponents) extends AbstractController(components) {
  private val postUrl = routes.HtmlAnalyzerController.post()

  // TODO: save list of analyzed websites, in order to show the history of analyzed sites as a list on the frontend

  def index = messagesAction { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.HtmlAnalyzer(None, form, postUrl))
  }

  def post = messagesAction { implicit request: MessagesRequest[AnyContent] =>

    def errorFunction = { formWithErrors: Form[Data] =>
      BadRequest(views.html.HtmlAnalyzer(None, formWithErrors, postUrl))
    }

    def successFunction = { data: Data =>
      Ok(views.html.HtmlAnalyzer(Some(htmlAnalyzer.analyze(new UrlWrapper(data.url))), form, postUrl))
    }

    def formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}
