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

package forms.mappings

trait Transforms {

  protected def vatRegistrationNumberTransform(value: String): String = {
    noSpaceWithUpperCaseTransform(value).replaceAll("^[gG][bB]", "")
  }

  protected def noSpaceWithUpperCaseTransform(value: String): String =
    toUpperCaseAlphaOnly(strip(value))

  protected def toUpperCaseAlphaOnly(value: String): String =
    value.map {
      case c if ('a' to 'z').contains(c) => c.toUpper
      case c => c
    }

  protected def strip(value: String): String = {
    value.replaceAll(" ", "")
  }

  protected def noTransform(value: String): String = {
    value
  }

  protected def standardTextTransform(value: String): String = {
    value.trim
  }

  protected def payeTransform(value: String): String = {
    noSpaceWithUpperCaseTransform(value).replaceAll("[\\\\/]", "").trim
  }

  protected def minimiseSpace(value: String): String =
    value.replaceAll(" {2,}", " ")

}
