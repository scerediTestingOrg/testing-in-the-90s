# Robot Simulation with Random Walk Behavior

This example demonstrates a simple robot simulation system based on the types defined in `Model.scala`. The simulation implements a random walk behavior with proximity sensor-based obstacle avoidance.

## Architecture Overview

The simulation is built around several key components:

### Core Types

- **`Robot`**: Represents a robot with sensors, actuators, position, and orientation
- **`SimulationState`**: Contains all robots in the simulation
- **`Sensor[T]`**: Reads environmental data and returns sensor readings
- **`Actuator[T]`**: Takes actuator values and updates the robot state
- **`Behavior`**: Maps robot state and simulation state to desired actuator configuration

### Implementation Classes

- **`SimpleRobot`**: Basic robot implementation
- **`SimpleProximitySensor`**: Detects distance to nearest obstacles (other robots)
- **`SimpleWheelsActuator`**: Updates robot position and orientation based on wheel commands
- **`RandomWalkBehavior`**: Implements random walk with obstacle avoidance

## How a Simulation Step Works

1. **Sensor Reading**: Each robot's sensors read the current simulation state
2. **Behavior Execution**: The behavior processes sensor data and generates actuator commands
3. **Actuator Application**: Actuators update the robot's physical state
4. **State Update**: The simulation state is updated with new robot positions

## Random Walk Behavior Details

The `RandomWalkBehavior` implements the following logic:

```scala
def apply(robot: Robot, state: SimulationState): ActuatorConfig =
  // 1. Read proximity sensors
  val proximityReadings = robot.sensorConfig.proximitySensors match
    case Some(sensors) => sensors.map(_.sense(state, robot))
    case None => Seq.empty[ProximityReading]
  
  // 2. Check for obstacles
  val obstacleDetected = proximityReadings.exists(_ < obstacleThreshold)
  
  // 3. Generate wheel commands
  val wheelValues = if obstacleDetected then
    // Turn away from obstacle
    val turnDirection = if random.nextBoolean() then 1.0 else -1.0
    (baseSpeed * -turnDirection, baseSpeed * turnDirection)
  else
    // Random walk with Gaussian noise
    val randomFactor = 0.3
    val leftRandom = (random.nextGaussian() * randomFactor).max(-1.0).min(1.0)
    val rightRandom = (random.nextGaussian() * randomFactor).max(-1.0).min(1.0)
    (baseSpeed + leftRandom, baseSpeed + rightRandom)
  
  // 4. Return actuator configuration
  SimpleActuatorConfig(
    wheels = Some((_, r) => SimpleWheelsActuator(wheelValues, r))
  )
```

### Behavior Features

1. **Proximity Sensor Reading**: Reads from all available proximity sensors
2. **Obstacle Detection**: Triggers when any sensor reading is below threshold (0.3 units)
3. **Obstacle Avoidance**: When obstacle detected, robot randomly turns left or right
4. **Random Walk**: When no obstacles, adds Gaussian noise to movement for exploration
5. **Differential Drive**: Uses left/right wheel speeds to control movement and turning

## Example Output

When you run the simulation, you'll see output like:

```text
=== Robot Simulation Example ===
Initial state:
Robot 1: Position(0.0, 0.0)
Robot 2: Position(0.2, 0.0)

--- Demonstrating Random Walk Behavior ---
Proximity sensor readings: 0.2, 0.2, 0.2
Minimum distance to obstacle: 0.2
⚠️  OBSTACLE DETECTED - Robot will turn away from obstacle!

Behavior output (ActuatorConfig created)
Applying wheel actuator...

Robot updated:
  Old position: (0.0, 0.0)
  New position: (0.0, 0.0)
  Old orientation: 0.0 rad
  New orientation: 0.05 rad
```

## Key Insights

1. **Sensor Integration**: The behavior successfully reads proximity sensor data
2. **Obstacle Avoidance**: When robots are close (< 0.3 units), the behavior triggers avoidance
3. **State Updates**: Actuators correctly update robot position and orientation
4. **Modular Design**: Each component (sensors, actuators, behaviors) is independently configurable

## Running the Simulation

To run the simulation example:

```bash
sbt run
```

This will execute the `Main.scala` file which demonstrates the random walk behavior with obstacle avoidance using the implemented types and classes.

## Extending the Simulation

The type-based design makes it easy to extend:

- **New Sensors**: Implement `Sensor[T]` for light, camera, GPS, etc.
- **New Actuators**: Implement `Actuator[T]` for arms, grippers, speakers, etc.
- **New Behaviors**: Implement behaviors for line following, swarm coordination, etc.
- **Complex Environments**: Add walls, obstacles, goals to the simulation state
