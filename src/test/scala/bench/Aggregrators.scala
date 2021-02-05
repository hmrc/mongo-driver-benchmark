package bench

import org.scalameter.Aggregator

object Aggregrators {
  import bench.QuantityHelper._

  def tupleAggregator[T](name: String, baseAggregator: Aggregator[T]): Aggregator[(T, T)] =
    Aggregator(name) { quantities =>
      val (first, second) = quantities.unzip
      pairQuantities(
        baseAggregator(first),
        baseAggregator(second)
      )
    }

  val tupleAverage = tupleAggregator("tuple average", Aggregator.average)
}
