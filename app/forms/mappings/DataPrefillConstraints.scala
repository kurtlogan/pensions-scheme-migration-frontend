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

package forms.mappings

import play.api.data.validation.{Constraint, Invalid, Valid}

trait DataPrefillConstraints {
  private val noneValue = -1
  private val maxNoOfEntity = 10

  protected def noValueInList(errorKey: String): Constraint[List[Int]] = {

    Constraint {
      case lst if lst.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey)
    }
  }

  protected def noneSelectedWithValue(errorKey: String): Constraint[List[Int]] = {

    Constraint {
      case lst if lst.size > 1 && lst.contains(noneValue) =>
        Invalid(errorKey)
      case _ =>
        Valid
    }
  }

  protected def moreThanTen(errorKey: String, entityCount: Int): Constraint[List[Int]] = {

    Constraint {
      case lst if (lst.size + entityCount) > maxNoOfEntity && !lst.contains(noneValue) =>
        Invalid(errorKey)
      case _ =>
        Valid
    }
  }
}
