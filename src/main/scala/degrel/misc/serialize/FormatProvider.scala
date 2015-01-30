package degrel.misc.serialize

trait FormatProvider[InputT, InterimT] {
  def dump(in: InputT): InterimT

  def load(in: InterimT): InputT

  def dumpString(in: InputT): String

  def loadString(in: String): InputT
}
