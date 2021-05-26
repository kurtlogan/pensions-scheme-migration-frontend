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

import base.SpecBase
import controllers.actions.FakeDataRetrievalAction
import controllers.benefitsAndInsurance.routes._
import identifiers._
import identifiers.benefitsAndInsurance._
import models._
import models.benefitsAndInsurance.BenefitsProvisionType.{DefinedBenefitsOnly, MixedBenefits, MoneyPurchaseOnly}
import models.benefitsAndInsurance.BenefitsType.OtherMoneyPurchaseBenefits
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.{JsString, Writes, Json}
import play.api.mvc.Call
import utils.{UserAnswers, Enumerable}

class AboutBenefitsAndInsuranceNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AboutBenefitsAndInsuranceNavigatorSpec._

  private val navigator: CompoundNavigator = applicationBuilder(
    dataRetrievalAction =
      new FakeDataRetrievalAction(
        dataToReturn = Some(UserAnswers(Json.obj()))
      )
  ).build().injector.instanceOf[CompoundNavigator]

  "AboutBenefitsAndInsuranceNavigator" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(IsInvestmentRegulatedId)(isOccupationalPensionPage, uaWithValue(IsInvestmentRegulatedId, false)),
          row(IsInvestmentRegulatedId)(isOccupationalPensionPage, uaWithValue(IsInvestmentRegulatedId, true)),
          row(IsOccupationalId)(howToProvideBenefitsPage, uaWithValue(IsOccupationalId, false)),
          row(IsOccupationalId)(howToProvideBenefitsPage, uaWithValue(IsOccupationalId, true)),
          row(HowProvideBenefitsId)(benefitsTypePage, uaWithValue(HowProvideBenefitsId, MoneyPurchaseOnly)),
          row(HowProvideBenefitsId)(areBenefitsSecuredPage, uaWithValue(HowProvideBenefitsId, DefinedBenefitsOnly)),
          row(HowProvideBenefitsId)(benefitsTypePage, uaWithValue(HowProvideBenefitsId, MixedBenefits)),

          row(BenefitsTypeId)(areBenefitsSecuredPage, uaWithValue(BenefitsTypeId, OtherMoneyPurchaseBenefits)),
//          row(AreBenefitsSecuredId)(checkYourAnswers(), uaWithValue(AreBenefitsSecuredId, false)),
          row(AreBenefitsSecuredId)(insuranceCompanyName, uaWithValue(AreBenefitsSecuredId, true))
    //      row(BenefitsInsuranceNameId)(someStringValue, policyNumber()),
    //      row(InsurancePolicyNumberId)(someStringValue, insurerPostcode()),
    //      row(InsurerEnterPostCodeId)(someSeqTolerantAddress, insurerAddressList()),
    //      row(InsurerSelectAddressId)(someTolerantAddress, checkYourAnswers()),
     //     row(InsurerConfirmAddressId)(someAddress, checkYourAnswers())
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation)
    }


  //  "in CheckMode" must {
  //    def navigation: TableFor3[Identifier, UserAnswers, Call] =
  //      Table(
  //        ("Id", "UserAnswers", "Next Page"),
  //        row(IsInvestmentRegulatedId)(checkYourAnswers(), uaWithValue(IsInvestmentRegulatedId, false)),
  //        row(IsOccupationalId)(checkYourAnswers(), uaWithValue(IsOccupationalId, false)),
  //        row(HowProvideBenefitsId)(checkYourAnswers(), uaWithValue(HowProvideBenefitsId, DefinedBenefitsOnly)),
  //        row(HowProvideBenefitsId)(moneyPurchaseBenefits(CheckMode), uaWithValue(HowProvideBenefitsId, MoneyPurchaseOnly)),
  //        //        row(MoneyPurchaseBenefitsId)(CashBalance, checkYourAnswers()),
  ////        row(BenefitsSecuredByInsuranceId)(false, checkYourAnswers()),
  ////        row(BenefitsSecuredByInsuranceId)(true, insuranceCompanyName(CheckMode)),
  ////        row(BenefitsInsuranceNameId)(someStringValue, policyNumber(NormalMode)),
  ////        row(InsurancePolicyNumberId)(someStringValue, checkYourAnswers()),
  ////        row(InsurerConfirmAddressId)(someAddress, checkYourAnswers())
  //      )
  //    behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation, None)
  //  }


}

object AboutBenefitsAndInsuranceNavigatorSpec extends OptionValues {

  private implicit def writes[A: Enumerable]: Writes[A] = Writes(value => JsString(value.toString))

  private def uaWithValue[A](idType:TypedIdentifier[A], idValue:A)(implicit writes: Writes[A]) =
    UserAnswers().set(idType, idValue).toOption

  private def isOccupationalPensionPage: Call  = IsOccupationalController.onPageLoad()
  private def howToProvideBenefitsPage: Call   = HowProvideBenefitsController.onPageLoad()
  private def benefitsTypePage: Call       = BenefitsTypeController.onPageLoad()
  private def areBenefitsSecuredPage: Call    = AreBenefitsSecuredController.onPageLoad()
  private def insuranceCompanyName: Call                  = BenefitsInsuranceNameController.onPageLoad()
//  private def policyNumber: Call             = InsurancePolicyNumberController.onPageLoad(mode, None)
//  private def insurerPostcode: Call          = InsurerEnterPostcodeController.onPageLoad(mode, None)
//  private def insurerAddressList: Call       = InsurerSelectAddressController.onPageLoad(mode, None)
//  private def checkYourAnswers: Call          = CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(mode, None)
}
