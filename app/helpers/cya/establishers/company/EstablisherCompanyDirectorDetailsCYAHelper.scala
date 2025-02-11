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

package helpers.cya.establishers.company

import controllers.establishers.company.director.routes
import controllers.establishers.company.director.details.{routes => detailsRoutes}
import controllers.establishers.company.director.address.{routes => addressRoutes}
import controllers.establishers.company.director.contact.{routes => contactRoutes}
import helpers.cya.CYAHelper
import helpers.cya.CYAHelper.getName
import identifiers.establishers.company.director._
import identifiers.establishers.company.director.address.{AddressId, AddressYearsId, PreviousAddressId}
import identifiers.establishers.company.director.contact.{EnterEmailId, EnterPhoneId}
import identifiers.establishers.company.director.details._
import models.requests.DataRequest
import models.{CheckMode, Index}
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.viewmodels.MessageInterpolators
import uk.gov.hmrc.viewmodels.SummaryList.Row
import utils.{Enumerable, UserAnswers}
import viewmodels.Message

class EstablisherCompanyDirectorDetailsCYAHelper
  extends CYAHelper
    with Enumerable.Implicits {

  //scalastyle:off method.length
  def detailsRows(
                   establisherIndex: Index, directorIndex: Index
                 )(
                   implicit request: DataRequest[AnyContent],
                   messages: Messages
                 ): Seq[Row] = {
    implicit val ua: UserAnswers =
      request.userAnswers
    val directorName: String =
      getName(DirectorNameId(establisherIndex,directorIndex))

    val rowsWithoutDynamicIndices = Seq(
      Some(answerOrAddRow(
        id                 = DirectorNameId(establisherIndex, directorIndex),
        message            = Message("messages__director__name").resolve,
        url                = Some(routes.DirectorNameController.onPageLoad(establisherIndex,directorIndex, CheckMode).url),
        visuallyHiddenText = Some(msg"messages__director__name__cya__visuallyHidden".withArgs(directorName)),
        answerTransform    = answerPersonNameTransform
      )),

      Some(answerOrAddRow(
        id                 = DirectorDOBId(establisherIndex, directorIndex),
        message            = Message("messages__dob__h1", directorName).resolve,
        url                = Some(detailsRoutes.DirectorDOBController.onPageLoad(establisherIndex,directorIndex,CheckMode).url),
        visuallyHiddenText = Some(msg"messages__dob__cya__visuallyHidden".withArgs(directorName)),
        answerTransform = answerDateTransform
      )),
      Some(answerOrAddRow(
        id                 = DirectorHasNINOId(establisherIndex, directorIndex),
        message            = Message("messages__hasNINO", directorName).resolve,
        url                = Some(detailsRoutes.DirectorHasNINOController.onPageLoad(establisherIndex, directorIndex, CheckMode).url),
        visuallyHiddenText = Some(msg"messages__hasNINO__cya__visuallyHidden".withArgs(directorName)),
        answerTransform    = answerBooleanTransform
      )),
      ua.get(DirectorHasNINOId(establisherIndex, directorIndex)) map {
        case true =>
          answerOrAddRow(
            id                 = DirectorNINOId(establisherIndex, directorIndex),
            message            = Message("messages__enterNINO__cya", directorName),
            url                = Some(detailsRoutes.DirectorEnterNINOController.onPageLoad(establisherIndex, directorIndex, CheckMode).url),
            visuallyHiddenText = Some(msg"messages__hasNINO__cya__visuallyHidden".withArgs(directorName)),
            answerTransform    = referenceValueTransform
          )
        case false =>
          answerOrAddRow(
            id                 = DirectorNoNINOReasonId(establisherIndex, directorIndex),
            message            = Message("messages__whyNoNINO", directorName),
            url                = Some(detailsRoutes.DirectorNoNINOReasonController.onPageLoad(establisherIndex, directorIndex, CheckMode).url),
            visuallyHiddenText = Some(msg"messages__whyNoNINO__cya__visuallyHidden".withArgs(directorName))
          )
      },
      Some(answerOrAddRow(
        id                 = DirectorHasUTRId(establisherIndex, directorIndex),
        message            = Message("messages__hasUTR", directorName).resolve,
        url                = Some(detailsRoutes.DirectorHasUTRController.onPageLoad(establisherIndex, directorIndex,CheckMode).url),
        visuallyHiddenText = Some(msg"messages__hasUTR__cya__visuallyHidden".withArgs(directorName)),
        answerTransform    = answerBooleanTransform
      )),
      ua.get(DirectorHasUTRId(establisherIndex, directorIndex)) map {
        case true =>
          answerOrAddRow(
            id                 = DirectorEnterUTRId(establisherIndex, directorIndex),
            message            = Message("messages__enterUTR__cya_label", directorName),
            url                = Some(detailsRoutes.DirectorEnterUTRController.onPageLoad(establisherIndex, directorIndex,CheckMode).url),
            visuallyHiddenText = Some(msg"messages__hasUTR__cya__visuallyHidden".withArgs(directorName)),
            answerTransform    = referenceValueTransform
          )
        case false =>
          answerOrAddRow(
            id                 = DirectorNoUTRReasonId(establisherIndex, directorIndex),
            message            = Message("messages__whyNoUTR", directorName),
            url                = Some(detailsRoutes.DirectorNoUTRReasonController.onPageLoad(establisherIndex, directorIndex,CheckMode).url),
            visuallyHiddenText = Some(msg"messages__whyNoUTR__cya__visuallyHidden".withArgs(directorName))
          )
      },
      Some( answerOrAddRow(
            id                  = AddressId(establisherIndex, directorIndex),
            message             = Message("addressList_cya_label", directorName).resolve,
            url                 = Some(addressRoutes.EnterPostcodeController.onPageLoad(establisherIndex, directorIndex,  CheckMode).url),
            visuallyHiddenText  = Some(msg"messages__visuallyHidden__address".withArgs(directorName)), answerAddressTransform
          ))
     ,
      Some(
        answerOrAddRow(
            id                  = AddressYearsId(establisherIndex, directorIndex),
            message             = Message("addressYears.title", directorName).resolve,
            url                 = Some(addressRoutes.AddressYearsController.onPageLoad(establisherIndex, directorIndex, CheckMode).url),
            visuallyHiddenText  = Some(msg"messages__visuallyhidden__addressYears".withArgs(directorName)), answerBooleanTransform
          ))
      ,
      if (ua.get(AddressYearsId(establisherIndex, directorIndex)).contains(true)) {
        None
      }else{
        Some( answerOrAddRow(
          id = PreviousAddressId(establisherIndex, directorIndex),
          message = Message("previousAddressList_cya_label", directorName).resolve,
          url = Some(addressRoutes.EnterPreviousPostcodeController.onPageLoad(establisherIndex, directorIndex, CheckMode).url),
          visuallyHiddenText = Some(msg"messages__visuallyHidden__previousAddress".withArgs(directorName)), answerAddressTransform
        ))
      },
      Some(answerOrAddRow(
        id                  = EnterEmailId(establisherIndex, directorIndex),
        message             = Message("messages__enterEmail_cya_label", directorName).resolve,
        url                 = Some(contactRoutes.EnterEmailController.onPageLoad(establisherIndex, directorIndex,CheckMode).url),
        visuallyHiddenText  = Some(msg"messages__enterEmail__cya__visuallyHidden".withArgs(directorName))
      )),
      Some(answerOrAddRow(
        id                  = EnterPhoneId(establisherIndex, directorIndex),
        message             = Message("messages__enterPhone_cya_label", directorName).resolve,
        url                 = Some(contactRoutes.EnterPhoneNumberController.onPageLoad(establisherIndex, directorIndex, CheckMode).url),
        visuallyHiddenText  = Some(msg"messages__enterPhone__cya__visuallyHidden".withArgs(directorName))
      ))
    ).flatten
    rowsWithDynamicIndices(rowsWithoutDynamicIndices)
  }
}
