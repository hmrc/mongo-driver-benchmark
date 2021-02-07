package benchmarks

import org.scalameter.Bench.LocalTime
import org.scalameter._

trait RangeBenchmark extends LocalTime {

  val sizes = Gen.range("size")(300000, 1500000, 300000)

  val ranges = for {
    size <- sizes
  } yield 0 until size

  performance of "Range" in {
    measure method "map" in {
      using(ranges) in {
        println("RANGE")
        r => r.map(_ + 1)
      }
    }
  }
}










