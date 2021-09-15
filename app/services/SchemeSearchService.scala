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

package services

import com.google.inject.Inject
import config.AppConfig
import connectors.cache.FeatureToggleConnector
import connectors.{AncillaryPsaException, DelimitedAdminException, ListOfSchemesConnector, MinimalDetailsConnector}
import controllers.preMigration.routes
import models.FeatureToggleName.MigrationTransfer
import models.{Items, MigrationType}
import models.MigrationType.isRacDac
import models.requests.AuthenticatedRequest
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContent, Result}
import renderer.Renderer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.viewmodels.Table.Cell
import uk.gov.hmrc.viewmodels.Text.Literal
import uk.gov.hmrc.viewmodels.{Content, Html, MessageInterpolators, Table}
import utils.SchemeFuzzyMatcher

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Results._
import uk.gov.hmrc.nunjucks.NunjucksSupport

class SchemeSearchService @Inject()(val appConfig: AppConfig,
                                    fuzzyMatching: SchemeFuzzyMatcher,
                                    listOfSchemesConnector:ListOfSchemesConnector,
                                    minimalDetailsConnector: MinimalDetailsConnector,
                                    paginationService: PaginationService,
                                    featureToggleConnector: FeatureToggleConnector,
                                    renderer: Renderer) extends NunjucksSupport {

  private val pagination: Int = appConfig.listSchemePagination
  private val pstrRegex = "^[0-9]{8}[A-Za-z]{2}$".r

  private val filterSchemesByPstrOrSchemeName
  : (String, List[Items]) => List[Items] =
    (searchText, list) =>
      searchText match {
        case _ if pstrRegex.findFirstIn(searchText).isDefined =>
          list.filter(_.pstr.equalsIgnoreCase(searchText))
        case _ =>
          list.flatMap { schemeDetail =>
            val isMatch = fuzzyMatching.doFuzzyMatching(searchText, schemeDetail.schemeName)
            if (isMatch) Some(schemeDetail) else None
          }
    }

  def search(psaId: String, searchText: Option[String], isRacDac: Boolean)
            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[Items]] =
    listOfSchemesConnector.getListOfSchemes(psaId).map {
        case Right(listOfSchemes) =>

          val filterSearchResults =
            searchText.fold[List[Items] => List[Items]](identity)(
              st => filterSchemesByPstrOrSchemeName(st, _: List[Items])
            )

          filterSearchResults(listOfSchemes.items.getOrElse(Nil).filter(_.racDac == isRacDac))
        case _ =>
          List.empty[Items]
      }


  def mapToTable(schemeDetails: List[Items], isRacDac: Boolean, viewOnly: Boolean): Table = {
      val head =
        if(isRacDac)
          Seq(
            Cell(msg"messages__listSchemes__column_racDacName"),
            Cell(msg"messages__listSchemes__column_pstr"),
            Cell(msg"messages__listSchemes__column_regDate")
          )
          else
        Seq(
        Cell(msg"messages__listSchemes__column_schemeName"),
        Cell(msg"messages__listSchemes__column_pstr"),
        Cell(msg"messages__listSchemes__column_regDate")
      )

    val formatter: String => String = date => LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))

    val schemeName: Items => Content = data => if(viewOnly) Literal(data.schemeName) else Html(
        s"""<a class=migrate-pstr-${data.pstr} href=${controllers.preMigration.routes.ListOfSchemesController.clickSchemeLink(data.pstr, isRacDac)}>${data.schemeName}</a>""".stripMargin)

    val rows = schemeDetails.map { data =>
      Seq(Cell(schemeName(data), Seq("govuk-!-width-one-quarter")),
        Cell(Literal(data.pstr), Seq("govuk-!-width-one-quarter")),
        Cell(Literal(formatter(data.schemeOpenDate)), Seq("govuk-!-width-one-half")))
    }

    Table(head, rows, attributes = Map("role" -> "table"))

  }

  private val msgPrefix:String ="messages__listSchemes__pagination__"

  def typeOfList(migrationType: MigrationType)(implicit messages: Messages):String =
    if(isRacDac(migrationType)) messages("messages__racdac") else messages("messages__pension_scheme")

  private def renderView(
                          schemeDetails: List[Items],
                          numberOfSchemes: Int,
                          pageNumber: Int,
                          numberOfPages: Int,
                          noResultsMessageKey: Option[String],
                          form: Form[String],
                          migrationType: MigrationType,
                          viewOnly: Boolean
                        )(implicit hc: HeaderCarrier,
                          messages: Messages,
                          request: AuthenticatedRequest[AnyContent],
                          ec: ExecutionContext): Future[Result] =
    minimalDetailsConnector.getPSADetails(request.psaId.id).flatMap {
      case md if md.deceasedFlag => Future.successful(Redirect(appConfig.deceasedContactHmrcUrl))
      case md if md.rlsFlag => Future.successful(Redirect(appConfig.psaUpdateContactDetailsUrl))
      case md =>

        val json: JsObject = Json.obj(
          "form" -> form,
          "psaName" -> md.name,
          "numberOfSchemes" -> numberOfSchemes,
          "pagination" -> pagination,
          "pageNumber" -> pageNumber,
          "pageNumberLinks" -> paginationService.pageNumberLinks(
            pageNumber,
            numberOfSchemes,
            pagination,
            numberOfPages
          ),
          "racDac" -> isRacDac(migrationType),
          "numberOfPages" -> numberOfPages,
          "noResultsMessageKey" -> noResultsMessageKey,
          "clearLinkUrl" -> routes.ListOfSchemesController.onPageLoad(migrationType).url,
          "returnUrl" -> appConfig.psaOverviewUrl,
          "paginationText" -> paginationText(pageNumber,pagination,numberOfSchemes,numberOfPages),
          "typeOfList" -> typeOfList(migrationType),
          "viewOnly" -> viewOnly
        ) ++  (if (schemeDetails.nonEmpty) Json.obj("schemes" -> mapToTable(schemeDetails, isRacDac(migrationType), viewOnly)) else Json.obj())

        renderer.render("preMigration/listOfSchemes.njk", json)
          .map(body => if (form.hasErrors) BadRequest(body) else Ok(body))

    } recoverWith {
      case _: DelimitedAdminException =>
        Future.successful(Redirect(appConfig.psaDelimitedUrl))
    }

  private  def paginationText(pageNumber:Int,pagination:Int,numberOfSchemes:Int,numberOfPages:Int)(implicit messages: Messages):String={
    messages(
      s"${msgPrefix}text",
      if (pageNumber == 1) pageNumber else ((pageNumber * pagination) - pagination) + 1,
      if (pageNumber == numberOfPages) numberOfSchemes else pageNumber * pagination,
      numberOfSchemes
    )
  }

  private def noResultsMessageKey(searchText: Option[String], searchResult: List[Items], migrationType: MigrationType)
                                 (implicit messages: Messages): Option[String] =
    (searchText.isDefined, searchResult.isEmpty) match {
      case (true, true) =>
        Some(messages("messages__listSchemes__search_noMatches", typeOfList(migrationType)))
      case (false, true) => Some(messages("messages__listSchemes__noSchemes", typeOfList(migrationType)))
      case _ => None
    }

  def searchAndRenderView(
                                   form: Form[String],
                                   pageNumber: Int,
                                   searchText: Option[String],
                                   migrationType: MigrationType
                                 )(implicit request: AuthenticatedRequest[AnyContent],
                                   messages: Messages,
                                   hc: HeaderCarrier,
                                   ec: ExecutionContext): Future[Result] =
    featureToggleConnector.get(MigrationTransfer.asString).flatMap { toggle =>
      search(request.psaId.id, searchText, isRacDac(migrationType)).flatMap { searchResult =>

        val numberOfSchemes: Int = searchResult.length

        val numberOfPages: Int =
          paginationService.divide(numberOfSchemes, pagination)

        renderView(
          selectPageOfResults(searchResult, pageNumber, numberOfPages),
          numberOfSchemes,
          pageNumber,
          numberOfPages,
          noResultsMessageKey(searchText, searchResult, migrationType),
          form,
          migrationType,
          toggle.isDisabled
        )
      } recoverWith {
        case _: AncillaryPsaException =>
          Future.successful(Redirect(routes.CannotMigrateController.onPageLoad()))
      }
    }

  def renderRacDacBulkView(
                           form: Form[Boolean],
                           pageNumber: Int
                         )(implicit request: AuthenticatedRequest[AnyContent],
                           messages: Messages,
                           hc: HeaderCarrier,
                           ec: ExecutionContext): Future[Result] =

      search(request.psaId.id, searchText, isRacDac(migrationType)).flatMap { searchResult =>

        val numberOfSchemes: Int = searchResult.length

        val numberOfPages: Int =
          paginationService.divide(numberOfSchemes, pagination)

        renderView(
          selectPageOfResults(searchResult, pageNumber, numberOfPages),
          numberOfSchemes,
          pageNumber,
          numberOfPages,
          form
        )
      } recoverWith {
        case _: AncillaryPsaException =>
          Future.successful(Redirect(routes.CannotMigrateController.onPageLoad()))
      }

  private def selectPageOfResults(
                                   searchResult: List[Items],
                                   pageNumber: Int,
                                   numberOfPages: Int
                                 ): List[Items] = {
    pageNumber match {
      case 1 => searchResult.take(pagination)
      case p if p <= numberOfPages =>

        searchResult.slice(
          (pageNumber * pagination) - pagination,
          pageNumber * pagination
        )

      case _ => throw new Exception
    }
  }

}
