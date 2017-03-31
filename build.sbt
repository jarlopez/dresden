name := "dresden"

organization := "edu.kth"

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq(
    Resolver.mavenLocal,
    "Kompics Releases" at "http://kompics.sics.se/maven/repository/",
    "Kompics Snapshots" at "http://kompics.sics.se/maven/snapshotrepository/"

)
libraryDependencies ++= Seq(
    "se.sics.kompics" %% "kompics-scala" % "0.9.2-SNAPSHOT",
    "se.sics.kompics.basic" % "kompics-component-netty-network" % "0.9.2-SNAPSHOT",
    "se.sics.kompics.basic" % "kompics-component-java-timer" % "0.9.2-SNAPSHOT",
    "se.sics.kompics.simulator" % "core" % "0.9.2-SNAPSHOT",
    "ch.qos.logback" % "logback-classic" % "0.9.28",
    "org.scala-lang.modules" %% "scala-pickling" % "0.10.1",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
//    "com.github.johnreedlol" %% "scala-trace-debug" % "3.0.6", // For Intellij Scala Trace Debug
//    "com.google.code.findbugs" % "jsr305" % "1.3.+" // For "Class javax.annotation.Nullabel not found"
)