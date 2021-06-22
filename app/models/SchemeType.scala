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

package models

import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json._
import uk.gov.hmrc.viewmodels.MessageInterpolators
import utils.WithName
import viewmodels.forNunjucks._

sealed trait SchemeType

object SchemeType {
  val values = Seq(
    SingleTrust,
    GroupLifeDeath,
    BodyCorporate
  )
  val mappings: Map[String, SchemeType] = values.map(v => (v.toString, v)).toMap




  def radios(form: Form[_])(implicit messages: Messages): Seq[Radios.Item] = {
    val items: Seq[Radios.Radio] = values.map(value =>
      Radios.Radio(msg"messages__scheme_type_${value.toString}", value.toString,
        Some(Hint(msg"messages__scheme_type_${value.toString}_hint", "hint-id"))))

    val input = TextInput(
      "schemeType.schemeTypeDetails",
      form("schemeType.schemeTypeDetails").value.getOrElse(""),
      "schemeType.schemeTypeDetails",
      Label(msg"messages__scheme_details__type_other_more",  Seq("govuk-label govuk-label--s"))
    )

    val otherItem: Radios.Radio = Radios.Radio(msg"messages__scheme_type_other", Other.toString.toLowerCase,
        Some(Hint(msg"messages__scheme_type_other_hint", "hint-id")),
      Some(Conditional(TextInput.inputHtml(input, messages))))

    Radios(form("schemeType.type"), items :+ otherItem)
  }

  def getSchemeType(schemeTypeStr: Option[String]): Option[String] =
      schemeTypeStr.flatMap { schemeStr =>
        List(SingleTrust.toString, GroupLifeDeath.toString, BodyCorporate.toString, "other").find(scheme =>
          schemeStr.toLowerCase.contains(scheme.toLowerCase)).map { str =>
          s"messages__scheme_details__type_$str"
        }
      }

  case class Other(schemeTypeDetails: String) extends WithName("other") with SchemeType

  case object SingleTrust extends WithName("single") with SchemeType

  case object GroupLifeDeath extends WithName("group") with SchemeType

  case object BodyCorporate extends WithName("corp") with SchemeType

  implicit val reads: Reads[SchemeType] = {

    (JsPath \ "name").read[String].flatMap {

      case schemeTypeName if schemeTypeName == "other" =>
        (JsPath \ "schemeTypeDetails").read[String]
          .map[SchemeType](Other)
          .orElse(Reads[SchemeType](_ => JsError("Other Value expected")))

      case schemeTypeName if mappings.keySet.contains(schemeTypeName) =>
        Reads(_ => JsSuccess(mappings.apply(schemeTypeName)))

      case _ => Reads(_ => JsError("Invalid Scheme Type"))
    }
  }

  implicit lazy val writes: Writes[SchemeType] = new Writes[SchemeType] {
    def writes(o: SchemeType): JsObject = {
      o match {
        case SchemeType.Other(schemeTypeDetails) =>
          Json.obj("name" -> "other", "schemeTypeDetails" -> schemeTypeDetails)
        case s if mappings.keySet.contains(s.toString) =>
          Json.obj("name" -> s.toString)
      }
    }
  }

}
