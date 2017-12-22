import org.apache.spark.{SparkConf, SparkContext}
import org.apache.log4j.{Level, LogManager, Logger}

import org.apache.spark.sql._
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature._
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

  // Technically should be set in $SPARK_HOME/conf/log4j.properties.template
  Logger.getLogger("org").setLevel(Level.ERROR)

  // Initialize session.
  val conf = new SparkConf().setAppName("Final Project - LDA on Tweets")
  val sc = new SparkContext(conf)
  val spark = SparkSession.builder().appName("Final Project - LDA on Tweets").getOrCreate()
  val log = LogManager.getRootLogger


  def main(args: Array[String]) {

    log.info("hello, world")

    val df = spark.read.option("header", "false").csv("tweets.txt").withColumnRenamed("_c0", "text")
    df.show(10)

    val vocabSize = 2900000

    //Tokenizing using the RegexTokenizer
    val tokenizer = new RegexTokenizer().setInputCol("text").setOutputCol("tokens")

    ////Removing the Stop-words using the Stop Words remover
    //val stopWordsRemover = new StopWordsRemover().setInputCol("rawTokens").setOutputCol("tokens")
    //stopWordsRemover.setStopWords(stopWordsRemover.getStopWords ++ customizedStopWords)

    //Converting the Tokens into the CountVector
    val countVectorizer = new CountVectorizer().setVocabSize(vocabSize).setInputCol("tokens").setOutputCol("features")

    val pipeline = new Pipeline().setStages(Array(tokenizer, countVectorizer))

    val countVectorizedDf = pipeline.fit(df).transform(df)

    countVectorizedDf.show(10)

    sc.stop()
  }
}