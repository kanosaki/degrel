package degrel.core.utils

import degrel.core._

import scala.collection.mutable

class PrettyPrinter(val root: Vertex) {
  val terminalPrinter = new TerminalPrinter()
  private val printerCache = new mutable.HashMap[Vertex, Printer]()

  def singleLine: String = {
    val sb = new StringBuilder()
    implicit val traj = new Trajectory()
    val printer = getPrinter(root)
    printer.scan()(new Trajectory())
    printer.single(sb)
    sb.toString()
  }

  def multiLine: String = {
    ???
  }

  // Flyweight
  protected def getPrinter(root: Vertex): Printer = {
    printerCache.getOrElseUpdate(root,
      root.label match {
        case Label.V.cell => new CellPrinter(root)
        case Label.V.reference => new RefPrinter(root)
        case Label.V.rule => new RulePrinter(root)
        case _ => new VertexPrinter(root)
      })
  }

  def repsep[T](src: Iterable[T],
                sb: StringBuilder,
                sep: String,
                eachFunc: (T, StringBuilder) => Unit): Unit = {
    val srcSeq = src.toSeq
    if (srcSeq.isEmpty) return
    srcSeq.dropRight(1).foreach(v => {
      eachFunc(v, sb)
      sb ++= sep
    })
    val lst = srcSeq.last
    eachFunc(lst, sb)
  }

  protected trait Printer {
    def single(sb: StringBuilder)(implicit traj: Trajectory): Unit

    def root: Vertex

    def children = {
      root.edges().map(e => getPrinter(e.dst))
    }

    var isCycled = false

    def scan()(implicit traj: Trajectory): Unit = {
      traj.walk(root) {
        case Unvisited(_) => {
          children.foreach(_.scan())
        }
        case Visited(_) => {
          this.isCycled = true
        }
      }
    }

    protected def idExpr = {
      if (this.isCycled)
        s"[${root.id}]"
      else
        ""
    }

    protected def labelExpr = {
      root.label.expr
    }
  }

  protected class VertexPrinter(val root: Vertex) extends Printer {
    private def edgesExprSingle(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      val edges = root.edges().toSeq
      if (edges.nonEmpty) {
        sb += '('
        repsep[Edge](edges, sb, ",", (e, sb_) => {
          sb ++= s"${e.label.expr}: "
          getPrinter(e.dst).single(sb_)
        })
        sb += ')'
      }
      // else => do nothing
    }

    override def single(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      val v = root
      traj.walk(v) {
        case Unvisited(trj) => {
          sb ++= s"${this.labelExpr}${this.idExpr}"
          edgesExprSingle(sb)
        }
        case Visited(trj) => {
          sb ++= s"<${this.labelExpr}[${v.id}]>"
        }
      }
    }

  }

  protected class CellPrinter(val root: Vertex) extends Printer {
    assert(root.label == Label.V.cell)

    override def single(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      val v = root
      traj.walk(v) {
        case Unvisited(trj) => {
          sb ++= "{"
          repsep[Edge](v.edges(Label.E.cellItem), sb, ";", (e, sb_) => {
            getPrinter(e.dst).single(sb_)
          })
          val ruleEdges = v.edges(Label.E.cellRule)
          if(ruleEdges.nonEmpty) {
            sb ++= ";"
            repsep[Edge](ruleEdges, sb, ";", (e, sb_) => {
              getPrinter(e.dst).single(sb_)
            })
          }
          sb ++= s"}${this.idExpr}"
        }
        case Visited(_) => {
          sb ++= s"{..}[${v.id}]"
        }
      }
    }
  }

  protected class RulePrinter(val root: Vertex) extends Printer {
    assert(root.label == Label.V.rule)

    override def single(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      val v = root
      traj.walk(v) {
        case Unvisited(_) => {
          val lhsRoot = v.thruSingle(Label.E.lhs)
          val rhsRoot = v.thruSingle(Label.E.rhs)
          getPrinter(lhsRoot).single(sb)
          sb ++= " -> "
          getPrinter(rhsRoot).single(sb)
        }
      }
    }
  }

  protected class RefPrinter(val root: Vertex) extends Printer {
    assert(root.label == Label.V.reference)
    val refTargetPrinter = getPrinter(root.thruSingle(Label.E.ref))

    override def single(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      val v = root
      traj.walk(v) {
        case Unvisited(trj) => {
          val next = v.thruSingle(Label.E.ref)
          getPrinter(next).single(sb)
        }
        case Visited(_) => {
          // Do nothing?
        }
      }
    }
  }

  protected class TerminalPrinter extends Printer {
    override def single(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      // do nothing
    }

    override def root: Vertex = null
  }

}
