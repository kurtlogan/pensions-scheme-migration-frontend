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

package helpers.cya.establishers.company

import helpers.cya.CYAHelper
import helpers.cya.CYAHelper.getCompanyName
import identifiers.establishers.company.CompanyDetailsId
import identifiers.establishers.company.address.{AddressId, AddressYearsId, PreviousAddressId, TradingTimeId}
import models.Index
import models.requests.DataRequest
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.viewmodels.MessageInterpolators
import uk.gov.hmrc.viewmodels.SummaryList.Row
import utils.{Enumerable, UserAnswers}
import viewmodels.Message

class EstablisherCompanyAddressCYAHelper
  extends CYAHelper
    with Enumerable.Implicits {

  //scalastyle:off method.length
  def rows(index: Index)(
                   implicit request: DataRequest[AnyContent],
                   messages: Messages
                 ): Seq[Row] = {
    implicit val ua: UserAnswers =
      request.userAnswers
    val establisherName: String =
      getCompanyName(CompanyDetailsId(index))

    val seqRowAddressAndYears = Seq(
      answerOrAddRow(
        AddressId(index),
        Message("messages__establisherAddress__whatYouWillNeed_h1", establisherName).resolve,
        Some(controllers.establishers.company.address.routes.EnterPostcodeController.onPageLoad(index).url),
        Some(msg"messages__visuallyHidden__address".withArgs(establisherName)), answerAddressTransform
      ),
      answerOrAddRow(
        AddressYearsId(index),
        Message("addressYears.title", establisherName).resolve,
        Some(controllers.establishers.company.address.routes.AddressYearsController.onPageLoad(index).url),
        Some(msg"messages__visuallyhidden__addressYears".withArgs(establisherName)), answerBooleanTransform
      )
    )

    val seqTradingTime = if (ua.get(AddressYearsId(index)).contains(false)) {
      Seq(
        answerOrAddRow(
          TradingTimeId(index),
          Message("tradingTime.title", establisherName).resolve,
          Some(controllers.establishers.company.address.routes.TradingTimeController.onPageLoad(index).url),
          Some(msg"messages__visuallyhidden__establisherTradingTime".withArgs(establisherName)), answerBooleanTransform
        )
      )
    } else {
      Nil
    }

    val seqRowPreviousAddress =(ua.get(AddressYearsId(index)), ua.get(TradingTimeId(index))) match {
      case (Some(false), Some(true)) =>
        Seq(
          answerOrAddRow(
            PreviousAddressId(index),
            Message("messages__establisherPreviousAddress").resolve,
            Some(controllers.establishers.company.address.routes.EnterPreviousPostcodeController.onPageLoad(index).url),
            Some(msg"messages__visuallyHidden__previousAddress".withArgs(establisherName)), answerAddressTransform
          )
        )
      case _ => Nil
    }

    val rowsWithoutDynamicIndices = seqRowAddressAndYears ++ seqTradingTime ++ seqRowPreviousAddress
    rowsWithDynamicIndices(rowsWithoutDynamicIndices)
  }
}
