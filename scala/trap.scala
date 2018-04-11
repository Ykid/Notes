/*
  在scala中，如果我们使用val型的变量使用下划线赋值的话，就会出现这种提示，如果是使用var型的变量，就不会有这种提示了。
  在一下例子中, “_” 的意思是缺省值，而不是如lamda function一般代表一個argument
*/
trait Foo{
  def test: Unit
}

val fooInstance = new Foo {
  override def test: Unit = _ //pass IDE check but fail on compiler check
}

fooInstance.test

/*
Error:(6, 30) unbound placeholder parameter <- 即此value需要被initialized
  override def test: Unit = _
                            ^
*/
