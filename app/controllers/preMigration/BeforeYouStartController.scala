/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.preMigration

import connectors.MinimalDetailsConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRetrievalAction}
import controllers.testonly.TestMongoController
import models.Scheme
import models.requests.OptionalDataRequest
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import renderer.Renderer
import uk.gov.hmrc.nunjucks.NunjucksSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BeforeYouStartController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           minimalDetailsConnector: MinimalDetailsConnector,
                                           userAnswersCacheConnector: UserAnswersCacheConnector,
                                           val controllerComponents: MessagesControllerComponents,
                                           val renderer: Renderer
                                         )(implicit val ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals
    with NunjucksSupport {

  def onPageLoad: Action[AnyContent] =
    (authenticate andThen getData).async {
      implicit request =>
        (request.userAnswers, request.lock) match {
          case (_, None) =>
            Future.successful(Redirect(controllers.preMigration.routes.ListOfSchemesController.onPageLoad(Scheme)))

          case (Some(ua), _) =>
            renderView

          case (None, Some(lock)) =>
            implicit val userAnswers: UserAnswers = UserAnswers(TestMongoController.data) //TODO once getSchemeDetails API is implemented, fetch data from API
            userAnswersCacheConnector.save(lock, userAnswers.data).flatMap { _ =>
              renderView
            }
        }

    }


  private def renderView(implicit request: OptionalDataRequest[_]): Future[Result]= {
    minimalDetailsConnector.getPSAName.flatMap { psaName =>
      renderer.render(
        template = "preMigration/beforeYouStart.njk",
        ctx = Json.obj(
          "pageTitle" -> Messages("messages__BeforeYouStart__title"),
          "continueUrl" -> controllers.routes.TaskListController.onPageLoad().url,
          "psaName" -> psaName,
          "returnUrl" -> controllers.routes.PensionSchemeRedirectController.onPageLoad().url
        )
      ).map(Ok(_))
    }
  }
}
