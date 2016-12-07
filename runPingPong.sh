#!/usr/bin/env bash
echo "java -DCONFIG=$1 -jar build/libs/helloworld-1.0.0-SNAPSHOT-all.jar cp $2 -cluster -cluster-host $3"
java -DCONFIG=$1 -jar build/libs/helloworld-1.0.0-SNAPSHOT-all.jar cp $2 -cluster -cluster-host $3

