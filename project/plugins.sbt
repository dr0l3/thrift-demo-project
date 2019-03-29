addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.twitter" %% "scrooge-sbt-plugin" % "19.2.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.18")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.5.0")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)



resolvers += "Twitter Repository" at "http://maven.twttr.com/"
