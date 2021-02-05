package bench

import org.scalameter.{Context, Measurer, Quantity}

object Measurers {
  import bench.QuantityHelper._

  def composite[L, R](measurerName: String, left: Measurer[L], right: Measurer[R])(
    implicit numericL: Numeric[L],
    numericR: Numeric[R]
  ): Measurer[(L, R)] =
    new Measurer[(L, R)] {
      override val name = measurerName

      override def measure[T](
        context: Context,
        measurements: Int,
        setup: T => Any,
        tear: T => Any,
        regen: () => T,
        snippet: T => Any
      ): Seq[Quantity[(L, R)]] =
        for {
          leftResult <- left.measure(
                          context,
                          measurements,
                          setup,
                          tear,
                          regen,
                          snippet
                        )
          rightResult <- right.measure(
                           context,
                           measurements,
                           setup,
                           tear,
                           regen,
                           snippet
                         )
        } yield pairQuantities(leftResult, rightResult)

    }

}
