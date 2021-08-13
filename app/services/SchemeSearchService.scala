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
import models.{Entity, Items, ListOfLegacySchemes}
import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.viewmodels.Table.Cell
import uk.gov.hmrc.viewmodels.Text.Literal
import uk.gov.hmrc.viewmodels.{Html, MessageInterpolators, Table}
import utils.SchemeFuzzyMatcher

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

class SchemeSearchService @Inject()(fuzzyMatching: SchemeFuzzyMatcher) {

  private val pstrRegex = "^[0-9]{8}[A-Za-z]{2}$".r

  private val filterSchemesByPstrOrSchemeName
  : (String, List[Items]) => List[Items] =
    (searchText, list) => {
      searchText match {
        case _ if pstrRegex.findFirstIn(searchText).isDefined =>
          list.filter(_.pstr.equalsIgnoreCase(searchText))
        case _ =>
          list.flatMap { schemeDetail =>
            val isMatch = fuzzyMatching.doFuzzyMatching(searchText, schemeDetail.schemeName)
            if (isMatch) Some(schemeDetail) else None
          }
      }
    }

  def search(psaId: String, searchText: Option[String])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[Items]] = {
    val listOfSchemes = ListOfLegacySchemes(2, Some(List(
      Items("11111111AB", "2020-01-01", false, "scheme 1", "2020-12-12", None),
      Items("22222222AB", "2021-01-01", false, "scheme 2", "2021-12-12", None)
    )))

//    listSchemesConnector.getListOfSchemes(psaId).map {
//        case Right(listOfSchemes) =>
//          val filterSearchResults =
//            searchText.fold[List[SchemeDetails] => List[SchemeDetails]](identity)(
//              st => filterSchemesByPstrOrSchemeName(st, _: List[SchemeDetails])
//            )
//
//          filterSearchResults(listOfSchemes.schemeDetails.getOrElse(List.empty[SchemeDetails]))
//        case _ => List.empty[SchemeDetails]
//      }


    val filterSearchResults =
          searchText.fold[List[Items] => List[Items]](identity)(
            st => filterSchemesByPstrOrSchemeName(st, _: List[Items])
          )

    Future.successful(filterSearchResults(listOfSchemes.items.getOrElse(List.empty[Items])))
  }

  def mapToTable(schemeDetails: List[Items]): Table = {
      val head = Seq(
        Cell(msg"messages__listSchemes__column_schemeName"),
        Cell(msg"messages__listSchemes__column_pstr"),
        Cell(msg"messages__listSchemes__column_regDate")
      )

    val formatter: String => String = date => LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))

    val rows = schemeDetails.map { data =>
      Seq(Cell(Literal(data.schemeName), Seq("govuk-!-width-one-quarter")),
        Cell(Literal(data.pstr), Seq("govuk-!-width-one-quarter")),
        Cell(Literal(formatter(data.schemeOpenDate)), Seq("govuk-!-width-one-half"))
        )
    }

    Table(head, rows, attributes = Map("role" -> "table"))

  }

}
