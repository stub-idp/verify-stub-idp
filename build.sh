#!/usr/bin/env bash

export GIT_COMMIT=$(git rev-parse HEAD)
export BUILD_NUMBER=1
export BUILD_TIMESTAMP=$(date -u -Iseconds)

mvn clean test package
