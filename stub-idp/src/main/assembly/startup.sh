#!/usr/bin/env bash

set -e

# for quick testing use an in-memory db
export DB_URI="jdbc:h2:mem:stubidpdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
# configure the host that is externally visible for users here - note no path
export EXTERNAL_HOST="localhost"
export EXPECTED_DESTINATION="http://$EXTERNAL_HOST:50140"
export VERIFY_SUBMISSION_URL="http://$EXTERNAL_HOST:40000/stub/sp/initiate-single-idp-service"
export STUB_IDP_HOSTNAME="$EXPECTED_DESTINATION"
export SECURE_COOKIES="false"

java -jar stub-idp-*.jar server configuration/stub-idp.yml
