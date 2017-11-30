package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, ControllerComponents}



@Singleton
class HttpAnalyzerController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  private val postUrl = routes.HttpAnalyzerController.analyzeURL()

  def showAnalysis = Action {
    Ok(views.html.HttpAnalyzer(postUrl))
  }


  def analyzeURL = Action {
    BadRequest(views.html.HttpAnalyzer(postUrl)) // TODO: implement POST method here.
  }
}
