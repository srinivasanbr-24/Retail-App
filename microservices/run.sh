#! /bin/bash

# Exit immediately if any command fails
set -e


echo "--- Starting Microservices ---"

# 1. Stop and remove existing containers and networks for the project
echo "1. Stopping and removing existing Docker containers..."
# Use 'docker compose' (modern) or 'docker-compose' (legacy)
docker compose down || docker-compose down

# 2. Build and start services in detached mode
echo "2. Building all images and starting containers..."
# The '--build' flag forces a rebuild using the latest code
docker compose up --build -d || docker-compose up --build -d

# 3. Check status
echo "3. Checking container status..."
if [ $? -eq 0 ]; then
    echo "--------------------------------------------------------"
    echo "✅ SUCCESS! All services are running."
    echo "Gateway is accessible at: http://localhost:8082"
    echo "--------------------------------------------------------"

    echo "To view service logs, run: docker compose logs -f"
    echo "To stop services, run: docker compose down"
else
    echo "❌ ERROR: Failed to start one or more containers."
    exit 1
fi
