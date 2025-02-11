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

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError

class PhoneFormProviderSpec extends StringFieldBehaviours with Constraints {

  private val maxPhoneNumberLength = 24
  private val keyPhoneNumberLength = "messages__enterPhone__error_maxLength"
  val keyPhoneRequired = "messages__enterPhone__error_required"
  val keyPhoneInvalid = "messages__enterPhone__error_invalid"
  val form = new PhoneFormProvider()(keyPhoneRequired)

  "phone" must {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      "0111-11111"
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, keyPhoneRequired)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "ABC",
      FormError(fieldName, keyPhoneInvalid, Seq(regexPhoneNumber))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxPhoneNumberLength,
      lengthError = FormError(fieldName, keyPhoneNumberLength, Seq(maxPhoneNumberLength))
    )

  }
}
