package lightmodel

import domain.*

/** Contratto LightModel con step e intensityAt. */
trait LightModel:
  def step(lights: Seq[Light], grid: Grid, robots: Seq[Robot]): LightModel
  def intensityAt(cell: Cell): Double
