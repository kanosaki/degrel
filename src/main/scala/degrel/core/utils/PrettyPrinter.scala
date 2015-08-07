package degrel.core.utils

import degrel.core._
import degrel.utils.PrettyPrintOptions

import scala.collection.mutable

class PrettyPrinter(val root: Vertex)
                   (implicit opts: PrettyPrintOptions = PrettyPrintOptions.default) {
  val terminalPrinter = new TerminalPrinter()
  private[this] val printerCache = new mutable.HashMap[Vertex, Printer]()

  def apply(): String = {
    val sb = new StringBuilder()
    implicit val traj = new Trajectory()
    val printer = getPrinter(root, null)
    printer.scan()(new Trajectory())
    printer.print(sb)
    sb.toString()
  }

  // Flyweight
  protected def getPrinter(root: Vertex, parent: Printer): Printer = {
    printerCache.getOrElseUpdate(
      root,
      root.label match {
        case Label.V.cell => new CellPrinter(root, parent)
        case Label.V.reference => new RefPrinter(root, parent)
        case Label.V.rule => new RulePrinter(root, parent)
        case _ => {
          if (root.edges.map(_.label).toSet == Set(Label.E.lhs, Label.E.rhs)) {
            new BinOpPrinter(root, parent)
          } else {
            new VertexPrinter(root, parent)
          }
        }
      })
  }


  protected trait Printer {
    def print(sb: StringBuilder)(implicit traj: Trajectory): Unit

    def root: Vertex

    val parent: Printer

    var indentLevel: Int = if (parent != null) parent.indentLevel else 0

    def children = {
      root.edges.map(e => getPrinter(e.dst, this))
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

    protected def putTypeInfo(sb: StringBuilder): Unit = {
      sb ++= PrettyPrintUtils.shortenTypeName(this.root)
      this.root match {
        case vh: VertexHeader => {
          sb += '/'
          sb ++= PrettyPrintUtils.shortenTypeName(vh.body)
        }
        case _ =>
      }

    }

    protected def putIdExpr(sb: StringBuilder): Unit = {
      if (this.isCycled || opts.showAllId) {
        sb += '['
        sb ++= this.root.id.toString
        if (opts.showType) {
          sb += '/'
          this.putTypeInfo(sb)
        }
        sb += ']'
      } else {
        if (opts.showType) {
          sb += '['
          this.putTypeInfo(sb)
          sb += ']'
        }
      }
    }

    protected def labelExpr = {
      root.label.expr
    }

    def putNameExpr(sb: StringBuilder): Unit = {
      root.attr(Label.A.capturedAs) match {
        case Some(capAs) if root.label == Label.V.wildcard => {
          sb += '@'
          sb ++= capAs
        }
        case Some(capAs) if root.label != Label.V.wildcard => {
          sb ++= this.labelExpr
          sb += '@'
          sb ++= capAs
        }
        case _ =>
          sb ++= this.labelExpr
      }
      this.putIdExpr(sb)
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
  }

  protected class VertexPrinter(val root: Vertex, val parent: Printer) extends Printer {
    private def edgesExprSingle(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      val edges = root.edges.toSeq
      if (edges.nonEmpty) {
        sb += '('
        repsep[Edge](edges, sb, ",", (e, sb_) => {
          sb ++= s"${e.label.expr}: "
          getPrinter(e.dst, this).print(sb_)
        })
        sb += ')'
      }
      // else => do nothing
    }

    override def print(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      val v = root
      traj.walk(v) {
        case Unvisited(trj) => {
          this.putNameExpr(sb)
          edgesExprSingle(sb)
        }
        case Visited(trj) => {
          sb += '<'
          this.putNameExpr(sb)
          sb += '>'
        }
      }
    }
  }

  protected class BinOpPrinter(val root: Vertex, val parent: Printer) extends Printer {
    override def print(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      val v = root
      traj.walk(v) {
        case Unvisited(trj) => {
          val lhsPrinter = getPrinter(v.thruSingle(Label.E.lhs), this)
          val rhsPrinter = getPrinter(v.thruSingle(Label.E.rhs), this)
          lhsPrinter.print(sb)
          sb ++= " "
          sb ++= v.label.expr
          sb ++= " "
          rhsPrinter.print(sb)
        }
        case Visited(trj) => {
          ???
        }
      }
    }
  }

  protected class CellPrinter(val root: Vertex, val parent: Printer) extends Printer {
    assert(root.label == Label.V.cell)

    this.indentLevel = parent match {
      case null => 1
      case p => p.indentLevel + 1
    }

    val itemSep = opts.multiLine match {
      case true => "\n" + opts.indentItem * indentLevel
      case false => ";"
    }

    val nl = opts.multiLine match {
      case true => "\n" + opts.indentItem * indentLevel
      case false => ""
    }

    val nlEnd = opts.multiLine match {
      case true => "\n" + opts.indentItem * (indentLevel - 1)
      case false => ""
    }

    val baseEdges = root.edgesWith(Label.E.cellBase)
    val ruleEdges = root.edgesWith(Label.E.cellRule)
    val itemEdges = root.edgesWith(Label.E.cellItem)
    val othersEdges = root.edges.filter(
      l => l.label != Label.E.cellItem
        && l.label != Label.E.cellRule
        && l.label != Label.E.cellBase).toSeq

    override def print(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      val v = root
      traj.walk(v) {
        case Unvisited(trj) => {
          sb ++= "{"
          this.putIdExpr(sb)
          sb ++= nl
          var lineCount = 0
          if (baseEdges.nonEmpty) {
            repsep[Edge](baseEdges, sb, itemSep, (e, sb_) => {
              sb_ ++= e.label.expr ++= ": "
              getPrinter(e.dst, this).print(sb_)
            })
          }
          lineCount += baseEdges.size
          if (itemEdges.nonEmpty) {
            if (lineCount != 0)
              sb ++= itemSep
            repsep[Edge](itemEdges, sb, itemSep, (e, sb_) => {
              getPrinter(e.dst, this).print(sb_)
            })
          }
          lineCount += itemEdges.size
          if (ruleEdges.nonEmpty) {
            if (lineCount != 0)
              sb ++= itemSep
            repsep[Edge](ruleEdges, sb, itemSep, (e, sb_) => {
              getPrinter(e.dst, this).print(sb_)
            })
          }
          lineCount += ruleEdges.size
          if (othersEdges.nonEmpty) {
            if (lineCount != 0)
              sb ++= itemSep
            repsep[Edge](othersEdges, sb, itemSep, (e, sb_) => {
              sb_ ++= e.label.expr ++= ": "
              getPrinter(e.dst, this).print(sb_)
            })
          }
          sb ++= nlEnd ++= "}"
        }
        case Visited(_) => {
          sb += '{'
          this.putIdExpr(sb)
          sb += '}'
        }
      }
    }
  }

  protected class RulePrinter(val root: Vertex, val parent: Printer) extends Printer {
    assert(root.label == Label.V.rule)

    override def print(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      val v = root
      traj.walk(v) {
        case Unvisited(_) => {
          val lhsRoot = v.thruSingle(Label.E.lhs)
          val rhsRoot = v.thruSingle(Label.E.rhs)
          getPrinter(lhsRoot, this).print(sb)
          sb ++= " -> "
          getPrinter(rhsRoot, this).print(sb)
        }
        case Visited(trj) => {
          sb ++= "<RULE"
          this.putIdExpr(sb)
          sb += '>'
        }
      }
    }
  }

  protected class RefPrinter(val root: Vertex, val parent: Printer) extends Printer {
    assert(root.label == Label.V.reference)
    val refTargetPrinter = root.thru(Label.E.ref).toList match {
      case target :: Nil => getPrinter(target, this)
      case first :: _ => new ConstantPrinter("<!! MULTI REFERENCE EDGE !!>", this)
      case Nil => new ConstantPrinter("<???>", this)
    }

    override def print(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      val v = root
      traj.walk(v) {
        case Unvisited(trj) => {
          refTargetPrinter.print(sb)
        }
        case Visited(_) => {
          // Do nothing?
        }
      }
    }
  }

  protected class ConstantPrinter(msg: String, val parent: Printer) extends Printer {
    override def print(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      sb.append(msg)
    }

    override def root: Vertex = null
  }

  protected class TerminalPrinter extends Printer {
    val parent = null

    override def print(sb: StringBuilder)(implicit traj: Trajectory): Unit = {
      // do nothing
    }

    override def root: Vertex = null
  }

}
