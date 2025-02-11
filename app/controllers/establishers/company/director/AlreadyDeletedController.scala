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

package controllers.establishers.company.director

import controllers.Retrievals
import controllers.actions._
import identifiers.establishers.company.director.DirectorNameId
import models.requests.DataRequest
import models.{Index, NormalMode}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Enumerable

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AlreadyDeletedController @Inject()(override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         val controllerComponents: MessagesControllerComponents,
                                         renderer: Renderer
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(establisherIndex: Index, directorIndex: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>
        directorName(establisherIndex, directorIndex) match {
          case Right(directorName) =>
            renderer.render("alreadyDeleted.njk", json(establisherIndex, directorName, existingSchemeName)).map(Ok(_))
          case Left(result) => result
        }

    }

  private def json(establisherIndex: Index, directorName: String, schemeName: Option[String])(implicit messages: Messages): JsObject = Json.obj(
    "title" -> messages("messages__alreadyDeleted__director_title"),
    "name" -> directorName,
    "schemeName" -> schemeName,
    "submitUrl" -> controllers.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(establisherIndex, NormalMode).url
  )

  private def directorName(establisherIndex: Index, directorIndex: Index)(implicit
                                                                          dataRequest: DataRequest[AnyContent])
  : Either[Future[Result], String] = {
    DirectorNameId(establisherIndex, directorIndex).retrieve.right.map(_.fullName)
  }

}
