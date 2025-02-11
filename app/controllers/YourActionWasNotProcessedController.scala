/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import helpers.cya.CYAHelper
import identifiers.beforeYouStart.SchemeNameId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class YourActionWasNotProcessedController @Inject()(override val messagesApi: MessagesApi,
                                                    authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    renderer: Renderer
                                     )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport {

  def onPageLoadScheme: Action[AnyContent] = (authenticate andThen getData andThen requireData()).async {
    implicit request =>
        renderer.render(
          template = "yourActionWasNotProcessed.njk",
          ctx =  schemeJson(request.userAnswers)
        ).map(Ok(_))
  }

  def onPageLoadRacDac: Action[AnyContent] = (authenticate andThen getData andThen requireData()).async {
    implicit request =>
        renderer.render(
          template = "yourActionWasNotProcessed.njk",
          ctx = racDacJson(request.userAnswers)
        ).map(Ok(_))
  }

  private def schemeJson(userAnswers:UserAnswers):JsObject = {
    Json.obj(
      "schemeName" -> CYAHelper.getAnswer(SchemeNameId)(userAnswers, implicitly),
      "returnUrl" -> controllers.routes.TaskListController.onPageLoad.url
    )
  }

  private def racDacJson(userAnswers:UserAnswers):JsObject = {
    Json.obj(
      "schemeName" -> CYAHelper.getAnswer(SchemeNameId)(userAnswers, implicitly),
      "returnUrl" -> controllers.racdac.individual.routes.CheckYourAnswersController.onPageLoad.url
    )
  }
}

