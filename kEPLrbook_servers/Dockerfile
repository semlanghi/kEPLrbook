# syntax=docker/dockerfile:1

from ubuntu:latest

RUN mkdir -p /usr/share/man/man1 /usr/share/man/man2

RUN apt-get update && \
apt-get install -y curl


RUN apt-get update && \
apt-get install -y --no-install-recommends \
        openjdk-11-jre 



# Prints installed java version, just for checking
RUN java --version

WORKDIR /apps

COPY ./ ./kEPLrbookservers

WORKDIR /apps/kEPLrbookservers

RUN ./mvnw dependency:go-offline

RUN chmod +x ./startup.sh

RUN ["./mvnw", "clean"]
RUN ["./mvnw", "install"]
RUN ["./mvnw", "package"]







