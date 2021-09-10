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

package controllers.establishers.company

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import forms.HasReferenceNumberFormProvider
import identifiers.establishers.company.OtherDirectorsId
import identifiers.establishers.company.director.DirectorNameId
import matchers.JsonMatchers
import models.{NormalMode, PersonName}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.scalatest.{BeforeAndAfterEach, TryValues}
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import play.twirl.api.Html
import renderer.Renderer
import uk.gov.hmrc.nunjucks.NunjucksSupport
import uk.gov.hmrc.viewmodels.Radios
import utils.Data.{schemeName, ua}
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message

import scala.concurrent.Future

class OtherDirectorsControllerSpec extends ControllerSpecBase
  with NunjucksSupport
  with JsonMatchers
  with TryValues
  with BeforeAndAfterEach{

  private val onwardRoute: Call =
    controllers.routes.IndexController.onPageLoad()
  private val formProvider: HasReferenceNumberFormProvider =
    new HasReferenceNumberFormProvider()

  private val personName: PersonName =
    PersonName("Jane", "Doe")
  private val userAnswers: UserAnswers =
    ua.set(DirectorNameId(0,0), personName).success.value

  private val templateToBeRendered: String =
    "hasReferenceValueWithHint.njk"
  private val form: Form[Boolean] =
    formProvider("Select yes if you need to tell us about other directors")


  private val commonJson: JsObject =



    Json.obj(
      "pageTitle"     -> Message("messages__otherDirectors__title"),
      "pageHeading"     -> Message("messages__otherDirectors__heading"),
      "schemeName"    -> schemeName,
      "paragraphs"    -> Seq(Message("messages__otherDirectors__lede")),
      "legendClass"   -> "govuk-visually-hidden",
      "isPageHeading" -> true
    )

  override def beforeEach: Unit = {
    reset(
      mockRenderer,
      mockUserAnswersCacheConnector
    )
  }

  private def controller(
                          dataRetrievalAction: DataRetrievalAction
                        ): OtherDirectorsController =
    new OtherDirectorsController(
      messagesApi               = messagesApi,
      navigator                 = new FakeNavigator(desiredRoute = onwardRoute),
      authenticate              = new FakeAuthAction(),
      getData                   = dataRetrievalAction,
      requireData               = new DataRequiredActionImpl,
      formProvider              = formProvider,
      controllerComponents      = controllerComponents,
      userAnswersCacheConnector = mockUserAnswersCacheConnector,
      renderer                  = new Renderer(mockAppConfig, mockRenderer)
    )

  "OtherDirectorsController" must {

    "return OK and the correct view for a GET" in{

      val getData = new FakeDataRetrievalAction(Some(userAnswers))
      val templateCaptor : ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val result: Future[Result] =
        controller(getData)
          .onPageLoad(0, NormalMode)(fakeDataRequest(userAnswers))

      status(result) mustBe OK

      verify(mockRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual  templateToBeRendered

      val json: JsObject =
        Json.obj("radios" -> Radios.yesNo(form("value")))

      jsonCaptor.getValue must containJson(commonJson ++ json)
    }

    "populate the view correctly on a GET when the question has previously been answered Yes" in {
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val ua =
        userAnswers
          .set(OtherDirectorsId(0), true).success.value

      val templateCaptor : ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

      val jsonCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

      val getData = new FakeDataRetrievalAction(Some(ua))

      val result: Future[Result] =
        controller(getData)
          .onPageLoad(0,NormalMode)(fakeDataRequest(userAnswers))

      status(result) mustBe OK

      verify(mockRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual templateToBeRendered

      val json: JsObject =
        Json.obj("radios" -> Radios.yesNo(form.fill(true).apply("value")))

      jsonCaptor.getValue must containJson(commonJson ++ json)
    }

    "populate the view correctly on a GET when the question has previously been answered No" in {
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val ua =
        userAnswers
          .set(OtherDirectorsId(0), false).success.value

      val templateCaptor : ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

      val jsonCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

      val getData = new FakeDataRetrievalAction(Some(ua))

      val result: Future[Result] =
        controller(getData)
          .onPageLoad(0,NormalMode)(fakeDataRequest(userAnswers))

      status(result) mustBe OK

      verify(mockRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual templateToBeRendered

      val json: JsObject =
        Json.obj("radios" -> Radios.yesNo(form.fill(false).apply("value")))

      jsonCaptor.getValue must containJson(commonJson ++ json)
    }

    "redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(Json.obj()))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        fakeRequest
          .withFormUrlEncodedBody(("value", "true"))

      val getData = new FakeDataRetrievalAction(Some(userAnswers))

      val result: Future[Result] =
        controller(getData)
          .onSubmit(0,NormalMode)(request)

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(onwardRoute.url)

      verify(mockUserAnswersCacheConnector, times(1))
        .save(any(), any())(any(), any())
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val request =
        fakeRequest
          .withFormUrlEncodedBody(("value", "invalid value"))

      val getData = new FakeDataRetrievalAction(Some(userAnswers))

      val jsonCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

      val templateCaptor : ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

      val result: Future[Result] =
        controller(getData)
          .onSubmit(0,NormalMode)(request)

      val boundForm = form.bind(Map("value" -> "invalid value"))

      status(result) mustBe BAD_REQUEST

      verify(mockRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual templateToBeRendered

      val json: JsObject =
        Json.obj("radios" -> Radios.yesNo(boundForm.apply("value")))

      jsonCaptor.getValue must containJson(commonJson ++ json)

      verify(mockUserAnswersCacheConnector, times(0))
        .save(any(), any())(any(), any())
    }


  }

}
