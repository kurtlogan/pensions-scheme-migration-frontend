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

///*
// * Copyright 2021 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package helpers.spokes.establishers.partnership
//
//import controllers.establishers.partnership.partner.details.routes._
//import helpers.spokes.Spoke
//import models.Index.indexToInt
//import models.{Index, NormalMode, TaskListLink}
//import play.api.i18n.Messages
//import utils.UserAnswers
//
//
//case class EstablisherPartnerDetails(
//                                         index: Index,
//                                         answers: UserAnswers
//                                       ) extends Spoke {
//  val messageKeyPrefix = "messages__schemeTaskList__partners_"
//  val isPartnerNotExists= answers.allPartnersAfterDelete(indexToInt(index)).isEmpty
//  val linkKeyAndRoute: (String, String) =
//    if (isPartnerNotExists)
//      (s"${messageKeyPrefix}addLink", WhatYouWillNeedController.onPageLoad(index).url)
//    else
//      (s"${messageKeyPrefix}changeLink", controllers.establishers.partnership.routes.AddPartnersController.onPageLoad(index,NormalMode).url)
//
//  override def changeLink(name: String)
//                         (implicit messages: Messages): TaskListLink =
//    TaskListLink(
//      text = Messages(linkKeyAndRoute._1, name),
//      target = linkKeyAndRoute._2,
//      visuallyHiddenText = None
//    )
//
//  override def completeFlag(answers: UserAnswers): Option[Boolean] = None
//}
//
