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

package forms

import forms.mappings.{Constraints, Mappings, Transforms}
import models.ReferenceValue
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import viewmodels.Message

import javax.inject.Inject

class CompanyNumberFormProvider @Inject()
  extends Mappings
    with Constraints
    with Transforms {

  def apply(companyName: String)
           (implicit messages: Messages): Form[ReferenceValue] =
    Form(
      mapping(
        "value" -> text(Message("messages__error__company_number_required", companyName))
          .transform(noSpaceWithUpperCaseTransform, noTransform)
          .verifying(
            validCrn(Message("messages__error__company_number_invalid"))
          )
      )(ReferenceValue.applyEditable)(ReferenceValue.unapplyEditable)
    )
}
