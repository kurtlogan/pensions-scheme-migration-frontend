import play.core.PlayVersion
import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-27"     % "4.1.0",
    "uk.gov.hmrc"             %%  "govuk-template"                % "5.65.0-play-27",
    "uk.gov.hmrc"             %%  "play-ui"                       % "9.1.0-play-27",
    "uk.gov.hmrc"             %% "play-conditional-form-mapping"  % "1.6.0-play-27",
    "uk.gov.hmrc"             %% "play-language"                  % "4.12.0-play-27",
    "uk.gov.hmrc"             %%  "domain"                        % "5.10.0-play-27",
    "uk.gov.hmrc"                   %%  "govuk-template"                % "5.63.0-play-27",
    "uk.gov.hmrc"                   %%  "domain"                        % "5.10.0-play-27",
    "uk.gov.hmrc"                   %%  "play-ui"                       % "9.0.0-play-27"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-27"   % "4.1.0" % Test,
    "org.scalatest"           %% "scalatest"                % "3.0.7"  % Test,
    "org.jsoup"               %  "jsoup"                    % "1.13.1" % Test,
    "com.typesafe.play"       %% "play-test"                % current  % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.36.8" % "test, it",
    "org.scalatestplus.play"      %% "scalatestplus-play" % "4.0.2",
    "org.mockito"             %  "mockito-all"              % "1.10.19",
    "org.scalacheck"          %% "scalacheck"               % "1.14.0",
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.21.0",
    "org.pegdown"                 %  "pegdown"            % "1.6.0",
    "org.mockito"                 %  "mockito-all"        % "1.10.19"
  )
}
