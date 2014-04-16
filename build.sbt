name := "campfire"

version := "1.0"

resolvers += "twitter repository" at "http://maven.twttr.com/"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.29"

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.6.4"

libraryDependencies += "io.netty" % "netty-all" % "5.0.0.Alpha1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.1"

libraryDependencies += "commons-codec" % "commons-codec" % "1.9"
