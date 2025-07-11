import io.github.sceredi.template.model.*

@main def hello(): Unit =
  println("=== Robot Simulation Example ===")
  
  // Create proximity sensors for obstacle detection
  val frontSensor = SimpleProximitySensor(0.0, 1.0) // Front-facing sensor
  val leftSensor = SimpleProximitySensor(math.Pi / 4, 1.0) // 45 degrees left
  val rightSensor = SimpleProximitySensor(-math.Pi / 4, 1.0) // 45 degrees right
  
  // Create robot configurations
  val sensorConfig = SimpleSensorConfig(
    proximitySensors = Some(Seq(frontSensor, leftSensor, rightSensor)),
    lightSensors = None
  )
  
  val actuatorConfig = SimpleActuatorConfig(
    wheels = Some((values, robot) => SimpleWheelsActuator(values, robot))
  )
  
  // Create robots
  val robot1 = SimpleRobot(
    sensorConfig = sensorConfig,
    actuatorConfig = actuatorConfig,
    position = (0.0, 0.0),
    orientation = 0.0
  )
  
  val robot2 = SimpleRobot(
    sensorConfig = sensorConfig,
    actuatorConfig = actuatorConfig,
    position = (0.2, 0.0), // Close to robot1 to trigger obstacle avoidance
    orientation = math.Pi / 2
  )
  
  // Create initial simulation state
  val simulationState = SimpleSimulationState(
    robots = Seq(robot1, robot2)
  )
  
  println("Initial state:")
  simulationState.robots.zipWithIndex.foreach { case (robot, index) =>
    val robotNum = index + 1
    val x = robot.position._1
    val y = robot.position._2
    println("Robot " + robotNum.toString + ": Position(" + x.toString + ", " + y.toString + ")")
  }
  
  // Demonstrate one simulation step
  println("\n--- Demonstrating Random Walk Behavior ---")
  
  val robot = robot1
  
  // Show sensor readings
  val proximityReadings = robot.sensorConfig.proximitySensors match
    case Some(sensors) => sensors.map(_.sense(simulationState, robot))
    case None => Seq.empty[ProximityReading]
  
  val readingsStr: String = proximityReadings.map(r => r.toString).mkString(", ")
  println("Proximity sensor readings: " + readingsStr)
  
  val minReading = if proximityReadings.nonEmpty then 
    proximityReadings.foldLeft(1.0)((acc, r) => if r < acc then r else acc)
  else 
    1.0
  println("Minimum distance to obstacle: " + minReading.toString)
  
  if minReading < 0.3 then
    println("⚠️  OBSTACLE DETECTED - Robot will turn away from obstacle!")
  else
    println("✅ No obstacles nearby - Robot will perform random walk")
  
  // Get behavior output
  val behaviorOutput = RandomWalkBehavior(robot, simulationState)
  println("\nBehavior output (ActuatorConfig created)")
  
  // Apply actuator to get new robot state
  val updatedRobot = behaviorOutput.wheels match
    case Some(wheelActuator) => 
      println("Applying wheel actuator...")
      wheelActuator((0.0, 0.0), robot) // Apply with placeholder values
    case None => 
      println("No wheel actuator configured")
      robot
  
  println("\nRobot updated:")
  println("  Old position: (" + robot.position._1.toString + ", " + robot.position._2.toString + ")")
  println("  New position: (" + updatedRobot.position._1.toString + ", " + updatedRobot.position._2.toString + ")")
  println("  Old orientation: " + robot.orientation.toString + " rad")
  println("  New orientation: " + updatedRobot.orientation.toString + " rad")

def msg = "Simulation demonstrates random walk behavior with proximity sensor obstacle avoidance!"
