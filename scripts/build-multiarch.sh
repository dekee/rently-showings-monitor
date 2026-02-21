#!/bin/bash
# =============================================================================
# Build & push multi-arch Docker image (amd64 + arm64)
# Run from the project root: ./scripts/build-multiarch.sh
# Requires: docker buildx (comes with Docker Desktop)
# =============================================================================
set -euo pipefail

REPO="dekee/rently-showings-monitor"
TAG="${1:-latest}"

# Ensure buildx builder exists
if ! docker buildx inspect multiarch-builder &>/dev/null; then
  echo "Creating buildx builder..."
  docker buildx create --name multiarch-builder --use
else
  docker buildx use multiarch-builder
fi

echo "=== Building rently-showings-monitor (linux/amd64 + linux/arm64) ==="
docker buildx build --platform linux/amd64,linux/arm64 \
  -t "${REPO}:${TAG}" \
  --push \
  .

echo ""
echo "=== Done! Image pushed: ==="
echo "  ${REPO}:${TAG}"
