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

package controllers.establishers.company.details

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import forms.PAYEFormProvider
import identifiers.establishers.company.CompanyDetailsId
import identifiers.establishers.company.details.PAYEId
import matchers.JsonMatchers
import models.{Index, NormalMode, ReferenceValue}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.{BeforeAndAfterEach, TryValues}
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import play.twirl.api.Html
import renderer.Renderer
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.Data.{company, schemeName, ua}
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message

import scala.concurrent.Future

class PAYEControllerSpec extends ControllerSpecBase with NunjucksSupport with JsonMatchers with TryValues with BeforeAndAfterEach {

  private val index: Index = Index(0)
  private val referenceValue: ReferenceValue = ReferenceValue("12345678")
  private val userAnswers: UserAnswers = ua.set(CompanyDetailsId(index), company).success.value

  private val formProvider: PAYEFormProvider = new PAYEFormProvider()
  private val form: Form[ReferenceValue] = formProvider(Message("messages__havePAYE__error", company.companyName))
  private val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  private val templateToBeRendered: String = "enterReferenceValueWithHint.njk"

  private val commonJson: JsObject =
    Json.obj(
      "pageTitle"     -> messages("messages__paye", messages("messages__company")),
      "pageHeading"     -> messages("messages__paye", company.companyName),
      "schemeName"    -> schemeName,
      "paragraphs" -> Json.arr(messages("messages__paye__p", company.companyName)),
      "legendClass"   -> "govuk-visually-hidden",
      "isPageHeading" -> true
    )

  private def controller(dataRetrievalAction: DataRetrievalAction): PAYEController =
    new PAYEController(messagesApi, new FakeNavigator(desiredRoute = onwardRoute), new FakeAuthAction(), dataRetrievalAction,
      new DataRequiredActionImpl, formProvider, controllerComponents, mockUserAnswersCacheConnector, new Renderer(mockAppConfig, mockRenderer))

  override def beforeEach: Unit = reset(mockRenderer, mockUserAnswersCacheConnector)

  "PAYEController" must {
    "return OK and the correct view for a GET" in {
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])
      val getData = new FakeDataRetrievalAction(Some(userAnswers))

      val result: Future[Result] = controller(getData).onPageLoad(0, NormalMode)(fakeDataRequest(userAnswers))

      status(result) mustBe OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual templateToBeRendered
      jsonCaptor.getValue must containJson(commonJson)

    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

      val ua = userAnswers.set(PAYEId(0), referenceValue).success.value
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])
      val getData = new FakeDataRetrievalAction(Some(ua))

      val result: Future[Result] = controller(getData).onPageLoad(0, NormalMode)(fakeDataRequest(userAnswers))

      status(result) mustBe OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual templateToBeRendered
      jsonCaptor.getValue must containJson(commonJson)
    }

    "redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(("value" -> referenceValue.value))
      val getData = new FakeDataRetrievalAction(Some(userAnswers))

      val result: Future[Result] = controller(getData).onSubmit(0, NormalMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      verify(mockUserAnswersCacheConnector, times(1)).save(any(), any())(any(), any())
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

      val request = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val getData = new FakeDataRetrievalAction(Some(userAnswers))
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result: Future[Result] = controller(getData).onSubmit(0, NormalMode)(request)
      val boundForm = form.bind(Map("value" -> "invalid value"))

      status(result) mustBe BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
      templateCaptor.getValue mustEqual templateToBeRendered
      jsonCaptor.getValue must containJson(commonJson)
      verify(mockUserAnswersCacheConnector, times(0)).save(any(), any())(any(), any())
    }
  }
}
