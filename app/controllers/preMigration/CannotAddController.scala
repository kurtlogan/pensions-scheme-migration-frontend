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

package controllers.preMigration

import config.AppConfig
import connectors.{AncillaryPsaException, ListOfSchemesConnector}
import controllers.actions.AuthAction
import models.{RacDac, Scheme}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.MessageInterpolators

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CannotAddController @Inject()(val appConfig: AppConfig,
                                    override val messagesApi: MessagesApi,
                                    authenticate: AuthAction,
                                    val controllerComponents: MessagesControllerComponents,
                                    listOfSchemesConnector: ListOfSchemesConnector,
                                    renderer: Renderer
                                    )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport {

  def onPageLoadScheme: Action[AnyContent] = authenticate.async { implicit request =>

    listOfSchemesConnector.getListOfSchemes(request.psaId.id).flatMap {
      case Right(list) =>

        if (list.items.getOrElse(Nil).exists(!_.racDac)) {

          val json: JsObject = Json.obj(
            "param1" -> msg"messages__pension_scheme".resolve,
            "param2" -> msg"messages__scheme".resolve,
            "continueUrl" -> routes.ListOfSchemesController.onPageLoad(Scheme).url,
            "contactHmrcUrl" -> appConfig.contactHmrcUrl
          )

          renderer.render("preMigration/cannotAdd.njk", json).map(Ok(_))
        } else {
          Future.successful(Redirect(controllers.routes.TaskListController.onPageLoad().url))
        }
      case _ => Future.successful(Redirect(controllers.routes.TaskListController.onPageLoad().url))
    } recoverWith {
      case _: AncillaryPsaException =>
        Future.successful(Redirect(routes.CannotMigrateController.onPageLoad()))
    }
  }

  def onPageLoadRacDac: Action[AnyContent] = authenticate.async { implicit request =>
    listOfSchemesConnector.getListOfSchemes(request.psaId.id).flatMap {
      case Right(list) =>
        if (list.items.getOrElse(Nil).exists(_.racDac)) {
          val json: JsObject = Json.obj(
            "param1" -> msg"messages__racdac".resolve,
            "param2" -> msg"messages__racdac".resolve,
            "continueUrl" -> routes.ListOfSchemesController.onPageLoad(RacDac).url,
            "contactHmrcUrl" -> appConfig.contactHmrcUrl
          )

          renderer.render("preMigration/cannotAdd.njk", json).map(Ok(_))
        } else {
          Future.successful(Redirect(controllers.routes.TaskListController.onPageLoad().url))
        }
      case _ => Future.successful(Redirect(controllers.routes.TaskListController.onPageLoad().url))
    } recoverWith {
      case _: AncillaryPsaException =>
        Future.successful(Redirect(routes.CannotMigrateController.onPageLoad()))
    }
  }
}
