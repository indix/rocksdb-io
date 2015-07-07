val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
val junit = "junit" % "junit" % "4.10" % "test"
val rocksDb = "org.rocksdb" % "rocksdbjni" % "3.10.1"
val hadoopCore = "org.apache.hadoop" % "hadoop-core" % "2.0.0-mr1-cdh4.2.1" % "provided"
val hadoopCommon = "org.apache.hadoop" % "hadoop-common" % "2.0.0-cdh4.2.1" % "provided"
val jacksonScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.4.4"
val cascadingCore = "cascading" % "cascading-core" % "2.7.0" % "provided"
val cascadingLocal = "cascading" % "cascading-local" % "2.7.0" % "provided"
val cascadingHadoop = "cascading" % "cascading-hadoop" % "2.7.0" % "provided"
val scaldingCore = "com.twitter" %% "scalding-core" % "0.12.0" % "provided"

val appVersion = sys.env.getOrElse("SNAP_PIPELINE_COUNTER", "1.0.0-SNAPSHOT")

lazy val commonSettings = Seq(
  organization := "com.indix",
  organizationName := "Indix",
  organizationHomepage := Some(url("http://www.indix.com")),
  version := appVersion,
  scalaVersion := "2.10.4",
  crossPaths := false,
  parallelExecution in This := false,
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  javacOptions ++= Seq("-Xlint:deprecation", "-source", "1.7"),
  resolvers ++= Seq(
    "Cloudera Repo" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
    "maven.org" at "http://repo2.maven.org/maven2",
    "conjars.org" at "http://conjars.org/repo"
  )
)
lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra :=
    <url>https://github.com/ind9/scalding-rocksdb</url>
      <licenses>
        <license>
          <name>Apache License</name>
          <url>https://raw.githubusercontent.com/ind9/scalding-rocksdb/master/LICENSE</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:ind9/scalding-rocksdb.git</url>
        <connection>scm:git:git@github.com:ind9/scalding-rocksdb.git</connection>
      </scm>
      <developers>
        <developer>
          <id>indix</id>
          <name>Indix</name>
          <url>http://www.indix.com</url>
        </developer>
      </developers>
)

lazy val rocksdbScalding = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "rocksdb-io",
    libraryDependencies ++= Seq(
      scalaTest, junit, rocksDb, hadoopCore, hadoopCommon, jacksonScala, cascadingCore, cascadingHadoop, scaldingCore
    )
  )

