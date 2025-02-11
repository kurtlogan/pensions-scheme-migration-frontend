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

package controllers.adviser

import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import helpers.cya.MandatoryAnswerMissingException
import identifiers.beforeYouStart.SchemeNameId
import models.NormalMode
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.nunjucks.NunjucksSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class WhatYouWillNeedController @Inject()(
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           val controllerComponents: MessagesControllerComponents,
                                           val renderer: Renderer
                                         )(implicit val ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals
    with NunjucksSupport {
  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData()).async {
    implicit request =>
      val json: JsObject = Json.obj(
        "schemeName" -> request.userAnswers.get(SchemeNameId).getOrElse(throw MandatoryAnswerMissingException(SchemeNameId.toString)),
        "continueUrl" -> routes.AdviserNameController.onPageLoad(NormalMode).url
      )
      renderer.render("adviser/whatYouWillNeed.njk", json).map(Ok(_))
  }
}
