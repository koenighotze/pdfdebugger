#!/usr/bin/env bash
# when a command fails, bash exits instead of continuing with the rest of the script
set -o errexit
# make the script fail, when accessing an unset variable
set -o nounset
# pipeline command is treated as failed, even if one command in the pipeline fails
set -o pipefail
# enable debug mode, by running your script as TRACE=1
if [[ "${TRACE-0}" == "1" ]]; then set -o xtrace; fi

: "${CONTAINER_REGISTRY?'Expected env var CONTAINER_REGISTRY not set'}"
: "${GITHUB_REPOSITORY?'Expected env var GITHUB_REPOSITORY not set'}"
: "${GITHUB_REF?'Expected env var GITHUB_REF not set'}"
: "${IMAGE_NAME?'Expected env var IMAGE_NAME not set'}"
: "${IMAGE_TAR?'Expected env var IMAGE_TAR not set'}"
: "${GITHUB_SHA?'Expected env var GITHUB_SHA not set'}"
: "${DOCKER_USERNAME?'Expected env var DOCKER_USERNAME not set'}"
: "${DOCKER_PASSWORD?'Expected env var DOCKER_PASSWORD not set'}"

if [[ "$GITHUB_REF" = refs/tags/* ]]; then
    GIT_TAG=${GITHUB_REF/refs\/tags\/}
    echo "Building for tag $GIT_TAG"
else
#    GIT_TAG="${GITHUB_SHA}"
    echo "Skip push for non-tag ref $GITHUB_REF"
    exit 0
fi

echo "Pushing image ${IMAGE_NAME} contained in ${IMAGE_TAR} to ${CONTAINER_REGISTRY}/${GITHUB_REPOSITORY}:${GIT_TAG}"

echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin

docker load --input "$IMAGE_TAR"
docker tag "$IMAGE_NAME" "$CONTAINER_REGISTRY/$GITHUB_REPOSITORY:$GIT_TAG"
docker push "$CONTAINER_REGISTRY/$GITHUB_REPOSITORY:$GIT_TAG"
