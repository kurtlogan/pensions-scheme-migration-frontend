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

package navigators

import com.google.inject.Inject
import connectors.cache.UserAnswersCacheConnector
import identifiers._
import controllers.benefitsAndInsurance.routes._
import identifiers.benefitsAndInsurance.{InsurerEnterPostCodeId, HowProvideBenefitsId, AreBenefitsSecuredId, InsurerAddressListId, BenefitsInsuranceNameId, BenefitsTypeId}
import models.benefitsAndInsurance.BenefitsProvisionType.DefinedBenefitsOnly
import models.requests.DataRequest
import play.api.mvc.{Call, AnyContent}
import utils.UserAnswers

class AboutBenefitsAndInsuranceNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector)
  extends Navigator {

  override protected def routeMap(ua: UserAnswers)
    (implicit request: DataRequest[AnyContent]): PartialFunction[Identifier, Call] = {
    case HowProvideBenefitsId if ua.get(HowProvideBenefitsId).contains(DefinedBenefitsOnly) =>
      CheckYourAnswersController.onPageLoad()
    case HowProvideBenefitsId => BenefitsTypeController.onPageLoad()
    case BenefitsTypeId => CheckYourAnswersController.onPageLoad()
    case AreBenefitsSecuredId if ua.get(AreBenefitsSecuredId).contains(false) =>
      CheckYourAnswersController.onPageLoad()
    case AreBenefitsSecuredId => BenefitsInsuranceNameController.onPageLoad()
    case BenefitsInsuranceNameId => BenefitsInsurancePolicyController.onPageLoad()
    case InsurerEnterPostCodeId => InsurerSelectAddressController.onPageLoad()
    case InsurerAddressListId => CheckYourAnswersController.onPageLoad()
  }
}
