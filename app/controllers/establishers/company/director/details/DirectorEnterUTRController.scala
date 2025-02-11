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
import controllers.EnterReferenceValueController
import controllers.actions._
import forms.UTRFormProvider
import identifiers.beforeYouStart.SchemeNameId
import identifiers.establishers.company.director.DirectorNameId
import identifiers.establishers.company.director.details.DirectorEnterUTRId
import identifiers.trustees.individual.details.TrusteeUTRId
import models.requests.DataRequest
import models.{CheckMode, Index, Mode, ReferenceValue}
import navigators.CompoundNavigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.DataUpdateService
import utils.UserAnswers
import viewmodels.Message

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DirectorEnterUTRController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            val navigator: CompoundNavigator,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: UTRFormProvider,
                                            dataUpdateService: DataUpdateService,
                                            val controllerComponents: MessagesControllerComponents,
                                            val userAnswersCacheConnector: UserAnswersCacheConnector,
                                            val renderer: Renderer
                                          )(implicit val executionContext: ExecutionContext)
  extends EnterReferenceValueController {

  def onPageLoad(establisherIndex: Index, directorIndex: Index, mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>
        SchemeNameId.retrieve.right.map {
          schemeName =>
            get(
              pageTitle = Message("messages__enterUTR", Message("messages__director")),
              pageHeading = Message("messages__enterUTR", name(establisherIndex, directorIndex)),
              isPageHeading = true,
              id = DirectorEnterUTRId(establisherIndex, directorIndex),
              form = form,
              schemeName = schemeName,
              legendClass = "govuk-visually-hidden",
              paragraphText = Seq(Message("messages__UTR__p1"), Message("messages__UTR__p2"))
            )
        }
    }

  private def name(establisherIndex: Index, directorIndex: Index)
                  (implicit request: DataRequest[AnyContent]): String =
    request
      .userAnswers
      .get(DirectorNameId(establisherIndex, directorIndex))
      .fold("the director")(_.fullName)

  private def form: Form[ReferenceValue] = formProvider()

  def onSubmit(establisherIndex: Index, directorIndex: Index, mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>
        SchemeNameId.retrieve.right.map {
          schemeName =>
            form.bindFromRequest().fold(
              (formWithErrors: Form[_]) =>
                renderer.render(
                  template = templateName(Seq(Message("messages__UTR__p1"), Message("messages__UTR__p2")), None),
                  ctx = Json.obj(
                    "pageTitle" -> Message("messages__enterUTR", Message("messages__director")),
                    "pageHeading" -> Message("messages__enterUTR", name(establisherIndex, directorIndex)),
                    "isPageHeading" -> true,
                    "form" -> formWithErrors,
                    "schemeName" -> schemeName,
                    "legendClass" -> "govuk-visually-hidden",
                    "paragraphs" -> Seq(Message("messages__UTR__p1"), Message("messages__UTR__p2"))
                  )
                ).map(BadRequest(_)),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(setUpdatedAnswers(establisherIndex, directorIndex, mode, value, request.userAnswers))
                  _ <- userAnswersCacheConnector.save(request.lock, updatedAnswers.data)
                } yield
                  Redirect(navigator.nextPage(DirectorEnterUTRId(establisherIndex, directorIndex), updatedAnswers, mode))
            )
        }
    }

  private def setUpdatedAnswers(establisherIndex: Index, directorIndex: Index, mode: Mode, value: ReferenceValue, ua: UserAnswers): Try[UserAnswers] = {
    val updatedUserAnswers =
      mode match {
        case CheckMode =>
          dataUpdateService.findMatchingTrustee(establisherIndex, directorIndex)(ua).map { trustee =>
            ua.setOrException(TrusteeUTRId(trustee.index), value)
          }.getOrElse(ua)
        case _ => ua
    }
    val finalUpdatedUserAnswers = updatedUserAnswers.set(DirectorEnterUTRId(establisherIndex, directorIndex), value)
    finalUpdatedUserAnswers
  }
}
