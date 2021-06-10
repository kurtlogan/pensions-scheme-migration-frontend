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

package helpers

import base.SpecBase
import identifiers.beforeYouStart.{EstablishedCountryId, SchemeTypeId, WorkingKnowledgeId}
import identifiers.establishers.EstablisherKindId
import identifiers.establishers.individual.EstablisherNameId
import models.establishers.EstablisherKind
import models.{EntitySpoke, _}
import org.scalatest.{MustMatchers, OptionValues, TryValues}
import utils.Data.{schemeName, ua}
import utils.Enumerable
import viewmodels.Message

class SpokeCreationServiceSpec
  extends SpecBase
    with MustMatchers
    with OptionValues
    with TryValues
    with Enumerable.Implicits {

  val spokeCreationService = new SpokeCreationService()

  "getBeforeYouStartSpoke" must {

    "display the spoke with link to cya page with complete status if the spoke is completed" in {
      val userAnswers = ua.set(SchemeTypeId, SchemeType.SingleTrust).get
        .set(WorkingKnowledgeId, true).get
        .set(EstablishedCountryId, "GB").get

      val expectedSpoke = Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__before_you_start_link_text", schemeName),
        controllers.beforeYouStartSpoke.routes.CheckYourAnswersController.onPageLoad().url), Some(true)))

      val result = spokeCreationService.getBeforeYouStartSpoke(userAnswers, schemeName)
      result mustBe expectedSpoke
    }
  }

  "getAboutSpoke" when {
    "in subscription" must {
      "display all the spokes with link to first page, blank status if the spoke is uninitiated" in {

        val expectedSpoke = Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_members_link_text", schemeName),
            controllers.aboutMembership.routes.CheckYourAnswersController.onPageLoad().url), None)
        )

        val result = spokeCreationService.membershipDetailsSpoke(ua, schemeName)
        result mustBe expectedSpoke
      }

    }
  }

  "getAddEstablisherHeaderSpokes" must {
    "return no spokes when no establishers and view only" in {
      val result = spokeCreationService.getAddEstablisherHeaderSpokes(ua, viewOnly = true)
      result mustBe Nil
    }

    "return all the spokes with appropriate links when no establishers and NOT view only" in {
      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_add_link"),
            controllers.establishers.routes.EstablisherKindController.onPageLoad(0).url), None)
        )

      val result = spokeCreationService.getAddEstablisherHeaderSpokes(ua, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when establishers and NOT view only" in {
      val userAnswers = ua.set(EstablisherKindId(0), EstablisherKind.Individual).flatMap(
        _.set(EstablisherNameId(0), PersonName("a", "b"))).get

      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_change_link"),
            controllers.establishers.routes.AddEstablisherController.onPageLoad().url), None)
        )

      val result = spokeCreationService.getAddEstablisherHeaderSpokes(userAnswers, viewOnly = false)
      result mustBe expectedSpoke
    }
  }

  "getEstablisherIndividualSpokes" must {
    "display all the spokes with appropriate links and incomplete status when no data is returned from TPSS" in {
      val userAnswers =
        ua
          .set(EstablisherKindId(0), EstablisherKind.Individual).success.value
          .set(EstablisherNameId(0), PersonName("a", "b")).success.value

      val expectedSpoke =
        Seq(
          EntitySpoke(
            link = TaskListLink(
              text = "Add details for a b",
              target = "someUrl",
              visuallyHiddenText = None
            ),
            isCompleted = Some(false)
          ),
          EntitySpoke(
            link = TaskListLink(
              text = "Add address for a b",
              target = "someUrl",
              visuallyHiddenText = None
            ),
            isCompleted = Some(false)
          ),
          EntitySpoke(
            link = TaskListLink(
              text = "Add contact details for a b",
              target = "someUrl",
              visuallyHiddenText = None
            ),
            isCompleted = Some(false)
          )
        )

      val result =
        spokeCreationService.getEstablisherIndividualSpokes(
          answers = userAnswers,
          name = "a b",
          index = Some(0)
        )
      result mustBe expectedSpoke
    }

    //    "display all the spokes with appropriate links, in progress status when establisher individual is in progress" in {
    //      val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true).
    //        establishersIndividualName(index = 0, PersonName("s", "l")).
    //        establishersIndividualNino(0, ReferenceValue("AB100100A")).
    //        establishersIndividualAddress(index = 0, address).
    //        establishersIndividualEmail(index = 0, email = "s@s.com")
    //      val expectedSpoke = estIndividualInProgressSpoke(NormalMode, srn = None, linkText = "change", status = Some(false))
    //
    //      val result = spokeCreationService.getEstablisherIndividualSpokes(userAnswers, NormalMode, None, schemeName, None)
    //      result mustBe expectedSpoke
    //    }
    //
    //    "display all the spokes with appropriate links, complete status when establisher individual is completed" in {
    //      val userAnswers = setCompleteEstIndividual(0, userAnswersWithSchemeName).isEstablisherNew(0, flag = true)
    //      val expectedSpoke = estIndividualCompleteSpoke(NormalMode, srn = None, linkText = "change", status = Some(true))
    //
    //      val result = spokeCreationService.getEstablisherIndividualSpokes(userAnswers, NormalMode, None, schemeName, None)
    //      result mustBe expectedSpoke
    //    }
  }

  "declarationSpoke" must {
    "return declaration spoke with link" in {
      val expectedSpoke = Seq(EntitySpoke(TaskListLink(
        messages("messages__schemeTaskList__declaration_link"),
        controllers.routes.DeclarationController.onPageLoad().url)
      ))

      spokeCreationService.declarationSpoke mustBe expectedSpoke
    }
  }


}