package degrel.visualize

// TODO: ちょうどいい行列ライブラリがあったら置き換え
class Vec(var x: Double, var y: Double) {
  def +(o: Vec) = Vec(this.x + o.x, this.y + o.y)

  def +(r: Double) = Vec(this.x + r, this.y + r)

  def -(o: Vec) = Vec(this.x - o.x, this.y - o.y)

  def -(r: Double) = Vec(this.x - r, this.y - r)

  def *(o: Vec) = Vec(this.x * o.x, this.y * o.y)

  def *(r: Double) = Vec(this.x * r, this.y * r)

  def /(o: Vec) = Vec(this.x / o.x, this.y / o.y)

  def /(r: Double) = Vec(this.x / r, this.y / r)

  def unit: Vec = {
    val n = this.norm
    Vec(this.x / n, this.y / n)
  }

  def normalize(norm: Double) = {
    val n = this.norm
    Vec(this.x * norm / n, this.y * norm / n)
  }

  def +=(o: Vec) = {
    x += o.x
    y += o.y
  }

  def -=(o: Vec) = {
    x -= o.x
    y -= o.y
  }

  def *=(o: Vec) = {
    x *= o.x
    y *= o.y
  }

  def /=(o: Vec) = {
    x /= o.x
    y /= o.y
  }

  def norm = math.sqrt(this.normSq)

  def normSq = x * x + y * y

  override def equals(o: Any) = o match {
    case v: Vec => this.x == v.x && this.y == v.y
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(x, y)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 127 * a + b)
  }

  override def toString(): String = {
    s"($x,$y)"
  }
}

object Vec {
  def zero: Vec = Vec(0, 0)

  def random(): Vec = new Vec(math.random, math.random)

  def apply(x: Double, y: Double) = new Vec(x, y)
}

