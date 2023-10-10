name := "Scala3-TG"
version:="0.3.0"
scalaVersion := "3.1.1"


libraryDependencies+= ("com.bot4s" %% "telegram-core" % "5.4.2").cross(CrossVersion.for3Use2_13)
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test
libraryDependencies += "io.github.pityka" %% "nspl-awt" % "0.10.0"
libraryDependencies += "io.github.pityka" %% "nspl-canvas-js" % "0.10.0"
