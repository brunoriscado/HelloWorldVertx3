#!/usr/bin/env bash

JAVA_AGENT=/opt/appDynamics/javaagent.jar
#AGENT_OPTS="-Dappdynamics.agent.applicationName=Vertx"
JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006"
AGENT_OPTS="-Dappdynamics.agent.tierName=HelloWorldVertx"
AGENT_OPTS="$AGENT_OPTS  -Dappdynamics.agent.nodeName=HelloWorldVertxNode1"

echo $JAVA_AGENT
echo $AGENT_OPTS

java -javaagent:$JAVA_AGENT $AGENT_OPTS $JAVA_OPTS -jar build/libs/helloworld-1.0.0-SNAPSHOT-all.jar

