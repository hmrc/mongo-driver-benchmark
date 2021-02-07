import benchmarks.SimpleReactiveMongoBenchmark
import org.scalameter.Bench

class BenchmarkSuite extends Bench.Group {
  include(new SimpleReactiveMongoBenchmark {})
}
