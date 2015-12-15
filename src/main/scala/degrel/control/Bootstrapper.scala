package degrel.control

import java.io.File

import akka.actor.{ActorSystem, AddressFromURIString}
import com.typesafe.config.{Config, ConfigFactory}
import degrel.cluster.{LocalNode, ClusterInterpreter}
import degrel.control.cluster.{ClusterConsole, ControllerFacade, WorkerFacade}
import degrel.control.console.ConsoleHandle
import degrel.core.{Cell, Label}
import degrel.engine.namespace.Repository
import degrel.engine.{Chassis, DriverFactory}
import org.apache.commons.io.FileUtils

// TODO: DI?
/**
  * Chassisをアプリケーション引数などから初期化します
  */
class Bootstrapper(val args: BootArguments) {
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
    val chassis = Chassis.create(main)
    this.initChassis(chassis)
    chassis
  }

  def initConfig(): Config = {
    val primConfig = ConfigFactory.parseString(
      s"akka.cluster.seed-nodes = [${args.seeds.map(str => "\"" + str + "\"").mkString(",")}]"
    )

    args.config match {
      case Some(cfg) => primConfig.withFallback(ConfigFactory.parseFile(new File(cfg))).withFallback(ConfigFactory.load())
      case None => primConfig.withFallback(ConfigFactory.load())
    }
  }

  def createActorSystem(): ActorSystem = {
    val config = this.initConfig()
    // "default" is a default Akka's ActorSystem name
    val name = args.name.getOrElse("default")
    ActorSystem(name, config)
  }

  def createClusterController(): ControllerFacade = {
    val system = this.createActorSystem()
    val lobbyAddr = AddressFromURIString(args.seeds.head)
    ControllerFacade(system, lobbyAddr)
  }

  protected def initChassis(chassis: Chassis): Unit = {
    chassis.verbose = args.verbose
  }

  def initInterpreter(): Interpreter = {
    val chassis = this.createChassis()
    if (args.cluster) {
      val cluster = this.createClusterController()
      new ClusterInterpreter(chassis, cluster)
    } else {
      new Interpreter(chassis)
    }
  }

  def initConsole(): ConsoleHandle = {
    val chassis = this.createChassis()
    if (args.cluster) {
      val cluster = this.createClusterController()
      new ClusterConsole(chassis, cluster)
    } else {
      new ConsoleHandle(chassis)
    }
  }

  def start() = {
    args.script match {
      case Some(scriptFile) => {
        val interpreter = this.initInterpreter()
        interpreter.start()
      }
      case None => {
        val console = this.initConsole()
        console.start()
      }
    }
  }

  def startWorker() = {
    val lobbyAddr = AddressFromURIString(args.seeds.head)
    val system = this.createActorSystem()
    val facade = WorkerFacade(system, lobbyAddr)
    facade.start()
  }
}


object Bootstrapper {
  def apply(args: BootArguments): Bootstrapper = {
    new Bootstrapper(args)
  }
}