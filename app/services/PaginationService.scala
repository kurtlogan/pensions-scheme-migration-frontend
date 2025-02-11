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

package services

import config.AppConfig
import models.Items
import play.api.i18n.Messages

import javax.inject.Inject

class PaginationService @Inject()(appConfig: AppConfig) {
  private def pagination: Int = appConfig.listSchemePagination
  private val msgPrefix: String = "messages__listSchemes__pagination__"

  def divide(numberOfSchemes: Int,
             pagination: Int): Int =
    if (pagination > 0 && numberOfSchemes > 0) {
      (BigDecimal(numberOfSchemes) / BigDecimal(pagination)).setScale(0, BigDecimal.RoundingMode.UP).toInt
    } else {
      0
    }

  // scalastyle:off magic.number
  def pageNumberLinks(currentPage: Int,
                      numberOfSchemes: Int,
                      pagination: Int,
                      numberOfPages: Int): Seq[Int] =
    if (numberOfSchemes < pagination) {
      Seq.empty
    } // build page links for numberOfPages if numberOfPages < max number of page links (5)
    else if (currentPage < 4 && numberOfPages < 6) {
      Seq.range(1, numberOfPages + 1)
    } // at start of pages (e.g pages 1 - 3) when numberOfPages > 5 always build 5
    else if (currentPage < 4 && numberOfPages > 5) {
      Seq.range(1, 6)
    } // when not at start or end of pages
    // build 5 page links but move range so current page is middle of seq
    else if (currentPage >= 4 && currentPage <= numberOfPages - 3) {
      Seq.range(currentPage - 2, currentPage + 3)
    } // build only last 5 page links at end of pages
    else {
      Seq.range(numberOfPages - 4, numberOfPages + 1)
    }

  def selectPageOfResults(
                                   searchResult: List[Items],
                                   pageNumber: Int,
                                   numberOfPages: Int
                                 ): List[Items] =
    pageNumber match {
      case 1 => searchResult.take(pagination)
      case p if p <= numberOfPages =>

        searchResult.slice(
          (pageNumber * pagination) - pagination,
          pageNumber * pagination
        )

      case _ => throw new IllegalArgumentException("Invalid page number passed")
    }

  def paginationText(pageNumber: Int, pagination: Int, numberOfSchemes: Int, numberOfPages: Int)(implicit messages: Messages): String = {
    messages(
      s"${msgPrefix}text",
      if (pageNumber == 1) pageNumber else ((pageNumber * pagination) - pagination) + 1,
      if (pageNumber == numberOfPages) numberOfSchemes else pageNumber * pagination,
      numberOfSchemes
    )
  }
}
