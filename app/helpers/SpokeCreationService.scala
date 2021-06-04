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

package helpers

import helpers.spokes.{AboutMembersSpoke, BeforeYouStartSpoke, Spoke}
import identifiers.establishers.IsEstablisherNewId
import models.Index.indexToInt
import models.{EntitySpoke, Index, TaskListLink}
import play.api.i18n.Messages
import utils.{Enumerable, UserAnswers}

class SpokeCreationService extends Enumerable.Implicits {

  def getBeforeYouStartSpoke(answers: UserAnswers, name: String)(implicit messages: Messages): Seq[EntitySpoke] =
    Seq(createSpoke(answers, BeforeYouStartSpoke, name))

  def membershipDetailsSpoke(answers: UserAnswers, name: String)(implicit messages: Messages): Seq[EntitySpoke] =
    Seq(createSpoke(answers, AboutMembersSpoke, name))

  def getAddEstablisherHeaderSpokes(answers: UserAnswers, viewOnly: Boolean)(implicit messages: Messages)
  : Seq[EntitySpoke] = {
    (answers.allEstablishersAfterDelete.isEmpty, viewOnly) match {
      case (_, true) =>
        Nil
      case (true, false) =>
        Seq(EntitySpoke(
          TaskListLink(messages("messages__schemeTaskList__sectionEstablishers_add_link"),
            controllers.establishers.routes.EstablisherKindController.onPageLoad(answers.allEstablishers.size).url), None)
        )
      case (false, false) =>
        Seq(EntitySpoke(
          TaskListLink(messages("messages__schemeTaskList__sectionEstablishers_view_link"),
            controllers.establishers.routes.AddEstablisherController.onPageLoad.url), None)
        )
    }
  }

  def getEstablisherIndividualSpokes(answers: UserAnswers, name: String, index: Option[Index]): Seq[EntitySpoke] = {
    val isEstablisherNew = answers.get(IsEstablisherNewId(indexToInt(index.getOrElse(Index(0))))).getOrElse(false)
    Nil
  }

  def declarationSpoke(implicit messages: Messages): Seq[EntitySpoke] =
    Seq(EntitySpoke(TaskListLink(
          messages("messages__schemeTaskList__declaration_link"),
          controllers.routes.DeclarationController.onPageLoad().url)
      ))

  def createSpoke(answers: UserAnswers, spoke: Spoke, name: String)(implicit messages: Messages): EntitySpoke =
    EntitySpoke(spoke.changeLink(name), spoke.completeFlag(answers))

}
