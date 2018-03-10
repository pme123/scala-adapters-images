import com.typesafe.sbt.digest.Import.{DigestKeys, digest}
import com.typesafe.sbt.gzip.Import.gzip
import com.typesafe.sbt.web.Import.Assets
import com.typesafe.sbt.web.SbtWeb.autoImport.pipelineStages
import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport.jsDependencies
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.sbtplugin.Stage
import play.sbt.PlayImport.{filters, guice, ws}
import sbt.Keys.{resolvers, _}
import sbt.{Def, ExclusionRule, URL, _}
import sbtbuildinfo.BuildInfoPlugin.autoImport._
import webscalajs.WebScalaJS.autoImport.scalaJSPipeline

object Settings {

  lazy val orgId = "pme123"
  lazy val orgHomepage = Some(new URL("https://github.com/pme123"))
  lazy val projectName = "scala-adapters-images"
  lazy val projectV = "0.1.0"

  // main versions
  lazy val scalaV = "2.12.4"

  lazy val scalaTestV = "3.0.4"
  lazy val playTelegramV = "0.1.0"
  lazy val adaptersV = "0.6.2"

  lazy val buildVersion: String = sys.env.getOrElse("BUILD_VERSION", default = projectV)
  lazy val buildNumber: String = sys.env.getOrElse("BUILD_NUMBER", default = s"${(System.currentTimeMillis / 1000).asInstanceOf[Int]}")

  lazy val organizationSettings = Seq(
    organization := orgId
    , organizationHomepage := orgHomepage
  )

  lazy val testStage: Stage = sys.props.get("testOpt").map {
    case "full" => FullOptStage
    case "fast" => FastOptStage
  }.getOrElse(FastOptStage)

  lazy val serverSettings: Seq[Def.Setting[_]] = Def.settings(
    buildInfoSettings
    , pipelineStages in Assets := Seq(scalaJSPipeline)
    , pipelineStages := Seq(digest, gzip)
    // triggers scalaJSPipeline when using compile or continuous compilation
    , compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value
    // to have routing also in ScalaJS
    // Create a map of versioned assets, replacing the empty versioned.js
    , DigestKeys.indexPath := Some("javascripts/versioned.js")
    // Assign the asset index to a global versioned var
    , DigestKeys.indexWriter ~= { writer => index => s"var versioned = ${writer(index)};" }
  )

  lazy val serverDependencies: Seq[Def.Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    ws
    , guice
    , filters
    , "com.github.pme123" % "play-akka-telegrambot4s" % playTelegramV
    , "com.github.pme123.scala-adapters" %% "scala-adapters-server" % adaptersV
    // TEST
    , "com.typesafe.akka" %% "akka-testkit" % "2.5.6" % Test
    , "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.6" % Test
    , "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
    , "org.awaitility" % "awaitility" % "3.0.0" % Test

    , "org.scalatest" %% "scalatest" % scalaTestV % Test
  ).map(_.excludeAll(ExclusionRule("org.slf4j", "slf4j-log4j12")))
  )

  lazy val clientSettings = Seq(
    scalacOptions ++= Seq("-Xmax-classfile-name", "78")
    , addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    , jsDependencies ++= Seq()
  )
  lazy val clientDependencies: Seq[Def.Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "com.github.pme123.scala-adapters" %%% "scala-adapters-client" % adaptersV
    , "org.scalatest" %%% "scalatest" % scalaTestV % Test
  ))

  lazy val sharedDependencies: Seq[Def.Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "com.github.pme123.scala-adapters" %%% "scala-adapters" % adaptersV
    , "org.scalatest" %%% "scalatest" % scalaTestV % Test

  ))

  lazy val jsSettings: Seq[Def.Setting[_]] = Seq(
    scalaJSStage in Global := testStage
  )

  lazy val sharedJsDependencies: Seq[Def.Setting[_]] = Def.settings(libraryDependencies ++= Seq(
  ))

  def sharedSettings(moduleName: Option[String] = None): Seq[Def.Setting[_]] = Seq(
    scalaVersion := scalaV
    , name := s"$projectName${moduleName.map("-" + _).getOrElse("")}"
    , version := s"$buildVersion-$buildNumber"
    , resolvers += "jitpack" at "https://jitpack.io"
    , publishArtifact in(Compile, packageDoc) := false
    , publishArtifact in packageDoc := false
    , sources in(Compile, doc) := Seq.empty
  ) ++ organizationSettings

  private lazy val buildInfoSettings = Seq(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoPackage := "version"
  )

}
