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

package identifiers.establishers.company.address

import identifiers.TypedIdentifier
import identifiers.establishers.EstablishersId
import play.api.libs.json.{Format, JsPath, Json}
import utils.UserAnswers

case class TradingTimeId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = EstablishersId(index).path \ TradingTimeId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): UserAnswers = {
    value match {
      case Some(false) => userAnswers
        .removeAll(Set(PreviousAddressId(index), PreviousAddressListId(index), EnterPreviousPostCodeId(index)))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object TradingTimeId {
  override lazy val toString: String = "tradingTime"

  implicit lazy val formats: Format[TradingTimeId] =
    Json.format[TradingTimeId]
}

