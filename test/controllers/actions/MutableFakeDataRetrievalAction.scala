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

package controllers.actions

import models.MigrationLock
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import utils.{Data, UserAnswers}

import scala.concurrent.{ExecutionContext, Future}

class MutableFakeDataRetrievalAction extends DataRetrievalAction {
  private var dataToReturn: Option[UserAnswers] = None
  private var lockToReturn: Option[MigrationLock] = Some(Data.migrationLock)

  def setDataToReturn(userAnswers: Option[UserAnswers]): Unit = dataToReturn = userAnswers

  def setLockToReturn(lock: Option[MigrationLock]): Unit = lockToReturn = lock

  override protected def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = {
    Future(OptionalDataRequest(request.request, dataToReturn, request.psaId, lockToReturn))
  }

  override protected implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
