package example

import java.sql.Statement
import java.time.Instant
import java.util.Date

import scala.language.higherKinds

object ExistentialtTypeExercise extends App {
  //https://www.cakesolutions.net/teamblogs/existential-types-in-scala
  case class User(name: String)
  val user = User("hi")
  //existential types


  /**
    * Demonstrate Type Variables
    */
  type F[A] = SomeClass[A]

  class SomeClass[T](input: T) {

  }

  object SomeClass {
    def apply[T](input: T): SomeClass[T] = new SomeClass(input)
  }

  val input1: F[String] = SomeClass("hello")
  val input2: F[Int] = SomeClass(1: Int)
  val input3: F[Boolean] = SomeClass(true)

  // does not compile
  // SomeClass("hello"): F[Int]

  // `A` appears only on the right, existential case
  type G = SomeClass[A] forSome {type A}
  val input12: G = input1
  val input22: G = input2
  val input32: G = input3


  /**
    * Demonstrate create existential type: reinvent the wheel
    */
  sealed trait Existential {
    type Inner
    val value: Inner
  }

  final case class MkEx[A](value: A) extends Existential {
    type Inner = A
  }

  MkEx("hello"): Existential
  MkEx(1: Int): Existential

  /**
    * We could think of MkEx as a type eraser: it doesn't matter what type of data we choose to put into MkEx, it will erase the type and always return Existential.
    *
    */

  val ex1: Existential = MkEx("hello")
  //the type is ex.Inner but not any more specific type
  //existential types intend to add some CONSTRAINTs
  val inner: ex1.Inner = ex1.value

  /**
    * Demonstrate how * operator is used
    * ref: https://docs.scala-lang.org/tutorials/FAQ/finding-symbols.html
    */
  def letsSee(ints: Int*): Unit = {
    ints.foreach(println _)
  }

  letsSee(1, 2, 3, 4, 5, 6)

  /**
    * Phase
    */
  def bind(objs: Object*): Statement = ???

  sealed trait AllowedType[A] {
    /**
      * The Java type we are converting to.
      *
      * Note the restrictions:
      *
      * * `:> Null` means that we can turn the type into `null`.
      * This is needed since many Java SQL libraries interpret NULL as `null`
      * * `<: AnyRef` just means "this is an Object". ie: not an AnyVal.
      */
    //>: Null makes the following toNull ok
    // <: AnyRef is required for toObject
    type JavaType >: Null <: AnyRef

    /**
      * Function that converts `A` (eg: Int) to the JavaType (eg: Integer)
      */
    def toJavaType(a: A): JavaType

    /**
      * Same as above, but upcasts to Object (which is what `bind` expects)
      *
      *
      */
    //Me: This gives us our filter and converter: we can only call AllowedType[A].toObject(a) if A implements our typeclass.
    def toObject(a: A): Object = toJavaType(a)
  }

  object AllowedType {
    implicit val intInstance: AllowedType[Int] = instance(Int.box(_))
    implicit val strInstance: AllowedType[String] = instance(identity)
    implicit val boolInstance: AllowedType[Boolean] = instance(Boolean.box(_))
    implicit val instantInst: AllowedType[Instant] = instance(Date.from(_))

    // For Option, we turn `None` into `null`; this is why we needed that `:> Null`
    // restriction
    implicit def optionInst[A](implicit ev: AllowedType[A]): AllowedType[Option[A]] =
      instance[Option[A], ev.JavaType](s => s.map(ev.toJavaType(_)).orNull)


    /**
      * short hand to call AllowedType[Int] instead of calling implicitly[ AllowedType[Int] ]
      */
    def apply[A](implicit ev: AllowedType[A]) = ev

    def instance[A, J >: Null <: AnyRef](f: A => J): AllowedType[A] =
      new AllowedType[A] {
        type JavaType = J

        def toJavaType(a: A) = f(a)
      }
  }

  sealed trait AnyAllowedType {
    type A
    val value: A
    val evidence: AllowedType[A] //type, not calling apply
  }

  final case class MkAnyAllowedType[A0](value: A0)(implicit val evidence: AllowedType[A0])
    extends AnyAllowedType {
    type A = A0
  }

  val allow1 = MkAnyAllowedType("Hello"): AnyAllowedType
  val allow2 = MkAnyAllowedType(1: Int): AnyAllowedType
  val allow3 = MkAnyAllowedType(Instant.now()): AnyAllowedType

  case class NotConform(value: String)

  //not allowed, since there is no instance of NotConform
  //val allow4 = MkAnyAllowedType(NotConform("shouldn't pass")): AnyAllowedType

  def safeBind(any: AnyAllowedType*): Statement =
    bind(any.map(ex => ex.evidence.toObject(ex.value)): _*)

  lazy val shouldcompile = {
    safeBind(MkAnyAllowedType(1), MkAnyAllowedType("Hello"), MkAnyAllowedType(Instant.now()))
  }

  //  lazy val shouldfail = {
  //    safeBind(MkAnyAllowedType(1), MkAnyAllowedType(user)) // Does not compile, no instance of AllowedType for User
  //
  //  }


  object AnyAllowedType {
    implicit val anyAllowedInst: AllowedType[AnyAllowedType] =
      AllowedType.instance(ex => ex.evidence.toJavaType(ex.value))

    //dedicate for demonstrating shouldcompile2
    implicit def anyAllowedToAny[A: AllowedType](a: A): AnyAllowedType =
      MkAnyAllowedType(a)
  }

  // Now we can simply do
  val ex: AnyAllowedType = MkAnyAllowedType(1)
  //since there's no side effect, it is written as AllowedType[AnyAllowedType]
  //AllowedType[AnyAllowedType].apply use anyAllowedInst in AnyAllowedType to convert ex to javaObject
  AllowedType[AnyAllowedType].toObject(ex)

  lazy val shouldcompile2 = {
    safeBind(1, "Hello", Instant.now(), true, Option(1))
  }

  lazy val shouldfail2 = {
    //    safeBind(user)
  }

  /**
    * Phase: Generalize AnyAllowedType. The following can replace AnyAllowedTypecode
    * TC[_] equivalent to AllowedType[Something] above
    * TC means type class
    */

  sealed trait TCBox[TC[_]] {
    type A
    val value: A
    val evidence: TC[A]
  }

  final case class MkTCBox[TC[_], B](value: B)(implicit val evidence: TC[B])
    extends TCBox[TC] {
    type A = B
  }

  object TCBox {
    implicit def anyTCBox[TC[_], A: TC](a: A): TCBox[TC] = MkTCBox(a)
  }

  /**
    * cannot use TCBox[AllowedType[_] ]. since toObject takes type A in AllowedType[A].
    * e.g.
    * val foo: TCBox[AllowedType[_] ] ; type of `foo.evidence.toObject` will not be `A` but _$1
    */
  def safeBind2(any: TCBox[AllowedType]*): Statement =
    bind({
      any.map((ex: TCBox[AllowedType]) => {
        ex.evidence.toObject(ex.value)
      })
    }: _*)

  lazy val shouldcompile3 = {
    safeBind2(MkTCBox(1), MkTCBox("Hello"), MkTCBox(Instant.now()))
  }

  //  lazy val shouldfail3 = {
  //    safeBind2(MkTCBox(1), MkTCBox(user)) // Does not compile, no instance of AllowedType for User
  //  }

  lazy val shouldcompile4 = {
    safeBind2(1, "Hello", Instant.now(), true, Option(1))
  }

  //  lazy val shouldfail4 = {
  //    safeBind2(user)
  //  }
  println("done")
}
