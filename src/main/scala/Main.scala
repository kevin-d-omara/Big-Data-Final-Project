import org.apache.spark.{SparkConf, SparkContext}
import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.sql._

// If running from IntelliJ, perform one-time setup:
//    # Add Spark libraries:
//    Click File > Project Structure
//    Click Project Settings > Libraries > 'green plus (first one, left side)' > Java
//        ^ enter the root of your spark libraries
//
//    # Make it run on a cluster:
//    Click Run > Run... > Main
//        ^ This should fail with the error: "org.apache.spark.SparkException: A master URL must be set in your configuration"
//    Click Run > Edit Configurations
//    In VM Options for Main, paste one of these:
//        -Dspark.master=local[2]                         <-- Run Spark in the JVM with 2 worker threads (a temporary cluster is spun-up for this).
//        -Dspark.master=spark://komara-VirtualBox:7077   <-- Run spark on a local cluster you've already started with `SPARK_HOME/sbin/start-master.sh`.
//                                                            This way you can benefit from the web UI.
//                                                            NOTE: The value 'spark://komara-VirtualBox:7077' will be different each time you run 'start-master.sh'.
//
object Main {

  def main(args: Array[String]) {

    // Technically should be set in $SPARK_HOME/conf/log4j.properties.template
    Logger.getLogger("org").setLevel(Level.ERROR)

    val conf = new SparkConf().setAppName("Final Project - LDA on Tweets").setMaster("local[*]").set("spark.executor.memory", "2g")
    val spark = SparkSession.builder().config(conf).getOrCreate()
    val sc = spark.sparkContext
    val log = LogManager.getRootLogger

    // Check for valid input.
    if (args.length < 1)
    {
      log.error("Requires one argument, " + args.length + " provided.")
      log.error("Usage:")
      log.error("args[0]          => path to input tweets file, one tweet per line, ex: s3n://komara.sdsu.big-data.LDATweets/tweets.txt")
      log.error("Optional:")
      log.error("-k, --topics     => number of topics to discover, default: 5")
      log.error("-n, --topicWords => number of words to associated with each topic, default: 20")
      log.error("-s, --stopWords  => path to custom stopwords file, one stopword per line, ex: s3n://komara.sdsu.big-data.LDATweets/customStopWords.txt")
      return
    }

    // LDA Params
    val params = LDAParams()
    params.tweetsFile = args(0)

    args.sliding(2, 1).toList.collect {
      case Array("-k", arg: String)       => params.k = arg.toInt
      case Array("--topics", arg: String) => params.k = arg.toInt

      case Array("-n", arg: String)           => params.numWordsPerTopics = arg.toInt
      case Array("--topicWords", arg: String) => params.numWordsPerTopics = arg.toInt

      case Array("-s", arg: String)          => params.customStopWordsFile = arg
      case Array("--stopWords", arg: String) => params.customStopWordsFile = arg
    }

    // Pre-Process
    val (corpus, vocabulary, actualCorpusSize) = new LDAPreProcessor(sc, spark).preprocess(params)

    // Run LDA
    new LDARunner(sc, spark).run(params, corpus, vocabulary, actualCorpusSize)

    sc.stop()
  }
}
