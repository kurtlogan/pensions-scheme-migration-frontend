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

import controllers.actions.{AuthAction, DataRetrievalAction}
import helpers.TaskListHelper
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import views.html.taskList

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TaskListController @Inject()(
                                    override val messagesApi: MessagesApi,
                                    authenticate: AuthAction,
                                    getData: DataRetrievalAction,
                                    taskListHelper: TaskListHelper,
                                    val controllerComponents: MessagesControllerComponents,
                                    val view: taskList
                                  )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData) {
    implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers.getOrElse(UserAnswers())
      Ok(view(taskListHelper.taskList(userAnswers), None))
  }

}
