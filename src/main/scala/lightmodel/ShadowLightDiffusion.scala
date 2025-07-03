package lightmodel

import domain.*
import squidpony.squidgrid.FOV

/**
 * Mappa diffusione luce (double) per API di squidpony.
 */
type LightMap = Array[Array[Double]]

def normalize(map: LightMap): LightMap =
  val maxVal = map.flatMap(_.toSeq).maxOption.getOrElse(0.0)
  if maxVal == 0.0 then map.map(_.clone) // ritorna una copia per evitare side effects
  else map.map(_.map(_ / maxVal))

case class ShadowLightDiffusion(
    width: Int,
    height: Int,
    lightMap: LightMap,
) extends LightModel:

  override def step(light: Seq[Light], grid: Grid, robots: Seq[Robot]): LightModel =
    val resistance = computeResistance(grid, robots)

    // Per ogni luce, calcola la mappa di diffusione e somma le mappe
    val combinedMap = light
      .map(l => calcLightDiffusionMap(l, resistance))
      .foldLeft(Array.fill(width, height)(0.0)) { (acc, lightMap) =>
        Array.tabulate(width, height)((x, y) => acc(x)(y) + lightMap(x)(y))
      }

    // Normalizzo la mappa combinata
    val normalizeMap = normalize(combinedMap)

    // provo normalizzazione
    //    println("Combined Light Map:")
    //    for (x <- 0 until width; y <- 0 until height) do
    //      println(s"Cell($x, $y): ${normalizeMap(combinedMap)(x)(y)}")

    ShadowLightDiffusion(width, height, normalizeMap)

  /**
   * Calcola matrice di resistenza per le celle della griglia. resistenza=1.0, altrimenti 0.0.
   */
  private def computeResistance(grid: Grid, robots: Seq[Robot]): LightMap =
    Array.tabulate(width, height) { (x, y) =>
      val pos = Cell(x, y)
      if grid.obstacles.contains(pos) || !robots.exists(_.pos == pos) then 0.0 // cella ostacolata, resistenza zero
      else 1.0 // cella libera, resistenza piena
    }

  /**
   * Calcola la mappa di diffusione della luce per una luce specifica. Diffonde la luce in base alla sua posizione,
   * intensità e raggio massimo.
   */
  private def calcLightDiffusionMap(light: Light, resistance: LightMap): LightMap =
    val buf = Array.fill(width, height)(0.0)
    FOV.reuseFOV(resistance, buf, light.pos.x, light.pos.y, light.rMax)
    Array.tabulate(width, height)((x, y) => light.intensity * buf(x)(y))

  override def intensityAt(c: Cell): Double = lightMap(c.x)(c.y)

end ShadowLightDiffusion

object ShadowLightDiffusion:

  /** Factory x inizializzare la mappa vuota. */
  def apply(w: Int, h: Int): ShadowLightDiffusion =
    val emptyMap = Array.fill(w, h)(0.0)
    new ShadowLightDiffusion(w, h, emptyMap)
