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

package utils.datacompletion

import identifiers.establishers.EstablisherKindId
import identifiers.establishers.company.details._
import identifiers.establishers.company.director.details._
import identifiers.establishers.company.director.{address => directorAddress, contact => directorContact}
import identifiers.establishers.company.{CompanyDetailsId, contact => companyContact}
import identifiers.establishers.individual.EstablisherNameId
import identifiers.establishers.individual.address.{AddressId, AddressYearsId, PreviousAddressId}
import identifiers.establishers.individual.contact.{EnterEmailId, EnterPhoneId}
import identifiers.establishers.individual.details._
import identifiers.establishers.partnership.partner.details._
import identifiers.establishers.partnership.partner.{address => partnerAddress, contact => partnerContact}
import identifiers.establishers.partnership.{PartnershipDetailsId, address => partnershipAddress, contact => partnershipContact, details => partnershipDetails}
import models.establishers.EstablisherKind
import models.{CompanyDetails, PartnershipDetails, PersonName, ReferenceValue}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{OptionValues, TryValues}
import utils.{Data, Enumerable, UserAnswers}

import java.time.LocalDate

class DataCompletionEstablishersSpec
  extends AnyWordSpec
    with Matchers
    with OptionValues
    with TryValues
    with Enumerable.Implicits {

  "Establisher Individual completion status should be returned correctly" when {
    "isEstablisherIndividualComplete" must {
      "return true when all answers are present" in {
        val ua =
          UserAnswers()
            .set(EstablisherKindId(0), EstablisherKind.Individual).success.value
            .set(EstablisherNameId(0), PersonName("a", "b")).success.value
            .set(EstablisherDOBId(0), LocalDate.parse("2001-01-01")).success.value
            .set(EstablisherHasNINOId(0), true).success.value
            .set(EstablisherNINOId(0), ReferenceValue("AB123456C")).success.value
            .set(EstablisherHasUTRId(0), true).success.value
            .set(EstablisherUTRId(0), ReferenceValue("1234567890")).success.value
            .setOrException(AddressId(0), Data.address)
            .setOrException(AddressYearsId(0), true)
            .set(EnterEmailId(0), "test@test.com").success.value
            .set(EnterPhoneId(0), "123").success.value

        ua.isEstablisherIndividualComplete(0) mustBe true
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(EstablisherKindId(0), EstablisherKind.Individual).success.value
            .set(EstablisherNameId(0), PersonName("a", "b")).success.value
            .set(EstablisherKindId(1), EstablisherKind.Individual).success.value

        ua.isEstablisherIndividualComplete(1) mustBe false
      }
    }

    "isEstablisherIndividualDetailsComplete" must {
      "return true when all answers are present" in {
        val ua =
          UserAnswers()
            .set(EstablisherDOBId(0), LocalDate.parse("2001-01-01")).success.value

        val ua1 =
          ua
            .set(EstablisherHasNINOId(0), true).success.value
            .set(EstablisherNINOId(0), ReferenceValue("AB123456C")).success.value
            .set(EstablisherHasUTRId(0), true).success.value
            .set(EstablisherUTRId(0), ReferenceValue("1234567890")).success.value

        val ua2 =
          ua
            .set(EstablisherHasNINOId(0), false).success.value
            .set(EstablisherNoNINOReasonId(0), "Reason").success.value
            .set(EstablisherHasUTRId(0), false).success.value
            .set(EstablisherNoUTRReasonId(0), "Reason").success.value

        ua1.isEstablisherIndividualDetailsComplete(0) mustBe Some(true)
        ua2.isEstablisherIndividualDetailsComplete(0) mustBe Some(true)
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(EstablisherDOBId(0), LocalDate.parse("2001-01-01")).success.value

        ua.isEstablisherIndividualDetailsComplete(0) mustBe Some(false)

      }
    }

    "isEstablisherIndividualAddressComplete" must {
      "return true when all answers are present" in {
        val ua1 =
          UserAnswers()
            .setOrException(AddressId(0), Data.address)
            .setOrException(AddressYearsId(0), true)

        val ua2 =
          UserAnswers()
            .setOrException(AddressId(0), Data.address)
            .setOrException(AddressYearsId(0), false)
            .setOrException(PreviousAddressId(0), Data.address)

        ua1.isEstablisherIndividualAddressComplete(0) mustBe Some(true)
        ua2.isEstablisherIndividualAddressComplete(0) mustBe Some(true)
      }

      "return false when some answer is missing" in {
        val ua1 =
          UserAnswers()
            .setOrException(AddressId(0), Data.address)
            .setOrException(AddressYearsId(0), false)

        val ua2 =
          UserAnswers()
            .setOrException(AddressId(0), Data.address)

        ua1.isEstablisherIndividualAddressComplete(0) mustBe Some(false)
        ua2.isEstablisherIndividualAddressComplete(0) mustBe Some(false)
      }

      "return None when no answers present" in {
        UserAnswers().isEstablisherIndividualAddressComplete(0) mustBe None
      }
    }

    "isEstablisherIndividualContactDetailsComplete" must {
      "return true when all answers are present" in {
        val ua =
          UserAnswers()
            .set(EnterEmailId(0), "test@test.com").success.value
            .set(EnterPhoneId(0), "123").success.value

        ua.isEstablisherIndividualContactDetailsComplete(0).value mustBe true
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(EnterEmailId(0), "test@test.com").success.value

        ua.isEstablisherIndividualContactDetailsComplete(0).value mustBe false
      }

      "return None when no answer is present" in {
        UserAnswers().isEstablisherIndividualContactDetailsComplete(0) mustBe None
      }
    }
  }

  "Establisher Company completion status should be returned correctly" when {
    "isEstablisherCompanyComplete" must {
      "return true when all answers are present" in {
        val ua =
          UserAnswers()
            .set(EstablisherKindId(0), EstablisherKind.Company).success.value
            .set(CompanyDetailsId(0), CompanyDetails("test company")).success.value
            .set(HaveCompanyNumberId(0), false).success.value
            .set(NoCompanyNumberReasonId(0), "reason").success.value
            .set(HaveUTRId(0), false).success.value
            .set(NoUTRReasonId(0), "reason").success.value
            .set(HaveVATId(0), false).success.value
            .set(HavePAYEId(0), false).success.value
            .set(partnershipAddress.AddressId(0), Data.address).success.value
            .set(partnershipAddress.PreviousAddressId(0), Data.address).success.value
            .set(partnershipAddress.AddressYearsId(0), false).success.value
            .set(companyContact.EnterEmailId(0), "test@test.com").success.value
            .set(companyContact.EnterPhoneId(0), "123").success.value

        ua.isEstablisherCompanyComplete(0) mustBe true
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(EstablisherKindId(0), EstablisherKind.Company).success.value
            .set(CompanyDetailsId(0), CompanyDetails("test company")).success.value
            .set(EstablisherKindId(1), EstablisherKind.Company).success.value

        ua.isEstablisherCompanyComplete(1) mustBe false
      }
    }

    "isEstablisherCompanyDetailsComplete" must {
      "return true when all answers are present" in {

        val ua1 =
          UserAnswers()
            .set(HaveCompanyNumberId(0), true).success.value
            .set(CompanyNumberId(0), ReferenceValue("AB123456C")).success.value
            .set(HaveUTRId(0), true).success.value
            .set(CompanyUTRId(0), ReferenceValue("1234567890")).success.value
            .set(HaveVATId(0), true).success.value
            .set(VATId(0), ReferenceValue("123456789")).success.value
            .set(HavePAYEId(0), true).success.value
            .set(PAYEId(0), ReferenceValue("12345678")).success.value

        val ua2 =
          UserAnswers()
            .set(HaveCompanyNumberId(0), false).success.value
            .set(NoCompanyNumberReasonId(0), "Reason").success.value
            .set(HaveUTRId(0), false).success.value
            .set(NoUTRReasonId(0), "Reason").success.value
            .set(HaveVATId(0), false).success.value
            .set(HavePAYEId(0), false).success.value


        ua1.isEstablisherCompanyDetailsComplete(0) mustBe Some(true)
        ua2.isEstablisherCompanyDetailsComplete(0) mustBe Some(true)
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(HaveCompanyNumberId(0), false).success.value

        ua.isEstablisherCompanyDetailsComplete(0) mustBe Some(false)

      }
    }

    "isEstablisherCompanyContactDetailsComplete" must {
      "return true when all answers are present" in {
        val ua =
          UserAnswers()
            .set(companyContact.EnterEmailId(0), "test@test.com").success.value
            .set(companyContact.EnterPhoneId(0), "123").success.value

        ua.isEstablisherCompanyContactDetailsComplete(0).value mustBe true
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(companyContact.EnterEmailId(0), "test@test.com").success.value

        ua.isEstablisherCompanyContactDetailsComplete(0).value mustBe false
      }

      "return None when no answer is present" in {
        UserAnswers().isEstablisherCompanyContactDetailsComplete(0) mustBe None
      }
    }

    "isEstablisherCompanyAddressComplete" must {
      "return true when address is complete and address years is true" in {
        val ua =
          UserAnswers()
            .set(EstablisherKindId(0), EstablisherKind.Partnership).success.value
            .set(PartnershipDetailsId(0), PartnershipDetails("test partnership")).success.value
            .set(partnershipAddress.AddressId(0), Data.address).success.value
            .set(partnershipAddress.AddressYearsId(0), true).success.value

        ua.isEstablisherCompanyAddressComplete(0).value mustBe true
      }

      "return true when address is complete and address years is false and trading time is false" in {
        val ua =
          UserAnswers()
            .set(EstablisherKindId(0), EstablisherKind.Partnership).success.value
            .set(PartnershipDetailsId(0), PartnershipDetails("test partnership")).success.value
            .set(partnershipAddress.AddressId(0), Data.address).success.value
            .set(partnershipAddress.AddressYearsId(0), false).success.value
            .set(partnershipAddress.TradingTimeId(0), false).success.value

        ua.isEstablisherCompanyAddressComplete(0).value mustBe true
      }

      "return true when address is complete and previous address is complete" in {
        val ua =
          UserAnswers()
            .set(EstablisherKindId(0), EstablisherKind.Partnership).success.value
            .set(PartnershipDetailsId(0), PartnershipDetails("test partnership")).success.value
            .set(partnershipAddress.AddressId(0), Data.address).success.value
            .set(partnershipAddress.AddressYearsId(0), false).success.value
            .set(partnershipAddress.TradingTimeId(0), true).success.value
            .set(partnershipAddress.PreviousAddressId(0), Data.address).success.value

        ua.isEstablisherCompanyAddressComplete(0).value mustBe true
      }

      "return false when address is complete but no address years is present" in {
        val ua =
          UserAnswers()
            .set(EstablisherKindId(0), EstablisherKind.Partnership).success.value
            .set(PartnershipDetailsId(0), PartnershipDetails("test partnership")).success.value
            .set(partnershipAddress.AddressId(0), Data.address).success.value

        ua.isEstablisherCompanyAddressComplete(0).value mustBe false
      }

      "return false when address is complete but no previous address is present" in {
        val ua =
          UserAnswers()
            .set(EstablisherKindId(0), EstablisherKind.Partnership).success.value
            .set(PartnershipDetailsId(0), PartnershipDetails("test partnership")).success.value
            .set(partnershipAddress.AddressId(0), Data.address).success.value
            .set(partnershipAddress.AddressYearsId(0), false).success.value
            .set(partnershipAddress.TradingTimeId(0), true).success.value

        ua.isEstablisherCompanyAddressComplete(0).value mustBe false
      }
    }

    "isDirectorComplete" must {
      "return true when all answers are present" in {
        val ua1 =
          UserAnswers()
            .set(DirectorDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(DirectorHasNINOId(0, 0), true).success.value
            .set(DirectorNINOId(0, 0), ReferenceValue("1234567890")).success.value
            .set(DirectorHasUTRId(0, 0), true).success.value
            .set(DirectorEnterUTRId(0, 0), ReferenceValue("123456789")).success.value
            .setOrException(directorAddress.AddressId(0, 0), Data.address)
            .setOrException(directorAddress.AddressYearsId(0, 0), true)
            .set(directorContact.EnterEmailId(0, 0), "test@test.com").success.value
            .set(directorContact.EnterPhoneId(0, 0), "123").success.value

        val ua2 =
          UserAnswers()
            .set(DirectorDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(DirectorHasNINOId(0, 0), false).success.value
            .set(DirectorNoNINOReasonId(0, 0), "Reason").success.value
            .set(DirectorHasUTRId(0, 0), false).success.value
            .set(DirectorNoUTRReasonId(0, 0), "Reason").success.value
            .setOrException(directorAddress.AddressId(0, 0), Data.address)
            .setOrException(directorAddress.AddressYearsId(0, 0), false)
            .setOrException(directorAddress.PreviousAddressId(0, 0), Data.address)
            .set(directorContact.EnterEmailId(0, 0), "test@test.com").success.value
            .set(directorContact.EnterPhoneId(0, 0), "123").success.value

        ua1.isDirectorComplete(0, 0) mustBe true
        ua2.isDirectorComplete(0, 0) mustBe true
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(DirectorDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(DirectorHasNINOId(0, 0), false).success.value
            .set(DirectorHasUTRId(0, 0), false).success.value
            .setOrException(directorAddress.AddressId(0, 0), Data.address)
            .setOrException(directorAddress.AddressYearsId(0, 0), false)

        ua.isDirectorComplete(0,0) mustBe false
      }
    }

    "isDirectorDetailsComplete" must {
      "return true when all answers are present" in {
        val ua1 =
          UserAnswers()
            .set(DirectorDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(DirectorHasNINOId(0, 0), true).success.value
            .set(DirectorNINOId(0, 0), ReferenceValue("1234567890")).success.value
            .set(DirectorHasUTRId(0, 0), true).success.value
            .set(DirectorEnterUTRId(0, 0), ReferenceValue("123456789")).success.value

        val ua2 =
          UserAnswers()
            .set(DirectorDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(DirectorHasNINOId(0, 0), false).success.value
            .set(DirectorNoNINOReasonId(0, 0), "Reason").success.value
            .set(DirectorHasUTRId(0, 0), false).success.value
            .set(DirectorNoUTRReasonId(0, 0), "Reason").success.value

        ua1.isDirectorDetailsComplete(0, 0) mustBe Some(true)
        ua2.isDirectorDetailsComplete(0, 0) mustBe Some(true)
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(DirectorDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(DirectorHasNINOId(0, 0), false).success.value
            .set(DirectorHasUTRId(0, 0), false).success.value

        ua.isDirectorDetailsComplete(0,0) mustBe Some(false)
      }
    }

    "isDirectorAddressComplete" must {
      "return true when all answers are present" in {
        val ua1 =
          UserAnswers()
            .setOrException(directorAddress.AddressId(0, 0), Data.address)
            .setOrException(directorAddress.AddressYearsId(0, 0), true)

        val ua2 =
          UserAnswers()
            .setOrException(directorAddress.AddressId(0, 0), Data.address)
            .setOrException(directorAddress.AddressYearsId(0, 0), false)
            .setOrException(directorAddress.PreviousAddressId(0, 0), Data.address)

        ua1.isDirectorAddressComplete(0, 0) mustBe Some(true)
        ua2.isDirectorAddressComplete(0, 0) mustBe Some(true)
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .setOrException(directorAddress.AddressId(0, 0), Data.address)
            .setOrException(directorAddress.AddressYearsId(0, 0), false)

        ua.isDirectorAddressComplete(0, 0) mustBe Some(false)
      }
    }
  }

  "Establisher Partnership completion status should be returned correctly" when {
    "isEstablisherPartnershipComplete" must {
      "return true when all answers are present" in {
        val ua =
          UserAnswers()
            .set(EstablisherKindId(0), EstablisherKind.Partnership).success.value
            .set(PartnershipDetailsId(0), PartnershipDetails("test partnership")).success.value
            .set(partnershipDetails.HaveUTRId(0), false).success.value
            .set(partnershipDetails.NoUTRReasonId(0), "reason").success.value
            .set(partnershipDetails.HaveVATId(0), false).success.value
            .set(partnershipDetails.HavePAYEId(0), false).success.value
            .setOrException(partnershipAddress.AddressId(0), Data.address)
            .setOrException(partnershipAddress.PreviousAddressId(0), Data.address)
            .setOrException(partnershipAddress.AddressYearsId(0), true)
            .set(partnershipContact.EnterEmailId(0), "test@test.com").success.value
            .set(partnershipContact.EnterPhoneId(0), "123").success.value

        ua.isEstablisherPartnershipComplete(0) mustBe true
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(EstablisherKindId(0), EstablisherKind.Partnership).success.value
            .set(PartnershipDetailsId(0), PartnershipDetails("test partnership")).success.value
            .set(EstablisherKindId(1), EstablisherKind.Partnership).success.value

        ua.isEstablisherPartnershipComplete(1) mustBe false
      }
    }

    "isEstablisherPartnershipDetailsComplete" must {
      "return true when all answers are present" in {

        val ua1 =
          UserAnswers()
            .set(partnershipDetails.HaveUTRId(0), true).success.value
            .set(partnershipDetails.PartnershipUTRId(0), ReferenceValue("1234567890")).success.value
            .set(partnershipDetails.HaveVATId(0), true).success.value
            .set(partnershipDetails.VATId(0), ReferenceValue("123456789")).success.value
            .set(partnershipDetails.HavePAYEId(0), true).success.value
            .set(partnershipDetails.PAYEId(0), ReferenceValue("12345678")).success.value

        val ua2 =
          UserAnswers()
            .set(partnershipDetails.HaveUTRId(0), false).success.value
            .set(partnershipDetails.NoUTRReasonId(0), "Reason").success.value
            .set(partnershipDetails.HaveVATId(0), false).success.value
            .set(partnershipDetails.HavePAYEId(0), false).success.value


        ua1.isEstablisherPartnershipDetailsComplete(0) mustBe Some(true)
        ua2.isEstablisherPartnershipDetailsComplete(0) mustBe Some(true)
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(partnershipDetails.HaveUTRId(0), false).success.value

        ua.isEstablisherPartnershipDetailsComplete(0) mustBe Some(false)

      }
    }

    "isEstablisherPartnershipAddressComplete" must {
      "return true when all answers are present" in {
        val ua1 =
          UserAnswers()
            .setOrException(partnershipAddress.AddressId(0), Data.address)
            .setOrException(partnershipAddress.AddressYearsId(0), true)

        val ua2 =
          UserAnswers()
            .setOrException(partnershipAddress.AddressId(0), Data.address)
            .setOrException(partnershipAddress.AddressYearsId(0), false)
            .setOrException(partnershipAddress.TradingTimeId(0), true)
            .setOrException(partnershipAddress.PreviousAddressId(0), Data.address)

        ua1.isEstablisherPartnershipAddressComplete(0) mustBe Some(true)
        ua2.isEstablisherPartnershipAddressComplete(0) mustBe Some(true)
      }

      "return false when some answer is missing" in {
        val ua1 =
          UserAnswers()
            .setOrException(partnershipAddress.AddressId(0), Data.address)
            .setOrException(partnershipAddress.AddressYearsId(0), false)

        val ua2 =
          UserAnswers()
            .setOrException(partnershipAddress.AddressId(0), Data.address)

        ua1.isEstablisherPartnershipAddressComplete(0) mustBe Some(false)
        ua2.isEstablisherPartnershipAddressComplete(0) mustBe Some(false)
      }

      "return None when no answers present" in {
        UserAnswers().isEstablisherPartnershipAddressComplete(0) mustBe None
      }
    }

    "isEstablisherPartnershipContactDetailsComplete" must {
      "return true when all answers are present" in {
        val ua =
          UserAnswers()
            .set(partnershipContact.EnterEmailId(0), "test@test.com").success.value
            .set(partnershipContact.EnterPhoneId(0), "123").success.value

        ua.isEstablisherPartnershipContactDetailsComplete(0).value mustBe true
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(partnershipContact.EnterEmailId(0), "test@test.com").success.value

        ua.isEstablisherPartnershipContactDetailsComplete(0).value mustBe false
      }

      "return None when no answer is present" in {
        UserAnswers().isEstablisherPartnershipContactDetailsComplete(0) mustBe None
      }
    }

    "isPartnerComplete" must {
      "return true when all answers are present" in {
        val ua1 =
          UserAnswers()
            .set(PartnerDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(PartnerHasNINOId(0, 0), true).success.value
            .set(PartnerNINOId(0, 0), ReferenceValue("1234567890")).success.value
            .set(PartnerHasUTRId(0, 0), true).success.value
            .set(PartnerEnterUTRId(0, 0), ReferenceValue("123456789")).success.value
            .setOrException(partnerAddress.AddressId(0, 0), Data.address)
            .setOrException(partnerAddress.AddressYearsId(0, 0), true)
            .set(partnerContact.EnterEmailId(0, 0), "test@test.com").success.value
            .set(partnerContact.EnterPhoneId(0, 0), "123").success.value

        val ua2 =
          UserAnswers()
            .set(PartnerDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(PartnerHasNINOId(0, 0), false).success.value
            .set(PartnerNoNINOReasonId(0, 0), "Reason").success.value
            .set(PartnerHasUTRId(0, 0), false).success.value
            .set(PartnerNoUTRReasonId(0, 0), "Reason").success.value
            .setOrException(partnerAddress.AddressId(0, 0), Data.address)
            .setOrException(partnerAddress.AddressYearsId(0, 0), false)
            .setOrException(partnerAddress.PreviousAddressId(0, 0), Data.address)
            .set(partnerContact.EnterEmailId(0, 0), "test@test.com").success.value
            .set(partnerContact.EnterPhoneId(0, 0), "123").success.value

        ua1.isPartnerComplete(0, 0) mustBe true
        ua2.isPartnerComplete(0, 0) mustBe true
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(PartnerDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(PartnerHasNINOId(0, 0), false).success.value
            .set(PartnerHasUTRId(0, 0), false).success.value
            .setOrException(partnerAddress.AddressId(0, 0), Data.address)
            .setOrException(partnerAddress.AddressYearsId(0, 0), false)

        ua.isPartnerComplete(0,0) mustBe false
      }
    }

    "isPartnerDetailsComplete" must {
      "return true when all answers are present" in {
        val ua1 =
          UserAnswers()
            .set(PartnerDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(PartnerHasNINOId(0, 0), true).success.value
            .set(PartnerNINOId(0, 0), ReferenceValue("1234567890")).success.value
            .set(PartnerHasUTRId(0, 0), true).success.value
            .set(PartnerEnterUTRId(0, 0), ReferenceValue("123456789")).success.value

        val ua2 =
          UserAnswers()
            .set(PartnerDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(PartnerHasNINOId(0, 0), false).success.value
            .set(PartnerNoNINOReasonId(0, 0), "Reason").success.value
            .set(PartnerHasUTRId(0, 0), false).success.value
            .set(PartnerNoUTRReasonId(0, 0), "Reason").success.value

        ua1.isPartnerDetailsComplete(0, 0) mustBe Some(true)
        ua2.isPartnerDetailsComplete(0, 0) mustBe Some(true)
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .set(PartnerDOBId(0, 0), LocalDate.parse("2001-01-01")).success.value
            .set(PartnerHasNINOId(0, 0), false).success.value
            .set(PartnerHasUTRId(0, 0), false).success.value

        ua.isPartnerDetailsComplete(0,0) mustBe Some(false)
      }
    }

    "isPartnerAddressComplete" must {
      "return true when all answers are present" in {
        val ua1 =
          UserAnswers()
            .setOrException(partnerAddress.AddressId(0, 0), Data.address)
            .setOrException(partnerAddress.AddressYearsId(0, 0), true)

        val ua2 =
          UserAnswers()
            .setOrException(partnerAddress.AddressId(0, 0), Data.address)
            .setOrException(partnerAddress.AddressYearsId(0, 0), false)
            .setOrException(partnerAddress.PreviousAddressId(0, 0), Data.address)

        ua1.isPartnerAddressComplete(0, 0) mustBe Some(true)
        ua2.isPartnerAddressComplete(0, 0) mustBe Some(true)
      }

      "return false when some answer is missing" in {
        val ua =
          UserAnswers()
            .setOrException(partnerAddress.AddressId(0, 0), Data.address)
            .setOrException(partnerAddress.AddressYearsId(0, 0), false)

        ua.isPartnerAddressComplete(0, 0) mustBe Some(false)
      }
    }
  }
}
