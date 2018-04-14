//object can be assigned as values
object Foo {
  println("init")
}

object MyApp extends App {
  lazy val foo = Foo //no print
  val bar = Foo //output: "init"
}