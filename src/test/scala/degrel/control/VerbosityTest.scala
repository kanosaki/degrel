package degrel.control

import org.scalatest.FlatSpec

class VerbosityTest extends FlatSpec {
  it should "commonsensible" in {
    assert(Verbosity.Error > Verbosity.Warning)
    assert(Verbosity.Warning > Verbosity.Info)
    assert(Verbosity.Info > Verbosity.Debug)
  }
}

