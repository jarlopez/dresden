name := "dresden"

organization := "edu.kth"

version := "1.0"

scalaVersion := "2.11.8"

val kompicsVersion = "0.9.2-SNAPSHOT"
val ktoolboxVersion = "2.0.2-SNAPSHOT"

logBuffered in Test := false
updateOptions := updateOptions.value.withLatestSnapshots(false)

resolvers ++= Seq(
    Resolver.mavenLocal,
    "Kompics Releases" at "http://kompics.sics.se/maven/repository/",
    "Kompics Snapshots" at "http://kompics.sics.se/maven/snapshotrepository/",
    "Artima Maven Repository" at "http://repo.artima.com/releases"
)

libraryDependencies ++= Seq(
    // Kompics
    "se.sics.kompics" %% "kompics-scala" % kompicsVersion,
    "se.sics.kompics.basic" % "kompics-component-netty-network" % kompicsVersion,
    "se.sics.kompics.basic" % "kompics-component-java-timer" % kompicsVersion,
    "se.sics.kompics.simulator" % "core" % kompicsVersion,

    // K-Toolbox
    "se.sics.ktoolbox.overlaymngr" % "core" % ktoolboxVersion,
    "se.sics.ktoolbox.overlaymngr.bootstrap" % "client" % ktoolboxVersion,
    "se.sics.ktoolbox.overlaymngr.bootstrap" % "server" % ktoolboxVersion,

    // Other
    "ch.qos.logback" % "logback-classic" % "0.9.28",
    "org.scala-lang.modules" %% "scala-pickling" % "0.10.1",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "io.suzaku" %% "boopickle" % "1.2.6",

    // Client
    "commons-cli" % "commons-cli" % "1.3.1",
    "org.jline" % "jline" % "3.1.3",
    "log4j" % "log4j" % "1.2.17",


    // Testing
    "org.scalactic" %% "scalactic" % "3.0.1",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "junit" % "junit" % "4.11" % Test,
    "com.novocode" % "junit-interface" % "0.11" % Test
)

