import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object ExempleStackOverFlow {

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    val conf = new SparkConf().setMaster("local[*]").setAppName("exemple")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val ssc = new StreamingContext(sc, Seconds(2))
    import sqlContext.implicits._
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("test")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
      stream
      .foreachRDD { rdd =>
        if (!rdd.isEmpty) {
          val offsetArray = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
          val df = sqlContext.read.json(rdd.map(_.value))
          //bug
          df.printSchema
          df.show(false)
          stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetArray)
        }
      }


    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }
//crash
  def bug = {
    val a = Array.empty
    a(0)
  }
}