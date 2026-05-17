# Use Ubuntu as the base image
FROM ubuntu:22.04

# Set non-interactive installation
ENV DEBIAN_FRONTEND=noninteractive

# Install essential packages
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    python3 \
    python3-pip \
    aapt \
    wget \
    git \
    unzip \
    vim 

WORKDIR /app

COPY  . /app

RUN pip3 install --no-cache-dir -r requirements.txt

RUN git clone https://github.com/Sable/android-platforms.git ~/android-platforms

RUN chmod +x verlog.sh runVerlogDemo.sh scripts/*.sh

CMD ["/bin/bash"]
