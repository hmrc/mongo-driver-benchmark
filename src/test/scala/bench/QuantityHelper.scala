package bench

import org.scalameter.Quantity

object QuantityHelper {
  implicit def unpairQuantity[L, R]: Quantity[(L, R)] => (Quantity[L], Quantity[R]) = {
    case Quantity((left, right), units) =>
      val tokenizedUnits = units.split(",")
      Quantity(left, tokenizedUnits(0)) -> Quantity(right, tokenizedUnits(1))
  }

  def pairQuantities[L, R](left: Quantity[L], right: Quantity[R]): Quantity[(L, R)] =
    Quantity(left.value -> right.value, s"${left.units},${right.units}")
}
