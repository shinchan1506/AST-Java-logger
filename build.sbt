name := "shin_imai_hw3"

version := "0.1"

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "hw3"

lazy val homeworkthree = (project in file("."))
  .settings(
    name := "homeworkthree",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.4", //typesafe
      // logback
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      // unit testing, junit
      "org.scalactic" %% "scalactic" % "3.0.8",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "junit" % "junit" % "4.11" % Test,
      "com.novocode" % "junit-interface" % "0.10-M4" % "test",
      // eclipse AST
      "org.eclipse.jdt" % "org.eclipse.jdt.annotation" % "2.0.0",
      "org.eclipse.jdt" % "org.eclipse.jdt.apt.core" % "3.6.500",
      "org.eclipse.jdt" % "org.eclipse.jdt.compiler.apt" % "1.2.100",
      "org.eclipse.jdt" % "org.eclipse.jdt.core" % "3.15.0",
      "org.eclipse.jdt" % "org.eclipse.jdt.core" % "3.19.0",
    ),
    libraryDependencies += "commons-io" % "commons-io" % "2.6",

    compileOrder := CompileOrder.JavaThenScala

  )