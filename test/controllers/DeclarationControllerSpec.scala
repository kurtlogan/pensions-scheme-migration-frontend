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

package controllers

import connectors.{EmailConnector, EmailSent, MinimalDetailsConnector}
import controllers.actions.MutableFakeDataRetrievalAction
import identifiers.beforeYouStart.{SchemeNameId, WorkingKnowledgeId}
import matchers.JsonMatchers
import models.MinPSA
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.mockito.ArgumentMatchers.any
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.nunjucks.NunjucksSupport
import utils.Data.{psaName, schemeName, ua}
import utils.{Enumerable, UserAnswers}

import scala.concurrent.Future

class DeclarationControllerSpec extends ControllerSpecBase with NunjucksSupport with JsonMatchers with Enumerable.Implicits {

  private val templateToBeRendered = "declaration.njk"

  private val mutableFakeDataRetrievalAction: MutableFakeDataRetrievalAction = new MutableFakeDataRetrievalAction()

  val extraModules: Seq[GuiceableModule] = Seq(
    bind[EmailConnector].toInstance(mockEmailConnector),
    bind[MinimalDetailsConnector].toInstance(mockMinimalDetailsConnector)
  )

  private val application: Application = applicationBuilderMutableRetrievalAction(mutableFakeDataRetrievalAction, extraModules).build()

  private def httpPathGET: String = controllers.routes.DeclarationController.onPageLoad().url
  private def httpPathPOST: String = controllers.routes.DeclarationController.onSubmit().url

  private val jsonToPassToTemplate: JsObject =
    Json.obj(
      "schemeName" -> schemeName,
      "isCompany" -> true,
      "hasWorkingKnowledge" -> true,
      "submitUrl" -> routes.DeclarationController.onSubmit().url
    )

  override def beforeEach: Unit = {
    super.beforeEach
    when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
  }


  "DeclarationController" must {

    "return OK and the correct view for a GET" in {
      val ua: UserAnswers = UserAnswers()
        .setOrException(SchemeNameId, schemeName)
        .setOrException(WorkingKnowledgeId, true)

      mutableFakeDataRetrievalAction.setDataToReturn(Some(ua))

      val templateCaptor:ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor:ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, httpGETRequest(httpPathGET)).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual templateToBeRendered

      jsonCaptor.getValue must containJson(jsonToPassToTemplate)
    }

    "redirect to next page when button is clicked" in {

      mutableFakeDataRetrievalAction.setDataToReturn(Some(ua))
      when(mockAppConfig.schemeConfirmationEmailTemplateId).thenReturn("test template name")
      when(mockMinimalDetailsConnector.getPSADetails(any())(any(), any()))
        .thenReturn(Future.successful(MinPSA("test@test.com", isPsaSuspended = false, Some(psaName), None, rlsFlag = false, deceasedFlag = false)))
      when(mockEmailConnector.sendEmail(any(), any(), any(), any())(any(),any())).thenReturn(Future.successful(EmailSent))

      val result = route(application, httpGETRequest(httpPathPOST)).value

      status(result) mustEqual SEE_OTHER

      verify(mockEmailConnector, times(1)).sendEmail(
        ArgumentMatchers.eq("test@test.com"),
        ArgumentMatchers.eq("test template name"),
        ArgumentMatchers.eq(Map("psaName" -> psaName.toString, "schemeName"-> schemeName)),
        any())(any(), any())

      redirectLocation(result) mustBe Some(controllers.routes.SchemeSuccessController.onPageLoad().url)
    }

  }
}
