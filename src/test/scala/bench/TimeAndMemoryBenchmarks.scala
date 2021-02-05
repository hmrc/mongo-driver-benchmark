package bench

import org.scalameter.Measurer.MemoryFootprint
import org.scalameter.api.LoggingReporter
import org.scalameter.execution.SeparateJvmsExecutor
import org.scalameter.{Bench, Executor, Measurer, Persistor}

trait TimeAndMemoryBenchmarks extends Bench[(Double, Double)] {

  import org.scalameter.picklers.Implicits._
  import Picklers.tuplePicker

  lazy val executor =
    SeparateJvmsExecutor(new Executor.Warmer.Default, Aggregrators.tupleAverage, measurer)

  lazy val measurer =
    Measurers.composite(measurerName = "time and memory", left = Measurer.Default(), right = new MemoryFootprint)

  lazy val reporter = LoggingReporter[(Double, Double)]()

  lazy val persistor = Persistor.None
}
