#!/bin/sh

java -Xms64m -Xmx256m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n -jar lib/iris-client-@@VERSION@@.jar iris-client.properties
