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

package controllers.establishers.company.director.details

import connectors.cache.UserAnswersCacheConnector
import controllers.HasReferenceValueController
import controllers.actions._
import forms.HasReferenceNumberFormProvider
import identifiers.beforeYouStart.SchemeNameId
import identifiers.establishers.company.director.DirectorNameId
import identifiers.establishers.company.director.details.DirectorHasUTRId
import identifiers.trustees.individual.details.TrusteeHasUTRId
import models.requests.DataRequest
import models.{CheckMode, Index, Mode}
import navigators.CompoundNavigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.DataUpdateService
import uk.gov.hmrc.viewmodels.Radios
import utils.UserAnswers
import viewmodels.Message

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DirectorHasUTRController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          val navigator: CompoundNavigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: HasReferenceNumberFormProvider,
                                          dataUpdateService: DataUpdateService,
                                          val controllerComponents: MessagesControllerComponents,
                                          val userAnswersCacheConnector: UserAnswersCacheConnector,
                                          val renderer: Renderer
                                        )(implicit val executionContext: ExecutionContext) extends
  HasReferenceValueController {

  def onPageLoad(establisherIndex: Index, directorIndex: Index, mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>
        SchemeNameId.retrieve.right.map {
          schemeName =>
            get(
              pageTitle = Message("messages__hasUTR", Message("messages__director")),
              pageHeading = Message("messages__hasUTR", name(establisherIndex, directorIndex)),
              isPageHeading = true,
              id = DirectorHasUTRId(establisherIndex, directorIndex),
              form = form(establisherIndex, directorIndex),
              schemeName = schemeName,
              paragraphText = Seq(Message("messages__UTR__p")),
              legendClass = "govuk-visually-hidden"
            )
        }
    }

  private def name(establisherIndex: Index, directorIndex: Index)
                  (implicit request: DataRequest[AnyContent]): String =
    request
      .userAnswers
      .get(DirectorNameId(establisherIndex, directorIndex))
      .fold("the director")(_.fullName)

  private def form(establisherIndex: Index, directorIndex: Index)
                  (implicit request: DataRequest[AnyContent]): Form[Boolean] =
    formProvider(
      errorMsg = Message("messages__genericHasUtr__error__required", name(establisherIndex, directorIndex))
    )

  def onSubmit(establisherIndex: Index, directorIndex: Index, mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>
        SchemeNameId.retrieve.right.map {
          schemeName =>
            form(establisherIndex, directorIndex).bindFromRequest().fold(
              (formWithErrors: Form[_]) =>
                renderer.render(
                  template = templateName(Seq(Message("messages__UTR__p"))),
                  ctx = Json.obj(
                    "pageTitle" -> Message("messages__hasUTR", Message("messages__director")),
                    "pageHeading" -> Message("messages__hasUTR", name(establisherIndex, directorIndex)),
                    "isPageHeading" -> true,
                    "form" -> formWithErrors,
                    "radios" -> Radios.yesNo(formWithErrors("value")),
                    "schemeName" -> schemeName,
                    "legendClass" -> "govuk-visually-hidden",
                    "paragraphs" -> Seq(Message("messages__UTR__p"))
                  )
                ).map(BadRequest(_)),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(setUpdatedAnswers(establisherIndex, directorIndex, mode, value, request.userAnswers))
                  _ <- userAnswersCacheConnector.save(request.lock, updatedAnswers.data)
                } yield
                  Redirect(navigator.nextPage(DirectorHasUTRId(establisherIndex, directorIndex), updatedAnswers, mode))
            )

        }
    }

  private def setUpdatedAnswers(establisherIndex: Index, directorIndex: Index, mode: Mode, value: Boolean, ua: UserAnswers): Try[UserAnswers] = {
    val updatedUserAnswers =
      mode match {
        case CheckMode =>
          dataUpdateService.findMatchingTrustee(establisherIndex, directorIndex)(ua).map { trustee =>
            ua.setOrException(TrusteeHasUTRId(trustee.index), value)
          }.getOrElse(ua)
        case _ => ua
    }
    val finalUpdatedUserAnswers = updatedUserAnswers.set(DirectorHasUTRId(establisherIndex, directorIndex), value)
    finalUpdatedUserAnswers
  }
}
