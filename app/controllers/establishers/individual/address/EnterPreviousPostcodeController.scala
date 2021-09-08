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

package controllers.establishers.individual.address

import config.AppConfig
import connectors.AddressLookupConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.PostcodeController
import forms.address.PostcodeFormProvider
import identifiers.beforeYouStart.SchemeNameId
import identifiers.establishers.individual.EstablisherNameId
import identifiers.establishers.individual.address.EnterPreviousPostCodeId
import models.requests.DataRequest
import models.{Mode, Index, NormalMode}
import navigators.CompoundNavigator
import play.api.data.Form
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.nunjucks.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EnterPreviousPostcodeController @Inject()(val appConfig: AppConfig,
                                               override val messagesApi: MessagesApi,
                                               val userAnswersCacheConnector: UserAnswersCacheConnector,
                                               val addressLookupConnector: AddressLookupConnector,
                                               val navigator: CompoundNavigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: PostcodeFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val renderer: Renderer
                                              )(implicit val ec: ExecutionContext) extends PostcodeController with I18nSupport with NunjucksSupport {

  def form: Form[String] = formProvider("individualEnterPreviousPostcode.required", "enterPostcode.invalid")

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", s"messages__error__postcode_$messageKey")
  }

  def onPageLoad(index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async { implicit request =>
      retrieve(SchemeNameId) { schemeName =>
        get(getFormToJson(schemeName, index, NormalMode))
      }
    }

  def onSubmit(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async{
    implicit request =>
      retrieve(SchemeNameId) { schemeName =>
        post(getFormToJson(schemeName, index, NormalMode), EnterPreviousPostCodeId(index), "enterPostcode.invalid")
      }
  }


  def getFormToJson(schemeName:String, index: Index, mode: Mode)(implicit request:DataRequest[AnyContent]): Form[String] => JsObject = {
    form => {
      val msg = request2Messages(request)
      val name = request.userAnswers.get(EstablisherNameId(index)).map(_.fullName).getOrElse(msg("establisherEntityTypeIndividual"))
      Json.obj(
        "entityType" -> msg("establisherEntityTypeIndividual"),
        "entityName" -> name,
        "form" -> form,
        "enterManuallyUrl" -> controllers.establishers.individual.address.routes.ConfirmPreviousAddressController.onPageLoad(index).url,
        "schemeName" -> schemeName,
        "h1MessageKey" -> "previousPostcode.title"
      )
    }
  }
}
