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

package identifiers.trustees.company.contacts

import identifiers.TypedIdentifier
import identifiers.trustees.TrusteesId
import play.api.libs.json.{Format, JsPath, Json}

case class EnterEmailId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath =
    TrusteesId(index).path \ EnterEmailId.toString
}

object EnterEmailId {
  override lazy val toString: String =
    "email"

  def collectionPath(index: Int): JsPath =
    TrusteesId(index).path \ EnterEmailId.toString

  implicit lazy val formats: Format[EnterEmailId] =
    Json.format[EnterEmailId]
}


