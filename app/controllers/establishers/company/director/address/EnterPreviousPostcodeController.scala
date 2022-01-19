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

package controllers.establishers.company.director.address

import config.AppConfig
import connectors.AddressLookupConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.PostcodeController
import forms.address.PostcodeFormProvider
import identifiers.beforeYouStart.SchemeNameId
import identifiers.establishers.company.director.DirectorNameId
import identifiers.establishers.company.director.address.EnterPreviousPostCodeId
import identifiers.trustees.individual.address.{EnterPreviousPostCodeId => trusteeEnterPreviousPostCodeId}
import models._
import models.requests.DataRequest
import navigators.CompoundNavigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.DataUpdateService
import uk.gov.hmrc.nunjucks.NunjucksSupport
import utils.UserAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class EnterPreviousPostcodeController @Inject()(val appConfig: AppConfig,
                                                override val messagesApi: MessagesApi,
                                                val userAnswersCacheConnector: UserAnswersCacheConnector,
                                                val addressLookupConnector: AddressLookupConnector,
                                                val navigator: CompoundNavigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: PostcodeFormProvider,
                                                dataUpdateService: DataUpdateService,
                                                val controllerComponents: MessagesControllerComponents,
                                                val renderer: Renderer
                                               )(implicit val ec: ExecutionContext) extends PostcodeController with I18nSupport with NunjucksSupport {

  def onPageLoad(establisherIndex: Index, directorIndex: Index, mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async { implicit request =>
      retrieve(SchemeNameId) { schemeName =>
        get(getFormToJson(schemeName, establisherIndex, directorIndex, mode))
      }
    }

  def onSubmit(establisherIndex: Index, directorIndex: Index, mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData()).async {
    implicit request =>
      retrieve(SchemeNameId) { schemeName =>
        val formToJson: Form[String] => JsObject = getFormToJson(schemeName, establisherIndex, directorIndex, mode)
        form.bindFromRequest().fold(
          formWithErrors =>
            renderer.render(viewTemplate, prepareJson(formToJson(formWithErrors))).map(BadRequest(_)),
          value =>
            addressLookupConnector.addressLookupByPostCode(value).flatMap {
              case Nil =>
                val json = prepareJson(formToJson(formWithError("enterPostcode.noresults")))
                renderer.render(viewTemplate, json).map(BadRequest(_))

              case addresses =>
                for {
                  updatedAnswers <- Future.fromTry(setUpdatedAnswers(establisherIndex, directorIndex, mode, addresses, request.userAnswers))
                  _ <- userAnswersCacheConnector.save(request.lock, updatedAnswers.data)
                } yield {
                  val finalMode = Some(mode).getOrElse(NormalMode)
                  Redirect(navigator.nextPage(EnterPreviousPostCodeId(establisherIndex, directorIndex), updatedAnswers, finalMode))
                }
            }
        )
      }
  }

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", s"messages__error__postcode_$messageKey")
  }

  def form: Form[String] = formProvider("individualEnterPreviousPostcode.required", "establisherEnterPreviousPostcode.invalid")

  def setUpdatedAnswers(establisherIndex: Index, directorIndex: Index, mode: Mode, value: Seq[TolerantAddress], ua: UserAnswers): Try[UserAnswers] = {
    var updatedUserAnswers: Try[UserAnswers] = Try(ua)
    if (mode == CheckMode) {
      val trustee = dataUpdateService.findMatchingTrustee(establisherIndex, directorIndex)(ua)
      if (trustee.isDefined)
        updatedUserAnswers = ua.set(trusteeEnterPreviousPostCodeId(trustee.get.index), value)
    }
    val finalUpdatedUserAnswers = updatedUserAnswers.get.set(EnterPreviousPostCodeId(establisherIndex, directorIndex), value)
    finalUpdatedUserAnswers
  }


  def getFormToJson(schemeName: String, establisherIndex: Index, directorIndex: Index, mode: Mode)
                   (implicit request: DataRequest[AnyContent]): Form[String] => JsObject = {
    form => {
      val msg = request2Messages(request)
      val name = request.userAnswers.get(DirectorNameId(establisherIndex, directorIndex)).map(_.fullName).getOrElse(msg("messages__director"))
      Json.obj(
        "entityType" -> msg("messages__director"),
        "entityName" -> name,
        "form" -> form,
        "enterManuallyUrl" -> routes.ConfirmPreviousAddressController.onPageLoad(establisherIndex, directorIndex, mode).url,
        "schemeName" -> schemeName,
        "h1MessageKey" -> "previousPostcode.title"
      )
    }
  }
}
