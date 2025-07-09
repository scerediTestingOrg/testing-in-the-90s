package io.github.sceredi.template.model

final case class Position(x: Double, y: Double):
  override def toString: String = "Position(x=" + x.toString + ", y=" + y.toString + ")"

final case class Angle(degrees: Double):
  override def toString: String = "Angle(degrees=" + degrees.toString + ")"

final case class SimulationState(): // Placeholder for full state
  override def toString: String = "SimulationState()"

final case class RobotState(
  position: Position,
  orientation: Angle
):
  override def toString: String = "RobotState(position=" + position.toString + ", orientation=" + orientation.toString + ")"


trait Sensor[Out]:
  def id: String
  def sense(state: SimulationState, self: RobotState): Out

trait Actuator[In]:
  val input: In
  def act(self: RobotState): RobotState

final case class ProximitySensor(id: String) extends Sensor[Double]:
  def sense(state: SimulationState, self: RobotState): Double =
    0.5 // mock distance

final case class LightSensor(id: String) extends Sensor[Double]:
  def sense(state: SimulationState, self: RobotState): Double =
    0.8 // mock intensity

final case class Wheels(val input: (Double, Double)) extends Actuator[(Double, Double)]:
  def act(self: RobotState): RobotState =
    // update position based on input
    self.copy(position = Position(self.position.x + input._1, self.position.y + input._2))

type SensorReading = Double
type ActuatorCommand = Double

trait Behavior[
  ProxSensors <: Tuple,
  LightSensors <: Tuple,
  Actuators <: Tuple
]:
  def decide(
    proximity: ProxSensors,
    light: LightSensors
  ): Actuators


object Behavior:
  def withHardware[
    ProxSensors <: Tuple,
    LightSensors <: Tuple,
    Actuators <: Tuple
  ] =
    new BehaviorBuilder[ProxSensors, LightSensors, Actuators]

class BehaviorBuilder[
  ProxSensors <: Tuple,
  LightSensors <: Tuple,
  Actuators <: Tuple
]:
  def define(
    f: (ProxSensors, LightSensors) => Actuators
  ): Behavior[ProxSensors, LightSensors, Actuators] =
    new Behavior:
      def decide(prox: ProxSensors, light: LightSensors) = f(prox, light)


// Proximity sensors: front-left to front-right
type ProxConfig = (
  ProximitySensor, ProximitySensor, ProximitySensor, ProximitySensor,
  ProximitySensor, ProximitySensor, ProximitySensor, ProximitySensor
)

type LightConfig = (
  LightSensor, LightSensor, LightSensor, LightSensor
)

type ActuatorConfig = Tuple1[Wheels]

val simpleAvoidanceBehavior = Behavior
  .withHardware[ProxConfig, LightConfig, ActuatorConfig]
  .define { (_, _) =>
    // For now, just return the wheels - behavior logic will be in main
    Tuple1(Wheels((0.5, 0.2)))
  }

object BehaviorMain:
  @main def main(): Unit = 
    val robotState = RobotState(Position(0, 0), Angle(0))
    val simState = SimulationState()

    // Define hardware (mock sensors and actuators)
    val proxSensors: ProxConfig = (
      ProximitySensor("p0"), ProximitySensor("p1"), ProximitySensor("p2"), ProximitySensor("p3"),
      ProximitySensor("p4"), ProximitySensor("p5"), ProximitySensor("p6"), ProximitySensor("p7")
    )

    val lightSensors: LightConfig = (
      LightSensor("l0"), LightSensor("l1"), LightSensor("l2"), LightSensor("l3")
    )

    // Apply behavior to get actuator instances
    val outputs = simpleAvoidanceBehavior.decide(proxSensors, lightSensors)

    // Get sensor readings for demonstration
    val frontLeftReading = proxSensors(1).sense(simState, robotState)
    val frontRightReading = proxSensors(6).sense(simState, robotState)
    
    // Calculate wheel speeds
    val leftWheelSpeed = 1.0 - frontLeftReading
    val rightWheelSpeed = 1.0 - frontRightReading

    // Apply actuators
    val wheels = outputs._1
    val newRobotState1 = wheels.act(robotState)

    println("Initial state: " + robotState.toString)
    println("Front left sensor: " + frontLeftReading.toString)
    println("Front right sensor: " + frontRightReading.toString)
    println("Left wheel speed: " + leftWheelSpeed.toString)
    println("Right wheel speed: " + rightWheelSpeed.toString)
    println("Final state: " + newRobotState1.toString)
