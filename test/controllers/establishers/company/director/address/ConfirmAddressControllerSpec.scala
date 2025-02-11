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

package controllers.establishers.company.director.address

import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import controllers.actions.MutableFakeDataRetrievalAction
import identifiers.beforeYouStart.SchemeNameId
import identifiers.establishers.company.director.DirectorNameId
import identifiers.establishers.company.director.address.AddressId
import matchers.JsonMatchers
import models.{NormalMode, PersonName}
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.nunjucks.NunjucksSupport
import utils.Data.ua
import utils.{Data, Enumerable, UserAnswers}
import models.Scheme
import scala.concurrent.Future

class ConfirmAddressControllerSpec extends ControllerSpecBase with NunjucksSupport with JsonMatchers with Enumerable.Implicits {

  private val mockAddressLookupConnector = mock[AddressLookupConnector]

  val extraModules: Seq[GuiceableModule] = Seq(
    bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
  )

  private val personName: PersonName = PersonName("Jane", "Doe")
  private val userAnswers: Option[UserAnswers] = Some(ua.setOrException(DirectorNameId(0,0), personName))
  private val mutableFakeDataRetrievalAction: MutableFakeDataRetrievalAction = new MutableFakeDataRetrievalAction()
  private val application: Application = applicationBuilderMutableRetrievalAction(mutableFakeDataRetrievalAction, extraModules).build()
  private val httpPathGET: String = controllers.establishers.company.director.address.routes.ConfirmAddressController.onPageLoad(0, 0, NormalMode).url
  private val httpPathPOST: String = controllers.establishers.company.director.address.routes.ConfirmAddressController.onSubmit(0, 0, NormalMode).url

  private val valuesValid: Map[String, Seq[String]] = Map(
    "line1" -> Seq("1"),
    "line2" -> Seq("2"),
    "line3" -> Seq("3"),
    "line4" -> Seq("4"),
    "postcode" -> Seq("ZZ1 1ZZ"),
    "country" -> Seq("GB")
  )

  private val valuesInvalid: Map[String, Seq[String]] = Map(
    "value" -> Seq.empty
  )

  override def beforeEach: Unit = {
    super.beforeEach
    when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
    when(mockAppConfig.validCountryCodes).thenReturn(Seq("GB"))
  }

  "ConfirmAddress Controller" must {

    "Return OK and the correct view for a GET" in {
      val ua: UserAnswers = UserAnswers()
        .setOrException(SchemeNameId, Data.schemeName)
        .setOrException(DirectorNameId(0, 0), personName)

      mutableFakeDataRetrievalAction.setDataToReturn(Some(ua))

      val result: Future[Result] = route(application, httpGETRequest(httpPathGET)).value

      status(result) mustEqual OK

      val jsonCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

      verify(mockRenderer, times(1))
        .render(ArgumentMatchers.eq("address/manualAddress.njk"), jsonCaptor.capture())(any())

      (jsonCaptor.getValue \ "schemeName").toOption.map(_.as[String]) mustBe Some(Data.schemeName)
    }


    "redirect to Session Expired page for a GET when there is no data" in {
      val ua: UserAnswers = UserAnswers()

      mutableFakeDataRetrievalAction.setDataToReturn(Some(ua))

      val result: Future[Result] = route(application, httpGETRequest(httpPathGET)).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.IndexController.onPageLoad().url
    }

    "Save data to user answers and redirect to next page when valid data is submitted" in {

      when(mockCompoundNavigator.nextPage(any(), any(), any())(any()))
        .thenReturn(controllers.establishers.company.director.details.routes.CheckYourAnswersController.onPageLoad(0, 0))
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(Json.obj()))

      mutableFakeDataRetrievalAction.setDataToReturn(userAnswers)
      val jsonCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])
      val result = route(application, httpPOSTRequest(httpPathPOST, valuesValid)).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.establishers.company.director.details.routes.CheckYourAnswersController.onPageLoad(0, 0).url)

      verify(mockUserAnswersCacheConnector, times(1)).save(any(),jsonCaptor.capture())(any(), any())
      val expectedJson = Json.obj(
          "addressLine1" -> "1",
          "addressLine2" -> "2",
          "addressLine3" -> "3",
          "addressLine4" -> "4",
          "postcode" -> "ZZ1 1ZZ",
          "country" -> "GB"
        )

      (jsonCaptor.getValue \ "establishers" \ 0 \ "director" \ 0 \ AddressId.toString).get mustBe expectedJson
    }

    "return a BAD REQUEST when invalid data is submitted" in {
      mutableFakeDataRetrievalAction.setDataToReturn(userAnswers)

      val result = route(application, httpPOSTRequest(httpPathPOST, valuesInvalid)).value

      status(result) mustEqual BAD_REQUEST

      verify(mockUserAnswersCacheConnector, times(0)).save(any(), any())(any(), any())
    }

    "redirect back to list of schemes for a POST when there is no data" in {
      mutableFakeDataRetrievalAction.setDataToReturn(None)

      val result = route(application, httpPOSTRequest(httpPathPOST, valuesValid)).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.preMigration.routes.ListOfSchemesController.onPageLoad(Scheme).url
    }
  }

}
