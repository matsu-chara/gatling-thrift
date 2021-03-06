import sbt.Keys._
import ReleaseTransformations._

parallelExecution in ThisBuild := false

lazy val versions = new {
  val finatra   = "2.13.0"
  val logback   = "1.1.7"
  val scalatest = "3.0.3"
  val jackson   = "2.9.0"
  val gatling   = "2.3.0"
}

lazy val baseSettings = Seq(
  organization := "com.github.3tty0n",
  scalaVersion := "2.12.3",
  scalafmtVersion in ThisBuild := "1.0.0-RC2",
  scalafmtOnCompile := true,
  ivyScala := ivyScala.value.map(_.copy(overrideScalaVersion = true)),
  scalacOptions := Seq(
    "-encoding",
    "UTF-8",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:implicitConversions",
    "-language:postfixOps"
  ),
  libraryDependencies ++= Seq(
    "com.twitter" %% "finatra-thrift" % versions.finatra excludeAll (
      ExclusionRule(organization = "com.fasterxml.jackson.module")
    ),
    "ch.qos.logback"               % "logback-classic"       % versions.logback,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % versions.jackson,
    "org.scalatest"                %% "scalatest"            % versions.scalatest % "test"
  ),
  resolvers += Resolver.sonatypeRepo("releases"),
  releaseProcess := aggregateReleaseProcess
)

lazy val assemblySettings = {
  val meta = """META.INF(.)*""".r
  Seq(
    assemblyMergeStrategy in assembly := {
      case PathList("io", "netty", xs @ _ *) =>
        MergeStrategy.first
      case meta(_) =>
        MergeStrategy.discard
      case "BUILD" =>
        MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    test in assembly := {}
  )
}

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo in ThisBuild := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  pomExtra :=
    <url>https://github.com/3tty0n/gatling-thrift</url>
      <developers>
        <developer>
          <id>3tty0n</id>
          <name>Yusuke Izawa</name>
          <url>https://github.com/3tty0n</url>
        </developer>
      </developers>
      <scm>
        <url>git@github.com:3tty0n/gatling-thrift.git</url>
        <connection>scm:git:git@github.com:3tty0n/gatling-thrift.git</connection>
      </scm>
)

lazy val aggregateReleaseProcess = Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("gatling-thrift/publishSigned"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

lazy val root = (project in file("."))
  .settings(baseSettings, publishSettings)
  .settings(name := "gatling-thrift", publish := Def.sequential(publish in `gatling-thrift`).value)
  .aggregate(`gatling-thrift`, `gatling-thrift-example`)

lazy val `gatling-thrift` = (project in file("gatling-thrift"))
  .settings(baseSettings, publishSettings)
  .settings(
    name := "gatling-thrift",
    libraryDependencies ++= Seq(
      "io.gatling"            % "gatling-app"               % versions.gatling,
      "io.gatling"            % "gatling-test-framework"    % versions.gatling,
      "io.gatling.highcharts" % "gatling-charts-highcharts" % versions.gatling
    )
  )

lazy val `gatling-thrift-example` = (project in file("gatling-thrift-example"))
  .enablePlugins(GatlingPlugin, JavaAppPackaging, UniversalDeployPlugin)
  .settings(baseSettings, assemblySettings)
  .settings(
    name := "gatling-thrift-example",
    assemblyJarName in assembly := "gatling-thrift-example.jar",
    mainClass in assembly := Some("simulation.ThriftSimulationMain"),
    mappings in Universal := {
      val universalMappings = (mappings in Universal).value
      val fatJar            = (assembly in Compile).value
      val filtered = universalMappings.filter {
        case (file, name) => !name.endsWith(".jar")
      }
      filtered :+ (fatJar -> ("lib/" + fatJar.getName))
    },
    scriptClasspath := Seq((assemblyJarName in assembly).value),
    publish := {},
    publishLocal := (publishLocal in Universal).value
  )
  .dependsOn(`gatling-thrift`)
