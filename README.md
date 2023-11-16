### Running the demo in a docker container (on a Linux x64/aarch64 machine)

#### Description
The crac8 demo should emulate a service that needs to load around 258 000 names from a
json file at startup. The "service" provides methods to create a number of random
names for either boys or girls. When started normally it will first load the data
from the json file into datastructures and then will create 5 random names for girls
and 5 random names for boys.
This will take around 1-2 sec depending on the machine you run it on.
Once you create a checkpoint and restore it from there, the response will only
take around 30-50 ms because the data was already loaded from the file and now
one can directly access it which leads to the fast "startup" time.


#### Build the jar file on the target platform:
To create the jar file you need to build it on the target platform (Linux x64 or aarch64) using
the a JDK that supports CRaC. You can find builds here [github.com/CRaC](https://github.com/CRaC/openjdk-builds/releases).
1. make sure you set JAVA_HOME to the JVM with CRaC support
2. go the project folder
3. run ```gradlew clean build```
4. Now you should find the the jar at ```build/libs/crac8-17.0.0-fat.jar```
5. This jar file will later be used to run on the docker container
6. Make sure to select the correct JDK in the docker file (x64 or aarch64)


#### Login into docker:
```docker login```


#### Build docker image:
```docker build -t crac8 .```


#### Commit image to dockerhub:
```docker commit crac8```


#### Run docker image without checkpoint:
```docker run -it --privileged --rm --name crac8 hansolo/crac8 java -XX:CRaCCheckpointTo=/opt/crac-files -jar /opt/app/crac8-17.0.0.jar```


#### Run docker image with checkpoint:
```docker run -it --privileged --rm --name $1 hansolo/crac8:checkpoint java -XX:CRaCRestoreFrom=/opt/crac-files```


#### 1. Start the application in a docker container
1. Open a shell window
2. Run ``` docker run -it --privileged --rm --name crac8 crac8 ```
3. In the docker container run</br>
```
cd /opt/app
java -XX:CRaCCheckpointTo=/opt/crac-files -jar crac8-17.0.0.jar keeprunning
```

</br>

#### 2. Start a 2nd shell window and create the checkpoint
1. Open a second shell window
2. Run ``` docker exec -it -u root crac8 /bin/bash ```
3. Execute ``` top ``` command and note the PID of the running java process
4. Take the PID and run ``` jcmd PID JDK.checkpoint```
5. In the first shell window the application should have created the checkpoint
6. Check the folder /opt/crac-files for the checkpoint files being present
7. In second shell window run ``` exit ``` to get back to your machine

</br>

#### 3. Commit the current state of the docker container
1. Now get the CONTAINER_ID from shell window 1 by execute ``` docker ps -a ``` in shell window 2
2. Exit the docker container in shell window 1 by executing ``` exit ```
3. Run ``` docker commit CONTAINER_ID crac8:checkpoint ``` in shell window 2

</br>

#### 4. Run the docker container from the saved state incl. the checkpoint
Now you can start the docker container from the checkpoint by executing
``` docker run -it --privileged --rm --name crac8 crac8:checkpoint java -XX:CRaCRestoreFrom=/opt/crac-files ```

</br>

#### 5. Create a shell script to restore multiple times
1. Open a shell window
2. Create a text file named ```restore_crac8.sh```
3. Add
```
#!/bin/bash

echo "docker run -it --privileged --rm --name $1 crac8:checkpoint java -XX:CRaCRestoreFrom=/opt/crac-files"

docker run -it --privileged --rm --name $1 crac8:checkpoint java -XX:CRaCRestoreFrom=/opt/crac-files
```
4. Make the script executable by executing ```chmod +x restore_crac8.sh```
5. Now you can start the docker container multiple times executing ```restore_crac8.sh NAME_OF_CONTAINER```

If you would like to start the original container without the checkpoint you can still
do that by executing the following command
```
docker run -it --privileged --rm --name crac8 crac8 java -jar /opt/app/crac8-17.0.0.jar
```