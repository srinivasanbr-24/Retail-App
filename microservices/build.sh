#!/bin/bash

# This script automates the Docker workflow: Build -> Tag -> Push.

# Exit immediately if a command exits with a non-zero status
set -e

# --- CONFIGURATION ---
DOCKER_USERNAME="${1:-}"
DOCKER_TOKEN="${2:-}"
SERVICES=("inventory" "payment" "gateway")
IMAGE_TAG="latest"
K8S_MANIFESTS=("deploy/inventory-deployment.yaml" "deploy/inventory-service.yaml" "deploy/payment-deployment.yaml" "deploy/payment-service.yaml" "deploy/gateway-deployment.yaml" "deploy/gateway-service.yaml")
# --------------------------------------------------------------------------

# --- FUNCTION FOR CROSS-PLATFORM SED ---
sedi() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS (BSD sed) requires a backup extension (use "" for no backup)
        sed -i "" "$@"
    else
        # Linux (GNU sed) does not require a backup extension argument
        sed -i "$@"
    fi
}
# ---------------------------------------

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

# 6. update K8s Manifests with Docker Username
echo "--- 4. Preparing K8s Manifests"

PLACEHOLDER="__DOCKER_USER__"

for MANIFEST in "${K8S_MANIFESTS[@]}"; do
  echo "   -> Updating image tag in ${MANIFEST}..."
  if [[ "$MANIFEST" == *deployment.yaml ]]; then

     echo "   -> Dynamically replacing $PLACEHOLDER with $DOCKER_USERNAME in $MANIFEST"

     sedi -e "s#$PLACEHOLDER#$DOCKER_USERNAME#g" "$MANIFEST"
     #sed -i "" -e "s#$PLACEHOLDER#$DOCKER_USERNAME#g" "$MANIFEST" 2>dev/null || \
     #sed -i "s#$PLACEHOLDER#$DOCKER_USERNAME#g" "$MANIFEST"
 fi
done

# 7. Apply K8s Manifests
echo "--- 5. Applying Kubernetes Manifests ---"
for MANIFEST in "${K8S_MANIFESTS[@]}"; do
    echo "   -> Applying $MANIFEST..."
     kubectl apply -f "$MANIFEST" # Uncomment this line when ready
done

# 8. Revert Changes (Crucial cleanup step)
echo "--- 6. Reverting K8s Manifest changes ---"
for MANIFEST in "${K8S_MANIFESTS[@]}"; do
    if [[ "$MANIFEST" == *deployment.yaml ]]; then
        echo "   -> Restoring placeholder in $MANIFEST"
        # Reverse the sed command to put the placeholder back
        sedi -e "s#$DOCKER_USERNAME#$PLACEHOLDER#g" "$MANIFEST"
        #sed -i "" -e "s#$DOCKER_USERNAME#$PLACEHOLDER#g" "$MANIFEST" 2>/dev/null || \
        #sed -i "s#$DOCKER_USERNAME#$PLACEHOLDER#g" "$MANIFEST"
    fi
done

# 9. Final Confirmation
echo "--------------------------------------------------------"
echo "✅ DEPLOYMENT COMPLETE!"
echo "✅ DOCKER BUILD & PUSH COMPLETE!"
echo "Your images are now available in the ${DOCKER_USERNAME} repository."
echo "--------------------------------------------------------"