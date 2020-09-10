#!/usr/bin/env sh

set -e

curl -XPOST localhost:50141/tasks/metadata-refresh
