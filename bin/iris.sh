#!/bin/sh

java -Xmx1024m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n -jar lib/iris-client-@@VERSION@@.jar iris-client.properties
