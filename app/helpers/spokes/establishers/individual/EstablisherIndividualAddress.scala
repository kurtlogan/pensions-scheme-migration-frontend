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

package helpers.spokes.establishers.individual

import helpers.spokes.Spoke
import models.TaskListLink
import play.api.i18n.Messages
import utils.UserAnswers


case object EstablisherIndividualAddress extends Spoke {
  override def changeLink(name: String)
                         (implicit messages: Messages): TaskListLink =
    TaskListLink(
      text = Messages("messages__schemeTaskList__establisherIndividualAddress_addLink", name),
      target = "someUrl",
      visuallyHiddenText = None
    )

  override def completeFlag(answers: UserAnswers): Option[Boolean] =
    answers.isEstablisherIndividualAddressCompleted
}

