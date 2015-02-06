package degrel.ui

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

  /**
   * 単位ベクトルを求めます
   */
  def unit: Vec = {
    val n = this.norm
    Vec(this.x / n, this.y / n)
  }

  /**
   * ベクトルの長さ
   */
  def norm = math.sqrt(this.normSq)

  /**
   * ベクトルの長さの平方
   */
  def normSq = x * x + y * y

  /**
   * ベクトルの向きを変えず，長さのみを調整します
   */
  def normalize(norm: Double) = {
    val n = this.norm
    Vec(this.x * norm / n, this.y * norm / n)
  }

  /**
   * ベクトルを回転します
   */
  def rotate(rad: Double) = {
    val cos = math.cos(rad)
    val sin = math.sin(rad)
    Vec(x * cos - y * sin, x * sin + y * cos)
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
  /**
   * ゼロベクトル
   */
  def zero: Vec = Vec(0, 0)

  def apply(x: Double, y: Double) = new Vec(x, y)

  /**
   * 0 <= x <= 1, 0 <= y <= 1を満たす(x,y)ベクトルをランダムに返します
   */
  def random(): Vec = new Vec(math.random, math.random)
}

