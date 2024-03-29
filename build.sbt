name := "LDATweets"

version := "1.0"

// Spark does not work with version 2.12 of Scala. Must use version 2.11.
scalaVersion := "2.11.12"

// Required for imports from the 'org.apache.spark.sql' namespace.
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.0"

// Required for imports from the 'org.apache.spark.ml' namespace.
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.2.0"

libraryDependencies ++= Seq(
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models",
  "com.google.protobuf" % "protobuf-java" % "2.6.1"
)