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

package identifiers.establishers.company.director

import identifiers._
import identifiers.establishers.EstablishersId
import models.PersonName
import play.api.libs.json.{Format, JsPath, Json}

case class DirectorNameId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[PersonName] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorNameId.toString
}

object DirectorNameId {
  override lazy val toString: String = "directorDetails"

  implicit lazy val formats: Format[DirectorNameId] = Json.format[DirectorNameId]

  def collectionPath(establisherIndex: Int): JsPath =
    EstablishersId(establisherIndex).path \ "director" \\ DirectorNameId.toString
}

