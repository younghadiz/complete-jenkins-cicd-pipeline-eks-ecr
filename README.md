# Complete Jenkins CI/CD Pipeline with Amazon EKS and ECR

A hands-on DevOps portfolio project implementing a complete CI/CD workflow for a Java Maven application using Jenkins, Docker, Amazon ECR, Kubernetes, Amazon EKS, and a reusable Jenkins Shared Library.

The project covers the complete delivery path from source control and automated testing through containerization, cloud deployment, networking, monitoring, end-to-end verification, and rollback.

## Project Objective

The objective is to implement and verify a CI/CD pipeline that:

1. Detects application changes from Git.
2. Automatically increments the Maven application version.
3. Builds and tests the Java application.
4. Produces an executable Spring Boot JAR.
5. Builds a versioned Docker image.
6. Authenticates to Amazon ECR.
7. Pushes the image to the private ECR repository.
8. Deploys the image to Amazon EKS.
9. Waits for the Kubernetes rollout to complete.
10. Exposes the application through Kubernetes networking.
11. Commits the updated Maven version back to Git.
12. Prevents Jenkins-generated commits from creating a recursive pipeline loop.
13. Verifies the deployment from Git through the publicly reachable application.
14. Demonstrates and verifies Kubernetes rollback.

## Architecture

```text
Developer
    │
    │ git push
    ▼
GitHub
    │
    │ webhook / Multibranch Pipeline
    ▼
Jenkins on DigitalOcean
    │
    │ loads
    ▼
Jenkins Shared Library
    │
    ├── Increment Maven Version
    ├── Build and Test Application
    ├── Build Docker Image
    ├── Push Image to Amazon ECR
    ├── Deploy to Amazon EKS
    └── Commit Version Update to Git
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
        Internet User
```

GitLab is maintained as the synchronized secondary repository.

See [Architecture](docs/architecture.md) for the detailed component and traffic flow.

## Technology Stack

* Java 17
* Spring Boot 3.5.5
* Maven
* JUnit
* Git
* GitHub
* GitLab
* Jenkins
* Jenkins Multibranch Pipeline
* Jenkins Shared Library
* Docker
* Linux
* DigitalOcean
* AWS CLI
* Amazon ECR
* Amazon EKS
* `eksctl`
* Kubernetes
* `kubectl`
* `envsubst`

## Application Attribution

The baseline Java Maven application used in this project was supplied as part of the **TechWorld with Nana DevOps Bootcamp**.

Upstream application:

```text
https://gitlab.com/twn-devops-bootcamp/latest/08-jenkins/java-maven-app
```

Starting branch:

```text
starting-code
```

Verified upstream baseline commit:

```text
f2a092e5375f015993151d078df7dc5dc0deae79
```

The supplied Java Maven application is not presented as original application code.

My independent work in this repository covers the DevOps engineering around the application, including repository workflow, automated testing, Maven build and artifact verification, containerization, Jenkins automation, Jenkins Shared Library integration, Amazon ECR, Amazon EKS, Kubernetes deployment, security configuration, networking, monitoring, troubleshooting, end-to-end testing, rollback, documentation, release procedures, and cleanup procedures.

## Repository Strategy

The project is maintained in two synchronized remote repositories:

```text
GitHub
Primary repository

GitLab
Secondary repository
```

Development follows:

```text
main
develop
feature/*
bugfix/*
hotfix/*
docs/*
```

Focused work is developed on dedicated branches and integrated with non-fast-forward merges where appropriate to preserve meaningful project history.

## Repository Structure

```text
complete-jenkins-cicd-pipeline-eks-ecr/
├── .dockerignore
├── .gitignore
├── Dockerfile
├── Jenkinsfile
├── README.md
├── RUNBOOK.md
├── pom.xml
│
├── docs/
│   ├── architecture.md
│   ├── troubleshooting.md
│   ├── verification-evidence.md
│   └── future-improvements.md
│
├── kubernetes/
│   ├── deployment.yaml
│   └── service.yaml
│
└── src/
    ├── main/
    │   ├── java/com/example/Application.java
    │   └── resources/static/index.html
    │
    └── test/
        └── java/com/example/ApplicationTest.java
```

## CI/CD Pipeline

The application `Jenkinsfile` loads the reusable Jenkins Shared Library:

```groovy
@Library('jenkins-shared-library') _
```

The application then invokes:

```groovy
singleServicePipeline(...)
```

The shared pipeline executes:

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

### Versioning

The verified successful pipeline changed:

```text
1.1.0-SNAPSHOT
→ 1.1.1
```

and combined the application version with the Jenkins build number:

```text
1.1.1-2
```

to create the deployment image tag.

## Jenkins Shared Library

Reusable pipeline logic is maintained separately in:

```text
https://github.com/younghadiz/jenkins-shared-library
```

The shared library handles:

* Maven version increment
* Maven build and packaging
* Docker image creation
* Docker Hub support
* Amazon ECR authentication and image push
* Amazon EKS deployment
* Kubernetes rollout verification
* Git version commit-back
* single-service pipeline orchestration

The application repository therefore keeps a small, readable `Jenkinsfile` while reusable implementation logic remains centralized.

## Local Build

Requirements:

```text
Java 17
Maven
Docker
Git
```

Build and test:

```bash
mvn clean package
```

Expected executable artifact:

```text
target/java-maven-app-<VERSION>.jar
```

Run locally:

```bash
java -jar target/java-maven-app-<VERSION>.jar
```

Application:

```text
http://localhost:8080/
```

Expected page content:

```text
Welcome to Java Maven Application
```

## Docker

Build:

```bash
docker build \
  -t java-maven-app:<VERSION> \
  .
```

Run:

```bash
docker run -d \
  --name java-maven-app-local \
  -p 8080:8080 \
  java-maven-app:<VERSION>
```

Verify:

```bash
curl -i \
  http://localhost:8080/
```

## Verified AWS Deployment

The verified project used:

```text
AWS region:
ca-central-1

ECR repository:
java-maven-app

EKS cluster:
java-maven-eks

Managed nodegroup:
java-maven-nodes

Worker type:
t3.small

Kubernetes namespace:
default
```

The successful pipeline deployed:

```text
Application version:
1.1.1

Docker image tag:
1.1.1-2
```

The application Deployment reached:

```text
READY:
1/1

AVAILABLE:
1
```

and the running Pod was verified as:

```text
Ready:
true

Restarts:
0
```

## Networking

The verified public network path was:

```text
Internet
    ↓
AWS Classic Load Balancer
    ↓
Service port 80
    ↓
NodePort
    ↓
Kubernetes Service
    ↓
targetPort 8080
    ↓
Spring Boot Pod
```

The Kubernetes Service uses:

```text
type:
LoadBalancer

port:
80

targetPort:
8080
```

The actual NodePort is dynamically allocated by Kubernetes and must not be treated as permanent configuration.

The verified deployment returned:

```text
HTTP 200
```

and:

```text
Welcome to Java Maven Application
```

from the public application endpoint.

## End-to-End Verification

The final validation confirmed the complete delivery chain:

```text
Git develop
        ↓
Maven version 1.1.1
        ↓
Jenkins SUCCESS
        ↓
ECR image 1.1.1-2
        ↓
ECR image digest
        ↓
EKS Deployment
        ↓
running Pod with matching digest
        ↓
LoadBalancer Service
        ↓
Classic ELB backend InService
        ↓
HTTP 200
        ↓
expected application content
```

The running Pod digest was verified against the ECR digest rather than relying only on the mutable image tag.

## Rollback

The project also verified a real Kubernetes rollback.

Initial state:

```text
revision 1
→ image 1.1.1-1

revision 2
→ image 1.1.1-2
```

Rollback:

```bash
kubectl rollout undo \
  deployment/java-maven-app \
  --namespace default \
  --to-revision=1
```

The older image became healthy and continued returning HTTP `200`.

After proving rollback functionality, the intended `1.1.1-2` image was restored and verified again.

See the complete rollback procedure in [RUNBOOK.md](RUNBOOK.md).

## Security

The repository does not store:

```text
AWS secret keys
GitHub tokens
GitLab tokens
Jenkins credentials
private keys
kubeconfig files
.env files containing secrets
```

Jenkins credential IDs are referenced in source code while actual credentials remain in the Jenkins credential store.

Known learning-environment limitations and future production security improvements are documented separately.

## Monitoring

Monitoring implemented in this project includes:

* Jenkins pipeline logs
* Kubernetes Deployment and Pod status
* `kubectl logs`
* Kubernetes events
* `kubectl describe`
* Metrics Server
* `kubectl top pod`
* `kubectl top node`
* EKS node conditions
* AWS Classic ELB health
* External HTTP verification

Prometheus and Grafana were not deployed as part of this capstone.

## Documentation

The documentation set is organized as follows:

* [RUNBOOK.md](RUNBOOK.md) — complete step-by-step rebuild, troubleshooting, verification, rollback, release, and cleanup manual
* [Architecture](docs/architecture.md) — component relationships and request/deployment flows
* [Troubleshooting](docs/troubleshooting.md) — verified failures, root causes, and fixes
* [Verification Evidence](docs/verification-evidence.md) — commands and expected evidence proving the project worked
* [Future Improvements](docs/future-improvements.md) — production improvements intentionally kept outside the learning implementation

## Project Status

Verified implementation completed through:

```text
Requirements
Repository Setup
Local Environment
Application Build
Automated Tests
Artifact Creation
Containerization
Local Container Testing
Pipeline Preparation
Infrastructure Preparation
Security Configuration
Server and Cloud Provisioning
Deployment
Networking
Monitoring
End-to-End Testing
Rollback
```

Documentation is being finalized on:

```text
docs/complete-project-documentation
```

Release and infrastructure cleanup are handled as the final controlled project phases.

## Important Cost Note

The project uses chargeable cloud infrastructure, including resources such as:

```text
DigitalOcean Jenkins server
Amazon EKS control plane
EC2 EKS worker node
AWS load balancer
Amazon ECR storage
```

See the cleanup section of `RUNBOOK.md` before leaving training infrastructure running unnecessarily.

## Author

Gafari Salaudeen

DevOps / Cloud Engineering Portfolio Project
