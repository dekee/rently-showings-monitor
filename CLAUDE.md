# Rently Showings Monitor

## Project Overview
Spring Boot (Kotlin) app that scrapes Rently activity logs for new showings and sends push notifications. Uses Playwright for browser scraping.

## Infrastructure

### Kubernetes Cluster (bare-metal kubeadm)
- **Control plane (`k8s-cp`)**: `192.168.0.186` (WiFi)
- **Worker node (`k8s-w1`)**: `192.168.0.84` (WiFi) / `192.168.0.82` (eth0)
- **CNI**: Flannel
- **Access**: SSH from Mac to control plane, run kubectl there:
  ```****
  ssh derrick@192.168.0.186 "KUBECONFIG=~/.kube/config kubectl <command>"
  ```
- **GitOps**: ArgoCD with auto-sync from `main` branch, path `k8s/rently-showings-monitor`
- **Ingress**: nginx ingress controller
- **TLS**: cert-manager with `homelab-ca-issuer`

### Container Images
- **Registry**: Docker Hub, namespace `dekee/`
- **Image**: `dekee/rently-showings-monitor:latest`
- **Build**: Multi-arch (amd64 + arm64) via `docker buildx`
- **Build script**: `./scripts/build-multiarch.sh [tag]`
- Follows same pattern as family-reunion project (`~/Development/family_reunion`)

### App-specific
- **Database**: PostgreSQL StatefulSet in-cluster; H2 for local dev
- **Secrets**: `rently-notify` and `postgres-auth` are NOT in Kustomization — must be applied manually
- **Dockerfile**: Multi-stage — Gradle build then slim JRE with Chromium deps for Playwright

## Deployment Steps
1. Build and push image:
   ```
   ./scripts/build-multiarch.sh
   ```
2. Apply secrets manually (not managed by ArgoCD):
   ```
   ssh derrick@192.168.0.186 "KUBECONFIG=~/.kube/config kubectl apply -f -" < k8s/rently-showings-monitor/secret-template.yaml
   ssh derrick@192.168.0.186 "KUBECONFIG=~/.kube/config kubectl apply -f -" < k8s/rently-showings-monitor/postgres-secret-template.yaml
   ```
3. ArgoCD auto-syncs deployment from git after push to `main`
4. Restart deployment to pick up new image or secret changes:
   ```
   ssh derrick@192.168.0.186 "KUBECONFIG=~/.kube/config kubectl rollout restart deployment/rently-showings-monitor -n rently-showings-monitor"
   ```

## Notifications
- **ntfy** (free, primary): topic `rently-fawn-lane` on ntfy.sh
- **Pushover** (legacy, disabled): was used previously, kept as fallback
- Both implement the `Notifier` interface and are toggled via `notify.<provider>.enabled`

## Key Paths
- `src/main/kotlin/com/example/rently/notify/` — notification providers
- `src/main/resources/application.yml` — app config
- `k8s/rently-showings-monitor/` — K8s manifests
- `k8s/argocd/apps/` — ArgoCD Application manifest
- `scripts/build-multiarch.sh` — Docker Hub build & push

## Homelab Context
- Other services run on the same cluster (e.g. homeassistant, tax-tracker, civic, family-reunion)
- ArgoCD namespace: `argocd`
- Timezone: America/Los_Angeles
- Note: local kubeconfig may have a stale `kind-argocd-lab` context — ignore it, use SSH
