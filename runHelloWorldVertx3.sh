#!/usr/bin/env bash

JAVA_AGENT=/opt/appDynamics/javaagent.jar
#AGENT_OPTS=-Dappdynamics.agent.applicationName=Vertx
AGENT_OPTS="$AGENT_OPTS  -Dappdynamics.agent.tierName=HelloWorldVertx"
AGENT_OPTS="$AGENT_OPTS  -Dappdynamics.agent.nodeName=HelloWordVertxNode1"

echo $JAVA_AGENT
echo $AGENT_OPTS

#java -Dappdynamics.agent.uniqueHostId=6v6 -javaagent:$JAVA_AGENT $AGENT_OPTS -jar build/libs/helloworld-1.0.0-SNAPSHOT-all.jar

java -javaagent:$JAVA_AGENT $AGENT_OPTS -jar build/libs/helloworld-1.0.0-SNAPSHOT-all.jar

