package example

import org.scalameter.Measurer.MemoryFootprint
import org.scalameter._
import org.scalameter.api.LoggingReporter
import org.scalameter.execution.SeparateJvmsExecutor
import org.scalameter.picklers.Implicits._
import org.scalameter.picklers.Pickler

object RangeBenchmark extends TimeAndMemoryBenchmarks {

  val sizes = Gen.range("size")(from = 300000, upto = 1500000, hop = 300000)

  val ranges = for {
    size <- sizes
  } yield 0 until size

  /* tests */

  performance of "Range" in {
    measure method "map" in {
      using(ranges) in { r =>
        r.map(_ + 1)
      }
    }
  }
}

trait TimeAndMemoryBenchmarks extends Bench[(Double, Double)] {

  import Picklers._

  lazy val executor =
    SeparateJvmsExecutor(new Executor.Warmer.Default, Aggregrators.tupleAverage, measurer)

  lazy val measurer =
    Measurers.composite(measurerName = "time and memory", left = Measurer.Default(), right = new MemoryFootprint)

  lazy val reporter = LoggingReporter[(Double, Double)]()

  lazy val persistor = Persistor.None
}

object Picklers {
  implicit def tuplePicker[L, R](implicit left: Pickler[L], right: Pickler[R]): Pickler[(L, R)] =
    new Pickler[(L, R)] {
      override def pickle(x: (L, R)): Array[Byte] = left.pickle(x._1) ++ right.pickle(x._2)
      override def unpickle(a: Array[Byte], from: Int): ((L, R), Int) = {
        val (leftValue, nextForRight) = left.unpickle(a, 0)
        val (rightValue, next)        = right.unpickle(a, nextForRight)
        ((leftValue, rightValue), next)
      }
    }
}

object QuantityHelper {
  implicit def unpairQuantity[L, R]: Quantity[(L, R)] => (Quantity[L], Quantity[R]) = {
    case Quantity((left, right), units) =>
      val tokenizedUnits = units.split(",")
      Quantity(left, tokenizedUnits(0)) -> Quantity(right, tokenizedUnits(1))
  }

  def pairQuantities[L, R](left: Quantity[L], right: Quantity[R]): Quantity[(L, R)] =
    Quantity(left.value -> right.value, s"${left.units},${right.units}")
}

object Aggregrators {
  import QuantityHelper._

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

object Measurers {
  import QuantityHelper._

  def composite[L, R](measurerName: String, left: Measurer[L], right: Measurer[R]): Measurer[(L, R)] =
    new Measurer[(L, R)] {
      override val name = measurerName

      override def measure[T](
        context: Context,
        measurements: Int,
        setup: T => Any,
        tear: T => Any,
        regen: () => T,
        snippet: T => Any
      ): Seq[Quantity[(L, R)]] =
        for {
          leftResult <- left.measure(
                          context,
                          measurements,
                          setup,
                          tear,
                          regen,
                          snippet
                        )
          rightResult <- right.measure(
                           context,
                           measurements,
                           setup,
                           tear,
                           regen,
                           snippet
                         )
        } yield pairQuantities(leftResult, rightResult)

    }

}
