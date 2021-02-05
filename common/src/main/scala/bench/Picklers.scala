package bench

import org.scalameter.picklers.Pickler

object Picklers {
  implicit def tuplePicker[L, R](implicit left: Pickler[L], right: Pickler[R]): Pickler[(L, R)] =
    new Pickler[(L, R)] {
      override def pickle(x: (L, R)): Array[Byte] = left.pickle(x._1) ++ right.pickle(x._2)
      override def unpickle(a: Array[Byte], from: Int): ((L, R), Int) = {
        val (leftValue, nextForRight) = left.unpickle(a, 0)
        val (rightValue, next)        = right.unpickle(a, nextForRight)
        ((leftValue, rightValue), next)
      }
    }
}
