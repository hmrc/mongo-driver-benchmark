package benchmarks

import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.profile.HotspotThreadProfiler
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.WarmupMode.INDI
import org.openjdk.jmh.runner.options.{CommandLineOptions, OptionsBuilder, TimeValue}
import profiler.MemoryProfiler

object DefaultRunner extends App {
  new Runner(
    new OptionsBuilder()
      .parent(new CommandLineOptions(args: _*))
      .shouldFailOnError(true)
      .forks(1)
      .threads(-1)
      .measurementTime(TimeValue.seconds(4))
      .warmupTime(TimeValue.seconds(2))
      .warmupMode(INDI)
      .mode(Mode.Throughput)
      .addProfiler(classOf[MemoryProfiler])
      .addProfiler(classOf[HotspotThreadProfiler])
      .include("benchmarks.*")
      .build()
  ).run()
}
