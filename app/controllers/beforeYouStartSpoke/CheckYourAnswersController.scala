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

package controllers.beforeYouStartSpoke

import controllers.Retrievals
import controllers.actions._
import helpers.cya.BeforeYouStartCYAHelper
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils._

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            cyaHelper: BeforeYouStartCYAHelper,
                                            val controllerComponents: MessagesControllerComponents,
                                            renderer: Renderer
                                          )(implicit val ec: ExecutionContext)
  extends FrontendBaseController
    with Enumerable.Implicits
    with I18nSupport
    with Retrievals {

  def onPageLoad: Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>

        // Workaround to enabled the Change link for type of scheme:-
        val isEnabledChange = request.request.getQueryString("cgl22").contains("true")

        val json = Json.obj(
          "list" -> cyaHelper.rowsForCYA(isEnabledChange),
          "schemeName" -> existingSchemeName,
          "submitUrl" -> controllers.routes.TaskListController.onPageLoad().url
        )

        renderer.render("check-your-answers.njk", json).map(Ok(_))
    }
}
