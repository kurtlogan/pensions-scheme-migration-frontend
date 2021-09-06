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

import connectors.cache.LockCacheConnector
import controllers.actions.FakeAuthAction
import org.mockito.ArgumentMatchers.any
import play.api.mvc.Results
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future


class LogoutControllerSpec extends ControllerSpecBase with Results {
  private val unauthorisedUrl = "/migrate-pension-scheme/unauthorised"
  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private val mockLockCacheConnector = mock[LockCacheConnector]
  def logoutController: LogoutController =
    new LogoutController(mockAuthConnector,mockAppConfig, controllerComponents, FakeAuthAction,mockLockCacheConnector)

  "Logout Controller" must {

    "redirect to feedback survey page for an Individual and clear down session data cache" in {
      when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any())).thenReturn(Future.successful(Some("id")))
      when(mockLockCacheConnector.removeLockByUser(any(),any())).thenReturn(Future.successful(Ok))
      when(mockAppConfig.serviceSignOut).thenReturn("signout")
      val result = logoutController.onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("signout")
      verify(mockAppConfig, times(1)).serviceSignOut
    }

    "redirect to unauthorised page for an Individual " in {
      when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any())).thenReturn(Future.successful(None))
      val result = logoutController.onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(unauthorisedUrl)
    }
  }
}
