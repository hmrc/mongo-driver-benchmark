package benchmarks

import org.openjdk.jmh.annotations.Mode.All
import org.openjdk.jmh.profile.{HotspotMemoryProfiler, HotspotThreadProfiler}
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.WarmupMode.BULK
import org.openjdk.jmh.runner.options.{CommandLineOptions, OptionsBuilder}

object DefaultRunner extends App {

  new Runner(
    new OptionsBuilder()
      .parent(new CommandLineOptions(args: _*))
      .forks(1)
      .threads(-1)
      .warmupMode(BULK)
      .addProfiler(classOf[HotspotMemoryProfiler])
      .addProfiler(classOf[HotspotThreadProfiler])
      .include("benchmarks.*")
      .mode(All)
      .build()
  ).run()
}
