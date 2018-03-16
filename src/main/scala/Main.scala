import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.log4j.{Level, Logger}
object Main {

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    val conf = new SparkConf().setMaster("local[*]").setAppName("historization")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val ssc = new StreamingContext(sc, Seconds(2))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array(/*"fr.edf.dsp.dlk.api.metrics","fr.edf.dsp.dlk.api.surveillance"*/
      "test")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    stream
      .map(record =>
        record.value)
      .foreachRDD { rdd =>
        if (!rdd.isEmpty) {
          import sqlContext.implicits._
          rdd.sparkContext
          val df = sqlContext.read.json(rdd)
          df.printSchema
          df.show(1,false)
//          df.write
//            .mode(SaveMode.Append)
//            .parquet("H:/Users/Administrator/Desktop/brut")
        }
      }
    stream.print

    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }
}