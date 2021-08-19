ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.11.12"
ThisBuild / organization := "org.example"

val spinalVersion = "1.6.0"
val spinalCore = "com.github.spinalhdl" %% "spinalhdl-core" % spinalVersion
val spinalLib = "com.github.spinalhdl" %% "spinalhdl-lib" % spinalVersion
val spinalIdslPlugin = compilerPlugin("com.github.spinalhdl" %% "spinalhdl-idsl-plugin" % spinalVersion)
val scalatest = "org.scalatest" %% "scalatest" % "3.2.9" % Test

lazy val root = project
  .in(file("."))
  .settings(
    name := "Sapper",
    libraryDependencies ++= Seq(spinalCore, spinalLib, spinalIdslPlugin, scalatest)
  )

fork := true