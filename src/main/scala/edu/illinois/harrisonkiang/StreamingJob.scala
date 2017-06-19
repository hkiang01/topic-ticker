package edu.illinois.harrisonkiang

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.dataartisans.flinktraining.exercises.datastream_java.datatypes.TaxiRide
import com.dataartisans.flinktraining.exercises.datastream_java.sources.TaxiRideSource
import com.dataartisans.flinktraining.exercises.datastream_java.utils.{GeoUtils, TaxiRideSchema}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.windowing.WindowFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010
import org.apache.flink.util.Collector

/**
 * Skeleton for a Flink Streaming Job.
 *
 * For a full example of a Flink Streaming Job, see the SocketTextStreamWordCount.java
 * file in the same package/directory or have a look at the website.
 *
 * You can also generate a .jar file that you can submit on your Flink
 * cluster. Just type
 * {{{
 *   mvn clean package
 * }}}
 * in the projects root directory. You will find the jar in
 * target/topic-ticker-1.0-SNAPSHOT.jar
 * From the CLI you can then run
 * {{{
 *    ./bin/flink run -c edu.illinois.harrisonkiang.StreamingJob target/topic-ticker-1.0-SNAPSHOT.jar
 * }}}
 *
 * For more information on the CLI see:
 *
 * http://flink.apache.org/docs/latest/apis/cli.html
 */
object StreamingJob {
  def main(args: Array[String]) {
    // set up the streaming execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    // get the taxi ride data stream
    val rides = env.addSource(
      new TaxiRideSource("./src/main/resources/nycTaxiRides.gz", 60, 600))

    /**
     * Here, you can start creating your execution plan for Flink.
     *
     * Start with getting some data from the environment, like
     *  env.readTextFile(textPath);
     *
     * then, transform the resulting DataStream[String] using operations
     * like
     *   .filter()
     *   .flatMap()
     *   .join()
     *   .group()
     *
     * and many more.
     * Have a look at the programming guide:
     *
     * http://flink.apache.org/docs/latest/apis/streaming/index.html
     *
     */

    val filteredRides = rides.filter(ride =>
      GeoUtils.isInNYC(ride.startLon, ride.startLat) && GeoUtils.isInNYC(ride.endLon, ride.endLat))

    // https://dataartisans.github.io/flink-training/dataStream/3-handsOn.htmlq

    filteredRides.addSink(new FlinkKafkaProducer010[TaxiRide](
      "localhost:9092", // Kafka broker host:port
      "cleansedRides",  // Topic to write to
      new TaxiRideSchema  // serializer (provided as util)
    ))

    // filteredRides.print()

//    val tuples = filteredRides.map(ride => {
//      if(ride.isStart) {
//        val startCell = GeoUtils.mapToGridCell(ride.startLon, ride.startLat)
//        (startCell, true)
//      } else {
//        val endCell = GeoUtils.mapToGridCell(ride.endLon, ride.endLat)
//        (endCell, false)
//      }
//    })
//
//    val keyed: KeyedStream[(Int, Boolean), (Int, Boolean)] = tuples.keyBy(identity(_))
//
//    val timed: WindowedStream[(Int, Boolean), (Int, Boolean), TimeWindow] = keyed.timeWindow(Time.minutes(15), Time.minutes(5))
//
//    val counts: DataStream[(Int, Long, Boolean, Int)] = timed.apply{
//      (key: (Int, Boolean), window, vals, out: Collector[(Int, Long, Boolean, Int)]) =>
//        out.collect((key._1, window.getEnd, key._2, vals.size))
//    }
//
//    val mappedBack: DataStream[(Float, Float, Long, Boolean, Int)] = counts.map(elem => {
//      val lon = GeoUtils.getGridCellCenterLon(elem._1)
//      val lat = GeoUtils.getGridCellCenterLat(elem._1)
//      (lon, lat, elem._2, elem._3, elem._4)
//    })
//
//    mappedBack.print()

    // execute program
    env.execute("Taxi Ride Cleaning Exercise")
  }
}