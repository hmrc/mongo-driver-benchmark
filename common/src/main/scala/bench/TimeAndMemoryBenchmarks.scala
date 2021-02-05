package bench

import org.scalameter.Measurer.MemoryFootprint
import org.scalameter.api.LoggingReporter
import org.scalameter.execution.SeparateJvmsExecutor
import org.scalameter.{Bench, Executor, Measurer, Persistor}

trait TimeAndMemoryBenchmarks extends Bench[(Double, Double)] {

  import Picklers.tuplePicker
  import org.scalameter.picklers.Implicits._

  lazy val executor =
    SeparateJvmsExecutor(new Executor.Warmer.Default, Aggregrators.tupleAverage, measurer)

  private lazy val timeMeasurer = new Measurer.Default()

  private lazy val memoryFootprintMeasurer = new MemoryFootprint

  lazy val measurer =
    Measurers.composite(measurerName = "time and memory", left = timeMeasurer, right = memoryFootprintMeasurer)

  lazy val reporter = LoggingReporter[(Double, Double)]()

  lazy val persistor = Persistor.None
}
