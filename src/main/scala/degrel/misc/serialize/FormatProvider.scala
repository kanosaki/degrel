package degrel.misc.serialize

trait FormatProvider[InT, OutT] {
  def dump(in: InT): OutT

  def load(in: OutT): InT
}
