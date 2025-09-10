# Simple docker-like container with java

# build the docker container

```sh
docker build --tag java-linux-jontainer .
```

# start the docker container - to get linux

- note: container will be removed when the process is stopped

```sh
docker run --rm --interactive --tty --privileged --volume $(pwd):/app --name jon-linux java-linux-jontainer
```

- to get a shell to the started container
```sh
docker exec --interactive --tty jon-linux /bin/bash
```

# start jontainer inside docker:

- start the docker container and run:
```sh
java -cp "lib/*:." jontainer.java run /bin/bash
```
