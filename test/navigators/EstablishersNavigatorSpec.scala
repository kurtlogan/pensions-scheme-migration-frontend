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
import controllers.establishers.individual.details
import controllers.establishers.individual.details._
import controllers.establishers.routes
import identifiers.Identifier
import identifiers.establishers.{AddEstablisherId, EstablisherKindId}
import identifiers.establishers.individual.EstablisherNameId
import identifiers.establishers.individual.details._
import models.{CheckMode, Index, Mode, NormalMode, PersonName, ReferenceValue}
import models.establishers.EstablisherKind
import org.scalatest.TryValues
import org.scalatest.prop.TableFor3
import play.api.mvc.Call
import utils.Data.ua
import utils.{Enumerable, UserAnswers}

import java.time.LocalDate

class EstablishersNavigatorSpec
  extends SpecBase
    with NavigatorBehaviour
    with Enumerable.Implicits
    with TryValues {

  private val navigator: CompoundNavigator = injector.instanceOf[CompoundNavigator]
  private val index: Index = Index(0)
  private val uaWithEstablisherKind: EstablisherKind => UserAnswers = kind => UserAnswers().set(EstablisherKindId(index), kind).get
  private val establisherNamePage: Call = controllers.establishers.individual.routes.EstablisherNameController.onPageLoad(index)
  private val addEstablisherPage: Call = controllers.establishers.routes.AddEstablisherController.onPageLoad()
  private val taskListPage: Call = controllers.routes.TaskListController.onPageLoad()
  private val establisherKindPage: Call = routes.EstablisherKindController.onPageLoad(index)
  private val indexPage: Call = controllers.routes.IndexController.onPageLoad()
  private val detailsUa: UserAnswers =
    ua.set(EstablisherNameId(0), PersonName("Jane", "Doe")).success.value
  private def hasNinoPage(mode: Mode): Call =
    details.routes.EstablisherHasNINOController.onPageLoad(index, mode)   
  private def enterNinoPage(mode: Mode): Call =
    details.routes.EstablisherEnterNINOController.onPageLoad(index, mode)
  private def noNinoPage(mode: Mode): Call =
    details.routes.EstablisherNoNINOReasonController.onPageLoad(index, mode)
  private def hasUtrPage(mode: Mode): Call =
    details.routes.EstablisherHasUTRController.onPageLoad(index, mode)   
  private def enterUtrPage(mode: Mode): Call =
    details.routes.EstablisherEnterUTRController.onPageLoad(index, mode)
  private def noUtrPage(mode: Mode): Call =
    details.routes.EstablisherNoUTRReasonController.onPageLoad(index, mode)
  private val cya: Call =
    details.routes.CheckYourAnswersController.onPageLoad(index)

  "EstablishersNavigator" when {
    def navigation: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "Next Page", "UserAnswers (Optional)"),
        row(EstablisherKindId(index))(establisherNamePage, Some(uaWithEstablisherKind(EstablisherKind.Individual))),
        row(EstablisherKindId(index))(indexPage, Some(uaWithEstablisherKind(EstablisherKind.Company))),
        row(EstablisherNameId(index))(addEstablisherPage),
        row(AddEstablisherId(Some(true)))(establisherKindPage),
        row(AddEstablisherId(Some(false)))(taskListPage),
        row(EstablisherDOBId(index))(hasNinoPage(NormalMode), Some(detailsUa.set(EstablisherDOBId(index), LocalDate.parse("2000-01-01")).success.value)),
        row(EstablisherHasNINOId(index))(enterNinoPage(NormalMode), Some(detailsUa.set(EstablisherHasNINOId(index), true).success.value)),
        row(EstablisherHasNINOId(index))(noNinoPage(NormalMode), Some(detailsUa.set(EstablisherHasNINOId(index), false).success.value)),
        row(EstablisherNINOId(index))(hasUtrPage(NormalMode), Some(detailsUa.set(EstablisherNINOId(index), ReferenceValue("AB123456C")).success.value)),
        row(EstablisherNoNINOReasonId(index))(hasUtrPage(NormalMode), Some(detailsUa.set(EstablisherNoNINOReasonId(index), "Reason").success.value)),
        row(EstablisherHasUTRId(index))(enterUtrPage(NormalMode), Some(detailsUa.set(EstablisherHasUTRId(index), true).success.value)),
        row(EstablisherHasUTRId(index))(noUtrPage(NormalMode), Some(detailsUa.set(EstablisherHasUTRId(index), false).success.value)),
        row(EstablisherUTRId(index))(cya, Some(detailsUa.set(EstablisherUTRId(index), ReferenceValue("1234567890")).success.value)),
        row(EstablisherNoUTRReasonId(index))(cya, Some(detailsUa.set(EstablisherNoUTRReasonId(index), "Reason").success.value))
      )

    def editNavigation: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "Next Page", "UserAnswers (Optional)"),
        row(EstablisherDOBId(index))(cya, Some(detailsUa.set(EstablisherDOBId(index), LocalDate.parse("2000-01-01")).success.value)),
        row(EstablisherHasNINOId(index))(enterNinoPage(CheckMode), Some(detailsUa.set(EstablisherHasNINOId(index), true).success.value)),
        row(EstablisherHasNINOId(index))(noNinoPage(CheckMode), Some(detailsUa.set(EstablisherHasNINOId(index), false).success.value)),
        row(EstablisherNINOId(index))(cya, Some(detailsUa.set(EstablisherNINOId(index), ReferenceValue("AB123456C")).success.value)),
        row(EstablisherNoNINOReasonId(index))(cya, Some(detailsUa.set(EstablisherNoNINOReasonId(index), "Reason").success.value)),
        row(EstablisherHasUTRId(index))(enterUtrPage(CheckMode), Some(detailsUa.set(EstablisherHasUTRId(index), true).success.value)),
        row(EstablisherHasUTRId(index))(noUtrPage(CheckMode), Some(detailsUa.set(EstablisherHasUTRId(index), false).success.value)),
        row(EstablisherUTRId(index))(cya, Some(detailsUa.set(EstablisherUTRId(index), ReferenceValue("1234567890")).success.value)),
        row(EstablisherNoUTRReasonId(index))(cya, Some(detailsUa.set(EstablisherNoUTRReasonId(index), "Reason").success.value))
      )

    "in NormalMode" must {
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation)
    }

    "CheckMode" must {
      behave like navigatorWithRoutesForMode(CheckMode)(navigator, editNavigation)
    }
  }
}
