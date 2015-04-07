package degrel.control.console

import degrel.control.console.commands.ConsoleCommand

class CommandsManager(commands: Iterable[ConsoleCommand]) {
  private val comMapping: Map[String, ConsoleCommand] = {
    val tempMap = scala.collection.mutable.HashMap[String, ConsoleCommand]()
    commands.foreach { com =>
      if (com.name == null) throw new RuntimeException("Command name should not to be null.")
      if (tempMap.contains(com.name)) {
        throw new RuntimeException(s"Command name ${com.name} conflict!")
      }
      tempMap += com.name -> com

      com.shortName match {
        case Some(sn) => {
          if (tempMap.contains(sn)) {
            throw new RuntimeException(s"Command short name ${com.shortName} conflict!")
          }
          tempMap += sn -> com
        }
        case _ =>
      }
    }
    tempMap.toMap
  }

  def handle(line: String, console: ConsoleHandle) = {
    val (comName, tail) = line.indexOf(' ') match {
      case firstSpace if firstSpace > 0 => {
        (line.substring(0, firstSpace),
          line.substring(firstSpace + 1))
      }
      case _ => {
        (line.substring(0, line.length), "")
      }
    }
    comMapping.get(comName) match {
      case Some(com) => {
        com.execute(tail, console)
      }
      case None => console.println(s"Command '$comName' not found")
    }
  }
}

