#!/bin/bash

# This script automates the Docker workflow: Build -> Tag -> Push.

# Exit immediately if a command exits with a non-zero status
set -e

# --- CONFIGURATION ---
DOCKER_USERNAME="${1:-}"
DOCKER_TOKEN="${2:-}"
SERVICES=("inventory" "payment" "gateway")
IMAGE_TAG="latest"

# --------------------------------------------------------------------------

# 1. Get Docker Username
if [ -z "$DOCKER_USERNAME" ]; then
    read -rp "Enter your Docker Hub Username: " DOCKER_USERNAME
    if [ -z "$DOCKER_USERNAME" ]; then
        echo "❌ Error: Docker Hub Username cannot be empty."
        exit 1
    fi
fi

# 2. Get Docker Token (Reads from env var first, then securely prompts)
if [ -z "$DOCKER_TOKEN" ]; then
    # -r: raw input, -s: silent (no echo) -p: prompt
    read -rsp "Enter your Docker Hub Token/Password: " DOCKER_TOKEN
    echo # Newline for clean output
    if [ -z "$DOCKER_TOKEN" ]; then
        echo "❌ Error: Docker Hub Token cannot be empty."
        exit 1
    fi
fi

# 3. Non-Interactive Login to Docker Hub using Token
echo "--- 1. Logging into Docker Hub ($DOCKER_USERNAME) ---"
# The --password-stdin flag enables non-interactive login, required for automation.
echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USERNAME" --password-stdin

# 4. Build Images Locally using Docker Compose
echo "--- 2. Building Images Locally ---"
# Use 'docker compose' (modern) or 'docker-compose' (legacy)
docker compose build || docker-compose build

# 5. Tag and Push Images
echo "--- 3. Tagging and Pushing Images to Docker Hub ---"
for SERVICE in "${SERVICES[@]}"; do
    LOCAL_IMAGE="${SERVICE}:${IMAGE_TAG}"
    REMOTE_IMAGE="${DOCKER_USERNAME}/${SERVICE}:${IMAGE_TAG}"

    echo "   -> Tagging ${LOCAL_IMAGE} as ${REMOTE_IMAGE}..."
    docker tag "$LOCAL_IMAGE" "$REMOTE_IMAGE"

    echo "   -> Pushing ${REMOTE_IMAGE}..."
    docker push "$REMOTE_IMAGE"

    echo "✅ Successfully pushed ${SERVICE} to Docker Hub."
done

# 6. Final Confirmation
echo "--------------------------------------------------------"
echo "✅ DOCKER BUILD & PUSH COMPLETE!"
echo "Your images are now available in the ${DOCKER_USERNAME} repository."
echo "--------------------------------------------------------"