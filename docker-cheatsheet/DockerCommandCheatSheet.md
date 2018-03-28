- edited on: https://jbe.github.io

- Intention of this file: To write down key points during my experience with Docker 

- Architecture
	- Docker uses a layer file system
	- it also employs a copy on write strategy

- in the docker container, if the process with pid:1 goes down, the container will be stopped. 

##### Containers
- create a container
```bash
	#This command creates a container and run certain commands in the container
	#-i means pipe the standard input into the container
    #-t means creating a sudo-terminal
    #-d means detached mode
    #--name means giving the container with a spcific name
	docker run -i -t --name containerName ubuntu:14.04 /bin/bash
```
- start an existing container
```bash
	docker start <container name/id>
```
- stop a container
```bash
	docker stop <container name/id>
```
- remove a stopped container
```bash
	docker rm <container name/id>
```
- run a command in a **running** container
```bash
	docker exec -it ubuntu:14.04 /bin/bash
```
- list containers
```bash
	#eg. list active ones
	docker ps
	#eg. list active and stopped ones
	docker ps -a
```

##### Images
- list images
```bash
	docker images
```
- build images(1):use commit
```bash
	docker commit [options] [container ID] [repository:tag] /bin/bash
    #eg.
    #if not provided, tag will be `latest`
    docker commit a74b933e3f5c david/someImageName:1.0
```
- build images(2):use Dockerfile
```bash
	FROM ubuntu:14.04 #this is the base image
    
    #run means running a shell command
    #-y for saying yes to questions
	RUN apt-get install -y curl
    
    #Mount a volume only on the container
    VOLUME path1 path2
    #or
    VOLUME ["path1", "path2"]
    
    #open ports
    EXPOSE port1 [por2]
    
    #CMD defines a default command to run when building a container
    #CMD can only be specify once
    #CMD can be overriden
    #shell format
    CMD ping 127.0.0.1 -c 30
    #Exec format
    CMD ["ping","127.0.0.1","-c","30"]
    
    ＃Alternatively, one can use entrypoint
    #entrypoint cannot be overriden
    ENTRYPOINT ["ping"]
```
- remove images
```bash
    docker rmi <image identifier>
```
- tag an image
```bash
    docker tag <image identifier> <repo>:tag
```

####Volumes
- mount a path on the container to be persistent data directory
- the data will be independent of the container status
	- it is actually mount to a secret folder on the docker VM if not specified external port
	- it will be reinitialized everytime when a new container is created
	- it will not persist if the image is updated

```bash
	#-v means mounting volume
    docker run -it -v /myvolume nginx:1.0 bash
    #mount to an external directory
    docker run -it -v /hostPath:/containerPath nginx:1.0 bash
```

####Networking
- link two containers
```bash
    docker run -d --name containerToBeLink postgre
    #can connect to containerToBeLink by URL
    docker run -it --name myContainer \ 
    				--link containerToBeLink:URL \ 
                    nginx:1.0 bash
```
- mapping ports
```bash
	#-p requires explicitly list out ports(container also needs to open ports)
    #map 8080 on localhost to port 80 on the container
    docker run -d -p 8080:80 nginx:1.0
    #-P assign a port randomly
    docker run -d -P nginx:1.0
```

 

####Working with Docker Hub
login to docker
```bash
    docker login
```
push image to docker
```bash
	#make sure the name exists in your remote repo
    docker push <image-name:tag>
```

