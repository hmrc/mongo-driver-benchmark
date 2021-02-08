package benchmarks

import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.profile.HotspotThreadProfiler
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.WarmupMode.BULK
import org.openjdk.jmh.runner.options.{CommandLineOptions, OptionsBuilder}
import profiler.MemoryProfiler

object DefaultRunner extends App {

  new Runner(
    new OptionsBuilder()
      .parent(new CommandLineOptions(args: _*))
      .forks(1)
      .threads(-1)
      .warmupMode(BULK)
      .addProfiler(classOf[MemoryProfiler])
      .addProfiler(classOf[HotspotThreadProfiler])
      .include("benchmarks.*")
      .mode(Mode.Throughput)
      .build()
  ).run()
}
