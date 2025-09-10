FROM openjdk:17-slim

RUN apt-get update && apt-get install -y \
    util-linux \
    procps \
    mount \
    debootstrap \
    && rm -rf /var/lib/apt/lists/*

# Create debian-fs directory with Debian bookworm (skip keyring check)
RUN mkdir -p /my-fs/debian-fs && \
    debootstrap --no-check-gpg --variant=minbase bookworm /my-fs/debian-fs http://deb.debian.org/debian

WORKDIR /app

# Copy the Java file
# -- TODO: seems to be redundent cause we mount volume later
COPY jontainer.java /app/

CMD ["/bin/bash"]
