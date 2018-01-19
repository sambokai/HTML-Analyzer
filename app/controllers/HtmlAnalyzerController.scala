package controllers

import javax.inject.{Inject, Singleton}

import domain.WebPage
import play.api.mvc._
import services.HtmlAnalyzer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class HtmlAnalyzerController @Inject()(htmlAnalyzer: HtmlAnalyzer, messagesAction: MessagesActionBuilder, components: ControllerComponents) extends AbstractController(components) with play.api.i18n.I18nSupport {

  private val postUrl = routes.HtmlAnalyzerController.triggerAnalysis()

  def index = Action { implicit request =>
    Ok(views.html.HtmlAnalyzerView(None, WebsiteForm.urlAnalysisForm, postUrl))
  }


  def triggerAnalysis = Action.async { implicit request =>
    val formData = WebsiteForm.urlAnalysisForm.bindFromRequest

    val result: Future[WebPage] = htmlAnalyzer.analyze(formData.data("url"))

    result.map(webpage =>
      Ok(views.html.HtmlAnalyzerView(Some(webpage), WebsiteForm.urlAnalysisForm, postUrl))
    )
  }
}
