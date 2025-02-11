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

package models

import play.api.data.Form
import uk.gov.hmrc.viewmodels.{MessageInterpolators, Radios}
import uk.gov.hmrc.viewmodels.Text.Literal
import models.prefill.{IndividualDetails => DataPrefillIndividualDetails}

object DataPrefillRadio {

  def radios(form: Form[_], values: Seq[DataPrefillIndividualDetails]): Seq[Radios.Item] = {
    val noneValue = "-1"
    val items = values.map(indvDetails => Radios.Radio(Literal(indvDetails.fullName), indvDetails.index.toString)) :+
      Radios.Radio(msg"messages__prefill__label__none", noneValue)
    Radios(form("value"), items)
  }
}
