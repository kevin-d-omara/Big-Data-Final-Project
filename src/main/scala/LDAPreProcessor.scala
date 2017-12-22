import edu.stanford.nlp.process.Morphology

import org.apache.spark.SparkContext
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.{Vector => MLVector}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}

class LDAPreProcessor(sc: SparkContext, spark: SparkSession)  {

  /**
    * Load documents, tokenize them, create vocabulary, and prepare documents as term count vectors.
    *
    * @return (corpus, vocabulary as array, total token count in corpus)
    */
  def preprocess(params: LDAParams): (RDD[(Long, Vector)], Array[String], Long) = {
    val preprocessStart = System.nanoTime()

    import spark.implicits._
    //Reading the Whole Text Files
    val initialrdd = spark.sparkContext.textFile(params.tweetsFile)
    initialrdd.cache()
    val rdd = initialrdd.mapPartitions { partition =>
      val morphology = new Morphology()
      partition.map { value =>
        LDAHelper.getLemmaText(value, morphology)
      }
    }.map(LDAHelper.filterSpecialCharacters)
    rdd.cache()
    initialrdd.unpersist()
    val df = rdd.toDF("docs")
    val englishStopWords: Array[String] = if (params.stopwordFile.isEmpty) {
      Array.empty[String]
    } else {
      val stopWordText = sc.textFile(params.stopwordFile).collect()
      stopWordText.flatMap(_.stripMargin.split("\n"))
    }
    val customStopWords: Array[String] = if (params.customStopWordsFile.isEmpty) {
      Array.empty[String]
    } else {
      val stopWordText = sc.textFile(params.customStopWordsFile).collect()
      stopWordText.flatMap(_.stripMargin.split("\n"))
    }
    //Tokenizing using the RegexTokenizer
    val tokenizer = new RegexTokenizer().setInputCol("docs").setOutputCol("rawTokens")

    //Removing the Stop-words using the Stop Words remover
    val stopWordsRemover = new StopWordsRemover().setInputCol("rawTokens").setOutputCol("tokens")
    stopWordsRemover.setStopWords(stopWordsRemover.getStopWords ++ englishStopWords ++ customStopWords)

    //Converting the Tokens into the CountVector
    val countVectorizer = new CountVectorizer().setVocabSize(params.vocabSize).setInputCol("tokens").setOutputCol("features")

    //Setting up the pipeline
    val pipeline = new Pipeline().setStages(Array(tokenizer, stopWordsRemover, countVectorizer))

    val model = pipeline.fit(df)
    val documents = model.transform(df).select("features").rdd.map {
      case Row(features: MLVector) => Vectors.fromML(features)
    }.zipWithIndex().map(_.swap)

    val vocabulary = model.stages(2).asInstanceOf[CountVectorizerModel].vocabulary
    val actualNumTokens = documents.map(_._2.numActives).sum().toLong


    // Display results.
    val actualCorpusSize = df.count
    val actualVocabSize = vocabulary.length
    val preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9

    println()
    println(s"Corpus summary:")
    println(s"\t Training set size: $actualCorpusSize documents")
    println(s"\t Vocabulary size: $actualVocabSize terms")
    println(s"\t Training set size: $actualNumTokens tokens")
    println(s"\t Preprocessing time: $preprocessElapsed sec")
    println()


    documents.cache()
    (documents, vocabulary, actualCorpusSize)
  }
}
