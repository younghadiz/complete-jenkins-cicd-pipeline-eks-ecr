# Complete Jenkins CI/CD Pipeline with Amazon EKS and ECR — Reusable DevOps Runbook

## Purpose

This runbook documents the complete implementation of a Java Maven CI/CD project using Git, Maven, Docker, Jenkins, a Jenkins Shared Library, Amazon ECR, Amazon EKS, Kubernetes, and a Jenkins controller hosted on DigitalOcean.

Its purpose is to make the project reproducible from an empty workspace without requiring access to previously configured cloud resources.

The runbook preserves three separate layers of information:

1. **TechWorld with Nana training method** — the learning sequence, techniques, and concepts demonstrated during the training.
2. **Verified implementation in this project** — the configuration, commands, files, troubleshooting, and behavior that were actually validated.
3. **Future production improvements** — security, reliability, maintainability, and scaling improvements that can be introduced later without changing the learning objective of the project.

The baseline Java Maven application came from TechWorld with Nana. The DevOps implementation around that application was completed independently.

---

# Project Information

## Project

```text
Complete Jenkins CI/CD Pipeline with Amazon EKS and AWS ECR
```

## Application

```text
java-maven-app
```

## Original training application

```text
https://gitlab.com/twn-devops-bootcamp/latest/08-jenkins/java-maven-app
```

## Required starting branch

```text
starting-code
```

## Verified upstream baseline commit

```text
f2a092e5375f015993151d078df7dc5dc0deae79
```

## Independent project repositories

Primary:

```text
https://github.com/younghadiz/complete-jenkins-cicd-pipeline-eks-ecr
```

Secondary:

```text
https://gitlab.com/devops-engineering-projects/complete-jenkins-cicd-pipeline-eks-ecr
```

## Jenkins Shared Library

```text
https://github.com/younghadiz/jenkins-shared-library
```

Secondary mirror:

```text
git@gitlab.com:younghadiz/jenkins-shared-library.git
```

## AWS region

```text
ca-central-1
```

## Verified AWS resources

```text
ECR repository:
java-maven-app

EKS cluster:
java-maven-eks

EKS managed nodegroup:
java-maven-nodes

Kubernetes namespace:
default
```

---

# Mandatory Implementation Order

This order is authoritative for both project development and this runbook.

```text
Requirements
→ repository setup
→ local environment
→ application build
→ automated tests
→ artifact creation
→ containerization
→ local container testing
→ pipeline preparation
→ infrastructure preparation
→ security configuration
→ server and cloud provisioning
→ deployment
→ networking
→ monitoring
→ end-to-end testing
→ rollback
→ documentation
→ release
→ cleanup
```

The project must not introduce a competing implementation sequence.

Within each phase:

```text
mandatory project phase
→ Nana's applicable learning sequence
→ verified implementation
→ troubleshooting
→ future production improvement
```

---

# Runbook Table of Contents

1. Requirements
2. Repository Setup
3. Local Environment
4. Application Build
5. Automated Tests
6. Artifact Creation
7. Containerization
8. Local Container Testing
9. Pipeline Preparation
10. Infrastructure Preparation
11. Security Configuration
12. Server and Cloud Provisioning
13. Deployment
14. Networking
15. Monitoring
16. End-to-End Testing
17. Rollback
18. Documentation
19. Release
20. Cleanup

---

# Important Runbook Rules

## Never commit secrets

Do not place any of the following in this repository:

```text
AWS access keys
AWS secret access keys
GitHub personal access tokens
GitLab access tokens
Docker Hub passwords
Jenkins credentials
private SSH keys
kubeconfig files containing access information
.env files containing secrets
```

Secrets must be stored in the appropriate credential store, such as Jenkins Credentials.

---

## Command locations

Commands in this runbook identify where they are expected to run.

The main execution environments are:

```text
LOCAL
Gafari's Mac / VS Code integrated terminal

JENKINS SERVER
DigitalOcean Linux server

JENKINS CONTAINER
Dockerized Jenkins controller/agent environment

AWS
Commands executed through AWS CLI or eksctl

KUBERNETES
Commands executed with kubectl against Amazon EKS
```

---

## Historical vs reconstructed commands

Some commands are directly recoverable from the original training/project execution history.

These are identified as:

```text
VERIFIED HISTORICAL COMMAND
```

When the exact original command text is unavailable but the resulting configuration has been fully verified, the runbook uses:

```text
REPRODUCIBLE IMPLEMENTATION
```

A reproducible command must reproduce the verified configuration and follow the same implementation sequence.

It must not be described as an exact historical command unless the historical command was actually recovered.

---

# 1. Requirements

## 1.1 Objective

The project implements a complete CI/CD workflow for a Java Maven application.

The pipeline must:

1. Read and increment the Maven application version.
2. Build and test the Java application.
3. Produce an executable JAR artifact.
4. Build a Docker image.
5. Assign a unique versioned Docker image tag.
6. Authenticate to Amazon ECR.
7. Push the image to the private ECR repository.
8. Deploy the image to Amazon EKS.
9. Wait for the Kubernetes rollout to complete.
10. Commit the updated Maven version back to the Git repository.
11. Prevent Jenkins-generated commits from creating an infinite pipeline loop.
12. Expose the application through Kubernetes networking.
13. Verify the deployment end-to-end.
14. Demonstrate rollback.
15. Provide cleanup instructions.

---

## 1.2 Architecture

The verified architecture is:

```text
Developer
    │
    │ Git push
    ▼
GitHub
    │
    │ webhook / multibranch discovery
    ▼
Jenkins
DigitalOcean
    │
    │ loads
    ▼
Jenkins Shared Library
    │
    ├── Increment Maven version
    ├── Maven build and tests
    ├── Build Docker image
    ├── Authenticate to Amazon ECR
    ├── Push Docker image
    ├── Deploy Kubernetes manifests
    └── Commit version update back to Git
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
        Internet user
```

GitLab is maintained as the synchronized secondary repository.

---

## 1.3 Technology Stack

```text
Java 17
Spring Boot
Maven
JUnit
Git
GitHub
GitLab
Docker
Jenkins
Jenkins Shared Library
Linux
DigitalOcean
AWS CLI
Amazon ECR
Amazon EKS
eksctl
Kubernetes
kubectl
envsubst
AWS IAM authentication
```

---

## 1.4 Application Attribution

The Java Maven application baseline was supplied through the TechWorld with Nana DevOps Bootcamp.

Upstream:

```text
https://gitlab.com/twn-devops-bootcamp/latest/08-jenkins/java-maven-app
```

Starting branch:

```text
starting-code
```

Verified upstream commit:

```text
f2a092e5375f015993151d078df7dc5dc0deae79
```

The supplied Java application must not be presented as original application development.

Independent work includes:

```text
repository design
Git workflow
automated tests
containerization
Jenkins automation
Jenkins Shared Library design
Docker registry integration
AWS ECR
Amazon EKS
Kubernetes deployment
networking
security configuration
monitoring
troubleshooting
end-to-end testing
rollback
documentation
release
cleanup
```

---

## 1.5 Git Strategy

Branches:

```text
main
develop
feature/*
bugfix/*
hotfix/*
docs/*
```

Remote names:

```text
github
gitlab
```

GitHub is primary.

GitLab is secondary.

Use:

```bash
git merge --no-ff
```

for feature integration.

Do not squash or rebase unless deliberately required.

---

## 1.6 Jenkins Strategy

The project uses a Jenkins Multibranch Pipeline.

The application itself is a single deployable service, so the Jenkinsfile calls:

```groovy
singleServicePipeline(...)
```

This should not be confused with:

```groovy
multiServicePipeline(...)
```

which is reserved for repositories containing multiple independently deployable services.

The shared library still includes a `multiServicePipeline.groovy` placeholder for future projects.

---

## 1.7 Versioning Strategy

Nana's training uses Maven version incrementing as part of the pipeline.

The project follows the same basic pattern.

Example:

```text
Maven source version:
1.1.0-SNAPSHOT

Pipeline increment:
1.1.1

Jenkins build number:
2

Docker tag:
1.1.1-2
```

This allows the Docker image to be tied to both the application version and the Jenkins execution.

---

## 1.8 Jenkins Commit-Back Strategy

After successful deployment, Jenkins commits the Maven version change back to the source repository.

Jenkins Git identity:

```text
Name:
jenkins

Email:
jenkins@example.com
```

Commit message:

```text
ci: version bump
```

Only:

```text
pom.xml
```

is staged by the automated version commit.

---

## 1.9 Pipeline Loop Prevention

Without protection, this could happen:

```text
developer push
→ Jenkins build
→ Jenkins modifies pom.xml
→ Jenkins commit
→ Git webhook
→ Jenkins starts again
→ another version increment
→ infinite loop
```

The training approach uses:

```text
Ignore Committer Strategy
```

and ignores commits from:

```text
jenkins@example.com
```

This prevents Jenkins-generated version commits from triggering another build.

---

## 1.10 Cloud Strategy

Jenkins:

```text
DigitalOcean
```

Container registry:

```text
Amazon ECR
```

Kubernetes:

```text
Amazon EKS
```

AWS region:

```text
ca-central-1
```

The implementation intentionally keeps the infrastructure simple and close to the TechWorld with Nana learning approach.

More complex production architecture is documented separately as a future improvement.

---

## 1.11 Cost Warning

The following resources may create AWS charges:

```text
Amazon EKS control plane
EC2 worker nodes
Elastic Load Balancer
EBS volumes
data transfer
other automatically created networking resources
```

The DigitalOcean Jenkins Droplet is also chargeable.

Do not leave training resources running indefinitely.

Cleanup is mandatory at the end of the project.

---

## 1.12 Phase 1 Completion Criteria

Requirements are complete when the following are known:

```text
application source
starting branch
baseline commit
project repositories
branch strategy
Jenkins architecture
AWS region
ECR repository name
EKS cluster name
nodegroup name
deployment method
testing expectations
rollback requirement
documentation requirement
cleanup requirement
```

Verified project values:

```text
Application:
java-maven-app

AWS Region:
ca-central-1

ECR Repository:
java-maven-app

EKS Cluster:
java-maven-eks

Nodegroup:
java-maven-nodes

Primary Git provider:
GitHub

Secondary Git provider:
GitLab
```

---

# 2. Repository Setup

## 2.1 Objective

Create an independent repository from Nana's unfinished `starting-code` branch while preserving attribution and preventing the new project from sharing Nana's Git history.

The required sequence is:

```text
inspect intended directory
→ clone starting-code only
→ inspect supplied application
→ record upstream repository
→ record upstream branch
→ record baseline commit
→ remove generated build artifacts
→ remove Nana's .git metadata
→ initialize independent Git repository
→ create project documentation and .gitignore
→ commit clean baseline
→ connect GitHub and GitLab
→ create develop
→ verify both remotes
```

---

## 2.2 Parent Workspace

LOCAL:

```text
/Users/younghadiz/Documents/tech-workspace/devops-capstone-projects
```

The organizational parent directory must not itself become a shared Git repository.

Verify:

```bash
cd /Users/younghadiz/Documents/tech-workspace/devops-capstone-projects

pwd

ls -la
```

Project directory:

```text
complete-jenkins-cicd-pipeline-eks-ecr
```

---

## 2.3 Clone Only Nana's Starting Branch

LOCAL.

Run:

```bash
git clone \
  --branch starting-code \
  --single-branch \
  https://gitlab.com/twn-devops-bootcamp/latest/08-jenkins/java-maven-app.git \
  complete-jenkins-cicd-pipeline-eks-ecr
```

Enter the repository:

```bash
cd complete-jenkins-cicd-pipeline-eks-ecr
```

---

## 2.4 Verify Before Removing Upstream Git Metadata

This verification is mandatory.

Run:

```bash
pwd

git status

git branch --show-current

git remote -v

git log -1 --oneline

git rev-parse HEAD
```

Expected upstream:

```text
https://gitlab.com/twn-devops-bootcamp/latest/08-jenkins/java-maven-app.git
```

Expected branch:

```text
starting-code
```

Expected commit:

```text
f2a092e5375f015993151d078df7dc5dc0deae79
```

Historical verification from this project showed:

```text
f2a092e (HEAD -> starting-code, origin/starting-code)
Merge branch 'update/starting-code' into 'starting-code'
```

Do not remove `.git` unless all three values have been confirmed:

```text
correct path
correct upstream
correct starting branch / baseline commit
```

---

## 2.5 Inspect the Supplied Application

Run:

```bash
ls -la

find . \
  -maxdepth 4 \
  -type f \
  ! -path './.git/*' \
  | sort
```

The supplied baseline contains:

```text
.gitignore
pom.xml
src/main/java/com/example/Application.java
src/main/resources/static/index.html
```

The original upstream `.gitignore` contained:

```gitignore
.idea/*
```

---

## 2.6 Remove Generated Build Artifacts If Present

During the original project setup, a generated:

```text
target/
```

directory was present as an untracked directory.

Generated Maven output must not become part of the independent baseline.

Before deleting, verify:

```bash
pwd
git status
```

If `target/` exists and contains only generated Maven output:

```bash
rm -rf target/
```

Verify:

```bash
git status
```

This deletion applies only to generated build output.

Do not delete source files.

---

## 2.7 Remove Nana's Git Metadata

Before this destructive action run again:

```bash
pwd
git status
git branch --show-current
git remote -v
git log -1 --oneline
```

Confirm:

```text
repository:
TechWorld with Nana java-maven-app

branch:
starting-code

baseline:
f2a092e5375f015993151d078df7dc5dc0deae79
```

Then remove only the upstream Git metadata:

```bash
rm -rf .git
```

Verify:

```bash
ls -la
```

The application files should remain.

`.git` should not.

---

## 2.8 Initialize the Independent Repository

Run:

```bash
git init -b main
```

Verify:

```bash
git status
git branch --show-current
```

Expected branch:

```text
main
```

---

## 2.9 Create the Independent `.gitignore`

Path:

```text
complete-jenkins-cicd-pipeline-eks-ecr/.gitignore
```

The original upstream `.gitignore` was only:

```gitignore
.idea/*
```

For the independent repository, use the following project-safe version.

```gitignore
# ------------------------------------------------------------
# Java / Maven
# ------------------------------------------------------------
target/
*.class
*.jar
*.war
*.ear

# Maven Wrapper / Maven local files
.mvn/timing.properties
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml

# ------------------------------------------------------------
# IDEs / Editors
# ------------------------------------------------------------
.idea/
*.iml
.vscode/

# ------------------------------------------------------------
# macOS
# ------------------------------------------------------------
.DS_Store

# ------------------------------------------------------------
# Logs and temporary files
# ------------------------------------------------------------
*.log
*.tmp
*.swp
*.swo

# ------------------------------------------------------------
# Environment and local configuration
# ------------------------------------------------------------
.env
.env.*
!.env.example

# ------------------------------------------------------------
# AWS / Kubernetes / Cloud credentials and local state
# Never commit credentials, kubeconfig files or secrets.
# ------------------------------------------------------------
.aws/
.kube/
kubeconfig
kubeconfig.*
*.pem
*.key
*.p12
*.pfx

# ------------------------------------------------------------
# Terraform / Infrastructure state
# Included for safety even though Terraform is not used in
# this Nana-aligned version of the project.
# ------------------------------------------------------------
.terraform/
*.tfstate
*.tfstate.*
.terraform.lock.hcl

# ------------------------------------------------------------
# Miscellaneous
# ------------------------------------------------------------
coverage/
dist/
tmp/
```

Why:

```text
target/
```

prevents generated Maven artifacts from being committed.

```text
.env*
.aws/
.kube/
*.pem
*.key
```

reduce the risk of accidentally committing credentials or cloud-access material.

Terraform entries are defensive only. Terraform is not used to provision this version of the capstone.

---

## 2.10 Create the Baseline README

Path:

```text
complete-jenkins-cicd-pipeline-eks-ecr/README.md
```

The historical repository README was intentionally minimal at this stage and later became outdated as implementation progressed.

For a clean rebuild, use:

````markdown
# Complete CI/CD Pipeline with EKS and AWS ECR

A hands-on DevOps portfolio project implementing a complete CI/CD workflow for a Java Maven application using Jenkins, Docker, Amazon ECR, Kubernetes, and Amazon EKS.

## Application Attribution

The baseline Java Maven application used in this project was supplied as part of the **TechWorld with Nana DevOps Bootcamp**.

Upstream application:

```text
https://gitlab.com/twn-devops-bootcamp/latest/08-jenkins/java-maven-app
````

Starting branch:

```text
starting-code
```

Verified upstream baseline commit:

```text
f2a092e5375f015993151d078df7dc5dc0deae79
```

The supplied Java Maven application is not presented as original application code.

The independent work in this repository covers the DevOps implementation around the application, including Git workflow, testing, containerization, Jenkins CI/CD automation, Jenkins Shared Library integration, Amazon ECR, Amazon EKS, Kubernetes deployment, security configuration, verification, rollback, documentation, and cleanup.

## Repository Strategy

This project is maintained in two synchronized remote repositories:

```text
GitHub — primary repository
GitLab — secondary repository
```

Development uses:

```text
main
develop
feature/*
bugfix/*
hotfix/*
docs/*
```

## Author

Gafari Salaudeen

````

### Historical note

The original README committed during this project later contained a Markdown fence formatting error and an outdated project-status section.

The version above is the corrected reproducible baseline.

Do not interpret this correction as a change to the application source.

---

## 2.11 Inspect What Will Be Committed

Run:

```bash
git status

git diff -- .gitignore README.md
````

Then:

```bash
git ls-files
```

At this point `git ls-files` may still be empty because this is a new repository.

Inspect the files manually:

```bash
find . \
  -maxdepth 5 \
  -type f \
  ! -path './.git/*' \
  ! -path './target/*' \
  | sort
```

---

## 2.12 Create the Independent Baseline Commit

Stage only the supplied clean application plus repository metadata:

```bash
git add \
  .gitignore \
  README.md \
  pom.xml \
  src/main/java/com/example/Application.java \
  src/main/resources/static/index.html
```

Verify staged content:

```bash
git status

git diff --cached --stat

git diff --cached
```

Commit:

```bash
git commit -m "chore: import starting application baseline"
```

Verified historical independent baseline commit:

```text
6b4bb6f
```

---

## 2.13 Add the Primary GitHub Remote

Create an empty repository on GitHub first.

Repository:

```text
complete-jenkins-cicd-pipeline-eks-ecr
```

Then:

```bash
git remote add github \
  https://github.com/younghadiz/complete-jenkins-cicd-pipeline-eks-ecr.git
```

Verify:

```bash
git remote -v
```

---

## 2.14 Add the Secondary GitLab Remote

Create an empty repository on GitLab:

```text
devops-engineering-projects/complete-jenkins-cicd-pipeline-eks-ecr
```

Add:

```bash
git remote add gitlab \
  https://gitlab.com/devops-engineering-projects/complete-jenkins-cicd-pipeline-eks-ecr.git
```

Verify:

```bash
git remote -v
```

The reusable runbook deliberately avoids embedding credentials in either remote URL.

---

## 2.15 Push `main`

Push GitHub:

```bash
git push -u github main
```

Push GitLab:

```bash
git push -u gitlab main
```

Verify:

```bash
git fetch github
git fetch gitlab

printf 'Local main:  '
git rev-parse main

printf 'GitHub main: '
git rev-parse github/main

printf 'GitLab main: '
git rev-parse gitlab/main
```

All hashes must match.

Historical baseline:

```text
6b4bb6f04b4cb26d43cf2adbf6dd93f55eb0b221
```

---

## 2.16 Create `develop`

Before switching branches:

```bash
git status
git branch --show-current
git remote -v
```

Create:

```bash
git switch -c develop
```

Push:

```bash
git push -u github develop
git push -u gitlab develop
```

Verify:

```bash
git fetch github
git fetch gitlab

printf 'Local develop:  '
git rev-parse develop

printf 'GitHub develop: '
git rev-parse github/develop

printf 'GitLab develop: '
git rev-parse gitlab/develop
```

The three hashes must match.

---

## 2.17 Verify the Independent Repository

Run:

```bash
git status

git branch -vv

git remote -v

git log \
  --oneline \
  --decorate \
  --graph \
  --all

git ls-files
```

Expected tracked baseline files:

```text
.gitignore
README.md
pom.xml
src/main/java/com/example/Application.java
src/main/resources/static/index.html
```

At the original verified baseline:

```text
main:
6b4bb6f

develop:
6b4bb6f
```

GitHub and GitLab pointed to the same commit.

---

## 2.18 Repository Setup Verification Checklist

```text
[ ] Correct upstream repository confirmed
[ ] starting-code branch confirmed
[ ] upstream baseline commit recorded
[ ] generated target directory excluded
[ ] upstream .git metadata removed safely
[ ] independent Git repository initialized
[ ] main branch created
[ ] project-safe .gitignore added
[ ] attribution README added
[ ] clean baseline committed
[ ] GitHub remote named github
[ ] GitLab remote named gitlab
[ ] main pushed to both remotes
[ ] develop created
[ ] develop pushed to both remotes
[ ] GitHub and GitLab commit hashes match
[ ] working tree clean
```

---

## 2.19 Evidence to Capture

Capture:

```text
pwd
git status
git branch -vv
git remote -v
git log --graph --decorate --oneline --all
git rev-parse github/main
git rev-parse gitlab/main
git rev-parse github/develop
git rev-parse gitlab/develop
git ls-files
```

Do not include authentication tokens or credential prompts in screenshots.

---

## 2.20 Common Problems

### Wrong branch cloned

Problem:

```text
main/master or completed solution used instead of starting-code
```

Correction:

```text
Remove the incorrect working copy only after verifying the path,
then clone starting-code again.
```

---

### Generated `target/` accidentally staged

Check:

```bash
git status
```

If generated Maven files are staged:

```bash
git restore --staged target/
```

Confirm:

```bash
git status
```

Ensure:

```text
target/
```

exists in `.gitignore`.

---

### Ambiguous `origin`

The independent repository intentionally uses:

```text
github
gitlab
```

rather than an ambiguous:

```text
origin
```

This makes synchronization explicit.

---

## 2.21 Security Notes

Do not copy:

```text
upstream Git credentials
local .aws directory
local .kube directory
private SSH keys
.env files
cloud credentials
```

into the new repository.

Before pushing:

```bash
git status
git diff --cached
```

Review exactly what is being committed.

---

## 2.22 Reusable Lesson

The safest way to convert a training repository into an independent portfolio repository is:

```text
clone the unfinished source
→ verify provenance
→ record upstream commit
→ remove only Git metadata
→ initialize independent history
→ attribute upstream source
→ push to your own remotes
```

This preserves attribution while ensuring the implementation history represents the work performed independently.

---

# 3. Local Environment

To be documented in the next verified documentation batch.

---

# 4. Application Build

To be documented in a later verified documentation batch.

---

# 5. Automated Tests

To be documented in a later verified documentation batch.

---

# 6. Artifact Creation

To be documented in a later verified documentation batch.

---

# 7. Containerization

To be documented in a later verified documentation batch.

---

# 8. Local Container Testing

To be documented in a later verified documentation batch.

---

# 9. Pipeline Preparation

To be documented in a later verified documentation batch.

---

# 10. Infrastructure Preparation

To be documented in a later verified documentation batch.

---

# 11. Security Configuration

To be documented in a later verified documentation batch.

---

# 12. Server and Cloud Provisioning

To be documented in a later verified documentation batch.

Where original provisioning commands cannot be recovered verbatim, this phase will provide a reproducible implementation matching the verified infrastructure state.

---

# 13. Deployment

To be documented in a later verified documentation batch.

---

# 14. Networking

To be documented in a later verified documentation batch.

---

# 15. Monitoring

To be documented in a later verified documentation batch.

---

# 16. End-to-End Testing

To be documented in a later verified documentation batch.

---

# 17. Rollback

To be documented in a later verified documentation batch.

---

# 18. Documentation

To be documented during final consolidation.

---

# 19. Release

To be documented after documentation verification.

---

# 20. Cleanup

To be documented after release verification.
