name := """relaxbot"""

version := "1.0"

scalaVersion := "2.12.3"

scalacOptions += "-Ypartial-unification"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"

libraryDependencies += "com.github.gilbertw1" %% "slack-scala-client" % "0.2.2"

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.18.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"

libraryDependencies += "com.enragedginger" %% "akka-quartz-scheduler" % "1.6.1-akka-2.5.x"

libraryDependencies += "com.typesafe.slick" %% "slick" % "3.2.1"

libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1"

libraryDependencies += "com.h2database" % "h2" % "1.4.187"

libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.7.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
