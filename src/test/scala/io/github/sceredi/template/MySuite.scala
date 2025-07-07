import org.scalatest.*

import flatspec.*
import matchers.*

class ExampleSpec extends AnyFlatSpec with should.Matchers:

  "String" should "equal itself" in:
    val s = "hello"
    s should be("hello")

  "msg size" should "say something nice" in:
    msg should be("I was compiled by Scala 3. :)")
