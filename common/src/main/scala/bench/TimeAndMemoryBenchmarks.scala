package bench

import org.scalameter.Measurer.MemoryFootprint
import org.scalameter.api.{LocalExecutor, LoggingReporter}
import org.scalameter.{Bench, Executor, Measurer, Persistor}

trait TimeAndMemoryBenchmarks extends Bench[(Double, Double)] {

  import Picklers.tuplePicker
  import org.scalameter.picklers.Implicits._

  lazy val executor =
    LocalExecutor(new Executor.Warmer.Default, Aggregators.tupleAverage, measurer)

  private lazy val timeMeasurer = new Measurer.Default()

  private lazy val memoryFootprintMeasurer = new MemoryFootprint

  lazy val measurer =
    Measurers.composite(measurerName = "time and memory", left = timeMeasurer, right = memoryFootprintMeasurer)

  lazy val reporter = LoggingReporter[(Double, Double)]()

  lazy val persistor = Persistor.None
}
