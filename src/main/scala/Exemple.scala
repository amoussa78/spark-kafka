import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.internal.SQLConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
object Exemple {

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    val conf = new SparkConf().setMaster("local[*]").setAppName("Exemple")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    val consumerRecord:  ConsumerRecord[String, String] = new ConsumerRecord("",1,2l,"myTopic","""{"id":1}""")
    val rdd = sc.parallelize(List(consumerRecord))
  //  val valueRDD = rdd.map(classifiedBrokenEvents)
    val rdd2=rdd.map(_.value)
    rdd2.foreach(println)
  }
}