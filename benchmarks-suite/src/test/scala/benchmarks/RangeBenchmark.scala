package benchmarks

import bench.TimeAndMemoryBenchmarks
import org.scalameter._

trait RangeBenchmark extends TimeAndMemoryBenchmarks {

  val sizes = Gen.range("size")(from = 300000, upto = 1500000, hop = 300000)

  val ranges = for {
    size <- sizes
  } yield 0 until size

  performance of "Range" in {
    measure method "map" in {
      using(ranges) in { r =>
        r.map(_ + 1)
      }
    }
  }
}










