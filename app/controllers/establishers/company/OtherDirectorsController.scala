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

package controllers.establishers.company

import connectors.cache.UserAnswersCacheConnector
import controllers.HasReferenceValueController
import controllers.actions._
import forms.HasReferenceNumberFormProvider
import identifiers.beforeYouStart.SchemeNameId
import identifiers.establishers.company.OtherDirectorsId
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.CompoundNavigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import viewmodels.Message

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class OtherDirectorsController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          val navigator: CompoundNavigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: HasReferenceNumberFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          val userAnswersCacheConnector: UserAnswersCacheConnector,
                                          val renderer: Renderer
                                        )(implicit val executionContext: ExecutionContext) extends
HasReferenceValueController {

  private def form()
                  (implicit request: DataRequest[AnyContent]): Form[Boolean] =
    formProvider(
      errorMsg = Message("messages__otherDirectors__error__required")
    )

  def onPageLoad(index: Index,mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>
        SchemeNameId.retrieve.right.map {
          schemeName =>
            get(
              pageTitle = Message("messages__otherDirectors__title"),
              pageHeading = Message("messages__otherDirectors__heading"),
              isPageHeading = true,
              id = OtherDirectorsId(index),
              form = form,
              schemeName = schemeName,
              paragraphText = Seq(Message("messages__otherDirectors__lede")),
              legendClass = "govuk-visually-hidden"
            )
        }
    }

  def onSubmit(index: Index,mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>
        SchemeNameId.retrieve.right.map {
          schemeName =>
            post(
              pageTitle = Message("messages__otherDirectors__title"),
              pageHeading = Message("messages__otherDirectors__heading"),
              isPageHeading = true,
              id = OtherDirectorsId(index),
              form = form,
              schemeName = schemeName,
              paragraphText = Seq(Message("messages__otherDirectors__lede")),
              legendClass = "govuk-visually-hidden",
              mode = mode
            )
        }
    }

}
