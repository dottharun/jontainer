FROM openjdk:17-slim

# Install unshare and other namespace utilities
RUN apt-get update && apt-get install -y \
    util-linux \
    procps \
    && rm -rf /var/lib/apt/lists/*

# Copy the Java file
COPY jontainer.java /app/

WORKDIR /app

# Default to bash shell
CMD ["/bin/bash"]
