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

package controllers.establishers.company

import config.AppConfig
import controllers.Retrievals
import controllers.actions._
import forms.establishers.company.director.AddCompanyDirectorsFormProvider
import helpers.AddToListHelper
import identifiers.establishers.company.AddCompanyDirectorsId
import models.Mode
import navigators.CompoundNavigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.nunjucks.NunjucksSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.Radios

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddCompanyDirectorsController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               navigator: CompoundNavigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddCompanyDirectorsFormProvider,
                                               helper: AddToListHelper,
                                               config: AppConfig,
                                               val controllerComponents: MessagesControllerComponents,
                                               renderer: Renderer
                                             )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with Retrievals
    with I18nSupport
    with NunjucksSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(index: Int,mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>

        println("\n\n\n request.userAnswers : "+request.userAnswers)
        val directors = request.userAnswers.allDirectorsAfterDelete(index)
        val itemList=   helper.directorsOrPartnersItemList(directors)
        renderer.render(
          template = "establishers/company/addDirector.njk",
          ctx = Json.obj(
            "form"       -> form,
            "itemList"      -> itemList,
            "radios"     -> Radios.yesNo(form("value")),
            "schemeName" -> existingSchemeName,
            "directorSize" -> directors.size ,
            "maxDirectors" -> config.maxDirectors

          )
        ).map(Ok(_))
    }

  def onSubmit(index: Int,mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData()).async {
      implicit request =>
        val directors = request.userAnswers.allDirectorsAfterDelete(index)
        val itemList=   helper.directorsOrPartnersItemList(directors)
        if (directors.isEmpty || directors.lengthCompare(config.maxDirectors) >= 0) {
          Future.successful(Redirect(
            navigator.nextPage(
              id          = AddCompanyDirectorsId(index),
              userAnswers = request.userAnswers
            )
          ))
        }
        else {
          form.bindFromRequest().fold(
            formWithErrors =>
              renderer.render(
                template = "establishers/company/addDirector.njk",
                ctx = Json.obj(
                  "form"       -> formWithErrors,
                  "itemList"      -> itemList,
                  "radios"     -> Radios.yesNo(formWithErrors("value")),
                  "schemeName" -> existingSchemeName,
                  "directorSize" -> directors.size ,
                  "maxDirectors" -> config.maxDirectors
                )
              ).map(BadRequest(_)),
            value => {

              val ua = request.userAnswers.set(AddCompanyDirectorsId(index),value).getOrElse(request.userAnswers)
              Future.successful(Redirect(
                navigator.nextPage(
                  id          = AddCompanyDirectorsId(index),
                  userAnswers = ua
                )
              ))
            }
          )
        }
    }
}
