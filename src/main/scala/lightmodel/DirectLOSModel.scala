package lightmodel

import scala.annotation.tailrec

import domain.*

/** DirectModel con lineOfSight (Bresenham). */
case class DirectLOSModel(
    lights: Seq[Light] = Nil,
    grid: Option[Grid] = None,
    blocks: Set[Cell] = Set.empty, // muri + robot opachi
) extends LightModel:

  /** Aggiorno luci, grid e (ostacoli + robot). */
  override def step(lights: Seq[Light], grid: Grid, robots: Seq[Robot]): LightModel =
    copy(
      lights = lights,
      grid = Some(grid),
      blocks = grid.obstacles ++ robots.map(_.pos),
    )

  /** Intensità attenuata, formula: 1/(1 + k d²) --> solo se la cella è in LOS. */
  override def intensityAt(c: Cell): Double =
    grid match
      case Some(_) =>
        def lineOfSight(from: Cell, to: Cell): Boolean =
          val dx = (to.x - from.x).abs
          val dy = (to.y - from.y).abs
          val sx = if from.x < to.x then 1 else -1
          val sy = if from.y < to.y then 1 else -1

          @tailrec
          def loop(x0: Int, y0: Int, err: Int): Boolean =
            if x0 == to.x && y0 == to.y then true
            else
              val e2 = 2 * err
              val (nx, ny, nerr) =
                if e2 > -dy then (x0 + sx, y0, err - dy)
                else (x0, y0 + sy, err + dx)
              val next = Cell(nx, ny)
              if blocks.contains(next) && next != to then false
              else loop(nx, ny, nerr)

          loop(from.x, from.y, dx - dy)

        lights
          .filter(l => c.dist2(l.pos) <= l.rMax * l.rMax)
          .filter(l => lineOfSight(l.pos, c))
          .map(l => l.intensity / (1 + l.attenuation * c.dist2(l.pos)))
          .sum

      case None => 0.0
end DirectLOSModel
