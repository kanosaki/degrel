package degrel.control

import java.io.File

import degrel.core.{Cell, Label}
import degrel.engine.namespace.Repository
import degrel.engine.{Chassis, DriverFactory, RootHashDriverFactory}
import org.apache.commons.io.FileUtils

// TODO: DI?
/**
 * Chassisをアプリケーション引数などから初期化します
 */
class Bootstrapper(val args: BootArguments) {

  val driverFactory: DriverFactory = {
    args.rewriteeSetName match {
      case "root_hash" => new RootHashDriverFactory()
      case "plain" => {
        DriverFactory.default
      }

    }
  }

  protected def loadMain(mainFile: File): Cell = {
    val src = FileUtils.readFileToString(mainFile)
    degrel.parseCell(src)
  }

  def createChassis(): Chassis = {
    args.script match {
      case Some(f) => {
        this.createChassis(this.loadMain(f))
      }
      case None => {
        this.createChassis(Cell())
      }
    }
  }

  def createChassis(mainFile: File): Chassis = {
    this.createChassis(this.loadMain(mainFile))
  }

  def createChassis(main: Cell): Chassis = {
    val chassis = new Chassis(new Repository(), this.driverFactory)
    chassis.registerCell(Label.N.main, main)
    this.initChassis(chassis)
    chassis
  }

  protected def initChassis(chassis: Chassis): Unit = {
    chassis.verbose = args.verbose
  }

  def initInterpreter(): Interpreter = {
    new Interpreter(this.createChassis())
  }
}


object Bootstrapper {
  def apply(args: BootArguments): Bootstrapper = {
    new Bootstrapper(args)
  }
}