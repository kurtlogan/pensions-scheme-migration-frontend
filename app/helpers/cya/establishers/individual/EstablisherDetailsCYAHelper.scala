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

package helpers.cya.establishers.individual

import helpers.cya.CYAHelper
import helpers.cya.CYAHelper.getName
import identifiers.establishers.individual.EstablisherNameId
import identifiers.establishers.individual.details._
import models.requests.DataRequest
import models.{Index, CheckMode}
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.viewmodels.MessageInterpolators
import uk.gov.hmrc.viewmodels.SummaryList.Row
import utils.{UserAnswers, Enumerable}
import viewmodels.Message

class EstablisherDetailsCYAHelper
  extends CYAHelper
    with Enumerable.Implicits {
  //scalastyle:off method.length
  def detailsRows(
                   index: Index
                 )(
                   implicit request: DataRequest[AnyContent],
                   messages: Messages
                 ): Seq[Row] = {
    implicit val ua: UserAnswers =
      request.userAnswers
    val establisherName: String =
      getName(EstablisherNameId(index))

    val rowsWithoutDynamicIndices = Seq(
      Some(answerOrAddRow(
        id                 = EstablisherDOBId(index),
        message            = Message("messages__dob__h1", establisherName).resolve,
        url                = Some(controllers.establishers.individual.details.routes.EstablisherDOBController.onPageLoad(index, CheckMode).url),
        visuallyHiddenText = Some(msg"messages__dob__cya__visuallyHidden".withArgs(establisherName)),
        answerTransform = answerDateTransform
      )),
      Some(answerOrAddRow(
        id                 = EstablisherHasNINOId(index),
        message            = Message("messages__hasNINO", establisherName).resolve,
        url                = Some(controllers.establishers.individual.details.routes.EstablisherHasNINOController.onPageLoad(index, CheckMode).url),
        visuallyHiddenText = Some(msg"messages__hasNINO__cya__visuallyHidden".withArgs(establisherName)),
        answerTransform    = answerBooleanTransform
      )),
      ua.get(EstablisherHasNINOId(index)) map {
        case true =>
          answerOrAddRow(
            id                 = EstablisherNINOId(index),
            message            = Message("messages__enterNINO__cya", establisherName),
            url                = Some(controllers.establishers.individual.details.routes.EstablisherEnterNINOController.onPageLoad(index, CheckMode).url),
            visuallyHiddenText = Some(msg"messages__enterNINO__cya_visuallyHidden".withArgs(establisherName)),
            answerTransform    = referenceValueTransform
          )
      case false =>
          answerOrAddRow(
            id                 = EstablisherNoNINOReasonId(index),
            message            = Message("messages__whyNoNINO", establisherName),
            url                = Some(controllers.establishers.individual.details.routes.EstablisherNoNINOReasonController.onPageLoad(index, CheckMode).url),
            visuallyHiddenText = Some(msg"messages__whyNoNINO__cya__visuallyHidden".withArgs(establisherName))
          )
      },
      Some(answerOrAddRow(
        id                 = EstablisherHasUTRId(index),
        message            = Message("messages__hasUTR", establisherName).resolve,
        url                = Some(controllers.establishers.individual.details.routes.EstablisherHasUTRController.onPageLoad(index, CheckMode).url),
        visuallyHiddenText = Some(msg"messages__hasUTR__cya__visuallyHidden".withArgs(establisherName)),
        answerTransform    = answerBooleanTransform
      )),
      ua.get(EstablisherHasUTRId(index)) map {
        case true =>
          answerOrAddRow(
            id                 = EstablisherUTRId(index),
            message            = Message("messages__enterUTR__cya_label", establisherName),
            url                = Some(controllers.establishers.individual.details.routes.EstablisherEnterUTRController.onPageLoad(index, CheckMode).url),
            visuallyHiddenText = Some(msg"messages__enterUTR__cya_visuallyHidden".withArgs(establisherName)),
            answerTransform    = referenceValueTransform
          )
        case false =>
          answerOrAddRow(
            id                 = EstablisherNoUTRReasonId(index),
            message            = Message("messages__whyNoUTR", establisherName),
            url                = Some(controllers.establishers.individual.details.routes.EstablisherNoUTRReasonController.onPageLoad(index, CheckMode).url),
            visuallyHiddenText = Some(msg"messages__whyNoUTR__cya__visuallyHidden".withArgs(establisherName))
          )
      }
    ).flatten
    rowsWithDynamicIndices(rowsWithoutDynamicIndices)
  }
}
