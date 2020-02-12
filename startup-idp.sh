#!/usr/bin/env bash

set -e

# for quick testing use an in-memory db
export DB_URI="jdbc:h2:mem:stubidpdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
# configure the host that is externally visible for users here - note no path
export EXPECTED_DESTINATION="http://localhost:50140"
export SECURE_COOKIES="false"

java -jar stub-idp-*.jar server configuration/stub-idp.yml
