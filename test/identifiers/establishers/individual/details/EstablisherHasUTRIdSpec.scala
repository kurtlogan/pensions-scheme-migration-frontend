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

package identifiers.establishers.individual.details

import base.SpecBase
import models.ReferenceValue
import org.scalatest.TryValues
import org.scalatest.matchers.must.Matchers
import utils.{Enumerable, UserAnswers}

class EstablisherHasUTRIdSpec
  extends SpecBase
    with Matchers
    with TryValues
    with Enumerable.Implicits {

  "EstablisherHasUTRId cleanup" must {

    val ua1 =
      UserAnswers()
        .set(EstablisherUTRId(0), ReferenceValue("1234567890")).success.value

    val ua2 =
      UserAnswers()
        .set(EstablisherNoUTRReasonId(0), "Reason").success.value

    "when set to false remove `EstablisherUTRId`" in {
      val res: UserAnswers =
        ua1.set(EstablisherHasUTRId(0), false).success.value

      res.get(EstablisherUTRId(0)).isDefined mustBe false
    }

    "when set to true remove `EstablisherNoUTRReasonId`" in {
      val res: UserAnswers =
        ua2.set(EstablisherHasUTRId(0), true).success.value

      res.get(EstablisherNoUTRReasonId(0)).isDefined mustBe false
    }
  }
}
