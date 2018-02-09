package controllers

import javax.inject.{Inject, Singleton}

import domain.WebPage
import play.api.Logger
import play.api.mvc._
import services.HtmlAnalyzer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.matching.Regex

@Singleton
class HtmlAnalyzerController @Inject()(htmlAnalyzer: HtmlAnalyzer, messagesAction: MessagesActionBuilder, components: ControllerComponents) extends AbstractController(components) with play.api.i18n.I18nSupport {

  import HtmlAnalyzerController._

  val logger: Logger = Logger(this.getClass)

  private val postUrl = routes.HtmlAnalyzerController.triggerAnalysis()

  def index = Action { implicit request =>
    Ok(views.html.HtmlAnalyzerView(None, WebsiteForm.urlAnalysisForm, postUrl, None))
  }


  def triggerAnalysis: Action[AnyContent] = Action.async { implicit request =>
    val formData = WebsiteForm.urlAnalysisForm.bindFromRequest
    val rawUserInput = formData.data("url")

    val userInputWithProtocol = rawUserInput match {
      case httpProtocol() => rawUserInput
      case _ => s"http://$rawUserInput"
    }

    htmlAnalyzer.analyze(userInputWithProtocol).map {
      case Right(webpage: WebPage) => Ok(views.html.HtmlAnalyzerView(Some(webpage), WebsiteForm.urlAnalysisForm, postUrl, None))
      case Left(errorMessage: String) => BadRequest(views.html.HtmlAnalyzerView(None, WebsiteForm.urlAnalysisForm, postUrl, Some(errorMessage)))
    }.recover {
      case e =>
        logger.error(s"Unexpected error", e)
        BadRequest(views.html.HtmlAnalyzerView(None, WebsiteForm.urlAnalysisForm, postUrl, Some("Something went wrong. Please try again or contact us. " + e)))
    }
  }
}

object HtmlAnalyzerController {
  val httpProtocol: Regex = "^https?:\\/\\/.*".r
}
