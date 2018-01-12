package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc._
import services.HtmlAnalyzer

@Singleton
class HtmlAnalyzerController @Inject()(htmlAnalyzer: HtmlAnalyzer, messagesAction: MessagesActionBuilder, components: ControllerComponents) extends AbstractController(components) {

}
