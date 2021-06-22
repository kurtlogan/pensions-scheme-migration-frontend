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

package controllers.beforeYouStartSpoke

import connectors.cache.UserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.MutableFakeDataRetrievalAction
import forms.beforeYouStart.EstablishedCountryFormProvider
import helpers.CountriesHelper
import identifiers.aboutMembership.CurrentMembersId
import identifiers.beforeYouStart.EstablishedCountryId
import matchers.JsonMatchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.{ArgumentCaptor, Matchers}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.nunjucks.{NunjucksRenderer, NunjucksSupport}
import utils.Data.{countryCodes, schemeName, ua}
import utils.Enumerable

import scala.concurrent.Future

class EstablishedCountryControllerSpec extends ControllerSpecBase with NunjucksSupport with JsonMatchers with Enumerable.Implicits with CountriesHelper {

  private val templateToBeRendered = "beforeYouStart/establishedCountry.njk"
  private val form: Form[String] = new EstablishedCountryFormProvider()()

  private val mutableFakeDataRetrievalAction: MutableFakeDataRetrievalAction = new MutableFakeDataRetrievalAction()
  val extraModules: Seq[GuiceableModule] = Seq(
    bind[NunjucksRenderer].toInstance(mockRenderer),
    bind[UserAnswersCacheConnector].to(mockUserAnswersCacheConnector)
  )
  private val application: Application = applicationBuilder(mutableFakeDataRetrievalAction, extraModules).build()

  private def httpPathGET: String = routes.EstablishedCountryController.onPageLoad().url
  private def httpPathPOST: String = routes.EstablishedCountryController.onSubmit().url

  private val valuesValid: Map[String, Seq[String]] = Map(
    "value" -> Seq("GB")
  )

  private val valuesInvalid: Map[String, Seq[String]] = Map(
    "value" -> Seq.empty
  )

  private val jsonToPassToTemplate: (Form[String], Option[String]) => JsObject = (form, countryOpt) =>
    Json.obj(
    "form" -> form,
    "schemeName" -> schemeName,
    "countries" ->   jsonCountries(countryOpt, mockAppConfig)
  )

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockAppConfig)
    when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
    when(mockAppConfig.validCountryCodes).thenReturn(countryCodes)
  }


  "Established Country Controller" must {

    "return OK and the correct view for a GET" in {
      mutableFakeDataRetrievalAction.setDataToReturn(Some(ua))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, httpGETRequest(httpPathGET)).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual templateToBeRendered

      jsonCaptor.getValue must containJson(jsonToPassToTemplate(form, None))
    }

    "return OK and the correct view for a GET when the question has previously been answered" in {
      val userAnswers = ua.set(EstablishedCountryId, "GB").toOption.get

      mutableFakeDataRetrievalAction.setDataToReturn(Option(userAnswers))

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, httpGETRequest(httpPathGET)).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual templateToBeRendered

      jsonCaptor.getValue must containJson(jsonToPassToTemplate(form.fill("GB"), Some("GB")))
    }

    "redirect to Session Expired page for a GET when there is no data" in {
      mutableFakeDataRetrievalAction.setDataToReturn(None)

      val result = route(application, httpGETRequest(httpPathGET)).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.IndexController.onPageLoad().url
    }

    "Save data to user answers and redirect to next page when valid data is submitted" in {

      val expectedJson = Json.obj()

      when(mockCompoundNavigator.nextPage(Matchers.eq(CurrentMembersId), any())(any()))
        .thenReturn(routes.CheckYourAnswersController.onPageLoad())
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(Json.obj()))

      mutableFakeDataRetrievalAction.setDataToReturn(Some(ua))

      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, httpPOSTRequest(httpPathPOST, valuesValid)).value

      status(result) mustEqual SEE_OTHER

      verify(mockUserAnswersCacheConnector, times(1)).save(any(), jsonCaptor.capture)(any(), any())

      jsonCaptor.getValue must containJson(expectedJson)

      redirectLocation(result) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }

    "return a BAD REQUEST when invalid data is submitted" in {
      mutableFakeDataRetrievalAction.setDataToReturn(Some(ua))

      val result = route(application, httpPOSTRequest(httpPathPOST, valuesInvalid)).value

      status(result) mustEqual BAD_REQUEST

      verify(mockUserAnswersCacheConnector, times(0)).save(any(), any())(any(), any())
    }

    "redirect to Session Expired page for a POST when there is no data" in {
      mutableFakeDataRetrievalAction.setDataToReturn(None)

      val result = route(application, httpPOSTRequest(httpPathPOST, valuesValid)).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.IndexController.onPageLoad().url
    }
  }
}