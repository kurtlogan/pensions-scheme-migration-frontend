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

package controllers.benefitsAndInsurance

import connectors.cache.UserAnswersCacheConnector
import controllers.ControllerSpecBase
import identifiers.beforeYouStart.SchemeNameId
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.nunjucks.NunjucksRenderer
import utils.{UserAnswers, Data}

import scala.concurrent.Future

class AreBenefitsSecuredControllerSpec extends ControllerSpecBase {

  val extraModules: Seq[GuiceableModule] = Seq(
    bind[NunjucksRenderer].toInstance(mockRenderer),
    bind[UserAnswersCacheConnector].to(mockUserAnswersCacheConnector)
  )

  private def application(ua:Option[UserAnswers]) = applicationBuilderUserAnswers(ua, Nil).build()

  private val httpPathGet: String = controllers.benefitsAndInsurance.routes.AreBenefitsSecuredController.onPageLoad().url

  "AreBenefitsSecured Controller" must {

    "Return OK for a GET" in {

      val ua: UserAnswers = UserAnswers().setOrException(SchemeNameId, Data.schemeName)

      val result: Future[Result] = route(application(Some(ua)), httpGETRequest(httpPathGet)).value

      status(result) mustEqual OK

    }

  }

}
