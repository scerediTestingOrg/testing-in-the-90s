package domain

/** Luce con posizione, intensità iniziale, raggio massimo e coefficiente di attenuazione. * */
final case class Light(pos: Cell, intensity: Double, rMax: Int, attenuation: Double = 1.0)

/** Robot con posizione. * */
final case class Robot(pos: Cell)

/** Cella (x,y) di 1m*1m. * */
final case class Cell(x: Int, y: Int):

  /** Distanza euclidea senza sqrt. * */
  def dist2(o: Cell): Int =
    val dx = x - o.x
    val dy = y - o.y
    dx * dx + dy * dy

/** Griglia con larghezza, altezza e ostacoli. * */
final case class Grid(w: Int, h: Int, obstacles: Set[Cell]):

  /** Check cella dentro i limiti della griglia. * */
  inline def inBounds(c: Cell): Boolean =
    c.x >= 0 && c.y >= 0 && c.x < w && c.y < h
