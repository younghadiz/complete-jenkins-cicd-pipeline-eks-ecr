# Project Architecture

## Overview

This document describes the verified architecture of the **Complete Jenkins CI/CD Pipeline with Amazon EKS and ECR** project.

The application is a Java Maven / Spring Boot application.

The DevOps delivery architecture uses:

```text
GitHub
GitLab
Jenkins
Jenkins Shared Library
Docker
Amazon ECR
Amazon EKS
Kubernetes
AWS Classic ELB
```

The architecture documented here reflects the implementation that was actually verified.

Technologies discussed only as future improvements are not represented as current components.

---

# 1. High-Level Architecture

```text
Developer
    │
    │ git push
    ▼
GitHub
    │
    │ webhook / branch discovery
    ▼
Jenkins
DigitalOcean
    │
    │ @Library(...)
    ▼
Jenkins Shared Library
    │
    ├── Increment Version
    ├── Build Application
    ├── Run Tests
    ├── Build Docker Image
    ├── Push Image to Amazon ECR
    ├── Deploy to Amazon EKS
    └── Commit Version Update
            │
            ├──────────────► GitHub
            │
            ▼
        Amazon ECR
            │
            ▼
        Amazon EKS
            │
            ▼
   Kubernetes Deployment
            │
            ▼
           Pod
            │
            ▼
   LoadBalancer Service
            │
            ▼
    AWS Classic ELB
            │
            ▼
         Internet
```

GitLab is maintained as the synchronized secondary repository.

---

# 2. Source-Control Architecture

Primary repository:

```text
GitHub
```

Secondary repository:

```text
GitLab
```

Branch model:

```text
main
develop
feature/*
bugfix/*
hotfix/*
docs/*
```

The normal development pattern is:

```text
develop
   │
   ├── feature/*
   ├── bugfix/*
   ├── docs/*
   └── hotfix/*
```

Focused branches are integrated using non-fast-forward merges where appropriate.

GitHub is the primary repository connected to Jenkins.

GitLab is synchronized separately as the secondary mirror.

---

# 3. Application Repository Responsibilities

The application repository contains:

```text
application source
automated test
Maven configuration
Dockerfile
Jenkinsfile
Kubernetes manifests
runbook
project documentation
```

Important files:

```text
pom.xml

src/main/java/com/example/Application.java

src/test/java/com/example/ApplicationTest.java

src/main/resources/static/index.html

Dockerfile

Jenkinsfile

kubernetes/deployment.yaml

kubernetes/service.yaml
```

---

# 4. Jenkins Architecture

Jenkins runs in a Docker container hosted on a DigitalOcean Linux server.

Conceptually:

```text
DigitalOcean Droplet
        │
        ├── Docker daemon
        │
        └── Jenkins container
                │
                ├── Jenkins
                ├── Maven integration
                ├── Docker CLI
                ├── AWS CLI
                ├── kubectl
                ├── envsubst
                └── kubeconfig
```

Persistent Jenkins state is stored through:

```text
jenkins_home
→ /var/jenkins_home
```

Docker access is provided through the host Docker socket:

```text
/var/run/docker.sock
```

The training implementation used Docker-outside-of-Docker rather than running a separate Docker daemon inside the Jenkins container.

---

# 5. Jenkins Multibranch Architecture

The application is configured as a Jenkins Multibranch Pipeline.

Jenkins:

```text
scans GitHub
    ↓
discovers branches
    ↓
looks for Jenkinsfile
    ↓
creates/updates branch jobs
```

The application Jenkinsfile loads:

```groovy
@Library('jenkins-shared-library') _
```

and invokes:

```groovy
singleServicePipeline(...)
```

A Jenkins Multibranch Pipeline and `multiServicePipeline.groovy` are different concepts.

This project uses:

```text
Jenkins Multibranch Pipeline
+
singleServicePipeline()
```

---

# 6. Jenkins Shared Library Architecture

Shared-library repository:

```text
https://github.com/younghadiz/jenkins-shared-library
```

The library separates Jenkins-facing functions from implementation classes.

```text
jenkins-shared-library/
├── vars/
│   ├── singleServicePipeline.groovy
│   ├── multiServicePipeline.groovy
│   ├── incrementVersion.groovy
│   ├── buildMaven.groovy
│   ├── buildDockerImage.groovy
│   ├── pushToDockerHub.groovy
│   ├── pushToEcr.groovy
│   ├── deployToEks.groovy
│   └── commitVersion.groovy
│
├── src/
│   └── com/younghadiz/devops/
│       ├── PipelineConfig.groovy
│       ├── DockerUtils.groovy
│       ├── AwsUtils.groovy
│       └── KubernetesUtils.groovy
│
└── resources/
    └── com/younghadiz/templates/
        └── deployment.yaml.template
```

Pipeline flow:

```text
singleServicePipeline()
        │
        ├── incrementVersion()
        │
        ├── buildMaven()
        │
        ├── buildDockerImage()
        │       └── DockerUtils
        │
        ├── pushToEcr()
        │       └── AwsUtils
        │
        ├── deployToEks()
        │       └── KubernetesUtils
        │
        └── commitVersion()
```

---

# 7. CI/CD Execution Flow

The verified pipeline stages are:

```text
Increment Version
        ↓
Build Application
        ↓
Build Docker Image
        ↓
Push Docker Image
        ↓
Deploy
        ↓
Commit Version Update
```

## 7.1 Increment Version

Starting Maven version:

```text
1.1.0-SNAPSHOT
```

Successful pipeline result:

```text
1.1.1
```

Jenkins then adds its build number.

Verified image tag:

```text
1.1.1-2
```

---

## 7.2 Build Application

Jenkins executes:

```bash
mvn clean package
```

The build performs:

```text
clean
compile
test
package
Spring Boot repackage
```

Verified test result:

```text
Tests run: 1
Failures: 0
Errors: 0
Skipped: 0
```

---

## 7.3 Build Docker Image

The Jenkins build produces an image based on:

```dockerfile
FROM eclipse-temurin:17-jre
```

The application JAR is copied to:

```text
/app/app.jar
```

and executed with:

```text
java -jar app.jar
```

---

## 7.4 Push to Amazon ECR

Verified repository:

```text
java-maven-app
```

Region:

```text
ca-central-1
```

The pipeline authenticates using:

```text
aws ecr get-login-password
```

with Jenkins-provided AWS credentials.

---

## 7.5 Deploy to Amazon EKS

Verified cluster:

```text
java-maven-eks
```

Verified managed nodegroup:

```text
java-maven-nodes
```

The pipeline renders the Kubernetes manifests with:

```text
envsubst
```

and applies them using:

```text
kubectl apply
```

The pipeline then waits for:

```text
kubectl rollout status
```

to report successful deployment.

---

# 8. AWS Infrastructure Architecture

Verified AWS region:

```text
ca-central-1
```

Infrastructure:

```text
AWS
│
├── Amazon ECR
│   └── java-maven-app
│
└── Amazon EKS
    └── java-maven-eks
        │
        └── Managed Nodegroup
            └── java-maven-nodes
                └── t3.small worker
```

Verified nodegroup configuration:

```text
capacity:
ON_DEMAND

minimum:
1

desired:
1

maximum:
2

architecture:
amd64
```

---

# 9. Kubernetes Architecture

Application resources:

```text
Deployment
java-maven-app
        │
        ▼
ReplicaSet
        │
        ▼
Pod
java-maven-app
```

Networking:

```text
LoadBalancer Service
java-maven-app
        │
        ▼
EndpointSlice
        │
        ▼
application Pod
```

The Deployment uses:

```text
replicas:
1
```

The application listens on:

```text
8080
```

---

# 10. Container Registry to Runtime Flow

```text
Jenkins
    │
    │ docker push
    ▼
Amazon ECR
java-maven-app:1.1.1-2
    │
    ▼
Amazon EKS
    │
    ▼
Kubernetes Deployment
    │
    ▼
Pod pulls image
```

The final validation compared:

```text
ECR image digest
```

with:

```text
running Pod ImageID digest
```

to verify the exact container content running in Kubernetes.

---

# 11. Network Architecture

The verified external request path is:

```text
Internet
    │
    │ HTTP :80
    ▼
AWS Classic Load Balancer
    │
    │ NodePort
    ▼
EKS worker
    │
    ▼
Kubernetes Service
    │
    │ targetPort :8080
    ▼
java-maven-app Pod
    │
    ▼
Spring Boot
```

Service configuration:

```text
type:
LoadBalancer

port:
80

targetPort:
8080
```

The NodePort is dynamically assigned by Kubernetes.

During verification it was:

```text
30321
```

This value must not be treated as permanent configuration.

---

# 12. Why the AWS Load Balancer Is a Classic ELB

AWS inspection confirmed that the Kubernetes Service created:

```text
Classic Load Balancer
```

with:

```text
scheme:
internet-facing
```

The project did not use:

```text
Application Load Balancer
Network Load Balancer
Kubernetes Ingress
AWS Load Balancer Controller
```

The simple `LoadBalancer` Service matches the scope of the learning project.

---

# 13. Git Commit-Back Architecture

After successful deployment, Jenkins commits the incremented Maven version.

Flow:

```text
successful Kubernetes rollout
        ↓
git add pom.xml
        ↓
git commit
ci: version bump
        ↓
GitHub develop
```

Jenkins identity:

```text
jenkins <jenkins@example.com>
```

The predictable machine identity allows Jenkins Ignore Committer Strategy to prevent recursive pipeline execution.

---

# 14. Recursive-Build Prevention

Without protection:

```text
developer push
→ Jenkins
→ version commit
→ Jenkins
→ another version commit
→ Jenkins
→ ...
```

The project uses:

```text
Ignore Committer Strategy
```

to ignore commits from:

```text
jenkins@example.com
```

---

# 15. Credential Architecture

Source code contains only Jenkins credential IDs such as:

```text
github-token
aws_ecr_creds
```

Actual credentials remain in Jenkins.

The repository must not contain:

```text
AWS secret keys
GitHub PATs
GitLab tokens
Jenkins passwords
private keys
kubeconfig
.env secrets
```

---

# 16. EKS Authentication Flow

During deployment:

```text
Jenkins
    │
    │ AWS credentials
    ▼
kubectl
    │
    │ kubeconfig / AWS token authentication
    ▼
EKS API
    │
    ▼
Kubernetes authorization
```

The project discovered an important failure when AWS credentials were available during ECR push but not during the later EKS Deploy stage.

The shared-library fix therefore provides AWS credentials again around the Deploy stage.

---

# 17. Monitoring Architecture

Monitoring implemented in the project:

```text
Jenkins
└── pipeline console logs

Kubernetes
├── kubectl get
├── kubectl describe
├── kubectl logs
├── events
├── Metrics Server
├── kubectl top pod
└── kubectl top node

AWS
├── EKS state
└── Classic ELB backend health

External
└── HTTP 200/content test
```

Prometheus and Grafana are not deployed in this project.

---

# 18. Rollback Architecture

Normal state:

```text
ECR
├── 1.1.1-1
└── 1.1.1-2

Kubernetes
├── revision 1 → 1.1.1-1
└── revision 2 → 1.1.1-2
```

Verified rollback:

```text
revision 2 / 1.1.1-2
        ↓
kubectl rollout undo --to-revision=1
        ↓
1.1.1-1
        ↓
HTTP verification
        ↓
kubectl set image
        ↓
1.1.1-2 restored
```

Rollback modifies Kubernetes runtime state.

It does not automatically rewrite Git history.

---

# 19. Trust and Security Boundaries

Important boundaries include:

```text
Developer workstation
        │
        ▼
GitHub

GitHub
        │
        ▼
Jenkins

Jenkins Credentials
        │
        ▼
AWS APIs

Jenkins
        │
        ▼
EKS API

Internet
        │
        ▼
Classic ELB
```

Credentials must remain within their appropriate trust boundary.

---

# 20. Current Learning-Environment Limitations

The verified architecture intentionally keeps several areas simple:

```text
long-lived AWS credentials in Jenkins

public EKS API endpoint

EKS public API CIDR was broad during the project

HTTP rather than HTTPS

Classic ELB rather than ALB/Ingress

single application replica

no explicit Kubernetes CPU/memory requests or limits

no readiness/liveness probes

application container does not explicitly run as non-root

no Prometheus/Grafana stack

EKS control-plane logging disabled

shared library loaded from master by default

no Terraform
```

These are documented limitations, not hidden omissions.

Production-oriented improvements are documented separately in:

```text
docs/future-improvements.md
```

---

# 21. Architecture Summary

The project demonstrates a complete delivery chain:

```text
Source
→ Build
→ Test
→ Package
→ Containerize
→ Publish
→ Deploy
→ Expose
→ Monitor
→ Verify
→ Roll Back
```

while separating:

```text
application repository
pipeline library
container registry
Kubernetes infrastructure
public networking
```

into clear responsibilities.
