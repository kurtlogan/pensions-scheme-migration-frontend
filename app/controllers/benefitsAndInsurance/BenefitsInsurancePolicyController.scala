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

package controllers.benefitsAndInsurance

import config.AppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.benefitsAndInsurance.BenefitsInsurancePolicyFormProvider
import identifiers.beforeYouStart.SchemeNameId
import identifiers.benefitsAndInsurance.{BenefitsInsuranceNameId, BenefitsInsurancePolicyId}
import navigators.CompoundNavigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.nunjucks.NunjucksSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Enumerable
import viewmodels.Message

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BenefitsInsurancePolicyController @Inject()(override val messagesApi: MessagesApi,
                                       userAnswersCacheConnector: UserAnswersCacheConnector,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       navigator: CompoundNavigator,
                                       formProvider: BenefitsInsurancePolicyFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       config: AppConfig,
                                       renderer: Renderer)(implicit ec: ExecutionContext)
  extends FrontendBaseController  with I18nSupport with Retrievals with Enumerable.Implicits with NunjucksSupport {

  private def form: Form[String] =
    formProvider()

  def onPageLoad: Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async { implicit request =>
      (request.userAnswers.get(SchemeNameId), request.userAnswers.get(BenefitsInsuranceNameId)) match {
        case (Some(schemeName), optionInsurancePolicyName) =>
          val preparedForm = request.userAnswers.get(BenefitsInsurancePolicyId) match {
            case Some(value) => form.fill(value)
            case None        => form
          }

          val heading = optionInsurancePolicyName.fold(Messages("benefitsInsurancePolicy.noCompanyName.h1"))(Message("benefitsInsurancePolicy.h1", _))

          val json = Json.obj(
            "schemeName" -> schemeName,
            "heading" -> heading,
            "form" -> preparedForm
          )
          renderer.render("benefitsAndInsurance/benefitsInsurancePolicy.njk", json).map(Ok(_))
        case _ => Future.successful(Redirect(controllers.routes.IndexController.onPageLoad()))
      }
    }

  def onSubmit: Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async { implicit request =>
      (request.userAnswers.get(SchemeNameId), request.userAnswers.get(BenefitsInsuranceNameId)) match {
        case (Some(schemeName), optionInsurancePolicyName) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              val heading = optionInsurancePolicyName.fold(Messages("benefitsInsurancePolicy.noCompanyName.h1"))(Message("benefitsInsurancePolicy.h1", _))
              val json = Json.obj(
                "schemeName" -> schemeName,
                "heading" -> heading,
                "form" -> formWithErrors
              )

              renderer.render("benefitsAndInsurance/benefitsInsurancePolicy.njk", json).map(BadRequest(_))
            },
            value => {
              val updatedUA = request.userAnswers.setOrException(BenefitsInsurancePolicyId, value)
              userAnswersCacheConnector.save(request.lock, updatedUA.data).map { _ =>
                Redirect(navigator.nextPage(BenefitsInsurancePolicyId, updatedUA))
              }
            }
          )
        case _ => Future.successful(Redirect(controllers.routes.IndexController.onPageLoad()))
      }
    }

}
