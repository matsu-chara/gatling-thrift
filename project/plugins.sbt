resolvers ++= Seq(
  Classpaths.sbtPluginSnapshots,
  Classpaths.sbtPluginReleases,
  Resolver.sonatypeRepo("snapshots"),
  "Twitter Maven" at "https://maven.twttr.com"
)

addSbtPlugin("com.twitter" % "scrooge-sbt-plugin" % "4.20.0")
addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.10")
addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")
