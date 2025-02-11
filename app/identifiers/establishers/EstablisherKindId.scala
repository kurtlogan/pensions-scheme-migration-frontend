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

package identifiers.establishers

import identifiers.TypedIdentifier
import models.establishers.EstablisherKind
import play.api.libs.json.JsPath

case class EstablisherKindId(index: Int) extends TypedIdentifier[EstablisherKind] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherKindId.toString
}

object EstablisherKindId {
  override lazy val toString: String = "establisherKind"
}
