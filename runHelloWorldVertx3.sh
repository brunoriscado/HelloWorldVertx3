#!/usr/bin/env bash

JAVA_AGENT=~/AppDynamics/Agents/4.2/AppServerAgent-4.2.1.0/javaagent.jar
AGENT_OPTS=-Dappdynamics.agent.applicationName=Vertx
AGENT_OPTS="$AGENT_OPTS  -Dappdynamics.agent.tierName=HelloWordVertx3"
AGENT_OPTS="$AGENT_OPTS  -Dappdynamics.agent.nodeName=HelloWordVertxNode1"

echo $JAVA_AGENT
echo $AGENT_OPTS

java -javaagent:$JAVA_AGENT $AGENT_OPTS -jar build/libs/helloworld-1.0.0-SNAPSHOT-all.jar

