### Running the demo in a docker container (on a Linux x64 machine)

#### Description
The crac8 demo should emulate a service that needs to load around 260000 names from a 
json file at startup. The "service" provides methods to create a number of random
names for either boys or girls. When started normally it will first load the data
from the json file into datastructures and then will create 5 random names for girls
and 5 random names for boys.
This will take around 1-2 sec depending on the machine you run it on.
Once you create a checkpoint and restore it from there, the response will only
take around 30-50 ms because the data was already loaded from the file and now
one can directly access it which leads to the fast "startup" time.


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
java -XX:CRaCCheckpointTo=/opt/crac-files -jar crac8-17.0.0.jar
```
4. Note the PID of the program

</br>

#### 2. Start a 2nd shell window and create the checkpoint
1. Open a second shell window
2. Run ``` docker exec -it -u root crac8 /bin/bash ```
3. Wait until the program outputs the results
4. Take the PID from shell 1 and run ``` jcmd PID JDK.checkpoint```
5. In the first shell window the application should have created the checkpoint
6. In second shell window run ``` exit ``` to get back to your machine

</br>

#### 3. Commit the current state of the docker container
1. Now get the CONTAINER_ID from shell window 1 by execute ``` docker ps -a ``` in shell window 2
2. Run ``` docker commit CONTAINER_ID crac8:checkpoint ``` in shell window 2
3. Go back to shell window 1 and press CTRL+C to stop the running application

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