[![Build Status](https://travis-ci.org/HSLdevcom/transitlog-passenger-count.svg?branch=master)](https://travis-ci.org/HSLdevcom/Transitlog-passenger-count)

# Transitlog-passenger-count

## Description

Application for listening to passenger count data from mqtt and storing it in a database.

## Building

### Dependencies

This project depends on [transitdata-common](https://github.com/HSLdevcom/transitdata-common) project.

### Locally

### Docker image

- Run [this script](build-image.sh) to build the Docker image

## Running

Requirements:
- Local Pulsar Cluster
  - By default uses localhost, override host in PULSAR_HOST if needed.
    - Tip: f.ex if running inside Docker in OSX set `PULSAR_HOST=host.docker.internal` to connect to the parent machine
  - You can use [this script](https://github.com/HSLdevcom/transitdata/blob/master/bin/pulsar/pulsar-up.sh) to launch it as Docker container

Launch Docker container with

```docker-compose -f compose-config-file.yml up <service-name>```   
