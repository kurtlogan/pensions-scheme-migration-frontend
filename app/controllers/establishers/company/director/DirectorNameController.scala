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

import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.PersonNameFormProvider
import identifiers.establishers.company.director.DirectorNameId
import identifiers.trustees.individual.TrusteeNameId
import models.{CheckMode, Index, Mode, PersonName}
import navigators.CompoundNavigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.DataUpdateService
import uk.gov.hmrc.nunjucks.NunjucksSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DirectorNameController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        navigator: CompoundNavigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: PersonNameFormProvider,
                                        dataUpdateService: DataUpdateService,
                                        val controllerComponents: MessagesControllerComponents,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        renderer: Renderer
                                      )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with Retrievals
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(establisherIndex: Index, directorIndex: Index, mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>
        renderer.render(
          template = "personName.njk",
          ctx = Json.obj(
            "form" -> request.userAnswers.get[PersonName](DirectorNameId(establisherIndex, directorIndex)).fold(form)(form.fill),
            "schemeName" -> existingSchemeName,
            "entityType" -> Messages("messages__director")
          )
        ).flatMap(view => Future.successful(Ok(view)))
    }

  def onSubmit(establisherIndex: Index, directorIndex: Index, mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            renderer.render(
              template = "personName.njk",
              ctx = Json.obj(
                "form" -> formWithErrors,
                "schemeName" -> existingSchemeName,
                "entityType" -> Messages("messages__director")
              )
            ).map(BadRequest(_)),
          value =>
            for {
              updatedAnswers <- Future.fromTry(setUpdatedAnswers(establisherIndex, directorIndex, mode, value, request.userAnswers))
              _ <- userAnswersCacheConnector.save(request.lock, updatedAnswers.data)
            } yield
              Redirect(navigator.nextPage(DirectorNameId(establisherIndex, directorIndex), updatedAnswers, mode))
        )
    }

  private def form(implicit messages: Messages): Form[PersonName] =
    formProvider("messages__error__director")

  private def setUpdatedAnswers(establisherIndex: Index, directorIndex: Index, mode: Mode, value: PersonName, ua: UserAnswers): Try[UserAnswers] = {
    val updatedUserAnswers =
      mode match {
        case CheckMode =>
          dataUpdateService.findMatchingTrustee(establisherIndex, directorIndex)(ua).map { trustee =>
            ua.setOrException(TrusteeNameId(trustee.index), value)
          }.getOrElse(ua)
        case _ => ua
      }
    val finalUpdatedUserAnswers = updatedUserAnswers.set(DirectorNameId(establisherIndex, directorIndex), value)
    finalUpdatedUserAnswers
  }

}
