import benchmarks.RangeBenchmark
import org.scalameter.Bench

class BenchmarkSuite extends Bench.Group {
  include(new RangeBenchmark {})
}
