#!/usr/bin/env bash

set -e

# configure the host that is externally visible for users here - note no path
export EXPECTED_DESTINATION="http://localhost:40000"

java -jar stub-sp-*.jar server configuration/stub-sp.yml
