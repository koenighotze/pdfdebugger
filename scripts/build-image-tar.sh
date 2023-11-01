#!/usr/bin/env bash
# when a command fails, bash exits instead of continuing with the rest of the script
set -o errexit
# make the script fail, when accessing an unset variable
set -o nounset
# pipeline command is treated as failed, even if one command in the pipeline fails
set -o pipefail
# enable debug mode, by running your script as TRACE=1
if [[ "${TRACE-0}" == "1" ]]; then set -o xtrace; fi

: "${GITHUB_SHA?'Expected env var GITHUB_SHA not set'}"
: "${DOCKER_USERNAME?'Expected env var DOCKER_USERNAME not set'}"
: "${DOCKER_PASSWORD?'Expected env var DOCKER_PASSWORD not set'}"

./mvnw \
  --no-transfer-progress \
  package \
  "jib:buildTar" \
  -Dorg.opencontainers.image.revision="${GITHUB_SHA}" \
  -Ddocker.tag="${GITHUB_SHA}" \
  -Djib.to.auth.username="${DOCKER_USERNAME}" \
  -Djib.to.auth.password="${DOCKER_PASSWORD}"

 echo "image-tar=target/jib-image.tar" >> "$GITHUB_ENV"