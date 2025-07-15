package io.github.sceredi.template.model

import org.scalatest.*

import flatspec.*
import matchers.*

class RobotSuite extends AnyFlatSpec with should.Matchers:

  "Robot" should "have an id" in:
    val robot = Robot("123")
    robot.id should be("123")
