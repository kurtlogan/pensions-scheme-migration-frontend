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

package identifiers.establishers.partnership.partner.details

import identifiers.TypedIdentifier
import identifiers.establishers.EstablishersId
import play.api.libs.json.{Format, JsPath, Json}
import utils.UserAnswers

case class PartnerHasNINOId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath =
    EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \ PartnerHasNINOId.toString

  override def cleanup(
                        value: Option[Boolean],
                        userAnswers: UserAnswers
                      ): UserAnswers =
    value match {
      case Some(true) =>
        userAnswers.remove(PartnerNoNINOReasonId(establisherIndex, partnerIndex))
      case Some(false) =>
        userAnswers.remove(PartnerNINOId(establisherIndex, partnerIndex))
      case _ =>
        super.cleanup(value, userAnswers)
    }
}

object PartnerHasNINOId {
  override lazy val toString: String =
    "hasNino"

  implicit lazy val formats: Format[PartnerHasNINOId] =
    Json.format[PartnerHasNINOId]
}



