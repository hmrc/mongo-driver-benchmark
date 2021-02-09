package runner

import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.profile.HotspotThreadProfiler
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.{CommandLineOptions, OptionsBuilder, TimeValue, WarmupMode}
import profiler.MemoryProfiler

object DefaultRunner extends App {
  new Runner(
    new OptionsBuilder()
      .parent(new CommandLineOptions(args: _*))
      .shouldFailOnError(true)
      .forks(1)
      .threads(-1)
      .measurementTime(TimeValue.seconds(5))
      .warmupTime(TimeValue.seconds(3))
      .warmupMode(WarmupMode.INDI)
      .mode(Mode.Throughput)
      .addProfiler(classOf[MemoryProfiler])
      .addProfiler(classOf[HotspotThreadProfiler])
      .include("benchmarks.*")
      .build()
  ).run()
}
