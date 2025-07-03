import org.scalatest.*

import flatspec.*
import matchers.*

class ExampleSpec extends AnyFlatSpec with should.Matchers:

  "Integer" should "equal itself" in:
    val i = 2
    i should be(2)

  "String" should "equal itself" in:
    val s = "hello"
    s should be("hello")
