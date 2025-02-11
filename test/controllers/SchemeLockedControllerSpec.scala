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

package controllers

import controllers.actions._
import matchers.JsonMatchers
import models.{RacDac, Scheme}
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, MockitoSugar}
import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{status, _}
import play.twirl.api.Html
import renderer.Renderer
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.Data.{schemeName, ua}

import scala.concurrent.Future

class SchemeLockedControllerSpec extends ControllerSpecBase with NunjucksSupport with JsonMatchers with TryValues with MockitoSugar {

  private val templateToBeRendered: String = "schemeLocked.njk"
  private val mutableFakeDataRetrievalAction: MutableFakeDataRetrievalAction = new MutableFakeDataRetrievalAction()

  private def schemeJson: JsObject = Json.obj(
    "schemeName" ->schemeName,
    "schemeType" -> messages("messages__scheme"),
    "returnUrl" -> controllers.preMigration.routes.ListOfSchemesController.onPageLoad(Scheme).url
  )

  val racDacJson: JsObject = Json.obj(
    "schemeName" -> schemeName,
    "schemeType" -> messages("messages__racdac"),
    "returnUrl" -> controllers.preMigration.routes.ListOfSchemesController.onPageLoad(RacDac).url
  )

  private def controller(): SchemeLockedController =
    new SchemeLockedController(messagesApi, new FakeAuthAction(),mutableFakeDataRetrievalAction,
      new DataRequiredActionImpl, controllerComponents,
      new Renderer(mockAppConfig, mockRenderer))

  "SchemeLockedController" must {
    "return OK and the correct view for a GET for scheme" in {
      mutableFakeDataRetrievalAction.setDataToReturn(Some(ua))
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      val templateCaptor : ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

      val result: Future[Result] = controller().onPageLoadScheme()(fakeDataRequest())

      status(result) mustBe OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual templateToBeRendered

      jsonCaptor.getValue must containJson(schemeJson)
    }

    "return OK and the correct view for a GET for rac dac" in {
      mutableFakeDataRetrievalAction.setDataToReturn(Some(ua))
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      val templateCaptor : ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

      val result: Future[Result] = controller().onPageLoadRacDac()(fakeDataRequest())

      status(result) mustBe OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual templateToBeRendered

      jsonCaptor.getValue must containJson(racDacJson)
    }
  }
}
