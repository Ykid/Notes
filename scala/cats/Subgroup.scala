package example

import cats.kernel.Monoid
import cats.kernel.instances.ListMonoid

object Hello extends App {
  //ref link: https://typelevel.org/cats/typeclasses/semigroup.html
  //key point
  //semi group operation == combine
  trait Semigroup[A] {
    def combine(x: A, y: A): A
  }

  //law: associativity
  //combine(x, combine(y, z)) = combine(combine(x, y), z)

  //associativity -> parallel computation


  //Semigroup is the value storing the companion object of SemiGroup
  //this is indeed a type alias
  import cats.Semigroup


  //this import provides all predefined semi group of cats for you
  import cats.instances.all._

  //actually calls the apply method, the implicit values comes from above import
  Semigroup[Int]

  //intellij command + shift + P: see which implicit method it is called
  Semigroup[List[Byte]]

  //can be used to investigate which class it is
  println(Semigroup[List[Byte]].getClass.getCanonicalName)

  Semigroup[Set[Int]]


  import cats.instances.all._

  //import all operators of semigroup
  import cats.syntax.semigroup._

  //intellij:highlight the expression and use Ctrl + Q to see which implicit method is called
  def optionCombine[A: Semigroup](a: A, opt: Option[A]): A =
    opt.map(a |+| _).getOrElse(a)

  //this might not be very efficient since it loops through all key value pairs
  //this method is the same as the combine method of SemiGroup[Map[K, ?]]
  def mergeMap[K, V: Semigroup](lhs: Map[K, V], rhs: Map[K, V]): Map[K, V] =
    lhs.foldLeft(rhs) {
      case (acc, (k, v)) => acc.updated(k, optionCombine(v, acc.get(k)))
    }

  val xm1 = Map('a' -> 1, 'b' -> 2)
  val xm2 = Map('b' -> 3, 'c' -> 4)
  val x = mergeMap(xm1, xm2)
  assert(Semigroup[Map[Char, Int]].combine(xm1, xm2) == x)
  assert((xm1 |+| xm2) == x)

  val ym1 = Map(1 -> List("hello"))
  val ym2 = Map(2 -> List("cats"), 1 -> List("world"))
  val y = mergeMap(ym1, ym2)
  assert(Semigroup[Map[Int, List[String]]].combine(ym1, ym2) == y)
  assert((ym1 |+| ym2) == y)
}

