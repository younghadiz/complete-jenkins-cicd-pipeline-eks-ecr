Future Production Improvements
Purpose

This document records improvements that could evolve the verified learning project toward a more production-oriented platform.

These items are not presented as current implementation.

Current project truth remains documented in:

README.md
RUNBOOK.md
docs/architecture.md
1. Infrastructure as Code
Current

AWS infrastructure was created using:

AWS CLI
eksctl
Future

Manage infrastructure declaratively with:

Terraform

Possible resources:

VPC
subnets
security groups
ECR
EKS
managed nodegroups
IAM roles
logging
DNS

Benefits:

repeatability
reviewable infrastructure changes
state tracking
environment recreation
reduced manual drift

Terraform is not part of the verified capstone.

2. Dedicated Jenkins AWS Identity
Current

The learning setup uses Jenkins-stored AWS access credentials.

The same credential ID is used for:

ECR
EKS authentication
Future

Create a dedicated Jenkins identity with:

least privilege
resource-specific access
limited lifecycle
regular rotation

Avoid broad administrative access.

3. Short-Lived AWS Credentials
Current

Jenkins uses long-lived:

AWS Access Key ID
AWS Secret Access Key
Future

Prefer short-lived authentication through an appropriate architecture, such as:

IAM role assumption
OIDC/workload federation
AWS-hosted role-based access

depending on where Jenkins runs.

4. EKS API Endpoint Restrictions
Current Verified State
public endpoint:
enabled

private endpoint:
disabled

public CIDR:
0.0.0.0/0
Future

Options:

restrict public CIDRs
enable private endpoint access
establish VPN/private connectivity
host CI within trusted AWS networking
5. HTTPS and TLS
Current

Public application uses:

HTTP
port 80
Future

Introduce:

custom DNS
TLS certificate
HTTPS

Possible AWS architecture:

Route 53 / external DNS
→ ALB
→ ACM certificate
→ Kubernetes Ingress
→ Service
→ Pod
6. AWS Load Balancer Controller
Current

Kubernetes:

Service type:
LoadBalancer

created a Classic ELB.

Future

Use:

AWS Load Balancer Controller

with appropriate:

Ingress
ALB

or suitable Service annotations/NLB where technically justified.

Benefits may include:

modern AWS load balancing
TLS termination
path routing
host routing
multiple applications
7. Health Probes
Current

Deployment has no explicit:

startupProbe
readinessProbe
livenessProbe
Future

Expose an application health endpoint such as Spring Boot Actuator and configure probes.

Example direction:

readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080

Do not add this until the application actually exposes the intended endpoint.

8. Resource Requests and Limits
Current

No explicit Kubernetes:

CPU requests
CPU limits
memory requests
memory limits
Future

Size requests and limits using measured resource consumption.

Do not blindly use example values as production sizing.

9. Multiple Replicas
Current
replicas:
1
Future

For higher availability:

replicas > 1

combined with:

readiness probes
PodDisruptionBudget
topology awareness
sufficient node capacity
10. Horizontal Pod Autoscaling

After resource requests and suitable metrics exist, consider:

HorizontalPodAutoscaler

based on:

CPU
memory
application metrics

where appropriate.

11. Nodegroup Scaling
Current
minimum:
1

desired:
1

maximum:
2
Future

Evaluate:

Cluster Autoscaler
Karpenter
larger/multiple nodegroups
spot capacity where appropriate

based on workload needs.

12. Immutable ECR Tags
Current

ECR tag mutability:

MUTABLE

Pipeline nevertheless creates unique tags:

1.1.1-1
1.1.1-2
Future

Set repository tags:

IMMUTABLE

or deploy by digest for stronger artifact integrity.

13. ECR Lifecycle Policies
Current

Images are retained manually.

Future

Introduce lifecycle rules for:

old development images
untagged images
retention limits

while preserving a sufficient rollback window.

Do not delete known-good rollback images too aggressively.

14. Container Vulnerability Scanning
Current

ECR:

scanOnPush=true
Future

Add pipeline security scanning such as:

Trivy

and enforce policy thresholds appropriate to the project.

15. Non-Root Application Container
Current

Dockerfile does not explicitly set:

USER

Application logs showed the process running as root inside the container.

Future

Create a dedicated non-root user in the image and run Java under that identity.

16. Pinned Base Image
Current
FROM eclipse-temurin:17-jre
Future

Consider controlled base-image version/digest pinning plus an update process.

Balance:

reproducibility
security patching
maintenance
17. Multi-Architecture Builds
Current

Local Mac validation used ARM64 while the EKS worker is AMD64.

Jenkins produced the deployment-compatible image.

Future

Use:

docker buildx

to publish multi-platform images where required:

linux/amd64
linux/arm64
18. Jenkins Container Reproducibility
Current

Some required tools are installed manually inside the Jenkins container.

If the container is recreated from the base image, manually installed binaries may disappear even though:

jenkins_home

persists.

Future

Build and version a custom Jenkins image containing:

Docker CLI
AWS CLI
kubectl
envsubst
required system tools

Keep credentials outside the image.

19. Docker Socket Security
Current Learning Method

Jenkins accesses the host Docker daemon through:

/var/run/docker.sock

Nana's training demonstrated:

chmod 666 /var/run/docker.sock
Future

Use a more controlled model, such as:

matching Docker group/GID
isolated build agents
ephemeral build infrastructure
alternative container builders

Docker socket access should be treated as privileged host access.

20. Jenkins HTTPS
Current

Learning setup uses Jenkins directly on:

http://<server>:8080
Future

Use:

DNS
HTTPS
reverse proxy
access restrictions

for administrative and webhook traffic.

21. Jenkins Shared Library Versioning
Current

Application loads:

@Library('jenkins-shared-library') _

which resolves the configured default branch:

master
Future

Publish stable shared-library tags/releases:

v1.0.0
v1.1.0

and pin applications when release stability is important.

This reduces unexpected pipeline behavior from immediate master changes.

22. Shared-Library Automated Tests
Current

Shared library contains a test placeholder/documentation area but no mature automated Groovy pipeline test suite.

Future

Add unit tests for:

PipelineConfig
version calculation
registry selection
Git commit behavior
credential handling
deployment command generation

using an appropriate Jenkins Pipeline testing framework.

23. Branch-Specific Deployment Controls
Nana Learning Concept

A common training pattern is:

test all branches
deploy main/master
Current Capstone

The final singleServicePipeline does not currently implement strong branch guards around the deployment stages.

Future

Introduce explicit policy such as:

feature/*
→ build/test only

develop
→ integration deployment

main
→ controlled release deployment

according to the desired workflow.

Do not document this behavior as current until it is implemented.

24. Environment Separation
Current

One main learning environment:

default namespace
single EKS cluster
Future

Introduce clear:

development
staging
production

separation through:

namespaces
clusters/accounts
configuration
credentials
promotion rules

as appropriate.

25. Helm
Current

Raw Kubernetes YAML:

deployment.yaml
service.yaml

rendered with:

envsubst
Future

For more complex configuration, evaluate:

Helm

for:

values
templates
environment overrides
release management
26. GitOps
Current

Jenkins directly executes:

kubectl apply
Future

Consider GitOps tools such as:

Argo CD
Flux

where a repository becomes the continuously reconciled deployment source of truth.

This would significantly change the deployment architecture and should be implemented as a separate project evolution.

27. Better Rollback Reconciliation
Current

Runtime rollback uses:

kubectl rollout undo

which can temporarily diverge from Git/Jenkins desired state.

Future

Implement a release model that keeps:

Git
artifact
deployment
rollback state

explicitly correlated.

Options may include:

release manifests
immutable image digests
Git revert + automated redeploy
GitOps revision rollback
28. Deployment Change Cause
Current

Rollout history showed:

CHANGE-CAUSE:
<none>
Future

Add deployment metadata/annotations describing:

Git SHA
Jenkins build
application version
image digest
release identifier

so rollout history is easier to audit.

29. Prometheus and Grafana
Current

Not deployed in this capstone.

Monitoring relies on:

kubectl logs
Kubernetes events
Metrics Server
kubectl top
AWS ELB health
external HTTP checks
Future

Introduce:

Prometheus
Grafana
Alertmanager

for persistent metrics, dashboards and alerts.

Potential metrics:

HTTP request rate
latency
error rate
JVM heap
garbage collection
CPU
memory
container restarts
Pod availability
30. Centralized Logging
Current

Application logs are read through:

kubectl logs
Future

Introduce centralized retention/search such as:

CloudWatch Logs
Loki
OpenSearch

depending on architecture and cost requirements.

31. EKS Control-Plane Logging
Current Verified State

Disabled:

api
audit
authenticator
controllerManager
scheduler
Future

Enable required categories and configure retention.

Consider:

security visibility
audit requirements
CloudWatch cost
retention
32. Alerts
Current

No automated alerting layer.

Future

Alert on conditions such as:

pipeline failure
Deployment unavailable
Pod restart loop
high memory
node pressure
ELB unhealthy backend
HTTP endpoint unavailable
33. Secret Scanning
Current

Repository uses:

.gitignore
manual grep checks
Jenkins Credentials
Future

Add automated secret scanning to:

local pre-commit
CI pipeline
repository security tooling
34. Dependency and SCA Scanning

Add tools appropriate to Java/Maven dependency security.

Possible categories:

dependency vulnerabilities
license review
software composition analysis
35. SBOM

Generate a Software Bill of Materials for released container/application artifacts.

Possible formats:

CycloneDX
SPDX

Associate the SBOM with the release/image digest.

36. Artifact Signing

Future release security could include:

container image signing
provenance
attestation

to verify artifact origin and integrity.

37. GitHub Authentication
Current

Jenkins uses:

github-token
Future

Prefer appropriately scoped, expiring authentication such as:

fine-grained PAT
GitHub App

depending on Jenkins integration needs.

38. Automatic GitLab Mirroring
Current

GitHub is primary.

GitLab synchronization is manual.

Future

If both remotes remain necessary, introduce controlled mirroring so GitLab does not rely on manual pushes.

The solution must not accidentally cause:

duplicate CI triggers
conflicting commit-backs
39. Release Tagging
Current

Container images receive versioned build tags.

Future

Formal release procedure could add:

Git tag
GitHub release
release notes
image digest
SBOM
deployment evidence

Phase 19 of the runbook establishes the initial controlled release process.

40. Documentation Automation

Future checks could validate:

Markdown links
heading structure
command snippets
architecture references
README/runbook consistency

as part of CI.

41. Production-Readiness Principle

The learning project should not be judged by whether it contains every possible DevOps technology.

A production improvement should be introduced only when it solves a clear requirement involving:

security
reliability
availability
scaling
observability
repeatability
compliance
developer experience
cost

The verified project remains intentionally understandable and reproducible before these improvements are layered on.