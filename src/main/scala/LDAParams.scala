/**
  * Parameters for the 'org.apache.spark.ml.clustering.LDA' class.
  */
case class LDAParams(
  // Command Line Params
  var tweetsFile: String = "",
  var k: Int = 5,
  var numWordsPerTopics: Int = 20,
  var customStopWordsFile: String = "",


  // Hard Coded Params
  var maxIterations: Int = 10,
  var docConcentration: Double = -1,
  var topicConcentration: Double = -1,
  var vocabSize: Int = 2900000,
  var stopwordFile: String = "src/main/resources/stopWords.txt",
  var algorithm: String = "em",
  var checkpointDir: Option[String] = None,
  var checkpointInterval: Int = 10)