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
//package controllers.establishers.partnership.details
//
//import connectors.cache.UserAnswersCacheConnector
//import controllers.EnterReferenceValueController
//import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
//import forms.PAYEFormProvider
//import identifiers.beforeYouStart.SchemeNameId
//import identifiers.establishers.partnership.PartnershipDetailsId
//import identifiers.establishers.partnership.details.PAYEId
//import models.requests.DataRequest
//import models.{Index, Mode, ReferenceValue}
//import navigators.CompoundNavigator
//import play.api.data.Form
//import play.api.i18n.{Messages, MessagesApi}
//import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
//import renderer.Renderer
//import viewmodels.Message
//
//import javax.inject.Inject
//import scala.concurrent.ExecutionContext
//
//class PAYEController @Inject()(
//                                override val messagesApi: MessagesApi,
//                                val navigator: CompoundNavigator,
//                                authenticate: AuthAction,
//                                getData: DataRetrievalAction,
//                                requireData: DataRequiredAction,
//                                formProvider: PAYEFormProvider,
//                                val controllerComponents: MessagesControllerComponents,
//                                val userAnswersCacheConnector: UserAnswersCacheConnector,
//                                val renderer: Renderer
//                                             )(implicit val executionContext: ExecutionContext)
//  extends EnterReferenceValueController {
//
//  private def name(index: Index)
//                  (implicit request: DataRequest[AnyContent]): String =
//    request
//      .userAnswers
//      .get(PartnershipDetailsId(index))
//      .fold("messages__partnership")(_.partnershipName)
//
//  private def form(name:String)(implicit messages:Messages): Form[ReferenceValue] = formProvider(name)
//
//  def onPageLoad(index: Index, mode: Mode): Action[AnyContent] =
//    (authenticate andThen getData andThen requireData).async {
//      implicit request =>
//        SchemeNameId.retrieve.right.map {
//          schemeName =>
//            get(
//              pageTitle     = Message("messages__paye", Message("messages__partnership")),
//              pageHeading     = Message("messages__paye", name(index)),
//              isPageHeading = true,
//              id            = PAYEId(index),
//              form          = form(name(index)),
//              schemeName    = schemeName,
//              legendClass   = "govuk-visually-hidden",
//              paragraphText = Seq(Message("messages__paye__p", name(index))),
//              hintText = Some(Message("messages__paye__hint"))
//            )
//        }
//    }
//
//  def onSubmit(index: Index, mode: Mode): Action[AnyContent] =
//    (authenticate andThen getData andThen requireData).async {
//      implicit request =>
//        SchemeNameId.retrieve.right.map {
//          schemeName =>
//            post(
//              pageTitle     = Message("messages__paye", Message("messages__partnership")),
//              pageHeading     = Message("messages__paye", name(index)),
//              isPageHeading = true,
//              id            = PAYEId(index),
//              form          = form(name(index)),
//              schemeName    = schemeName,
//              legendClass   = "govuk-visually-hidden",
//              paragraphText = Seq(Message("messages__paye__p", name(index))),
//              mode          = mode,
//              hintText = Some(Message("messages__paye__hint"))
//            )
//        }
//    }
//}
