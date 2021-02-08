package profiler

import org.openjdk.jmh.infra.{BenchmarkParams, IterationParams}
import org.openjdk.jmh.profile.InternalProfiler
import org.openjdk.jmh.results.{AggregationPolicy, IterationResult, Result, ScalarResult}

import java.util
import scala.collection.JavaConverters._

class MemoryProfiler extends InternalProfiler {

  override def beforeIteration(benchmarkParams: BenchmarkParams, iterationParams: IterationParams): Unit = {}

  override def afterIteration(
    benchmarkParams: BenchmarkParams,
    iterationParams: IterationParams,
    result: IterationResult
  ): util.Collection[_ <: Result[_]] =
    List(
      new ScalarResult("Memory", Runtime.getRuntime.totalMemory() / 1024 / 1024 , "MB", AggregationPolicy.MAX)
    ).asJava

  override def getDescription: String = "Memory profiling"
}
