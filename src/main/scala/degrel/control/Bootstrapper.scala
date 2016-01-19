package degrel.control

import java.io.File

import akka.actor.{ActorSystem, AddressFromURIString}
import com.typesafe.config.{Config, ConfigFactory}
import degrel.cluster.{Roles, MemberRole, ClusterInterpreter}
import degrel.control.cluster.{ClusterConsole, ControllerFacade, LobbyFacade, WorkerFacade}
import degrel.control.console.ConsoleHandle
import degrel.core.Cell
import degrel.engine.Chassis
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

  def initConfig(roles: Seq[MemberRole]): Config = {
    val primConfig = ConfigFactory.parseString(
      s"""akka.cluster.seed-nodes = [${args.seeds.map(str => "\"" + str + "\"").mkString(",")}]
         |akka.remote.netty.tcp.port = ${args.port.getOrElse(0)}
         |akka.cluster.roles = [${roles.map(_.name).mkString(",")}]
       """.stripMargin
    )
    // degrel.conf -> reference.conf
    val degrelConfig = ConfigFactory.load("degrel").withFallback(ConfigFactory.load())

    args.config match {
      case Some(cfg) => primConfig.withFallback(ConfigFactory.parseFile(new File(cfg))).withFallback(degrelConfig)
      case None => primConfig.withFallback(degrelConfig).withFallback(degrelConfig)
    }
  }

  def createActorSystem(roles: Seq[MemberRole]): ActorSystem = {
    val config = this.initConfig(roles)
    // "default" is a default Akka's ActorSystem name
    val name = args.name.getOrElse("default")
    ActorSystem(name, config)
  }

  def createClusterController(): ControllerFacade = {
    val system = this.createActorSystem(Seq(Roles.Controller))
    val lobbyAddr = AddressFromURIString(args.seeds.head)
    ControllerFacade(system, lobbyAddr)
  }

  protected def initChassis(chassis: Chassis): Unit = {
    chassis.verbose = args.verbose
  }

  def initInterpreter(): Interpreter = {
    val chassis = this.createChassis()
    if (args.seeds.nonEmpty) {
      val cluster = this.createClusterController()
      new ClusterInterpreter(chassis, cluster)
    } else {
      new Interpreter(chassis)
    }
  }

  def initConsole(): ConsoleHandle = {
    val chassis = this.createChassis()
    chassis.main.preventStop = true
    if (args.seeds.nonEmpty) {
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
    val system = this.createActorSystem(Seq(Roles.Worker))
    val facade = WorkerFacade(system, lobbyAddr)
    facade.start()
  }

  def startLobby() = {
    val system = this.createActorSystem(Seq(Roles.Lobby))
    val lobby = LobbyFacade(system)
    lobby.start()
  }
}


object Bootstrapper {
  def apply(args: BootArguments): Bootstrapper = {
    new Bootstrapper(args)
  }
}