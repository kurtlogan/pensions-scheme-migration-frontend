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
import controllers.trustees.routes
import identifiers.trustees.individual.address._
import identifiers.{TypedIdentifier, Identifier}
import identifiers.trustees.TrusteeKindId
import identifiers.trustees.individual.TrusteeNameId
import identifiers.trustees.individual.contact.{EnterEmailId, EnterPhoneId}
import models.trustees.TrusteeKind
import models._
import org.scalatest.TryValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.Writes
import play.api.mvc.Call
import utils.Data.ua
import utils.{UserAnswers, Enumerable}

class TrusteesNavigatorSpec
  extends SpecBase
    with NavigatorBehaviour
    with Enumerable.Implicits
    with TryValues {

  private val navigator: CompoundNavigator = injector.instanceOf[CompoundNavigator]
  private val index: Index = Index(0)
  private val detailsUa: UserAnswers =
    ua.set(TrusteeNameId(0), PersonName("Jane", "Doe")).success.value
  private val uaWithTrusteeKind: TrusteeKind => UserAnswers = kind => UserAnswers().set(TrusteeKindId(index), kind).get
  private val indvDetailsUa: UserAnswers = uaWithTrusteeKind(TrusteeKind.Individual).set(TrusteeNameId(0), PersonName("Jane", "Doe")).success.value
  private val trusteeNamePage: Call = controllers.trustees.individual.routes.TrusteeNameController.onPageLoad(index)
  private def trusteeEmailPage(mode: Mode): Call = controllers.trustees.individual.contact.routes.EnterEmailController.onPageLoad(index, mode)
  private def trusteePhonePage(mode: Mode): Call = controllers.trustees.individual.contact.routes.EnterPhoneController.onPageLoad(index, mode)
  private val addTrusteePage: Call = controllers.trustees.routes.AddTrusteeController.onPageLoad()
  private val taskListPage: Call = controllers.routes.TaskListController.onPageLoad()
  private val trusteeKindPage: Call = routes.TrusteeKindController.onPageLoad(index)
  private val indexPage: Call = controllers.routes.IndexController.onPageLoad()
  private val cyaContact: Call =
    controllers.trustees.individual.contact.routes.CheckYourAnswersController.onPageLoad(index)


  private def addressUAWithValue[A](idType:TypedIdentifier[A], idValue:A)(implicit writes: Writes[A]) =
    detailsUa.set(idType, idValue).toOption

  private val seqAddresses = Seq(
    TolerantAddress(Some("1"),Some("1"),Some("c"),Some("d"), Some("zz11zz"), Some("GB")),
    TolerantAddress(Some("2"),Some("2"),Some("c"),Some("d"), Some("zz11zz"), Some("GB")),
  )

  val address = Address("addr1", "addr2", None, None, Some("ZZ11ZZ"), "GB")

  private val cyaAddress: Call =
    controllers.trustees.individual.address.routes.CheckYourAnswersController.onPageLoad(index)

  private def enterPreviousPostcode(mode:Mode): Call =
    controllers.trustees.individual.address.routes.EnterPreviousPostcodeController.onPageLoad(index)

  private def selectAddress(mode:Mode): Call =
    controllers.trustees.individual.address.routes.SelectAddressController.onPageLoad(index)

  private def selectPreviousAddress(mode:Mode): Call =
    controllers.trustees.individual.address.routes.SelectPreviousAddressController.onPageLoad(index)

  private def addressYears(mode:Mode): Call =
    controllers.trustees.individual.address.routes.AddressYearsController.onPageLoad(index)


  "TrusteesNavigator" when {
    def navigation: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "Next Page", "UserAnswers (Optional)"),
        row(TrusteeKindId(index))(trusteeNamePage, Some(uaWithTrusteeKind(TrusteeKind.Individual))),
        //row(TrusteeKindId(index))(indexPage, Some(uaWithTrusteeKind(TrusteeKind.Company))),
        //row(TrusteeNameId(index))(addTrusteePage),
        //row(AddTrusteeId(Some(true)))(trusteeKindPage),
        //row(AddTrusteeId(Some(false)))(taskListPage)
          row(EnterPostCodeId(index))(selectAddress(NormalMode), addressUAWithValue(EnterPostCodeId(index), seqAddresses)),
        row(AddressListId(index))(addressYears(NormalMode), addressUAWithValue(AddressListId(index), 0)),
        row(AddressId(index))(addressYears(NormalMode), addressUAWithValue(AddressId(index), address)),

        row(AddressYearsId(index))(cyaAddress, addressUAWithValue(AddressYearsId(index), true)),
        row(AddressYearsId(index))(enterPreviousPostcode(NormalMode), addressUAWithValue(AddressYearsId(index), false)),

        row(EnterPreviousPostCodeId(index))(selectPreviousAddress(NormalMode), addressUAWithValue(EnterPreviousPostCodeId(index), seqAddresses)),
        row(PreviousAddressListId(index))(cyaAddress, addressUAWithValue(PreviousAddressListId(index), 0)),
        row(PreviousAddressId(index))(cyaAddress, addressUAWithValue(PreviousAddressId(index), address)),
          row(EnterEmailId(index))(trusteePhonePage(NormalMode), Some(indvDetailsUa.set(EnterEmailId(index), "test@test.com").success.value)),
        row(EnterPhoneId(index))(cyaContact, Some(indvDetailsUa.set(EnterPhoneId(index), "123").success.value))

      )

    def editNavigation: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "Next Page", "UserAnswers (Optional)"),
        row(EnterEmailId(index))(cyaContact, Some(indvDetailsUa.set(EnterEmailId(index), "test@test.com").success.value)),
        row(EnterPhoneId(index))(cyaContact, Some(indvDetailsUa.set(EnterPhoneId(index), "123").success.value))
      )

    "in NormalMode" must {
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation)
    }

    //"CheckMode" must {
    //  behave like navigatorWithRoutesForMode(CheckMode)(navigator, editNavigation)
    //}
  }
}
