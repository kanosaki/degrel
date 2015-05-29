package degrel.control

import java.nio.file.Paths

import degrel.control.console.ConsoleHandle
import degrel.engine.Chassis
import degrel.misc.benchmark.FilesBenchmark
import jline.console.ConsoleReader


sealed trait CLICommand {
  def start(arg: CLIArguments): Unit
}

/**
 * サブコマンド一覧
 */
object CLICommand {

  /**
   * ベンチマークを実行します
   * @param targets ベンチマークを実行するファイル一覧
   * @param outputJson 結果を出力するファイル
   */
  case class Benchmark(targets: Seq[String] = Seq(), outputJson: Option[String] = None) extends CLICommand {
    override def start(arg: CLIArguments): Unit = {
      val bench = new FilesBenchmark(targets.map(Paths.get(_)), outputJson.map(Paths.get(_)))
      bench.start()
    }
  }

  /**
   * 入力を構文解析してASTを表示するだけのREPL
   */
  case object Parse extends CLICommand {
    override def start(arg: CLIArguments): Unit = {
      val console = new ConsoleReader()
      while (true) {
        val line = console.readLine(s"parse> ")
        if (line != null) {
          try {
            val ast = degrel.front.Parser.vertex(line)
            println(ast)
          } catch {
            case ex: Throwable => {
              System.err.println(s"Message: ${ex.getMessage}")
              ex.printStackTrace()
            }
          }
        } else {
          return
        }
      }
    }
  }

  /**
   * サブコマンドが指定されない場合
   * ファイルが引数に存在する場合はインタプリタ，しない場合はREPLを起動します
   */
  case object Plain extends CLICommand {
    override def start(arg: CLIArguments): Unit = {
      arg.script match {
        case Some(scriptFile) => {
          val interpreter = new Interpreter(
            mainFile = scriptFile)
          interpreter.start()
        }
        case None => {
          val console = new ConsoleHandle(Chassis.createWithMain())
          console.start()
        }
      }
    }
  }

}

