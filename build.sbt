import Dependencies._

scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.timeseries",
      scalaVersion := "2.12.2",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Timeseries",
    libraryDependencies += scalaTest % Test
  )
