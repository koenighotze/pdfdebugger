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
: "${GITHUB_REF?'Expected env var GITHUB_REF not set'}"
: "${GITHUB_SERVER_URL?'Expected env var GITHUB_SERVER_URL not set'}"
: "${GITHUB_REPOSITORY?'Expected env var GITHUB_REPOSITORY not set'}"
: "${CONTAINER_REGISTRY?'Expected env var CONTAINER_REGISTRY not set'}"
: "${DOCKER_USERNAME?'Expected env var DOCKER_USERNAME not set'}"
: "${DOCKER_PASSWORD?'Expected env var DOCKER_PASSWORD not set'}"
: "${CONTAINER_PORTS:=8080}"
: "${OUTPUT_MODE:=}"

IMAGE_NAME="$CONTAINER_REGISTRY/$GITHUB_REPOSITORY:$GITHUB_SHA"

echo "Building image $IMAGE_NAME"

if [[ "$GITHUB_REF" = refs/tags/* ]]; then
    GIT_TAG=${GITHUB_REF/refs\/tags\/}
    echo "Building for tag $GIT_TAG"

    echo "git-tag=$(echo "$GIT_TAG" | tr . -)" >> "$GITHUB_ENV"
else
    echo "git-tag=main-latest" >> "$GITHUB_ENV"
fi

DOCKER_BUILD_OPTIONS=""
if [[ -n "${GIT_TAG:=}" ]]; then
    DOCKER_BUILD_OPTIONS="--tag=$CONTAINER_REGISTRY/$GITHUB_REPOSITORY:$GIT_TAG"
fi
echo "$DOCKER_BUILD_OPTIONS"

#<org.opencontainers.image.revision>123</org.opencontainers.image.revision>

./mvnw \
  --no-transfer-progress \
  package \
  "jib:build${OUTPUT_MODE}" \
  -Dorg.opencontainers.image.revision="${GITHUB_SHA}" \
  -Ddocker.tag="${GITHUB_REF#refs/tags/}" \
  -Djib.to.auth.username="${DOCKER_USERNAME}" \
  -Djib.to.auth.password="${DOCKER_PASSWORD}"

if [[ "$GITHUB_REF" = refs/tags/* ]]; then
    # shellcheck disable=SC2086
    echo "Tagged image name is $CONTAINER_REGISTRY/$GITHUB_REPOSITORY:$GIT_TAG"
    echo "image-name=$CONTAINER_REGISTRY/$GITHUB_REPOSITORY:$GIT_TAG" >> "$GITHUB_ENV"
else
    echo "image-name=$IMAGE_NAME" >> "$GITHUB_ENV"
fi

echo "::endgroup::"