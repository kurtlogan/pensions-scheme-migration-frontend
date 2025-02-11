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

package controllers.establishers.partnership.address

import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.CommonTradingTimeController
import forms.address.TradingTimeFormProvider
import identifiers.beforeYouStart.SchemeNameId
import identifiers.establishers.partnership.PartnershipDetailsId
import identifiers.establishers.partnership.address.TradingTimeId
import models.{Index, Mode}
import navigators.CompoundNavigator
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import utils.Enumerable

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TradingTimeController @Inject()(override val messagesApi: MessagesApi,
                                      val userAnswersCacheConnector: UserAnswersCacheConnector,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      val navigator: CompoundNavigator,
                                      formProvider: TradingTimeFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      val renderer: Renderer)(implicit ec: ExecutionContext)
  extends CommonTradingTimeController
    with Enumerable.Implicits {

  private def form: Form[Boolean] =
    formProvider("partnershipTradingTime.error.required")

  def onPageLoad(index: Index, mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async { implicit request =>
      (PartnershipDetailsId(index) and SchemeNameId).retrieve.right.map {
        case partnershipDetails ~ schemeName =>
          get(Some(schemeName), partnershipDetails.partnershipName, Messages("messages__partnership"), form, TradingTimeId(index))
      }
    }

  def onSubmit(index: Index, mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async { implicit request =>
      (PartnershipDetailsId(index) and SchemeNameId).retrieve.right.map {
        case partnershipDetails ~ schemeName =>
          post(Some(schemeName), partnershipDetails.partnershipName, Messages("messages__partnership"), form, TradingTimeId(index),Some(mode))
      }
    }
}

