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

package identifiers.establishers.company.details

import identifiers.TypedIdentifier
import identifiers.establishers.EstablishersId
import models.ReferenceValue
import play.api.libs.json.{Format, JsPath, Json}
import utils.UserAnswers

case class CompanyNumberId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(index).path \ CompanyNumberId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): UserAnswers =
    userAnswers.remove(NoCompanyNumberReasonId(index))
}

object CompanyNumberId {
  override lazy val toString: String = "companyNumber"
  implicit lazy val formats: Format[CompanyNumberId] = Json.format[CompanyNumberId]
}
