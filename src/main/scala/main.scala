import domain.*
import lightmodel.*

object Shade:

  /** Mappa intensità a carattere ombreggiato. */
  def char(i: Double): Char =
    if i >= 0.60 then '█'
    else if i >= 0.40 then '▓'
    else if i >= 0.20 then '▒'
    else if i >= 0.05 then '░'
    else '.'

/** Calcola l'intensità massima via intensità diretta. */
def maxI(g: Grid, m: LightModel): Double =
  val maxIntensity = (
    for
      x <- 0 until g.w
      y <- 0 until g.h
    yield m.intensityAt(Cell(x, y))
  ).maxOption.getOrElse(0.0)

  math.max(maxIntensity, 1.0) // forza almeno 1 come denominatore

/** Mostra la griglia con ostacoli, luci, robot e intensità normalizzata. */
def show(title: String, g: Grid, lights: Seq[Light], robots: Seq[Robot], model: LightModel): Unit =
  println(s"\n$title\n")
  val mx = maxI(g, model)
  for y <- 0 until g.h do
    for x <- 0 until g.w do
      val c = Cell(x, y)
      val ch =
        if g.obstacles.contains(c) then '#'
        else if lights.exists(_.pos == c) then '✸'
        else if robots.exists(_.pos == c) then 'R'
        else Shade.char(model.intensityAt(c) / mx)
      print(ch)
    println()

/** Misura il tempo di esecuzione di un blocco. */
def time[R](block: => R): (R, Long) =
  val t0 = System.nanoTime()
  val result = block
  val t1 = System.nanoTime()
  (result, (t1 - t0) / 1000000) // tempo in millisecondi

@main def ShowModels(): Unit =
  val g = Grid(
    w = 20,
    h = 10,
    obstacles = Set(
      Cell(5, 2),
      Cell(5, 3),
      Cell(5, 4), // muro verticale
      Cell(10, 6),
      Cell(11, 6),
      Cell(12, 6), // muro orizzontale
      Cell(15, 1),
      Cell(15, 2),
      Cell(15, 3),
      Cell(15, 4),
      Cell(15, 5), // altro muro verticale
    ),
  )

  val robots = Seq(
    Robot(Cell(3, 3)),
    Robot(Cell(7, 8)),
    Robot(Cell(17, 2)),
  )

  val testLights = Seq(
    Light(Cell(2, 2), intensity = 200, rMax = 4), // luce intensa, raggio piccolo
    Light(Cell(10, 2), intensity = 80, rMax = 8), // luce media, raggio medio
    Light(Cell(18, 8), intensity = 30, rMax = 12), // luce debole, raggio grande
  )

  // Test DirectLOSModel
  val (directModel, timeDirect) = time:
    DirectLOSModel().step(testLights, g, robots)
  println(s"\n[DirectLOSModel computed in $timeDirect ms]")
  show("Direct LOS Model", g, testLights, robots, directModel)

  // Test ShadowLightDiffusion per ciascuna luce individualmente
  testLights.foreach { light =>
    val singleLight = Seq(light)
    val (shadowModel, timeShadow) = time:
      ShadowLightDiffusion(g.w, g.h).step(singleLight, g, robots)
    println(s"\n[ShadowLightDiffusion computed in $timeShadow ms]")
    show(
      s"Shadow Model - Light at (${light.pos.x}, ${light.pos.y}) i=${light.intensity} rMax=${light.rMax}",
      g,
      singleLight,
      robots,
      shadowModel,
    )
  }

  // Test ShadowLightDiffusion con tutte le luci insieme
  val (shadowCombined, timeShadowCombined) = time:
    ShadowLightDiffusion(g.w, g.h).step(testLights, g, robots)
  println(s"\n[ShadowLightDiffusion (combined) computed in $timeShadowCombined ms]")
  show("Shadow Model - Combined Lights", g, testLights, robots, shadowCombined)

  // Confronto tempi
  println("\nSummary:")
  println(f"Direct Model total time: $timeDirect ms")
  println(f"Shadow Model combined time: $timeShadowCombined ms")
end ShowModels
