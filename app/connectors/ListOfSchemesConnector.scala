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

package connectors

import com.google.inject.{Inject, Singleton, ImplementedBy}
import config.AppConfig
import models.ListOfLegacySchemes
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsResultException, JsError, JsSuccess, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpReads.is5xx
import uk.gov.hmrc.http.{HttpClient, HttpResponse, HeaderCarrier}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ListOfSchemesConnectorImpl])
trait ListOfSchemesConnector {

  def getListOfSchemes(psaId: String)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[HttpResponse, ListOfLegacySchemes]]

}

@Singleton
class ListOfSchemesConnectorImpl @Inject()(
                                            http: HttpClient,
                                            config: AppConfig
                                          ) extends ListOfSchemesConnector {

  private val logger = Logger(classOf[ListOfSchemesConnectorImpl])

  def getListOfSchemes(psaId: String)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[HttpResponse, ListOfLegacySchemes]] = {
    val (url, schemeHc) = (config.listOfSchemesUrl, hc.withExtraHeaders("psaId" -> psaId))

    http.GET[HttpResponse](url)(implicitly, schemeHc, implicitly).map { response =>
      response.status match {
        case OK =>
            Json.parse(response.body).validate[ListOfLegacySchemes] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(errors) => throw JsResultException(errors)
          }
        case UNPROCESSABLE_ENTITY if response.body.contains(ancillaryPsaError) => throw AncillaryPsaException()
        case response if is5xx(response) => throw ListOfSchemes5xxException()
        case _ =>
          logger.error(response.body)
          Left(response)
      }
    }
  }

  private val ancillaryPsaError: String = "Administrator is a subordinate in mapping table"

}

sealed trait ListOfSchemesException extends Exception
case class AncillaryPsaException() extends ListOfSchemesException
case class ListOfSchemes5xxException() extends ListOfSchemesException

