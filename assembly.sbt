import AssemblyKeys._ 

assemblySettings

jarName in assembly := "degrel.jar"

mainClass in assembly := Some("degrel.Main")

test in assembly := {}
