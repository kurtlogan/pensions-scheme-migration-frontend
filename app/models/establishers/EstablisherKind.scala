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

package models.establishers

import play.api.data.Form
import play.api.mvc.JavascriptLiteral
import uk.gov.hmrc.viewmodels.{MessageInterpolators, Radios}
import utils.{Enumerable, WithName}

sealed trait EstablisherKind

object EstablisherKind {
  val values: Seq[EstablisherKind] = Seq(
    Company, Individual, Partnership
  )

  def radios(form: Form[_]): Seq[Radios.Item] = {
    val items = values.map(value => Radios.Radio(msg"kind.${value.toString}", value.toString))
    Radios(form("value"), items)
  }

  case object Company extends WithName("company") with EstablisherKind

  case object Individual extends WithName("individual") with EstablisherKind

  case object Partnership extends WithName("partnership") with EstablisherKind

  implicit val enumerable: Enumerable[EstablisherKind] =
    Enumerable(values.map(v => v.toString -> v): _*)

  //noinspection ConvertExpressionToSAM
  implicit val jsLiteral: JavascriptLiteral[EstablisherKind] = new JavascriptLiteral[EstablisherKind] {
    override def to(value: EstablisherKind): String = value match {
      case Company => "Company"
      case Individual => "Individual"
      case Partnership => "Partnership"
    }
  }
}
