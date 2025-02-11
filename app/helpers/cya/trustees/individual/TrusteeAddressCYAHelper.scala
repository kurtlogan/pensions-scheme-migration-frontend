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

package helpers.cya.trustees.individual

import helpers.cya.CYAHelper
import helpers.cya.CYAHelper.getName
import identifiers.trustees.individual.TrusteeNameId
import identifiers.trustees.individual.address.{AddressId, AddressYearsId, PreviousAddressId}
import models.{CheckMode, Index}
import models.requests.DataRequest
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.viewmodels.MessageInterpolators
import uk.gov.hmrc.viewmodels.SummaryList.Row
import utils.{Enumerable, UserAnswers}
import viewmodels.Message

class TrusteeAddressCYAHelper
  extends CYAHelper
    with Enumerable.Implicits {

  //scalastyle:off method.length
  def rows(index: Index)(
                   implicit request: DataRequest[AnyContent],
                   messages: Messages
                 ): Seq[Row] = {
    implicit val ua: UserAnswers =
      request.userAnswers
    val trusteeName: String =
      getName(TrusteeNameId(index))

    val seqRowAddressAndYears = Seq(
      answerOrAddRow(
        AddressId(index),
        Message("messages__address__whatYouWillNeed_h1", trusteeName).resolve,
        Some(controllers.trustees.individual.address.routes.EnterPostcodeController.onPageLoad(index, CheckMode).url),
        Some(msg"messages__visuallyHidden__address".withArgs(trusteeName)), answerAddressTransform
      ),
      answerOrAddRow(
        AddressYearsId(index),
        Message("addressYears.title", trusteeName).resolve,
        Some(controllers.trustees.individual.address.routes.AddressYearsController.onPageLoad(index, CheckMode).url),
        Some(msg"messages__visuallyhidden__trusteeAddressYears".withArgs(trusteeName)), answerBooleanTransform
      )
    )

    val seqRowPreviousAddress = if (ua.get(AddressYearsId(index)).contains(true)) {
      Nil
    } else {
      Seq(
        answerOrAddRow(
          PreviousAddressId(index),
          Message("messages__previousAddress", trusteeName).resolve,
          Some(controllers.trustees.individual.address.routes.EnterPreviousPostcodeController.onPageLoad(index, CheckMode).url),
          Some(msg"messages__visuallyHidden__previousAddress".withArgs(trusteeName)), answerAddressTransform
        )
      )
    }

    val rowsWithoutDynamicIndices = seqRowAddressAndYears ++ seqRowPreviousAddress
    rowsWithDynamicIndices(rowsWithoutDynamicIndices)
  }
}
