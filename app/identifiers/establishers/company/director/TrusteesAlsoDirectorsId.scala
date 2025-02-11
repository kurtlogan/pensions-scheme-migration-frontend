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

package identifiers.establishers.company.director

import identifiers.TypedIdentifier
import identifiers.establishers.EstablishersId
import play.api.libs.json.JsPath

case class TrusteesAlsoDirectorsId(establisherIndex: Int) extends TypedIdentifier[Seq[Int]] {
  override def path: JsPath = EstablishersId(establisherIndex)
    .path \ TrusteeAlsoDirectorId.toString
}
object TrusteesAlsoDirectorsId {
  override def toString: String = "trusteesAlsoDirectors"
}


