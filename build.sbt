import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import play.sbt.routes.RoutesKeys

val appName = "pensions-scheme-migration-frontend"

val silencerVersion = "1.7.0"

lazy val microservice = Project(appName, file("."))
  .disablePlugins(JUnitXmlReportPlugin)
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(
    name                             := appName,
    majorVersion                     := 0,
    scalaVersion                     := "2.12.12",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    PlayKeys.playDefaultPort         := 8213,
    TwirlKeys.templateImports ++= Seq(
      "config.AppConfig"
    ),
    RoutesKeys.routesImport ++= Seq(
      "models.Index",
      "models.establishers.EstablisherKind",
      "models.trustees.TrusteeKind",
      "models.Mode",
      "models.CheckMode",
      "models.NormalMode",
      "models.MigrationType",
      "models.Scheme",
      "models.RacDac"
    ),
      // concatenate js
      Concat.groups := Seq(
  "javascripts/application.js" -> group(
    Seq(
      "lib/govuk-frontend/govuk/all.js",
      "lib/hmrc-frontend/hmrc/all.js",
      "javascripts/psm.js"
    )
  )
),
// prevent removal of unused code which generates warning errors due to use of third-party libs
uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
// below line required to force asset pipeline to operate in dev rather than only prod
pipelineStages in Assets := Seq(concat, uglify)
  )
  .settings(silencerSettings)
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo,
    )
  )
  .settings(
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*models.*;.*repositories.*;" +
      ".*BuildInfo.*;.*javascript.*;.*Routes.*;.*GuiceInjector;.*UserAnswersCacheConnector;" +
      ".*ControllerConfiguration;.*LanguageSwitchController;.*TestMongoController;.*LanguageSelect.*;.*TestMongoPage.*;.*ErrorTemplate.*",
    ScoverageKeys.coverageMinimum := 65,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
  .settings(
    scalacOptions ++= Seq(/*"-Xfatal-warnings",*/ "-feature"),
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )

lazy val silencerSettings: Seq[Setting[_]] = {
  val silencerVersion = "1.7.0"
  Seq(
    libraryDependencies ++= Seq(compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full),
    // silence all warnings on autogenerated files
    scalacOptions += "-P:silencer:pathFilters=target/.*",
    scalacOptions += "-P:silencer:pathFilters=routes",
    // Make sure you only exclude warnings for the project directories, i.e. make builds reproducible
    scalacOptions += s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"
  )
}
