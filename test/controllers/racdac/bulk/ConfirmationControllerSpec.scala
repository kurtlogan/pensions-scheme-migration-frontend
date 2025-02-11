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

package controllers.racdac.bulk

import connectors.ListOfSchemesConnector
import connectors.cache.CurrentPstrCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.{BulkDataAction, MutableFakeBulkDataAction}
import matchers.JsonMatchers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.nunjucks.NunjucksSupport
import utils.{Data, Enumerable}

import scala.concurrent.Future

class ConfirmationControllerSpec extends ControllerSpecBase with NunjucksSupport with JsonMatchers with Enumerable.Implicits {

  private val templateToBeRendered = "racdac/confirmation.njk"
  private val mockSchemeCacheConnector = mock[CurrentPstrCacheConnector]
  private val mockListOfSchemesConnector: ListOfSchemesConnector = mock[ListOfSchemesConnector]
  private val mutableFakeBulkDataAction: MutableFakeBulkDataAction = new MutableFakeBulkDataAction(false)
  val extraModules: Seq[GuiceableModule] = Seq(
    bind[CurrentPstrCacheConnector].to(mockSchemeCacheConnector),
    bind[ListOfSchemesConnector].to(mockListOfSchemesConnector)
  )

  private val application: Application = new GuiceApplicationBuilder()
    .configure(
      "metrics.jvm" -> false,
      "metrics.enabled" -> false
    )
    .overrides(
      modules ++ extraModules ++ Seq[GuiceableModule](
        bind[BulkDataAction].toInstance(mutableFakeBulkDataAction)
      ): _*
    ).build()

  private def httpPathGET: String = controllers.racdac.bulk.routes.ConfirmationController.onPageLoad().url

  private val jsonToPassToTemplate: JsObject =
    Json.obj(
      "email" -> Data.email,
      "finishUrl" -> mockAppConfig.psaOverviewUrl
    )

  private val confirmationData = Json.obj(
    "confirmationData" -> Json.obj(
      "email" -> Data.email,
      "psaId" -> ""
    )
  )

  override def beforeEach: Unit = {
    super.beforeEach
    when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
  }

  "ConfirmationController" must {

    "return OK and the correct view for a GET" in {
      when(mockSchemeCacheConnector.remove(any(), any())).thenReturn(Future(Ok))
      when(mockSchemeCacheConnector.fetch(any(), any())).thenReturn(Future(Some(confirmationData)))
      when(mockListOfSchemesConnector.removeCache(any())(any(), any())).thenReturn(Future.successful(Ok))
      val templateCaptor : ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, httpGETRequest(httpPathGET)).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual templateToBeRendered

      jsonCaptor.getValue must containJson(jsonToPassToTemplate)
    }

  }
}
