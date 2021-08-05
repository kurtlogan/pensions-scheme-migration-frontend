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

import controllers.trustees.company.contacts.routes._
import controllers.routes._
import controllers.trustees.routes._
import controllers.trustees.company.details.{routes => detailsRoutes}
import identifiers._
import identifiers.trustees.company.CompanyDetailsId
import identifiers.trustees.company.details._
import models.{CheckMode, Index, Mode, NormalMode}
import models.requests.DataRequest
import models.{Mode, NormalMode}
import identifiers.trustees.company.contacts.{EnterEmailId, EnterPhoneId}
import play.api.mvc.{AnyContent, Call}
import utils.{Enumerable, UserAnswers}

class TrusteesCompanyNavigator
  extends Navigator
    with Enumerable.Implicits {

  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(ua: UserAnswers)
                                 (implicit request: DataRequest[AnyContent]): PartialFunction[Identifier, Call] = {
    case CompanyDetailsId(_) => AddTrusteeController.onPageLoad()
    case HaveCompanyNumberId(index) => companyNumberRoutes(index, ua, NormalMode)
    case CompanyNumberId(index) => detailsRoutes.HaveUTRController.onPageLoad(index, NormalMode)
    case NoCompanyNumberReasonId(index) => detailsRoutes.HaveUTRController.onPageLoad(index, NormalMode)
    case HaveUTRId(index) => utrRoutes(index, ua, NormalMode)
    case CompanyUTRId(index) => detailsRoutes.HaveVATController.onPageLoad(index, NormalMode)
    case NoUTRReasonId(index) => detailsRoutes.HaveVATController.onPageLoad(index, NormalMode)
    case HaveVATId(index) => vatRoutes(index, ua, NormalMode)
    case VATId(index) => detailsRoutes.HavePAYEController.onPageLoad(index, NormalMode)
    case HavePAYEId(index) => payeRoutes(index, ua, NormalMode)
    case PAYEId(index) => detailsRoutes.CheckYourAnswersController.onPageLoad(index)
    case EnterEmailId(index) => EnterPhoneController.onPageLoad(index, NormalMode)
    case EnterPhoneId(index) => cyaContactDetails(index)

  }

  override protected def editRouteMap(ua: UserAnswers)
                                     (implicit request: DataRequest[AnyContent]): PartialFunction[Identifier, Call] = {
    case CompanyDetailsId(_) => IndexController.onPageLoad()
    case HaveCompanyNumberId(index) => companyNumberRoutes(index, ua, CheckMode)
    case CompanyNumberId(index) => detailsRoutes.CheckYourAnswersController.onPageLoad(index)
    case NoCompanyNumberReasonId(index) => detailsRoutes.CheckYourAnswersController.onPageLoad(index)
    case HaveUTRId(index) => utrRoutes(index, ua, CheckMode)
    case CompanyUTRId(index) => detailsRoutes.CheckYourAnswersController.onPageLoad(index)
    case NoUTRReasonId(index) => detailsRoutes.CheckYourAnswersController.onPageLoad(index)
    case HaveVATId(index) => vatRoutes(index, ua, CheckMode)
    case VATId(index) => detailsRoutes.CheckYourAnswersController.onPageLoad(index)
    case HavePAYEId(index) => payeRoutes(index, ua, CheckMode)
    case PAYEId(index) => detailsRoutes.CheckYourAnswersController.onPageLoad(index)
    case EnterEmailId(index) => EnterPhoneController.onPageLoad(index, NormalMode)
    case EnterPhoneId(index) => cyaContactDetails(index)
  }

  private def companyNumberRoutes(
                                   index: Index,
                                   answers: UserAnswers,
                                   mode: Mode
                                 ): Call =
    answers.get(HaveCompanyNumberId(index)) match {
      case Some(true) => detailsRoutes.CompanyNumberController.onPageLoad(index, mode)
      case Some(false) => detailsRoutes.NoCompanyNumberReasonController.onPageLoad(index, mode)
      case None => controllers.routes.TaskListController.onPageLoad()
    }

  private def utrRoutes(
                         index: Index,
                         answers: UserAnswers,
                         mode: Mode
                       ): Call =
    answers.get(HaveUTRId(index)) match {
      case Some(true) => detailsRoutes.UTRController.onPageLoad(index, mode)
      case Some(false) => detailsRoutes.NoUTRReasonController.onPageLoad(index, mode)
      case None => controllers.routes.TaskListController.onPageLoad()
    }

  private def vatRoutes(
                         index: Index,
                         answers: UserAnswers,
                         mode: Mode
                       ): Call =
    answers.get(HaveVATId(index)) match {
      case Some(true) => detailsRoutes.VATController.onPageLoad(index, mode)
      case Some(false) => detailsRoutes.HavePAYEController.onPageLoad(index, mode)
      case None => controllers.routes.TaskListController.onPageLoad()
    }

  private def payeRoutes(
                          index: Index,
                          answers: UserAnswers,
                          mode: Mode
                        ): Call =
    answers.get(HavePAYEId(index)) match {
      case Some(true) => detailsRoutes.PAYEController.onPageLoad(index, mode)
      case Some(false) => detailsRoutes.CheckYourAnswersController.onPageLoad(index)
      case None => controllers.routes.TaskListController.onPageLoad()
    }
  private def cyaContactDetails(index:Int): Call = controllers.trustees.company.contacts.routes.CheckYourAnswersController.onPageLoad(index)
}
