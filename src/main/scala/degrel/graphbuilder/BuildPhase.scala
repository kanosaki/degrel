package degrel.graphbuilder

trait BuildPhase {

}

object NothingDone extends BuildPhase

object MainPhase extends BuildPhase

object FinalizePhase extends BuildPhase

object Completed extends BuildPhase
