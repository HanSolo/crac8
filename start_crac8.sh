#!/bin/bash

echo "docker run -it --privileged --rm --name crac8 hansolo/crac8:checkpoint java -jar /opt/app/crac8-17.0.0.jar"

docker run -it --privileged --rm --name crac8 hansolo/crac8:checkpoint java -XX:CRaCCheckpointTo=/opt/crac-files -jar /opt/app/crac8-17.0.0.jar