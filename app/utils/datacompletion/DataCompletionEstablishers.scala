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

package utils.datacompletion

import identifiers.establishers.EstablisherKindId
import identifiers.establishers.company.address.{TradingTimeId, AddressId => CompanyAddressId, AddressYearsId => CompanyAddressYearsId, PreviousAddressId => CompanyPreviousAddressId}
import identifiers.establishers.company.details._
import identifiers.establishers.partnership.address.{TradingTimeId => PartnershipTradingTimeId, AddressId => PartnershipAddressId, AddressYearsId => PartnershipAddressYearsId, PreviousAddressId => PartnershipPreviousAddressId}
import identifiers.establishers.company.director.{address => directorAddress}
import identifiers.establishers.company.director.{contact => directorContact}
import identifiers.establishers.company.director.details._
import identifiers.establishers.company.{CompanyDetailsId, contact => companyContact}
import identifiers.establishers.individual.EstablisherNameId
import identifiers.establishers.individual.address.{AddressId, AddressYearsId, PreviousAddressId}
import identifiers.establishers.individual.contact.{EnterEmailId, EnterPhoneId}
import identifiers.establishers.individual.details._
import utils.UserAnswers

trait DataCompletionEstablishers extends DataCompletion {

  self: UserAnswers =>

  def isEstablisherIndividualComplete(index: Int): Boolean =
    isComplete(
      Seq(
        isAnswerComplete(EstablisherNameId(index)),
        isAnswerComplete(EstablisherKindId(index))
      )
    ).getOrElse(false)

  def isEstablisherIndividualDetailsCompleted(index: Int): Option[Boolean] =
    isComplete(
      Seq(
        isAnswerComplete(EstablisherDOBId(index)),
        isAnswerComplete(EstablisherHasNINOId(index), EstablisherNINOId(index), Some(EstablisherNoNINOReasonId(index))),
        isAnswerComplete(EstablisherHasUTRId(index), EstablisherUTRId(index), Some(EstablisherNoUTRReasonId(index)))
      )
    )

  def isEstablisherIndividualAddressCompleted(
    index: Int,
    userAnswers: UserAnswers
  ): Option[Boolean] = {
    val atAddressMoreThanOneYear = userAnswers.get(AddressYearsId(index)).contains(true)
    isComplete(
      Seq(
        isAnswerComplete(AddressId(index)),
        isAnswerComplete(AddressYearsId(index)),
        if (atAddressMoreThanOneYear) Some(true) else isAnswerComplete(PreviousAddressId(index))
      )
    )
  }

  def isEstablisherCompanyDetailsCompleted(index: Int): Option[Boolean] = {
    isComplete(
      Seq(
        isAnswerComplete(HaveCompanyNumberId(index),CompanyNumberId(index),Some(NoCompanyNumberReasonId(index))),
        isAnswerComplete(HaveUTRId(index), CompanyUTRId(index), Some(NoUTRReasonId(index))),
        isAnswerComplete(HaveVATId(index),VATId(index),None),
        isAnswerComplete(HavePAYEId(index),PAYEId(index),None)
      )
    )
  }

  def isEstablisherCompanyAddressCompleted(
                                            index: Int,
                                            userAnswers: UserAnswers
                                          ): Option[Boolean] = {

    val previousAddress = (userAnswers.get(CompanyAddressYearsId(index)), userAnswers.get(TradingTimeId(index))) match {
      case (Some(true), _) => Some(true)
      case (Some(false), Some(true)) => isAnswerComplete(CompanyPreviousAddressId(index))
      case (Some(false), Some(false)) => Some(true)
      case _ => None
    }

    isComplete(
      Seq(
        isAnswerComplete(CompanyAddressId(index)),
        isAnswerComplete(CompanyAddressYearsId(index)),
        previousAddress
      )
    )
  }

  def isEstablisherPartnershipAddressCompleted(
                                                index: Int,
                                                userAnswers: UserAnswers
                                              ): Option[Boolean] = {

    val previousAddress = (userAnswers.get(PartnershipAddressYearsId(index)), userAnswers.get(PartnershipTradingTimeId(index))) match {
      case (Some(true), _) => Some(true)
      case (Some(false), Some(true)) => isAnswerComplete(PartnershipPreviousAddressId(index))
      case (Some(false), Some(false)) => Some(true)
      case _ => None
    }

    isComplete(
      Seq(
        isAnswerComplete(PartnershipAddressId(index)),
        isAnswerComplete(PartnershipAddressYearsId(index)),
        previousAddress
      )
    )
  }

  def isEstablisherIndividualContactDetailsCompleted(index: Int): Option[Boolean] =
    isComplete(
      Seq(
        isAnswerComplete(EnterEmailId(index)),
        isAnswerComplete(EnterPhoneId(index))
      )
    )

  def isEstablisherCompanyComplete(index: Int): Boolean =
    isComplete(
      Seq(
        isAnswerComplete(CompanyDetailsId(index)),
        isAnswerComplete(EstablisherKindId(index))
      )
    ).getOrElse(false)

  def isDirectorComplete(estIndex: Int, dirIndex: Int): Boolean =
    isComplete(Seq(
      isDirectorDetailsComplete(estIndex, dirIndex),
      isDirectorAddressComplete(estIndex, dirIndex),
      isContactDetailsComplete(directorContact.EnterEmailId(estIndex, dirIndex), directorContact.EnterPhoneId(estIndex, dirIndex))
      )
    ).getOrElse(false)

  def isDirectorDetailsComplete(estIndex: Int, dirIndex: Int): Option[Boolean]  =
    isComplete(Seq(
      isAnswerComplete(DirectorDOBId(estIndex, dirIndex)),
      isAnswerComplete(DirectorHasNINOId(estIndex, dirIndex), DirectorNINOId(estIndex, dirIndex), Some(DirectorNoNINOReasonId(estIndex, dirIndex))),
      isAnswerComplete(DirectorHasUTRId(estIndex, dirIndex), DirectorEnterUTRId(estIndex, dirIndex), Some(DirectorNoUTRReasonId(estIndex, dirIndex)))
    ))

  def isDirectorAddressComplete(estIndex: Int,
                                 dirIndex: Int): Option[Boolean] = {
    val atAddressMoreThanOneYear = self.get(directorAddress.AddressYearsId(estIndex, dirIndex)).contains(true)
    isComplete(
      Seq(
        isAnswerComplete(directorAddress.AddressId(estIndex, dirIndex)),
        isAnswerComplete(directorAddress.AddressYearsId(estIndex, dirIndex)),
        if (atAddressMoreThanOneYear) Some(true) else isAnswerComplete(directorAddress.PreviousAddressId(estIndex, dirIndex))
      )
    )
  }

  def isEstablisherCompanyContactDetailsCompleted(index: Int): Option[Boolean] =
    isComplete(
      Seq(
        isAnswerComplete(companyContact.EnterEmailId(index)),
        isAnswerComplete(companyContact.EnterPhoneId(index))
      )
    )
}
