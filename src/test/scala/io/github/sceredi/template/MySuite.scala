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

  "msg size" should "say something nice" in:
    msg should be("I was compiled by Scala 3. :)")

  "anint" should "equal 1" in:
    anint should be(1)
