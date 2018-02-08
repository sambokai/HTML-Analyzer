package controllers

import javax.inject.{Inject, Singleton}

import domain.WebPage
import play.api.mvc._
import services.HtmlAnalyzer

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HtmlAnalyzerController @Inject()(htmlAnalyzer: HtmlAnalyzer, messagesAction: MessagesActionBuilder, components: ControllerComponents) extends AbstractController(components) with play.api.i18n.I18nSupport {

  private val postUrl = routes.HtmlAnalyzerController.triggerAnalysis()

  def index = Action { implicit request =>
    Ok(views.html.HtmlAnalyzerView(None, WebsiteForm.urlAnalysisForm, postUrl, None))
  }


  def triggerAnalysis: Action[AnyContent] = Action.async { implicit request =>
    val formData = WebsiteForm.urlAnalysisForm.bindFromRequest

    htmlAnalyzer.analyze(formData.data("url")).map {
      case Right(webpage: WebPage) => Ok(views.html.HtmlAnalyzerView(Some(webpage), WebsiteForm.urlAnalysisForm, postUrl, None))
      case Left(errorMessage: String) => BadRequest(views.html.HtmlAnalyzerView(None, WebsiteForm.urlAnalysisForm, postUrl, Some(errorMessage)))
    }.recover {
      case e => BadRequest(views.html.HtmlAnalyzerView(None, WebsiteForm.urlAnalysisForm, postUrl, Some("Something went wrong. Please try again or contact us. " + e)))
    }
  }
}
