#!/usr/bin/env sh

set -e

curl -XPOST localhost:40001/tasks/idp-metadata-refresh
