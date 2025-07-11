package io.github.sceredi.template.model

import scala.util.Random

type Position = (Double, Double)
type Orientation = Double

type ProximityReading = Double
type LightReading = Double

type ActuatorValue[T] = T

type WheelsValue = (Double, Double)

trait Robot:
  val sensorConfig: SensorConfig
  val actuatorConfig: ActuatorConfig
  val position: Position
  val orientation: Orientation

type SensorReading[T] = T

trait SimulationState:
  val robots: Seq[Robot]

trait Sensor[T]:
  val orientation: Orientation
  val sense: (SimulationState, Robot) => SensorReading[T]

type Actuator[T] = (ActuatorValue[T], Robot) => Robot

trait SensorConfig:
  val proximitySensors: Option[Seq[Sensor[ProximityReading]]]
  val lightSensors: Option[Seq[Sensor[LightReading]]]

trait ActuatorConfig:
  val wheels: Option[Actuator[WheelsValue]]

type Behavior = (Robot, SimulationState) => ActuatorConfig

// Simple implementations

/** A simple robot implementation with basic sensors and actuators */
final case class SimpleRobot(
    sensorConfig: SensorConfig,
    actuatorConfig: ActuatorConfig,
    position: Position,
    orientation: Orientation,
) extends Robot

/** Simple sensor configuration with proximity sensors */
final case class SimpleSensorConfig(
    proximitySensors: Option[Seq[Sensor[ProximityReading]]],
    lightSensors: Option[Seq[Sensor[LightReading]]],
) extends SensorConfig

/** Simple actuator configuration with wheels */
final case class SimpleActuatorConfig(
    wheels: Option[Actuator[WheelsValue]],
) extends ActuatorConfig

/** Simple simulation state containing robots */
final case class SimpleSimulationState(
    robots: Seq[Robot],
) extends SimulationState

/** A simple proximity sensor implementation */
class SimpleProximitySensor(val orientation: Orientation, maxRange: Double) extends Sensor[ProximityReading]:

  val sense: (SimulationState, Robot) => SensorReading[ProximityReading] = (state, robot) =>
    // For simplicity, check distance to other robots
    val distances = state.robots
      .filter(otherRobot => otherRobot.position._1 != robot.position._1 || otherRobot.position._2 != robot.position._2)
      .map { otherRobot =>
        val dx = otherRobot.position._1 - robot.position._1
        val dy = otherRobot.position._2 - robot.position._2
        math.sqrt(dx * dx + dy * dy)
      }

    // Return the minimum distance, or maxRange if no obstacles
    if distances.nonEmpty then distances.foldLeft(maxRange)((acc, dist) => if dist < acc then dist else acc)
    else maxRange

/** Simple wheels actuator that moves the robot */
object SimpleWheelsActuator:

  def apply(wheelValues: ActuatorValue[WheelsValue], robot: Robot): Robot =
    val (leftWheel, rightWheel) = wheelValues
    val speed = (leftWheel + rightWheel) / 2.0
    val angularVelocity = (rightWheel - leftWheel) / 2.0 // Simplified differential drive

    // Update position and orientation
    val newOrientation = robot.orientation + angularVelocity * 0.1 // dt = 0.1
    val newX = robot.position._1 + speed * math.cos(newOrientation) * 0.1
    val newY = robot.position._2 + speed * math.sin(newOrientation) * 0.1

    robot match
      case r: SimpleRobot =>
        r.copy(
          position = (newX, newY),
          orientation = newOrientation,
        )
      case _ => robot // Fallback for other robot types

/** Random walk behavior with obstacle avoidance */
object RandomWalkBehavior:
  private val random = Random()
  private val baseSpeed = 0.5
  private val obstacleThreshold = 0.3

  def apply(robot: Robot, state: SimulationState): ActuatorConfig =
    // Read proximity sensors if available
    val proximityReadings = robot.sensorConfig.proximitySensors match
      case Some(sensors) => sensors.map(_.sense(state, robot))
      case None => Seq.empty[ProximityReading]

    // Check if any sensor detects a close obstacle
    val obstacleDetected = proximityReadings.exists(_ < obstacleThreshold)

    val wheelValues = if obstacleDetected then
      // Stop or turn away from obstacle
      val turnDirection = if random.nextBoolean() then 1.0 else -1.0
      (baseSpeed * -turnDirection, baseSpeed * turnDirection)
    else
      // Random walk: add some randomness to movement
      val randomFactor = 0.3
      val leftRandom = (random.nextGaussian() * randomFactor).max(-1.0).min(1.0)
      val rightRandom = (random.nextGaussian() * randomFactor).max(-1.0).min(1.0)
      (baseSpeed + leftRandom, baseSpeed + rightRandom)

    // Return actuator configuration with wheel values that will be used by the actuator
    SimpleActuatorConfig(
      wheels = Some((_, r) => SimpleWheelsActuator(wheelValues, r)),
    )
  end apply
end RandomWalkBehavior
