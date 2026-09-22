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

## 3.1 Objective

Prepare and verify the local development environment before modifying the application or provisioning any cloud resources.

The local-first rule for this project is:

```text
verify local tools
→ inspect application configuration
→ compile application
→ run automated tests
→ create and inspect artifact
→ containerize
→ test container locally
→ prepare CI/CD and infrastructure files
→ only then provision cloud resources
```

No AWS EKS, ECR, load balancer, worker node, or other chargeable cloud resource is required during this phase.

---

## 3.2 Working Directory

LOCAL — VS Code integrated terminal.

```text
/Users/younghadiz/Documents/tech-workspace/devops-capstone-projects/complete-jenkins-cicd-pipeline-eks-ecr
```

Enter the project:

```bash
cd /Users/younghadiz/Documents/tech-workspace/devops-capstone-projects/complete-jenkins-cicd-pipeline-eks-ecr
```

Verify:

```bash
pwd

git status

git branch --show-current

git remote -v
```

Expected development branch during the original implementation:

```text
develop
```

For a fresh rebuild, perform application work from `develop` unless a focused feature branch is required.

---

## 3.3 Verify Git

Run:

```bash
git --version
```

Git must be available because all implementation work is recorded through Git branches and commits.

Also verify repository access:

```bash
git remote -v
```

Expected remote names:

```text
github
gitlab
```

GitHub is primary.

GitLab is the synchronized secondary repository.

---

## 3.4 Verify Java

Run:

```bash
java -version
```

The application requires Java 17.

Verify the Java compiler too:

```bash
javac -version
```

Expected major version:

```text
17
```

During the verified local artifact execution, the application ran using:

```text
Java 17.0.18
```

The exact patch version does not need to be identical when recreating the project, but the Java major version must remain compatible with the Maven compiler configuration.

---

## 3.5 Verify Maven

Run:

```bash
mvn -version
```

Confirm that Maven is installed and that Maven is using Java 17.

The output should identify:

```text
Apache Maven 3.x
Java version: 17.x
```

The exact local Maven patch version was not preserved as an authoritative project requirement.

The important requirement is that Maven can compile this Java 17 Spring Boot project.

---

## 3.6 Verify Docker Availability

Docker is not used to build the image until the Containerization phase, but its availability can be checked now as part of environment preparation.

Run:

```bash
docker --version

docker info >/dev/null && echo "Docker daemon is available"
```

Do not build the Docker image yet.

Image creation belongs to:

```text
Phase 7 — Containerization
```

---

## 3.7 Inspect the Project Structure

Run:

```bash
find . \
  -path './.git' -prune -o \
  -path './target' -prune -o \
  -type f -print | sort
```

At this stage the important application files are:

```text
.gitignore
README.md
pom.xml
src/main/java/com/example/Application.java
src/main/resources/static/index.html
```

The automated test file is introduced later during Phase 5.

---

## 3.8 Inspect Maven Configuration

Path:

```text
pom.xml
```

The application version before Jenkins performed its automated version increment was:

```text
1.1.0-SNAPSHOT
```

For a clean rebuild of the pre-pipeline project, the Maven file is:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>java-maven-app</artifactId>
    <version>1.1.0-SNAPSHOT</version>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>3.5.5</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- to handle any Java version mismatch, add the following configuration for maven-compiler-plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>3.5.5</version>
        </dependency>

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>9.0</version>
        </dependency>
    </dependencies>

</project>
```

Important configuration:

```text
artifactId:
java-maven-app

initial application version:
1.1.0-SNAPSHOT

Java source:
17

Java target:
17

Spring Boot:
3.5.5

JUnit:
4.13.2
```

Later in the CI/CD pipeline, Jenkins changes:

```text
1.1.0-SNAPSHOT
```

to:

```text
1.1.1
```

This version change must not be performed manually during the local build phases.

---

## 3.9 Inspect the Static Application Page

Path:

```text
src/main/resources/static/index.html
```

Complete file:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>MyApp</title>
</head>
<body>
<h1>Welcome to Java Maven Application</h1>
<!-- add image here  <img src="" width="" /> -->
</body>
</html>
```

This content later becomes the primary application-response verification string:

```text
Welcome to Java Maven Application
```

---

## 3.10 Local Environment Verification

Run:

```bash
echo "===== Git ====="
git --version

echo
echo "===== Java ====="
java -version

echo
echo "===== Java Compiler ====="
javac -version

echo
echo "===== Maven ====="
mvn -version

echo
echo "===== Docker ====="
docker --version
```

Do not include cloud credentials in screenshots or logs.

---

## 3.11 Security Notes

Local environment files that must remain outside Git include:

```text
.env
.aws/
.kube/
*.pem
*.key
```

Verify the repository ignores appropriate sensitive files:

```bash
cat .gitignore
```

Never create a local plaintext file containing:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
GitHub token
GitLab token
Jenkins password
Docker registry password
```

for the purpose of committing it.

---

## 3.12 Cost

Local environment verification has no AWS infrastructure cost.

Docker Desktop and locally installed development tools are local resources.

---

## 3.13 Completion Criteria

```text
[ ] Correct repository selected
[ ] develop branch selected
[ ] working tree clean
[ ] Git available
[ ] Java 17 available
[ ] javac 17 available
[ ] Maven available
[ ] Maven uses Java 17
[ ] Docker CLI available
[ ] pom.xml inspected
[ ] application source inspected
[ ] no cloud infrastructure provisioned
```

---

# 4. Application Build

## 4.1 Objective

Verify that Nana's supplied Java application can be compiled successfully before adding additional project automation.

The mandatory project order separates:

```text
application build
→ automated tests
→ artifact creation
```

Therefore this phase verifies compilation without treating the packaged JAR as the primary deliverable yet.

---

## 4.2 Working Directory

LOCAL:

```text
/Users/younghadiz/Documents/tech-workspace/devops-capstone-projects/complete-jenkins-cicd-pipeline-eks-ecr
```

Verify:

```bash
pwd

git status

git branch --show-current
```

Expected:

```text
develop
working tree clean
```

---

## 4.3 Compile the Application

For the ordered reusable workflow, run:

```bash
mvn clean compile
```

This performs:

```text
clean previous Maven output
→ resolve dependencies
→ process resources
→ compile Java source
→ place compiled classes in target/classes
```

Expected final result:

```text
BUILD SUCCESS
```

---

## 4.4 Verify Compiled Output

Run:

```bash
find target/classes \
  -maxdepth 5 \
  -type f \
  -print | sort
```

At minimum, the compiled application class should exist:

```text
target/classes/com/example/Application.class
```

The static HTML resource should also be copied into the build output:

```text
target/classes/static/index.html
```

Verify:

```bash
test -f target/classes/com/example/Application.class \
  && echo "PASS: Application.class exists"

test -f target/classes/static/index.html \
  && echo "PASS: index.html exists"
```

---

## 4.5 Maven Encoding Warning

During the verified project builds Maven displayed warnings similar to:

```text
Using platform encoding (UTF-8 actually)...
```

This did not cause the build to fail.

For the Nana-aligned implementation, the warning was accepted because it did not block the learning objective.

Future production improvement:

explicitly configure project source encoding in `pom.xml`, for example:

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

Do not introduce that change into the historical implementation unless intentionally improving the project later.

---

## 4.6 Troubleshooting Java Version Mismatch

If Maven reports source/target compatibility errors, verify:

```bash
java -version
javac -version
mvn -version
```

The project compiler configuration requires:

```text
source = 17
target = 17
```

Do not solve a Java mismatch by silently changing the application to another Java major version.

---

## 4.7 Verify Git Remains Clean

Maven-generated output belongs under:

```text
target/
```

which is ignored.

Run:

```bash
git status

git check-ignore -v target/
```

Expected:

```text
nothing to commit, working tree clean
```

Generated build output must not be committed.

---

## 4.8 Build Completion Criteria

```text
[ ] Maven clean completed
[ ] Java source compiled
[ ] Application.class created
[ ] static resource copied
[ ] BUILD SUCCESS returned
[ ] target directory ignored by Git
[ ] source repository remained clean
```

---

# 5. Automated Tests

## 5.1 Objective

Introduce a simple automated unit test before packaging or containerizing the application.

The purpose is to prove that the Maven build has an executable automated test rather than allowing the CI/CD pipeline to report success without testing application behavior.

---

## 5.2 Git Branch

The verified feature branch used for this work was:

```text
feature/add-application-test
```

Before switching:

```bash
git status

git branch --show-current

git remote -v
```

Create the feature branch from `develop`:

```bash
git switch -c feature/add-application-test
```

Verify:

```bash
git branch --show-current
```

Expected:

```text
feature/add-application-test
```

---

## 5.3 Update `Application.java`

Path:

```text
src/main/java/com/example/Application.java
```

Complete file after the test-support change:

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class Application {

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init()
    {
        Logger log = LoggerFactory.getLogger(Application.class);
        log.info("Java app started");
    }

    public String getStatus() {
        return "OK";
    }
}
```

The added method is:

```java
public String getStatus() {
    return "OK";
}
```

It provides a small deterministic unit that can be tested without starting the full Spring Boot server.

---

## 5.4 Create the Test File

Create:

```text
src/test/java/com/example/ApplicationTest.java
```

Complete content:

```java
package com.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationTest {

    @Test
    public void shouldReturnOkStatus() {
        Application application = new Application();

        assertEquals("OK", application.getStatus());
    }
}
```

The test verifies:

```text
Application.getStatus()
        ↓
returns
        ↓
"OK"
```

---

## 5.5 Run the Automated Test

Run:

```bash
mvn test
```

Expected Maven test provider:

```text
org.apache.maven.surefire.junit4.JUnit4Provider
```

Expected result:

```text
Tests run: 1
Failures: 0
Errors: 0
Skipped: 0
```

and:

```text
BUILD SUCCESS
```

Do not continue to artifact packaging if the test fails.

---

## 5.6 Inspect Test Output

Maven creates test reports under:

```text
target/surefire-reports/
```

Inspect:

```bash
find target/surefire-reports \
  -maxdepth 1 \
  -type f \
  -print | sort
```

For a simple text review:

```bash
cat target/surefire-reports/com.example.ApplicationTest.txt
```

The report should confirm one successful test.

---

## 5.7 Failure Procedure

If the test fails:

1. Stop.
2. Read the actual Maven/Surefire error.
3. Verify the expected string.
4. Verify `ApplicationTest.java`.
5. Verify `Application.getStatus()`.
6. Correct the source or test.
7. Run `mvn test` again.
8. Continue only after success.

Do not use:

```bash
-DskipTests
```

to bypass a failing test.

---

## 5.8 Inspect the Git Change

Run:

```bash
git status

git diff -- \
  src/main/java/com/example/Application.java \
  src/test/java/com/example/ApplicationTest.java
```

Only the intended source/test changes should be present.

---

## 5.9 Commit the Automated Test

Stage:

```bash
git add \
  src/main/java/com/example/Application.java \
  src/test/java/com/example/ApplicationTest.java
```

Verify:

```bash
git diff --cached
```

Commit:

```bash
git commit -m "test: add application status unit test"
```

Verified historical feature commit:

```text
69d553c
```

---

## 5.10 Push the Feature Branch

GitHub is primary:

```bash
git push -u github feature/add-application-test
```

Synchronize GitLab without changing the branch upstream:

```bash
git push gitlab feature/add-application-test
```

---

## 5.11 Merge into `develop`

Before switching:

```bash
git status
git branch --show-current
git remote -v
```

Return to `develop`:

```bash
git switch develop
```

Update if required:

```bash
git pull --ff-only github develop
```

Merge while preserving history:

```bash
git merge --no-ff \
  feature/add-application-test \
  -m "merge: add application status unit test"
```

Verified historical merge commit:

```text
3132735
```

Push primary:

```bash
git push github develop
```

Synchronize secondary:

```bash
git push gitlab develop
```

---

## 5.12 Verify Remote Synchronization

Run:

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

All hashes should match.

At this historical point they resolved to the merge containing:

```text
3132735
```

---

## 5.13 Security and Cost

The test contains no secrets and creates no cloud infrastructure.

Cost:

```text
No AWS cost
No DigitalOcean change
Local compute only
```

---

## 5.14 Automated Test Completion Criteria

```text
[ ] focused feature branch created
[ ] Application.java updated
[ ] ApplicationTest.java created
[ ] mvn test executed
[ ] 1 test executed
[ ] 0 failures
[ ] 0 errors
[ ] BUILD SUCCESS
[ ] source/test changes committed
[ ] feature branch pushed to GitHub
[ ] feature branch synchronized to GitLab
[ ] feature merged with --no-ff
[ ] develop pushed to both remotes
[ ] remote hashes verified
```

---

# 6. Artifact Creation

## 6.1 Objective

Package the tested Java application into an executable Spring Boot JAR and verify that the artifact is valid before containerization.

This phase produces the artifact later copied into the Docker image.

---

## 6.2 Working Directory

LOCAL:

```text
/Users/younghadiz/Documents/tech-workspace/devops-capstone-projects/complete-jenkins-cicd-pipeline-eks-ecr
```

Verify:

```bash
pwd

git status

git branch --show-current
```

Expected:

```text
develop
working tree clean
```

---

## 6.3 Package the Application

A verified historical command used during this project was:

```bash
mvn package
```

The build executed the automated JUnit test and then created the JAR.

For a reproducible build from a potentially non-clean workspace, use:

```bash
mvn clean package
```

The difference is:

```text
mvn package
    → uses the existing target lifecycle state where applicable

mvn clean package
    → removes old target output first
    → recompiles
    → runs tests
    → packages a fresh artifact
```

The Jenkins pipeline later uses:

```text
mvn clean package
```

so that is the preferred reproducible command.

---

## 6.4 Expected Test Result During Packaging

Packaging must still run the unit test.

Expected:

```text
Running com.example.ApplicationTest

Tests run: 1
Failures: 0
Errors: 0
Skipped: 0
```

If tests fail, Maven packaging must be considered failed.

---

## 6.5 Verified Artifact

The local project produced:

```text
target/java-maven-app-1.1.0-SNAPSHOT.jar
```

The Spring Boot Maven plugin also preserved the original non-repackaged JAR as:

```text
target/java-maven-app-1.1.0-SNAPSHOT.jar.original
```

Verified local artifact size was approximately:

```text
23 MB
```

The exact size may vary slightly when dependency versions or build metadata change.

---

## 6.6 List the Generated Artifacts

Run:

```bash
find target \
  -maxdepth 1 \
  -type f \
  -print | sort
```

Expected:

```text
target/java-maven-app-1.1.0-SNAPSHOT.jar
target/java-maven-app-1.1.0-SNAPSHOT.jar.original
```

Inspect size:

```bash
find target \
  -maxdepth 1 \
  -type f \
  -name '*.jar' \
  -exec ls -lh {} \;
```

---

## 6.7 Inspect JAR Contents

The correct command must include the artifact path:

```bash
jar tf \
  target/java-maven-app-1.1.0-SNAPSHOT.jar
```

For shorter output:

```bash
jar tf \
  target/java-maven-app-1.1.0-SNAPSHOT.jar \
  | head -50
```

Important Spring Boot paths include:

```text
META-INF/
BOOT-INF/classes/
BOOT-INF/lib/
BOOT-INF/classpath.idx
BOOT-INF/layers.idx
```

Application classes should be under:

```text
BOOT-INF/classes/com/example/
```

Dependencies should be under:

```text
BOOT-INF/lib/
```

---

## 6.8 Confirm Spring Boot Manifest

Run:

```bash
unzip -p \
  target/java-maven-app-1.1.0-SNAPSHOT.jar \
  META-INF/MANIFEST.MF
```

The verified artifact contained information including:

```text
Main-Class: org.springframework.boot.loader.launch.JarLauncher
Start-Class: com.example.Application
Spring-Boot-Version: 3.5.5
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
```

This proves that the Spring Boot Maven plugin repackaged the Maven artifact as an executable Spring Boot JAR.

---

## 6.9 Confirm Artifact Version

Inspect Maven metadata inside the JAR:

```bash
unzip -p \
  target/java-maven-app-1.1.0-SNAPSHOT.jar \
  META-INF/maven/com.example/java-maven-app/pom.properties
```

Expected version at this project stage:

```text
1.1.0-SNAPSHOT
```

Jenkins later increments this version before its CI/CD build.

---

## 6.10 Confirm Git Ignores Generated Artifacts

Run:

```bash
git status

git check-ignore -v \
  target/java-maven-app-1.1.0-SNAPSHOT.jar

git ls-files target
```

Verified project behavior:

```text
.gitignore: target/
```

and:

```text
git ls-files target
```

returns nothing.

Generated artifacts must not be committed.

---

## 6.11 Confirm the JAR Runs

Start the executable artifact:

```bash
java -jar \
  target/java-maven-app-1.1.0-SNAPSHOT.jar
```

Verified application behavior included:

```text
Spring Boot 3.5.5
Java 17
Tomcat port 8080
Java app started
Started Application
```

The historical local execution used:

```text
Java 17.0.18
```

and Tomcat started on:

```text
8080
```

---

## 6.12 Verify the Running Application

Keep the Java process running and open a second VS Code integrated terminal.

Run:

```bash
curl -sS \
  -o /dev/null \
  -w 'HTTP status: %{http_code}\n' \
  http://localhost:8080/
```

Expected:

```text
HTTP status: 200
```

Verify content:

```bash
curl -fsS \
  http://localhost:8080/ \
  | grep -F "Welcome to Java Maven Application"
```

Expected:

```html
<h1>Welcome to Java Maven Application</h1>
```

Return to the terminal running Java and stop it with:

```text
Ctrl+C
```

A graceful Spring Boot shutdown is expected.

---

## 6.13 Confirm Port 8080 Is Released

After stopping the application:

```bash
lsof -nP \
  -iTCP:8080 \
  -sTCP:LISTEN
```

No Java application should remain listening unless another local application uses that port.

---

## 6.14 Confirmed Troubleshooting — Incorrect JAR Path

### Error encountered

The artifact was created under:

```text
target/
```

but the initial inspection command was run as:

```bash
jar tf java-maven-app-1.1.0-SNAPSHOT.jar
```

from the repository root.

This produced:

```text
java.nio.file.NoSuchFileException:
java-maven-app-1.1.0-SNAPSHOT.jar
```

### Root cause

The command referenced:

```text
./java-maven-app-1.1.0-SNAPSHOT.jar
```

while the actual file was:

```text
./target/java-maven-app-1.1.0-SNAPSHOT.jar
```

### Confirmed correction

Use:

```bash
jar tf \
  target/java-maven-app-1.1.0-SNAPSHOT.jar
```

### Reusable lesson

Always inspect build output first:

```bash
find target \
  -maxdepth 1 \
  -type f \
  -print | sort
```

Then use the actual artifact path rather than assuming the artifact exists in the repository root.

---

## 6.15 Artifact Security

Do not place credentials, tokens, `.env` files, kubeconfig files, SSH keys, or AWS credentials inside the JAR.

The JAR should contain only application code, resources, metadata, and dependencies required by the application.

---

## 6.16 Artifact Production Improvement

The current training implementation produces an executable Spring Boot JAR using Maven.

Potential future improvements include:

```text
artifact checksum
artifact signing
software bill of materials
dependency vulnerability scan
artifact repository retention
reproducible build controls
```

These are not required for the current Nana-aligned capstone.

---

## 6.17 Cost

Artifact creation runs locally.

```text
AWS cost: none
EKS cost: none
ECR cost: none
DigitalOcean change: none
```

---

## 6.18 Artifact Evidence to Capture

Capture:

```bash
mvn clean package

find target \
  -maxdepth 1 \
  -type f \
  -print | sort

find target \
  -maxdepth 1 \
  -type f \
  -name '*.jar' \
  -exec ls -lh {} \;

jar tf \
  target/java-maven-app-1.1.0-SNAPSHOT.jar \
  | head -50

unzip -p \
  target/java-maven-app-1.1.0-SNAPSHOT.jar \
  META-INF/MANIFEST.MF

git status

git check-ignore -v \
  target/java-maven-app-1.1.0-SNAPSHOT.jar

git ls-files target
```

Do not include sensitive environment variables in screenshots.

---

## 6.19 Artifact Completion Criteria

```text
[ ] clean Maven package executed
[ ] automated test ran during package
[ ] test passed
[ ] BUILD SUCCESS returned
[ ] executable JAR created
[ ] JAR location verified
[ ] JAR size inspected
[ ] JAR contents inspected
[ ] Spring Boot manifest inspected
[ ] correct Main-Class confirmed
[ ] correct Start-Class confirmed
[ ] application started from JAR
[ ] application listened on port 8080
[ ] HTTP 200 verified
[ ] expected page content verified
[ ] application stopped cleanly
[ ] target directory ignored by Git
[ ] no generated artifact committed
```

---

# 7. Containerization

## 7.1 Objective

Package the verified executable Java Maven application into a Docker image before introducing Jenkins, Amazon ECR, Amazon EKS, or any chargeable cloud infrastructure.

The project sequence remains:

```text
artifact creation
→ containerization
→ local container testing
→ pipeline preparation
```

The Docker image must first work locally before Jenkins is allowed to build or publish it.

---

## 7.2 Nana Learning Connection

The TechWorld with Nana Jenkins workflow builds the application artifact first and then creates a Docker image that contains that artifact.

The basic pattern is:

```text
Maven
   │
   ▼
Executable JAR
   │
   ▼
Dockerfile
   │
   ▼
Docker image
```

This capstone keeps that same simple learning model.

The application is **not compiled inside Docker**.

Instead:

```text
Maven creates JAR
→ Docker copies JAR
→ Java runtime starts JAR
```

This separation keeps the Dockerfile easy to understand and matches the later Jenkins workflow.

---

## 7.3 Working Directory

LOCAL — VS Code integrated terminal:

```text
/Users/younghadiz/Documents/tech-workspace/devops-capstone-projects/complete-jenkins-cicd-pipeline-eks-ecr
```

Verify:

```bash
pwd

git status

git branch --show-current

git remote -v
```

The original containerization work was completed from:

```text
feature/containerize-application
```

---

## 7.4 Create the Feature Branch

Before switching branches:

```bash
git status
git branch --show-current
git remote -v
```

From `develop`:

```bash
git switch -c feature/containerize-application
```

Verify:

```bash
git branch --show-current
```

Expected:

```text
feature/containerize-application
```

---

## 7.5 Confirm the JAR Exists

The Dockerfile expects an already-created Maven artifact.

Run:

```bash
find target \
  -maxdepth 1 \
  -type f \
  -name 'java-maven-app-*.jar' \
  -print
```

For the local project stage, the main executable artifact was:

```text
target/java-maven-app-1.1.0-SNAPSHOT.jar
```

If the JAR is missing, rebuild it:

```bash
mvn clean package
```

Do not continue with Docker until Maven returns:

```text
BUILD SUCCESS
```

---

## 7.6 Create `Dockerfile`

Full path:

```text
complete-jenkins-cicd-pipeline-eks-ecr/Dockerfile
```

Complete verified file:

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/java-maven-app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 7.7 Dockerfile Explanation

### Base image

```dockerfile
FROM eclipse-temurin:17-jre
```

The application requires Java 17.

Only a Java Runtime Environment is required because compilation already happened with Maven.

The container does not need:

```text
Maven
javac
source code
```

to start the packaged application.

---

### Working directory

```dockerfile
WORKDIR /app
```

All subsequent container operations use:

```text
/app
```

as the working directory.

---

### Copy the executable JAR

```dockerfile
COPY target/java-maven-app-*.jar app.jar
```

The source artifact remains versioned in the Maven `target/` directory, while the file inside the container receives the stable name:

```text
/app/app.jar
```

This lets the Dockerfile work with later Maven versions such as:

```text
java-maven-app-1.1.0-SNAPSHOT.jar
java-maven-app-1.1.1.jar
```

without hardcoding the Maven version into the Dockerfile.

---

### Application port

```dockerfile
EXPOSE 8080
```

The Spring Boot application uses:

```text
8080
```

inside the container.

`EXPOSE` documents the intended container port.

It does not by itself publish the port to the host.

The actual local port mapping happens with:

```bash
-p 8080:8080
```

when the container is started.

---

### Entrypoint

```dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Docker starts the application as:

```bash
java -jar app.jar
```

inside:

```text
/app
```

---

## 7.8 Create `.dockerignore`

Full path:

```text
complete-jenkins-cicd-pipeline-eks-ecr/.dockerignore
```

Complete verified file:

```dockerignore
.git
.gitignore

.idea
.vscode
.DS_Store

*.log
*.tmp
*.swp

.env
.env.*
*.pem
*.key
*.p12
*.pfx

target/*
!target/java-maven-app-*.jar
target/*.jar.original
```

---

## 7.9 Why `.dockerignore` Matters

Docker sends a build context to the Docker daemon.

Without `.dockerignore`, that context could unnecessarily include:

```text
Git history
editor configuration
logs
temporary files
local environment files
private key files
unneeded Maven output
```

The important Maven rule is:

```dockerignore
target/*
```

which excludes Maven output generally.

Then:

```dockerignore
!target/java-maven-app-*.jar
```

adds the executable application JAR back into the Docker build context.

Finally:

```dockerignore
target/*.jar.original
```

keeps Maven's original pre-Spring-Boot artifact out of the image context.

The result is:

```text
Docker receives the executable JAR
but not unnecessary target/ content
```

---

## 7.10 Verify Docker Build Context Files

Run:

```bash
cat Dockerfile

echo
echo "===== Docker Ignore ====="
cat .dockerignore

echo
echo "===== Executable Artifact ====="
find target \
  -maxdepth 1 \
  -type f \
  -name 'java-maven-app-*.jar' \
  ! -name '*.jar.original' \
  -print
```

---

## 7.11 Build the Local Docker Image

The verified local image tag was:

```text
java-maven-app:1.1.0-SNAPSHOT
```

Build:

```bash
docker build \
  -t java-maven-app:1.1.0-SNAPSHOT \
  .
```

Equivalent one-line form used during the project:

```bash
docker build -t java-maven-app:1.1.0-SNAPSHOT .
```

Expected final behavior:

```text
Docker reads Dockerfile
→ pulls/resolves eclipse-temurin:17-jre
→ creates /app
→ copies executable JAR to /app/app.jar
→ configures ENTRYPOINT
→ creates java-maven-app:1.1.0-SNAPSHOT
```

---

## 7.12 Verify Image Exists

Run:

```bash
docker image ls java-maven-app
```

Expected repository/tag:

```text
REPOSITORY       TAG
java-maven-app   1.1.0-SNAPSHOT
```

---

## 7.13 Inspect the Built Image

Run:

```bash
docker image inspect \
  java-maven-app:1.1.0-SNAPSHOT \
  --format 'Architecture={{.Architecture}} OS={{.Os}} WorkingDir={{.Config.WorkingDir}} Entrypoint={{json .Config.Entrypoint}}'
```

The verified local image returned:

```text
Architecture=arm64
OS=linux
WorkingDir=/app
Entrypoint=["java","-jar","app.jar"]
```

### Why ARM64 Was Expected Locally

The local development machine was an Apple Silicon Mac.

Docker Desktop therefore built the local image for:

```text
linux/arm64
```

by default.

This did not become a problem for the deployed EKS image because the production CI image was later built by Jenkins on the Linux DigitalOcean environment rather than pushing this local Apple Silicon image directly into ECR.

The EKS worker node was:

```text
amd64 / x86_64
```

so the architecture of the final Jenkins-built image had to be compatible with that environment.

---

## 7.14 Important Architecture Lesson

A container image must be compatible with the CPU architecture where it will run.

Common architectures include:

```text
arm64
amd64
```

Apple Silicon:

```text
arm64
```

Typical cloud Linux servers and the EKS worker used in this project:

```text
amd64
```

For this project the flow was:

```text
Mac local validation
    │
    └── ARM64 image

Jenkins on DigitalOcean
    │
    └── builds deployment image for its Linux platform
            │
            ▼
           ECR
            │
            ▼
       EKS x86_64 node
```

Therefore the local development image was a validation artifact, not the image pushed to ECR.

---

## 7.15 Future Multi-Architecture Improvement

For a workflow where the same image must support both architectures, a future improvement could use Docker Buildx:

```bash
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  ...
```

This was **not required** for the Nana-aligned implementation.

Do not add multi-architecture complexity unless the deployment requirements need it.

---

## 7.16 Current Container Security Limitation

The current Dockerfile does not declare:

```dockerfile
USER
```

Therefore the Java process starts as the image's default user.

Later container logs confirmed the deployed application was running as:

```text
root
```

This is acceptable for reproducing the basic training implementation, but it is a security improvement opportunity.

Future production improvement:

```text
create dedicated application user
→ change ownership where needed
→ run Java as non-root
```

That improvement must be documented separately rather than silently replacing the implementation used in this project.

---

## 7.17 Additional Future Container Improvements

Potential future improvements include:

```text
pin base image by digest
run as non-root
add OCI labels
scan image for vulnerabilities
create SBOM
use multi-stage build if appropriate
configure JVM container limits
support multi-architecture images
```

These are not required for the current Nana-style implementation.

---

## 7.18 Containerization Cost

Local Docker image creation does not create AWS charges.

```text
AWS cost: none
ECR storage: none yet
EKS cost: none yet
DigitalOcean infrastructure change: none
```

---

# 8. Local Container Testing

## 8.1 Objective

Run the Docker image locally and verify:

```text
container starts
→ Spring Boot starts
→ port 8080 is published
→ application responds
→ expected HTML is returned
→ container state is healthy enough for deployment work
→ container can stop cleanly
→ test container can be removed
```

No image should be pushed to ECR before this test succeeds.

---

## 8.2 Check Port 8080 Before Starting

Run:

```bash
lsof -nP \
  -iTCP:8080 \
  -sTCP:LISTEN
```

If nothing is returned, port `8080` is free.

Do not start the Docker container if an unrelated required application already occupies the port.

---

## 8.3 Check for an Existing Test Container

Run:

```bash
docker ps -a \
  --filter name=java-maven-app-local
```

The verified project initially returned no existing test container.

If an old stopped container from this project exists, inspect it before removing it.

Do not blindly delete unrelated Docker containers.

---

## 8.4 Start the Application Container

Verified historical command:

```bash
docker run -d \
  --name java-maven-app-local \
  -p 8080:8080 \
  java-maven-app:1.1.0-SNAPSHOT
```

Explanation:

```text
-d
→ run detached

--name java-maven-app-local
→ assign a predictable test-container name

-p 8080:8080
→ host port 8080
→ container port 8080

java-maven-app:1.1.0-SNAPSHOT
→ local image and tag
```

Expected result:

Docker prints the container ID.

---

## 8.5 Verify Container Is Running

Run:

```bash
docker ps \
  --filter name=java-maven-app-local
```

The verified container showed:

```text
IMAGE:
java-maven-app:1.1.0-SNAPSHOT

COMMAND:
java -jar app.jar

STATUS:
Up

PORT:
0.0.0.0:8080->8080/tcp

NAME:
java-maven-app-local
```

Docker also exposed the IPv6 mapping:

```text
[::]:8080->8080/tcp
```

---

## 8.6 Inspect Application Logs

Run:

```bash
docker logs \
  java-maven-app-local
```

Verified logs included:

```text
Spring Boot 3.5.5

Starting Application using Java 17.0.20

Tomcat initialized with port 8080

Java app started

Tomcat started on port 8080

Started Application
```

This proves that the JAR was started successfully by Docker.

---

## 8.7 Verify the Full HTTP Response

Run:

```bash
curl -i \
  http://localhost:8080/
```

The verified project returned:

```text
HTTP/1.1 200
```

and:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>MyApp</title>
</head>
<body>
<h1>Welcome to Java Maven Application</h1>
<!-- add image here  <img src="" width="" /> -->
</body>
</html>
```

---

## 8.8 Verify HTTP Status Only

Run:

```bash
curl -s \
  -o /dev/null \
  -w 'HTTP status: %{http_code}\n' \
  http://localhost:8080/
```

Verified result:

```text
HTTP status: 200
```

---

## 8.9 Verify Expected Page Content

Run:

```bash
curl -fsS \
  http://localhost:8080/ \
  | grep -F "Welcome to Java Maven Application"
```

Expected:

```html
<h1>Welcome to Java Maven Application</h1>
```

This checks more than connectivity.

It proves the expected application is actually responding.

---

## 8.10 Inspect Container State

Verified command:

```bash
docker inspect \
  java-maven-app-local \
  --format 'Name={{.Name}}
Image={{.Config.Image}}
Status={{.State.Status}}
Running={{.State.Running}}
ExitCode={{.State.ExitCode}}
Ports={{json .NetworkSettings.Ports}}'
```

Verified result:

```text
Name=/java-maven-app-local
Image=java-maven-app:1.1.0-SNAPSHOT
Status=running
Running=true
ExitCode=0
Ports={"8080/tcp":[{"HostIp":"0.0.0.0","HostPort":"8080"},{"HostIp":"::","HostPort":"8080"}]}
```

This verifies:

```text
correct container
correct image
running state
no startup failure
correct port mapping
```

---

## 8.11 Stop the Local Container

Run:

```bash
docker stop \
  java-maven-app-local
```

Verified output:

```text
java-maven-app-local
```

Inspect:

```bash
docker ps -a \
  --filter name=java-maven-app-local
```

The historical test showed:

```text
Exited (143)
```

---

## 8.12 Why Exit Code 143 Was Expected

Linux exit code:

```text
143
```

normally means:

```text
128 + 15
```

where signal `15` is:

```text
SIGTERM
```

`docker stop` first sends SIGTERM to allow the application to shut down gracefully.

Therefore:

```text
Exited (143)
```

after an intentional `docker stop` was not evidence of an application crash in this test.

---

## 8.13 Confirm the Application Is No Longer Reachable

Run:

```bash
curl -s \
  -o /dev/null \
  -w 'HTTP status after stop: %{http_code}\n' \
  --max-time 3 \
  http://localhost:8080/
```

Verified result:

```text
HTTP status after stop: 000
```

For this test, `000` indicates `curl` did not receive an HTTP response because the local container was no longer serving the application.

---

## 8.14 Remove the Test Container

Run:

```bash
docker rm \
  java-maven-app-local
```

Verify:

```bash
docker ps -a \
  --filter name=java-maven-app-local
```

Expected:

```text
no matching container
```

The local image can remain because it may still be useful for inspection.

---

## 8.15 Verify Only Intended Repository Files Changed

Run:

```bash
git status

git branch --show-current

git diff --stat
```

At the historical point before committing, the project correctly showed only:

```text
.dockerignore
Dockerfile
```

as new files.

Generated Maven output remained ignored.

---

## 8.16 Inspect Both New Files Before Commit

Run:

```bash
echo "===== Dockerfile ====="
cat Dockerfile

echo
echo "===== .dockerignore ====="
cat .dockerignore
```

Confirm no secrets are present.

---

## 8.17 Stage the Containerization Files

Run:

```bash
git add \
  Dockerfile \
  .dockerignore
```

Inspect:

```bash
git status

git diff --cached
```

Only these files should be staged:

```text
Dockerfile
.dockerignore
```

---

## 8.18 Commit Containerization

Verified commit message:

```bash
git commit \
  -m "feat: containerize Java Maven application"
```

Verified historical commit:

```text
07958db
```

---

## 8.19 Push the Feature Branch

GitHub primary:

```bash
git push -u \
  github \
  feature/containerize-application
```

GitLab secondary:

```bash
git push \
  gitlab \
  feature/containerize-application
```

Do not use `-u` on the GitLab push when GitHub should remain the branch upstream.

---

## 8.20 Merge into `develop`

Before switching:

```bash
git status
git branch --show-current
git remote -v
```

Switch:

```bash
git switch develop
```

Update from GitHub if necessary:

```bash
git pull --ff-only \
  github \
  develop
```

Merge:

```bash
git merge --no-ff \
  feature/containerize-application \
  -m "merge: add application containerization"
```

Verified historical merge:

```text
35cfa89
```

---

## 8.21 Push `develop`

Primary:

```bash
git push \
  github \
  develop
```

Secondary:

```bash
git push \
  gitlab \
  develop
```

---

## 8.22 Verify Synchronization

Run:

```bash
git fetch github
git fetch gitlab

printf 'Local develop: '
git rev-parse develop

printf 'GitHub develop: '
git rev-parse github/develop

printf 'GitLab develop: '
git rev-parse gitlab/develop
```

The verified historical value at this stage was:

```text
35cfa896e527c51183350ea672c1f89095dd4dbd
```

Feature branch:

```text
07958dbc20e83608b2674f763f8c2a4241ffa459
```

All copies were synchronized before moving to pipeline preparation.

---

## 8.23 Verified Containerization Git History

At this point the history contained:

```text
35cfa89 merge: add application containerization
|
| 07958db feat: containerize Java Maven application
|
3132735 merge: add application status unit test
```

This preserves the focused feature branch and non-fast-forward merge history.

---

## 8.24 Security Review

Verified positives:

```text
no credentials in Dockerfile
no credentials in image command
.env excluded
private key extensions excluded
Git metadata excluded
Maven original JAR excluded
```

Known basic-training limitation:

```text
container runs using the base image's default user
```

Future improvement:

```text
run application with a dedicated non-root user
```

Do not change the current historical Dockerfile merely to make the documentation appear more advanced.

---

## 8.25 Troubleshooting — Port Already in Use

If Docker reports that port `8080` cannot be bound:

```bash
lsof -nP \
  -iTCP:8080 \
  -sTCP:LISTEN
```

Identify the process first.

Do not kill an unrelated process without understanding what it is.

Alternative temporary test mapping:

```bash
docker run -d \
  --name java-maven-app-local \
  -p 8081:8080 \
  java-maven-app:1.1.0-SNAPSHOT
```

Then test:

```text
http://localhost:8081
```

The verified historical project used `8080:8080`.

---

## 8.26 Troubleshooting — Container Exits Immediately

Inspect:

```bash
docker ps -a \
  --filter name=java-maven-app-local

docker logs \
  java-maven-app-local
```

Check:

```text
JAR copied successfully
ENTRYPOINT correct
Java runtime available
application port correct
application startup exception
```

Do not repeatedly recreate the container without first reading its logs.

---

## 8.27 Troubleshooting — JAR Missing During Build

A Docker error involving:

```text
target/java-maven-app-*.jar
```

usually means the Maven artifact was not built before Docker.

Verify:

```bash
find target \
  -maxdepth 1 \
  -type f \
  -name 'java-maven-app-*.jar' \
  -print
```

If missing:

```bash
mvn clean package
```

Then repeat:

```bash
docker build \
  -t java-maven-app:1.1.0-SNAPSHOT \
  .
```

This reinforces the mandatory order:

```text
artifact creation
before
containerization
```

---

## 8.28 Evidence to Capture

Capture:

```text
Dockerfile
.dockerignore

docker build output

docker image ls java-maven-app

docker image inspect result

docker run output

docker ps

docker logs java-maven-app-local

curl HTTP 200

expected HTML heading

docker inspect container state

docker stop

HTTP test after stop

docker rm

git status

feature commit

merge commit

GitHub/GitLab synchronization
```

Do not capture unrelated Docker containers if screenshots can be limited to the capstone container.

---

## 8.29 Cost

Local Docker validation:

```text
AWS cost: none
ECR cost: none
EKS cost: none
Load balancer cost: none
```

No cloud resource needs to exist yet.

---

## 8.30 Containerization Completion Checklist

```text
[ ] feature/containerize-application created
[ ] executable Maven artifact exists
[ ] Dockerfile created
[ ] .dockerignore created
[ ] Dockerfile reviewed
[ ] Docker context reviewed
[ ] image built successfully
[ ] image tag verified
[ ] image architecture inspected
[ ] container started
[ ] port 8080 mapped correctly
[ ] Spring Boot logs verified
[ ] HTTP 200 verified
[ ] expected application content verified
[ ] container state inspected
[ ] container stopped
[ ] stopped application became unreachable
[ ] test container removed
[ ] only Dockerfile and .dockerignore committed
[ ] commit 07958db represented containerization work
[ ] feature branch pushed
[ ] merged using --no-ff
[ ] merge 35cfa89 represented integration
[ ] develop synchronized to GitHub
[ ] develop synchronized to GitLab
[ ] no cloud infrastructure created
```

---

# 9. Pipeline Preparation

## 9.1 Objective

Prepare the complete CI/CD pipeline locally before provisioning or changing cloud infrastructure.

This phase follows the project’s local-first rule:

```text
application build
→ tests
→ artifact
→ Docker image
→ local container validation
→ prepare Jenkins pipeline
→ prepare Jenkins Shared Library
→ prepare Kubernetes manifests
→ validate files locally
→ commit and push
→ only then provision cloud resources
```

No Amazon EKS cluster, Amazon ECR repository, load balancer, or other chargeable AWS resource is required merely to create the pipeline files.

---

## 9.2 Nana Learning Sequence

The TechWorld with Nana pipeline progression relevant to this project is:

```text
increment application version
→ build application
→ build Docker image
→ deploy
→ commit version update back to Git
```

The training initially demonstrated much of this directly inside a Jenkinsfile so that the CI/CD flow remained visible and easy to understand.

The same concepts were later extracted into reusable Jenkins Shared Library functions.

This project deliberately keeps Nana's learning sequence while moving reusable logic into the shared library.

The final design is:

```text
Application repository
        │
        │ Jenkinsfile
        ▼
Jenkins Shared Library
        │
        ├── incrementVersion()
        ├── buildMaven()
        ├── buildDockerImage()
        ├── pushToEcr()
        ├── deployToEks()
        └── commitVersion()
```

---

# 9.3 Two Repositories Participate in the Pipeline

The CI/CD implementation spans two independent repositories.

Application repository:

```text
complete-jenkins-cicd-pipeline-eks-ecr
```

Shared-library repository:

```text
jenkins-shared-library
```

Their responsibilities are intentionally different.

Application repository:

```text
application source
pom.xml
Dockerfile
Jenkinsfile
Kubernetes manifests
application tests
```

Shared library:

```text
pipeline orchestration
Maven helper
Docker helper
registry helper
Kubernetes deployment helper
version increment helper
Git commit-back helper
pipeline configuration validation
```

This prevents the application Jenkinsfile from becoming a large collection of repeated CI/CD implementation details.

---

# 9.4 Shared Library Repository

Local path:

```text
/Users/younghadiz/Documents/tech-workspace/jenkins-shared-library
```

GitHub:

```text
https://github.com/younghadiz/jenkins-shared-library
```

GitLab:

```text
git@gitlab.com:younghadiz/jenkins-shared-library.git
```

Before changing the library:

```bash
cd /Users/younghadiz/Documents/tech-workspace/jenkins-shared-library

pwd

git status

git branch --show-current

git remote -v
```

The historical restructure began from a clean `master` branch.

---

# 9.5 Why the Shared Library Was Restructured

The earlier shared library was much smaller:

```text
jenkins-shared-library/
├── vars/
│   ├── buildJar.groovy
│   └── buildImage.groovy
└── src/
    └── com/younghadiz/devops/
        └── Docker.groovy
```

This worked for the training exercises but packed several responsibilities together.

The project adopted the clearer structure:

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
├── resources/
│   └── com/younghadiz/templates/
│       └── deployment.yaml.template
│
├── test/
│   └── README.md
│
├── .gitignore
└── README.md
```

The key design rule is:

```text
vars/
→ Jenkins-facing public pipeline steps

src/
→ reusable implementation classes

resources/
→ reusable reference resources

test/
→ future shared-library tests
```

---

# 9.6 Shared Library Restructure Branch

The historical branch was:

```text
feature/restructure-shared-library
```

Before switching:

```bash
git status
git branch --show-current
git remote -v
```

Create:

```bash
git switch -c feature/restructure-shared-library
```

---

# 9.7 Remove the Legacy Files

Before deleting anything:

```bash
pwd
git status
git branch --show-current
git remote -v
```

The legacy files removed were:

```text
vars/buildJar.groovy
vars/buildImage.groovy
src/com/younghadiz/devops/Docker.groovy
```

A temporary repository-level `Jenkinsfile` that was not required for the shared library was also removed.

Commands used during the restructure:

```bash
rm Jenkinsfile
rm vars/buildJar.groovy
rm vars/buildImage.groovy
rm src/com/younghadiz/devops/Docker.groovy
```

Verify:

```bash
for file in \
  Jenkinsfile \
  vars/buildJar.groovy \
  vars/buildImage.groovy \
  src/com/younghadiz/devops/Docker.groovy
do
    if [ -e "$file" ]; then
        echo "ERROR - STILL EXISTS: $file"
    else
        echo "REMOVED: $file"
    fi
done
```

Historical verification showed all four were removed.

---

# 9.8 `PipelineConfig.groovy`

Path:

```text
src/com/younghadiz/devops/PipelineConfig.groovy
```

Complete file:

```groovy
package com.younghadiz.devops

class PipelineConfig implements Serializable {

    static String required(Map config, String key) {
        def value = config[key]

        if (value == null || value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Required pipeline configuration '${key}' was not provided."
            )
        }

        return value.toString()
    }

    static String optional(
        Map config,
        String key,
        String defaultValue = ''
    ) {
        def value = config[key]

        if (value == null || value.toString().trim().isEmpty()) {
            return defaultValue
        }

        return value.toString()
    }
}
```

Purpose:

```text
centralize required parameter validation
centralize optional/default parameter handling
avoid repeating validation code in pipeline entry points
```

---

# 9.9 `DockerUtils.groovy`

Path:

```text
src/com/younghadiz/devops/DockerUtils.groovy
```

Complete file:

```groovy
package com.younghadiz.devops

class DockerUtils implements Serializable {

    def script

    DockerUtils(script) {
        this.script = script
    }

    void buildImage(
        String appDir,
        String imageName,
        String imageTag
    ) {
        validateImage(imageName, imageTag)

        script.dir(appDir) {
            script.echo "Building Docker image: ${imageName}:${imageTag}"

            script.withEnv([
                "DOCKER_IMAGE_NAME=${imageName}",
                "DOCKER_IMAGE_TAG=${imageTag}"
            ]) {
                script.sh '''
                    set -e

                    command -v docker >/dev/null 2>&1 || {
                        echo "ERROR: Docker is not available on the Jenkins agent."
                        exit 1
                    }

                    docker build \
                        -t "$DOCKER_IMAGE_NAME:$DOCKER_IMAGE_TAG" \
                        .
                '''
            }
        }
    }

    void pushToDockerHub(
        String imageName,
        String imageTag,
        String credentialsId
    ) {
        validateImage(imageName, imageTag)

        script.echo "Pushing Docker image to Docker Hub: ${imageName}:${imageTag}"

        script.withCredentials([
            script.usernamePassword(
                credentialsId: credentialsId,
                usernameVariable: 'DOCKERHUB_USER',
                passwordVariable: 'DOCKERHUB_PASS'
            )
        ]) {
            script.withEnv([
                "DOCKER_IMAGE_NAME=${imageName}",
                "DOCKER_IMAGE_TAG=${imageTag}"
            ]) {
                script.sh '''
                    set -e

                    echo "$DOCKERHUB_PASS" |
                        docker login \
                            --username "$DOCKERHUB_USER" \
                            --password-stdin

                    docker push \
                        "$DOCKER_IMAGE_NAME:$DOCKER_IMAGE_TAG"
                '''
            }
        }
    }

    private void validateImage(
        String imageName,
        String imageTag
    ) {
        if (!imageName?.trim() ||
            imageName == 'null' ||
            imageName.contains('null')) {
            script.error(
                "Docker image name is empty or invalid: ${imageName}"
            )
        }

        if (!imageTag?.trim()) {
            script.error 'Docker image tag is empty.'
        }
    }
}
```

This deliberately separates:

```text
Docker build
from
Docker registry push
```

so Jenkins can represent them as separate pipeline stages.

---

# 9.10 `AwsUtils.groovy`

Path:

```text
src/com/younghadiz/devops/AwsUtils.groovy
```

Complete file:

```groovy
package com.younghadiz.devops

class AwsUtils implements Serializable {

    def script

    AwsUtils(script) {
        this.script = script
    }

    void pushToEcr(
        String imageName,
        String imageTag,
        String awsRegion,
        String ecrRegistryServer,
        String credentialsId
    ) {
        validateEcrConfig(
            imageName,
            imageTag,
            awsRegion,
            ecrRegistryServer
        )

        script.echo "Pushing Docker image to AWS ECR: ${imageName}:${imageTag}"

        script.withCredentials([
            script.usernamePassword(
                credentialsId: credentialsId,
                usernameVariable: 'AWS_ACCESS_KEY_ID',
                passwordVariable: 'AWS_SECRET_ACCESS_KEY'
            )
        ]) {
            script.withEnv([
                "DOCKER_IMAGE_NAME=${imageName}",
                "DOCKER_IMAGE_TAG=${imageTag}",
                "AWS_REGION_VALUE=${awsRegion}",
                "ECR_REGISTRY_SERVER_VALUE=${ecrRegistryServer}"
            ]) {
                script.sh '''
                    set -e

                    command -v aws >/dev/null 2>&1 || {
                        echo "ERROR: AWS CLI is not installed on the Jenkins agent."
                        exit 1
                    }

                    command -v docker >/dev/null 2>&1 || {
                        echo "ERROR: Docker is not available on the Jenkins agent."
                        exit 1
                    }

                    echo "Logging in to AWS ECR..."

                    aws ecr get-login-password \
                        --region "$AWS_REGION_VALUE" |
                        docker login \
                            --username AWS \
                            --password-stdin "$ECR_REGISTRY_SERVER_VALUE"

                    echo "Pushing Docker image to AWS ECR..."

                    docker push \
                        "$DOCKER_IMAGE_NAME:$DOCKER_IMAGE_TAG"
                '''
            }
        }
    }

    private void validateEcrConfig(
        String imageName,
        String imageTag,
        String awsRegion,
        String ecrRegistryServer
    ) {
        if (!imageName?.trim() ||
            imageName == 'null' ||
            imageName.contains('null')) {
            script.error(
                "ECR image name is empty or invalid: ${imageName}"
            )
        }

        if (!imageTag?.trim()) {
            script.error 'ECR image tag is required.'
        }

        if (!awsRegion?.trim()) {
            script.error 'AWS region is required for ECR.'
        }

        if (!ecrRegistryServer?.trim()) {
            script.error 'ECR registry server is required.'
        }
    }
}
```

The credential ID is only a Jenkins reference.

Never store actual AWS values in this file.

---

# 9.11 `KubernetesUtils.groovy`

Path:

```text
src/com/younghadiz/devops/KubernetesUtils.groovy
```

Complete file:

```groovy
package com.younghadiz.devops

class KubernetesUtils implements Serializable {

    def script

    KubernetesUtils(script) {
        this.script = script
    }

    void deployToEks(
        String appDir,
        String manifestDir,
        String appName,
        String imageName,
        String imageTag,
        String namespace = 'default'
    ) {
        validateDeploymentConfig(
            manifestDir,
            appName,
            imageName,
            imageTag,
            namespace
        )

        script.dir(appDir) {
            script.echo "Deploying ${appName} to Kubernetes..."
            script.echo "Namespace: ${namespace}"
            script.echo "Image: ${imageName}:${imageTag}"

            script.withEnv([
                "APP_NAME=${appName}",
                "IMAGE_NAME=${imageName}",
                "IMAGE_TAG=${imageTag}",
                "K8S_NAMESPACE=${namespace}"
            ]) {
                script.sh """
                    set -e

                    command -v kubectl >/dev/null 2>&1 || {
                        echo "ERROR: kubectl is not installed on the Jenkins agent."
                        exit 1
                    }

                    command -v envsubst >/dev/null 2>&1 || {
                        echo "ERROR: envsubst is not installed on the Jenkins agent."
                        exit 1
                    }

                    test -f "${manifestDir}/deployment.yaml" || {
                        echo "ERROR: ${manifestDir}/deployment.yaml was not found."
                        exit 1
                    }

                    test -f "${manifestDir}/service.yaml" || {
                        echo "ERROR: ${manifestDir}/service.yaml was not found."
                        exit 1
                    }

                    echo "Applying Kubernetes deployment..."

                    envsubst < "${manifestDir}/deployment.yaml" |
                        kubectl apply \
                            --namespace "\$K8S_NAMESPACE" \
                            -f -

                    echo "Applying Kubernetes service..."

                    envsubst < "${manifestDir}/service.yaml" |
                        kubectl apply \
                            --namespace "\$K8S_NAMESPACE" \
                            -f -

                    echo "Waiting for deployment rollout..."

                    kubectl rollout status \
                        deployment/"\$APP_NAME" \
                        --namespace "\$K8S_NAMESPACE" \
                        --timeout=180s
                """
            }
        }
    }

    private void validateDeploymentConfig(
        String manifestDir,
        String appName,
        String imageName,
        String imageTag,
        String namespace
    ) {
        if (!manifestDir?.trim()) {
            script.error 'Kubernetes manifest directory is required.'
        }

        if (!appName?.trim()) {
            script.error 'Application name is required.'
        }

        if (!imageName?.trim()) {
            script.error 'Docker image name is required.'
        }

        if (!imageTag?.trim()) {
            script.error 'Docker image tag is required.'
        }

        if (!namespace?.trim()) {
            script.error 'Kubernetes namespace is required.'
        }
    }
}
```

Important separation:

```text
singleServicePipeline
→ provides pipeline configuration and credentials

deployToEks
→ Jenkins-facing helper

KubernetesUtils
→ Kubernetes mechanics
```

This separation later made the EKS authentication problem easier to diagnose.

---

# 9.12 `buildMaven.groovy`

Path:

```text
vars/buildMaven.groovy
```

Complete file:

```groovy
#!/usr/bin/env groovy

def call(
    String appDir = '.',
    String mavenCommand = 'mvn clean package'
) {
    dir(appDir) {
        echo 'Building Java Maven application...'
        echo "Running Maven command: ${mavenCommand}"

        sh "${mavenCommand}"
    }
}
```

Default command:

```bash
mvn clean package
```

This performs:

```text
clean
→ compile
→ test
→ package
```

---

# 9.13 `buildDockerImage.groovy`

Path:

```text
vars/buildDockerImage.groovy
```

Complete file:

```groovy
#!/usr/bin/env groovy

import com.younghadiz.devops.DockerUtils

def call(
    String appDir,
    String imageName,
    String imageTag
) {
    echo 'Building Docker image...'

    new DockerUtils(this).buildImage(
        appDir,
        imageName,
        imageTag
    )
}
```

---

# 9.14 `pushToDockerHub.groovy`

Path:

```text
vars/pushToDockerHub.groovy
```

Complete file:

```groovy
#!/usr/bin/env groovy

import com.younghadiz.devops.DockerUtils

def call(
    String imageName,
    String imageTag,
    String credentialsId = 'dockerhub-creds'
) {
    echo 'Pushing Docker image to Docker Hub...'

    new DockerUtils(this).pushToDockerHub(
        imageName,
        imageTag,
        credentialsId
    )
}
```

This capability is retained for reuse even though this capstone deploys through Amazon ECR.

---

# 9.15 `pushToEcr.groovy`

Path:

```text
vars/pushToEcr.groovy
```

Complete file:

```groovy
#!/usr/bin/env groovy

import com.younghadiz.devops.AwsUtils

def call(
    String imageName,
    String imageTag,
    String awsRegion,
    String ecrRegistryServer,
    String credentialsId = 'aws_ecr_creds'
) {
    echo 'Pushing Docker image to AWS ECR...'

    new AwsUtils(this).pushToEcr(
        imageName,
        imageTag,
        awsRegion,
        ecrRegistryServer,
        credentialsId
    )
}
```

---

# 9.16 `deployToEks.groovy`

Path:

```text
vars/deployToEks.groovy
```

Complete file:

```groovy
#!/usr/bin/env groovy

import com.younghadiz.devops.KubernetesUtils

def call(
    String appDir,
    String manifestDir,
    String appName,
    String imageName,
    String imageTag,
    String namespace = 'default'
) {
    echo 'Deploying application to Amazon EKS...'

    new KubernetesUtils(this).deployToEks(
        appDir,
        manifestDir,
        appName,
        imageName,
        imageTag,
        namespace
    )
}
```

---

# 9.17 Maven Incremental Versioning

Nana's training introduced Maven Build Helper and Versions plugins to calculate the next Maven version dynamically.

Starting version:

```text
1.1.0-SNAPSHOT
```

Desired next patch version:

```text
1.1.1
```

Conceptually:

```text
major = 1
minor = 1
incremental = 0
nextIncremental = 1
```

The Maven command is:

```bash
mvn build-helper:parse-version versions:set \
  '-DnewVersion=${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.nextIncrementalVersion}' \
  versions:commit
```

The single quotes are important in a shell context because Maven must receive `${parsedVersion...}` rather than having the shell attempt to expand those expressions.

---

# 9.18 `incrementVersion.groovy`

Path:

```text
vars/incrementVersion.groovy
```

Complete file:

```groovy
#!/usr/bin/env groovy

def call(String appDir = '.') {

    def version = ''

    dir(appDir) {
        echo 'Incrementing application version...'

        sh '''
            mvn build-helper:parse-version versions:set \
                '-DnewVersion=${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.nextIncrementalVersion}' \
                versions:commit
        '''

        def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'

        if (!matcher.find()) {
            error 'Unable to read application version from pom.xml'
        }

        version = matcher.group(1)

        echo "Application version: ${version}"
    }

    return version
}
```

Verified pipeline behavior later showed:

```text
1.1.0-SNAPSHOT
→ 1.1.1
```

The pipeline then combines this application version with Jenkins:

```text
BUILD_NUMBER
```

to produce an image tag.

Example:

```text
Application version:
1.1.1

Jenkins build:
2

Docker tag:
1.1.1-2
```

This behavior was later confirmed in the successful Jenkins build.

---

# 9.19 Why Jenkins Must Commit the Version Back

Without commit-back:

```text
Git contains:
1.1.0-SNAPSHOT

Jenkins build #1:
1.1.0-SNAPSHOT
→ 1.1.1

workspace disappears / next checkout occurs

Git still contains:
1.1.0-SNAPSHOT

Jenkins build #2:
1.1.0-SNAPSHOT
→ 1.1.1
```

The version would repeatedly reset.

Nana's solution was:

```text
Jenkins modifies pom.xml
→ git add
→ git commit
→ git push
```

so Git becomes the source of truth for the new application version.

---

# 9.20 Detached HEAD Consideration

Jenkins Multibranch checkouts commonly operate at a specific commit.

This can result in a detached HEAD state.

Therefore the safe push form is:

```bash
git push <remote> HEAD:<branch>
```

rather than relying on:

```bash
git push
```

The project implements this pattern.

---

# 9.21 `commitVersion.groovy`

Path:

```text
vars/commitVersion.groovy
```

Complete file:

```groovy
#!/usr/bin/env groovy

def call(
    String appDir = '.',
    String credentialsId = 'github-token',
    String repositoryUrl = '',
    String targetBranch = '',
    String commitMessage = 'ci: version bump',
    String gitUserName = 'jenkins',
    String gitUserEmail = 'jenkins@example.com'
) {
    if (!repositoryUrl?.trim()) {
        error 'Git repository URL is required.'
    }

    def branch = targetBranch?.trim()
        ? targetBranch.trim()
        : env.BRANCH_NAME

    if (!branch?.trim()) {
        error 'Unable to determine the Git branch for version commit.'
    }

    dir(appDir) {
        echo 'Committing application version update...'
        echo "Target branch: ${branch}"

        withCredentials([
            usernamePassword(
                credentialsId: credentialsId,
                usernameVariable: 'GIT_USER',
                passwordVariable: 'GIT_PASS'
            )
        ]) {
            withEnv([
                "GIT_REPOSITORY_URL=${repositoryUrl}",
                "GIT_TARGET_BRANCH=${branch}",
                "GIT_COMMIT_MESSAGE=${commitMessage}",
                "GIT_COMMITTER_NAME=${gitUserName}",
                "GIT_COMMITTER_EMAIL=${gitUserEmail}"
            ]) {
                sh '''
                    set -e

                    git config user.email "$GIT_COMMITTER_EMAIL"
                    git config user.name "$GIT_COMMITTER_NAME"

                    echo "Git status before version commit:"
                    git status --short

                    echo "Current Git branch information:"
                    git branch --show-current || true

                    git add pom.xml

                    if git diff --cached --quiet; then
                        echo "No version change to commit."
                        exit 0
                    fi

                    git commit -m "$GIT_COMMIT_MESSAGE"

                    echo "Pushing version commit to $GIT_TARGET_BRANCH..."

                    GIT_ASKPASS="$(mktemp)"
                    export GIT_ASKPASS
                    export GIT_TERMINAL_PROMPT=0

                    cat > "$GIT_ASKPASS" <<'EOF'
#!/bin/sh

case "$1" in
    *Username*)
        printf '%s\n' "$GIT_USER"
        ;;
    *Password*)
        printf '%s\n' "$GIT_PASS"
        ;;
esac
EOF

                    chmod 700 "$GIT_ASKPASS"

                    trap 'rm -f "$GIT_ASKPASS"' EXIT

                    git push \
                        "$GIT_REPOSITORY_URL" \
                        "HEAD:$GIT_TARGET_BRANCH"
                '''
            }
        }
    }
}
```

---

# 9.22 Why `GIT_ASKPASS` Is Used

An earlier simple Nana-style implementation embedded the username/password into a temporary HTTPS remote URL.

This teaches the Git authentication concept clearly but can expose credentials more easily through process output or remote configuration.

The current shared-library implementation instead creates a temporary:

```text
GIT_ASKPASS
```

helper.

Jenkins supplies:

```text
GIT_USER
GIT_PASS
```

through Credentials Binding.

Git asks the helper for authentication only when needed.

The temporary script is removed through:

```bash
trap 'rm -f "$GIT_ASKPASS"' EXIT
```

This preserves Nana's commit-back concept while avoiding permanently embedding credentials into the Git remote.

---

# 9.23 Only `pom.xml` Is Committed Automatically

The helper intentionally runs:

```bash
git add pom.xml
```

rather than:

```bash
git add .
```

This is important.

It prevents unrelated Jenkins workspace changes from accidentally becoming part of the automated CI commit.

---

# 9.24 Jenkins Commit Identity

The automated identity is:

```text
jenkins <jenkins@example.com>
```

This identity has two purposes:

1. Identify machine-generated version commits.
2. Allow the Jenkins Multibranch Ignore Committer Strategy to exclude these commits from starting another build.

---

# 9.25 Pipeline Loop Problem

Without protection:

```text
developer commit
→ webhook
→ Jenkins
→ increment version
→ Jenkins commit
→ webhook
→ Jenkins
→ increment again
→ Jenkins commit
→ webhook
→ ...
```

This becomes an infinite CI loop.

---

# 9.26 Nana's Ignore Committer Strategy

The chosen learning method is the Jenkins:

```text
Ignore Committer Strategy
```

Configure the Multibranch Pipeline so commits from:

```text
jenkins@example.com
```

do not cause another build.

Conceptual flow:

```text
Developer commit
      │
      ▼
GitHub webhook
      │
      ▼
Jenkins Multibranch
      │
      ▼
pipeline runs
      │
      ▼
Jenkins commits pom.xml
as jenkins@example.com
      │
      ▼
SCM event occurs
      │
      ▼
Ignore Committer Strategy
      │
      ▼
no recursive pipeline
```

This preserves normal developer-triggered builds while suppressing the machine-generated version commit.

---

# 9.27 Shared Library Single-Service Entry Point

This project contains one independently deployable application.

Therefore it uses:

```text
singleServicePipeline.groovy
```

and not:

```text
multiServicePipeline.groovy
```

A Jenkins **Multibranch Pipeline** and a **multi-service pipeline** are different concepts.

```text
Multibranch Pipeline
→ Jenkins discovers multiple Git branches

multiServicePipeline
→ one repository contains multiple independently buildable/deployable services
```

This capstone uses:

```text
Jenkins Multibranch Pipeline
+
singleServicePipeline()
```

---

# 9.28 `singleServicePipeline.groovy`

Path:

```text
vars/singleServicePipeline.groovy
```

Complete final file:

```groovy
#!/usr/bin/env groovy

import com.younghadiz.devops.PipelineConfig

def call(Map config = [:]) {

    def appDir = PipelineConfig.optional(
        config,
        'appDir',
        '.'
    )

    def manifestDir = PipelineConfig.optional(
        config,
        'manifestDir',
        'kubernetes'
    )

    def appName = PipelineConfig.required(
        config,
        'appName'
    )

    def registryType = PipelineConfig.optional(
        config,
        'registryType',
        'ecr'
    ).toLowerCase()

    def imageName = PipelineConfig.required(
        config,
        'imageName'
    )

    def awsRegion = PipelineConfig.optional(
        config,
        'awsRegion',
        ''
    )

    def ecrRegistryServer = PipelineConfig.optional(
        config,
        'ecrRegistryServer',
        ''
    )

    def ecrCredentialsId = PipelineConfig.optional(
        config,
        'ecrCredentialsId',
        'aws_ecr_creds'
    )

    def dockerHubCredentialsId = PipelineConfig.optional(
        config,
        'dockerHubCredentialsId',
        'dockerhub-creds'
    )

    def gitCredentialsId = PipelineConfig.optional(
        config,
        'gitCredentialsId',
        'github-token'
    )

    def repositoryUrl = PipelineConfig.required(
        config,
        'repositoryUrl'
    )

    def namespace = PipelineConfig.optional(
        config,
        'namespace',
        'default'
    )

    pipeline {
        agent any

        tools {
            maven 'Maven'
        }

        stages {

            stage('Increment Version') {
                steps {
                    script {
                        echo "Running pipeline for branch: ${env.BRANCH_NAME}"

                        def version = incrementVersion(appDir)

                        env.APP_VERSION = version
                        env.IMAGE_TAG = "${version}-${env.BUILD_NUMBER}"

                        echo "Application version: ${env.APP_VERSION}"
                        echo "Docker image tag: ${env.IMAGE_TAG}"
                    }
                }
            }

            stage('Build Application') {
                steps {
                    script {
                        buildMaven(
                            appDir,
                            'mvn clean package'
                        )
                    }
                }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                        buildDockerImage(
                            appDir,
                            imageName,
                            env.IMAGE_TAG
                        )
                    }
                }
            }

            stage('Push Docker Image') {
                steps {
                    script {

                        if (registryType == 'ecr') {

                            pushToEcr(
                                imageName,
                                env.IMAGE_TAG,
                                awsRegion,
                                ecrRegistryServer,
                                ecrCredentialsId
                            )

                        } else if (registryType == 'dockerhub') {

                            pushToDockerHub(
                                imageName,
                                env.IMAGE_TAG,
                                dockerHubCredentialsId
                            )

                        } else {
                            error(
                                "Unsupported registry type: ${registryType}"
                            )
                        }
                    }
                }
            }

            stage('Deploy') {
                steps {
                    script {
                        withCredentials([
                            usernamePassword(
                                credentialsId: ecrCredentialsId,
                                usernameVariable: 'AWS_ACCESS_KEY_ID',
                                passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                            )
                        ]) {
                            withEnv([
                                "AWS_DEFAULT_REGION=${awsRegion}",
                                "AWS_REGION=${awsRegion}"
                            ]) {
                                deployToEks(
                                    appDir,
                                    manifestDir,
                                    appName,
                                    imageName,
                                    env.IMAGE_TAG,
                                    namespace
                                )
                            }
                        }
                    }
                }
            }

            stage('Commit Version Update') {
                steps {
                    script {
                        commitVersion(
                            appDir,
                            gitCredentialsId,
                            repositoryUrl,
                            env.BRANCH_NAME,
                            'ci: version bump',
                            'jenkins',
                            'jenkins@example.com'
                        )
                    }
                }
            }
        }

        post {
            success {
                echo 'Pipeline completed successfully.'
            }

            failure {
                echo 'Pipeline failed.'
            }

            always {
                echo 'Pipeline finished.'
            }
        }
    }
}
```

### Important historical note

The AWS credential wrapper around the Deploy stage was added **later as a confirmed troubleshooting fix**.

The original version called:

```groovy
deployToEks(...)
```

without wrapping the Deploy stage in AWS credentials.

That later caused EKS authentication to fail because kubeconfig invokes:

```text
aws eks get-token
```

and the ECR credential scope had already ended.

The confirmed fix is documented later in the troubleshooting/deployment sections.

The complete file above represents the **final verified implementation**, not the first pre-fix version.

---

# 9.29 `multiServicePipeline.groovy`

Path:

```text
vars/multiServicePipeline.groovy
```

Complete file:

```groovy
#!/usr/bin/env groovy

/*
  Multi-service pipeline entry point.

  Intended for repositories containing multiple independently
  buildable and deployable services, for example:

  services/
  ├── frontend/
  ├── backend/
  └── worker/

  This pipeline will be implemented when a project requires
  multiple services.

  Current single-service projects should use:

      singleServicePipeline(...)

  Keeping this entry point separate prevents multi-service
  requirements from adding unnecessary complexity to the
  single-service pipeline.
*/

def call(Map config = [:]) {
    error '''
Multi-service pipeline is not implemented yet.

Use singleServicePipeline(...) for a single deployable application.
Implement this pipeline when a project contains multiple independently
buildable or deployable services.
'''
}
```

This file exists intentionally but is not used by the capstone.

---

# 9.30 Shared Library Reference Kubernetes Template

Path:

```text
resources/com/younghadiz/templates/deployment.yaml.template
```

Complete file:

```yaml
# Reference Kubernetes deployment template.
#
# Application repositories should normally maintain their own
# Kubernetes manifests.
#
# This file is provided as a reusable reference for future projects
# that choose to load templates from the Jenkins Shared Library.

apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${APP_NAME}
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ${APP_NAME}
  template:
    metadata:
      labels:
        app: ${APP_NAME}
    spec:
      containers:
        - name: ${APP_NAME}
          image: ${IMAGE_NAME}:${IMAGE_TAG}
          ports:
            - containerPort: 8080
```

The current pipeline does **not** automatically use this resource.

The application repository owns its deployment manifests.

---

# 9.31 Shared Library Test Placeholder

Path:

```text
test/README.md
```

Complete file:

```markdown
# Shared Library Tests

Automated tests for the Jenkins Shared Library are not implemented yet.

The current library is intentionally kept at the scope required for the DevOps training projects.

A future improvement can introduce Jenkins Pipeline Unit or another suitable Groovy/Jenkins testing framework to validate shared-library functions independently from a Jenkins controller.
```

---

# 9.32 Verify the Shared Library Structure

Run:

```bash
find . \
  -path './.git' -prune -o \
  -type f -print | sort
```

Expected important structure:

```text
./.gitignore
./README.md
./resources/com/younghadiz/templates/deployment.yaml.template
./src/com/younghadiz/devops/AwsUtils.groovy
./src/com/younghadiz/devops/DockerUtils.groovy
./src/com/younghadiz/devops/KubernetesUtils.groovy
./src/com/younghadiz/devops/PipelineConfig.groovy
./test/README.md
./vars/buildDockerImage.groovy
./vars/buildMaven.groovy
./vars/commitVersion.groovy
./vars/deployToEks.groovy
./vars/incrementVersion.groovy
./vars/multiServicePipeline.groovy
./vars/pushToDockerHub.groovy
./vars/pushToEcr.groovy
./vars/singleServicePipeline.groovy
```

Check for legacy API references:

```bash
grep -R \
  --exclude-dir=.git \
  --exclude=README.md \
  -nE \
  'buildJar\(|buildImage\(|import com\.younghadiz\.devops\.Docker$|new Docker\(' \
  vars src resources test \
  || echo "No legacy API references found."
```

The historical check returned only legitimate references inside the new Docker implementation rather than the old API.

---

# 9.33 Commit the Shared Library Restructure

Inspect:

```bash
git status
git diff --stat
git diff
```

Stage the intended restructure.

Then:

```bash
git commit \
  -m "refactor: restructure Jenkins shared library"
```

Verified historical feature commit:

```text
f881dba
```

Merge to `master` using a non-fast-forward merge.

Verified historical merge:

```text
4e922ff merge: restructure Jenkins shared library
```

Push both remotes.

---

# 9.34 Return to the Application Repository

LOCAL:

```bash
cd /Users/younghadiz/Documents/tech-workspace/devops-capstone-projects/complete-jenkins-cicd-pipeline-eks-ecr
```

Verify:

```bash
pwd

git status

git branch --show-current

git remote -v
```

---

# 9.35 Pipeline Preparation Feature Branch

The verified branch was:

```text
feature/prepare-jenkins-pipeline
```

Create from `develop`:

```bash
git switch -c feature/prepare-jenkins-pipeline
```

---

# 9.36 Application Kubernetes Directory

Create:

```bash
mkdir -p kubernetes
```

Application-specific Kubernetes manifests stay in the application repository:

```text
kubernetes/
├── deployment.yaml
└── service.yaml
```

This allows application and deployment changes to be versioned together.

---

# 9.37 Kubernetes Deployment Manifest

Path:

```text
kubernetes/deployment.yaml
```

Complete verified file:

```yaml
apiVersion: apps/v1
kind: Deployment

metadata:
  name: ${APP_NAME}

spec:
  replicas: 1

  selector:
    matchLabels:
      app: ${APP_NAME}

  template:
    metadata:
      labels:
        app: ${APP_NAME}

    spec:
      containers:
        - name: ${APP_NAME}
          image: ${IMAGE_NAME}:${IMAGE_TAG}
          imagePullPolicy: Always

          ports:
            - containerPort: 8080
```

The variables:

```text
${APP_NAME}
${IMAGE_NAME}
${IMAGE_TAG}
```

are replaced at deployment time using:

```text
envsubst
```

This keeps environment-specific image values out of the committed Kubernetes file.

---

# 9.38 Kubernetes Service Manifest

Path:

```text
kubernetes/service.yaml
```

Complete verified file:

```yaml
apiVersion: v1
kind: Service

metadata:
  name: ${APP_NAME}

spec:
  selector:
    app: ${APP_NAME}

  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080

  type: LoadBalancer
```

Traffic mapping:

```text
AWS load balancer
        │
        ▼
Kubernetes Service port 80
        │
        ▼
targetPort 8080
        │
        ▼
Spring Boot container
```

The `LoadBalancer` resource is only prepared here.

It does **not** create a real AWS load balancer until the manifest is later applied to EKS.

---

# 9.39 Kubernetes Manifest Local Validation

Because the variables are intentionally unresolved, create temporary values only for validation.

Example:

```bash
export APP_NAME=java-maven-app
export IMAGE_NAME=example.invalid/java-maven-app
export IMAGE_TAG=local-validation
```

Render:

```bash
envsubst < kubernetes/deployment.yaml

envsubst < kubernetes/service.yaml
```

Inspect that the output contains:

```text
java-maven-app
example.invalid/java-maven-app:local-validation
```

Do not apply the manifests to a cluster during this phase.

The objective is local/static preparation only.

---

# 9.40 Application Jenkinsfile Design

The application repository deliberately keeps its Jenkinsfile small.

The shared library provides the pipeline implementation.

Final structure:

```groovy
@Library('jenkins-shared-library') _

singleServicePipeline(
    ...
)
```

Nana's training showed that:

```text
@Library('jenkins-shared-library') _
```

loads a Jenkins Shared Library registered through Jenkins Global Pipeline Libraries.

The underscore separates the annotation from the pipeline code when no direct import or variable follows it.

---

# 9.41 Reproducible Pre-Cloud Jenkinsfile

At Pipeline Preparation time, cloud-specific values may not exist yet.

Use safe placeholders:

```groovy
@Library('jenkins-shared-library') _

singleServicePipeline(
    appName: 'java-maven-app',

    appDir: '.',

    manifestDir: 'kubernetes',

    registryType: 'ecr',

    imageName: '<AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/java-maven-app',

    awsRegion: '<AWS_REGION>',

    ecrRegistryServer: '<AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com',

    ecrCredentialsId: 'aws_ecr_creds',

    gitCredentialsId: 'github-token',

    repositoryUrl: 'https://github.com/younghadiz/complete-jenkins-cicd-pipeline-eks-ecr.git',

    namespace: 'default'
)
```

This is the **reproducible local-preparation version**.

The actual verified AWS account/region values were inserted later during AWS configuration.

Do not put AWS secret keys in this file.

---

# 9.42 Final Verified Jenkinsfile

After cloud configuration, the final project file became:

```groovy
@Library('jenkins-shared-library') _

singleServicePipeline(
    appName: 'java-maven-app',

    appDir: '.',

    manifestDir: 'kubernetes',

    registryType: 'ecr',

    imageName: '002184382122.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app',

    awsRegion: 'ca-central-1',

    ecrRegistryServer: '002184382122.dkr.ecr.ca-central-1.amazonaws.com',

    ecrCredentialsId: 'aws_ecr_creds',

    gitCredentialsId: 'github-token',

    repositoryUrl: 'https://github.com/younghadiz/complete-jenkins-cicd-pipeline-eks-ecr.git',

    namespace: 'default'
)
```

The AWS account ID is a resource identifier rather than a secret.

For a reusable copy, replace it with:

```text
<AWS_ACCOUNT_ID>
```

---

# 9.43 Pipeline Flow

The application Jenkinsfile delegates to:

```text
singleServicePipeline()
```

which performs:

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

This matches Nana's learning progression while making the Jenkinsfile easier to read.

---

# 9.44 Jenkins Tool Name

The shared library uses:

```groovy
tools {
    maven 'Maven'
}
```

Therefore Jenkins must later have a Maven installation configured with exactly:

```text
Maven
```

The Jenkins UI setup itself belongs to the later server/cloud provisioning phase.

---

# 9.45 Pipeline Credential References

The application Jenkinsfile uses credential IDs only:

```text
aws_ecr_creds
github-token
```

No secret values belong in Git.

These credentials are created/configured later in Jenkins.

---

# 9.46 Commit Pipeline Preparation

Inspect:

```bash
git status

git diff -- Jenkinsfile

git diff -- kubernetes/deployment.yaml

git diff -- kubernetes/service.yaml
```

Stage:

```bash
git add \
  Jenkinsfile \
  kubernetes/deployment.yaml \
  kubernetes/service.yaml
```

Verify:

```bash
git diff --cached
```

Commit:

```bash
git commit \
  -m "ci: prepare Jenkins pipeline and Kubernetes deployment"
```

Verified historical feature commit:

```text
b6185a1
```

---

# 9.47 Push Feature Branch

GitHub primary:

```bash
git push -u \
  github \
  feature/prepare-jenkins-pipeline
```

GitLab secondary:

```bash
git push \
  gitlab \
  feature/prepare-jenkins-pipeline
```

---

# 9.48 Merge to `develop`

Before switching:

```bash
git status
git branch --show-current
git remote -v
```

Switch:

```bash
git switch develop
```

Merge:

```bash
git merge --no-ff \
  feature/prepare-jenkins-pipeline \
  -m "merge: prepare Jenkins pipeline and Kubernetes deployment"
```

Verified historical merge:

```text
809a44f
```

Push:

```bash
git push github develop
git push gitlab develop
```

---

# 9.49 Phase 9 Historical Git Position

At this point the application history contained:

```text
809a44f merge: prepare Jenkins pipeline and Kubernetes deployment
|
| b6185a1 ci: prepare Jenkins pipeline and Kubernetes deployment
|
35cfa89 merge: add application containerization
```

ECR-specific configuration was added later:

```text
86a5f33 ci: configure AWS ECR deployment
```

and merged as:

```text
e217b83 merge: configure AWS ECR deployment
```

Do not incorrectly describe `86a5f33` as part of the initial pipeline-preparation commit.

---

# 9.50 Phase 9 Part 1 Security Review

Prepared locally:

```text
Jenkinsfile
Kubernetes manifests
shared-library code
credential IDs
Git commit identity
```

Not committed:

```text
AWS secret access key
GitHub token
GitLab token
kubeconfig
private SSH keys
Jenkins passwords
Docker passwords
```

---

# 9.51 Cost

Pipeline preparation itself:

```text
AWS infrastructure cost: none
ECR storage cost: none
EKS cost: none
load balancer cost: none
```

Jenkins may already exist from training, but no new cloud provisioning is required simply to prepare these files.

---

# 9.52 Phase 9 Part 1 Verification Checklist

```text
[ ] shared-library repository inspected
[ ] feature/restructure-shared-library created
[ ] legacy buildJar/buildImage/Docker files removed
[ ] PipelineConfig created
[ ] DockerUtils created
[ ] AwsUtils created
[ ] KubernetesUtils created
[ ] buildMaven created
[ ] buildDockerImage created
[ ] pushToDockerHub created
[ ] pushToEcr created
[ ] deployToEks created
[ ] incrementVersion created
[ ] commitVersion created
[ ] singleServicePipeline created
[ ] multiServicePipeline placeholder created
[ ] Kubernetes reference template created
[ ] test README created
[ ] shared-library restructure committed
[ ] commit f881dba represented restructure
[ ] merge 4e922ff represented integration
[ ] application feature/prepare-jenkins-pipeline created
[ ] Jenkinsfile prepared
[ ] deployment.yaml prepared
[ ] service.yaml prepared
[ ] manifests rendered locally with envsubst
[ ] no cloud apply performed
[ ] b6185a1 represented pipeline preparation
[ ] 809a44f represented merge to develop
[ ] GitHub synchronized
[ ] GitLab synchronized
[ ] no secrets committed
```

---

## Phase 9 continuation

## 9.53 Jenkins-Side Preparation

The application and shared-library files can be prepared locally, but Jenkins must also be configured to understand and execute them.

The Jenkins-side preparation required by this project is:

```text
configure Maven tool
→ configure GitHub credential
→ configure AWS credential reference
→ register Jenkins Shared Library
→ install Ignore Committer Strategy plugin
→ create Multibranch Pipeline
→ configure branch source
→ configure branch discovery
→ configure Ignore Committer Strategy
→ configure repository webhook
→ scan repository
→ verify Jenkinsfile discovery
→ verify shared-library loading
```

This section documents configuration only.

Actual cloud deployment belongs to later phases.

---

## 9.54 Jenkins Administrator vs Pipeline Configuration

Nana's Jenkins training separates Jenkins administration from pipeline use.

Jenkins administration includes:

```text
plugins
credentials
tools
global shared libraries
security
nodes / agents
system settings
```

Pipeline configuration includes:

```text
job creation
repository configuration
branch discovery
Jenkinsfile execution
build triggers
CI/CD workflow
```

In a small learning environment the same DevOps Engineer may perform both roles.

---

## 9.55 Required Jenkins Capabilities

For this project, Jenkins needs access to:

```text
Git
Java
Maven
Docker
AWS CLI
kubectl
envsubst
GitHub
Amazon ECR
Amazon EKS
```

Some of these are configured through Jenkins itself.

Others must exist in the Jenkins runtime/container and are documented later under:

```text
Phase 12 — Server and Cloud Provisioning
```

Do not install cloud tooling merely because the Jenkinsfile already references it.

The files are prepared first.

The runtime is provisioned later.

---

## 9.56 Configure Maven in Jenkins

Nana's training path was:

```text
Jenkins
→ Manage Jenkins
→ Global Tool Configuration
→ Maven
→ Add Maven
```

On Jenkins versions where the UI has been reorganized, the equivalent may appear under:

```text
Manage Jenkins
→ Tools
```

The important requirement is the configured Maven installation name.

The current shared library contains:

```groovy
tools {
    maven 'Maven'
}
```

Therefore the Jenkins Maven installation must be named exactly:

```text
Maven
```

Do not configure it as:

```text
Maven3.9
Maven-3.9
Apache Maven
```

unless the shared-library code is changed to match.

Recommended learning setup:

```text
Name:
Maven

Install automatically:
Enabled

Version:
A Jenkins-supported Maven 3.x release
```

The exact Maven patch version is not an assignment requirement.

The configured name is critical because Jenkins resolves the tool by name.

---

## 9.57 Why Tool Names Must Match

This shared-library code:

```groovy
tools {
    maven 'Maven'
}
```

means:

```text
look in Jenkins configured tools
→ locate Maven installation called "Maven"
→ place that Maven installation in the pipeline environment
```

If Jenkins instead contains:

```text
Maven3.9
```

the pipeline may fail to resolve:

```text
Maven
```

even though Maven itself is installed.

---

## 9.58 Verify Maven Configuration

After saving the Maven tool, the eventual Jenkins pipeline should contain a Declarative stage similar to:

```text
Declarative: Tool Install
```

A successful pipeline later confirmed that Jenkins resolved the Maven tool and executed:

```bash
mvn build-helper:parse-version ...
```

and:

```bash
mvn clean package
```

successfully.

The successful build produced:

```text
Application version:
1.1.1
```

and then built:

```text
java-maven-app-1.1.1.jar
```

---

## 9.59 Configure Jenkins Credentials

Training path:

```text
Jenkins
→ Manage Jenkins
→ Credentials
→ System
→ Global credentials
```

Do not place credential values into:

```text
Jenkinsfile
shared-library source
README
RUNBOOK
GitHub
GitLab
screenshots
terminal history
```

Only credential IDs belong in source code.

---

## 9.60 GitHub Credential

The application repository uses:

```text
github-token
```

Purpose:

```text
checkout application source
push Jenkins-generated Maven version commit
```

Recommended credential representation for this Nana-aligned implementation:

```text
Kind:
Username with password

ID:
github-token

Username:
<GITHUB_USERNAME>

Password:
<GITHUB_PERSONAL_ACCESS_TOKEN>
```

For this project:

```text
Username:
younghadiz
```

Do not document the actual token.

The token must have sufficient repository permissions for the operations Jenkins performs.

At minimum Jenkins requires the ability to:

```text
read repository contents
checkout branches
push updated pom.xml to the branch
```

---

## 9.61 Verified Application Checkout Credential

The successful capstone Jenkins console later showed:

```text
using credential github-token
```

while fetching:

```text
https://github.com/younghadiz/complete-jenkins-cicd-pipeline-eks-ecr.git
```

This confirms that the Multibranch application source used the intended Jenkins credential.

---

## 9.62 AWS Credential Reference

The current shared library expects:

```text
aws_ecr_creds
```

with the Nana-aligned Jenkins credential representation:

```text
Kind:
Username with password

ID:
aws_ecr_creds

Username:
AWS Access Key ID

Password:
AWS Secret Access Key
```

The shared library maps those fields to:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
```

Example from the library:

```groovy
usernamePassword(
    credentialsId: credentialsId,
    usernameVariable: 'AWS_ACCESS_KEY_ID',
    passwordVariable: 'AWS_SECRET_ACCESS_KEY'
)
```

Never replace the credential ID in source code with actual AWS keys.

The IAM identity and its permissions are documented under:

```text
Phase 11 — Security Configuration
```

---

## 9.63 Docker Hub Credential

The reusable shared library also supports:

```text
dockerhub-creds
```

Expected type:

```text
Kind:
Username with password

ID:
dockerhub-creds
```

This credential is not required for the current ECR deployment path.

It exists so the shared library remains reusable for projects using Docker Hub.

---

## 9.64 Credential Scope Principle

Use this rule:

```text
source code
→ credential ID only

Jenkins credential store
→ actual secret
```

Example:

```groovy
ecrCredentialsId: 'aws_ecr_creds'
```

is safe to commit.

This is not:

```groovy
awsSecretAccessKey: 'actual-secret-value'
```

---

## 9.65 Register the Jenkins Shared Library

Nana's training path:

```text
Jenkins
→ Manage Jenkins
→ System
→ Global Pipeline Libraries
→ Add
```

Configure:

```text
Name:
jenkins-shared-library
```

This name must match:

```groovy
@Library('jenkins-shared-library') _
```

in the application Jenkinsfile.

---

## 9.66 Shared Library Default Version

For the learning implementation:

```text
Default Version:
master
```

The project currently uses:

```groovy
@Library('jenkins-shared-library') _
```

without an explicit version.

Jenkins therefore uses the default version configured in Global Pipeline Libraries.

Verified successful pipeline behavior later showed:

```text
Loading library jenkins-shared-library@master
```

---

## 9.67 Shared Library Retrieval Method

Choose:

```text
Retrieval method:
Modern SCM
```

Then:

```text
SCM:
Git
```

Repository:

```text
https://github.com/younghadiz/jenkins-shared-library.git
```

The repository is public.

The verified successful capstone library checkout showed:

```text
No credentials specified
```

for the shared-library repository.

Therefore credentials are not required merely to read this public shared library.

This is different from the private/authenticated application checkout, which used:

```text
github-token
```

---

## 9.68 Global Library Configuration Summary

Reproducible configuration:

```text
Name:
jenkins-shared-library

Default version:
master

Load implicitly:
No

Allow default version to be overridden:
Yes / acceptable for learning and troubleshooting

Retrieval:
Modern SCM

SCM:
Git

Repository:
https://github.com/younghadiz/jenkins-shared-library.git

Credentials:
None required while repository remains public
```

If the shared-library repository becomes private, configure an appropriate GitHub credential.

---

## 9.69 Shared Library Version Override

The global configuration may define:

```text
master
```

but a Jenkinsfile can explicitly request another version:

```groovy
@Library('jenkins-shared-library@v1.0.0') _
```

or temporarily:

```groovy
@Library('jenkins-shared-library@fix/eks-deployment-aws-credentials') _
```

The capstone later used this behavior to test the EKS credential fix without merging the fix immediately.

This allowed the application test branch to load:

```text
fix/eks-deployment-aws-credentials
```

while normal pipelines continued using:

```text
master
```

---

## 9.70 Production Shared-Library Versioning Improvement

Using:

```text
master
```

is easy for training but means a change to the shared-library master branch can affect every pipeline using the default version.

A more controlled production pattern is:

```text
v1.0.0
v1.1.0
v2.0.0
```

Then:

```groovy
@Library('jenkins-shared-library@v1.0.0') _
```

provides a predictable pipeline dependency.

This is a future production improvement.

Do not silently replace the verified capstone's `master` behavior.

---

## 9.71 Create the Multibranch Pipeline

Nana's sequence:

```text
Jenkins
→ New Item
→ Multibranch Pipeline
```

For a reproducible project, use a clear name such as:

```text
complete-jenkins-cicd-pipeline-eks-ecr
```

The exact Jenkins display name is less important than using one Multibranch Pipeline for this application repository.

---

## 9.72 Why Multibranch Pipeline Is Used

The Git workflow contains:

```text
main
develop
feature/*
bugfix/*
hotfix/*
docs/*
```

A Multibranch Pipeline allows Jenkins to:

```text
scan repository
→ discover branches
→ look for Jenkinsfile
→ create branch-specific Jenkins jobs
```

This avoids manually creating a separate Jenkins job for every branch.

---

## 9.73 Configure the Branch Source

Inside the Multibranch job:

```text
Configure
→ Branch Sources
→ Add source
```

Use Git/GitHub according to the installed Jenkins branch-source plugin.

Repository:

```text
https://github.com/younghadiz/complete-jenkins-cicd-pipeline-eks-ecr.git
```

Credential:

```text
github-token
```

The successful Jenkins execution later confirmed this repository was fetched using `github-token`.

---

## 9.74 Discover Branches

Configure branch discovery so Jenkins can see the project branches.

Nana's training used the concept:

```text
Discover branches
→ all branches
```

or a branch-name filter matching:

```text
.*
```

The goal is:

```text
main
develop
feature/add-application-test
feature/containerize-application
feature/prepare-jenkins-pipeline
feature/configure-aws-deployment
bugfix/...
```

can be discovered when they contain a Jenkinsfile.

The verified Jenkins scan later reported seven remote branches during the capstone test.

---

## 9.75 Script Path

Set:

```text
Script Path:
Jenkinsfile
```

The application repository keeps the Jenkinsfile at the repository root:

```text
complete-jenkins-cicd-pipeline-eks-ecr/Jenkinsfile
```

Do not use a different script path unless the file is actually moved.

---

## 9.76 What Happens During Multibranch Scan

Jenkins performs:

```text
connect to GitHub
→ inspect branches
→ find Jenkinsfile
→ create/update branch jobs
```

For example:

```text
complete-jenkins-cicd-pipeline-eks-ecr
├── develop
├── main
├── feature/configure-aws-deployment
└── bugfix/verify-eks-deployment-credentials
```

Jenkins workspaces may sanitize or shorten job/branch names.

Do not depend on the workspace directory name as a permanent API.

---

## 9.77 Verified Branch Discovery

During the final capstone validation Jenkins reported branches including:

```text
bugfix/verify-eks-deployment-credentials
develop
feature/add-application-test
feature/configure-aws-deployment
feature/containerize-application
feature/prepare-jenkins-pipeline
main
```

and reported:

```text
Seen 7 remote branches
```

This confirmed that Multibranch branch discovery was working.

---

## 9.78 Nana's Branch-Based Pipeline Lesson

Nana's Multibranch lesson also teaches branch-specific behavior.

Example:

```groovy
stage('Build') {
    when {
        branch 'master'
    }

    steps {
        sh 'mvn package'
    }
}
```

The learning objective is:

```text
feature branch
→ test only

main/master
→ test
→ build
→ deploy
```

This is an important production CI/CD pattern.

---

## 9.79 Current Capstone Branch Behavior

The verified `singleServicePipeline.groovy` used by this capstone does **not** currently contain:

```groovy
when {
    branch 'main'
}
```

or equivalent deployment branch guards.

Therefore do not document the current implementation as:

```text
feature branches only test
main deploys
```

because that is not what the final shared-library file implements.

Instead, current behavior is:

```text
Jenkins discovers a branch
→ Jenkinsfile loads singleServicePipeline()
→ all configured stages are available to run
```

The Multibranch job/build strategy determines which discovered branch events actually result in builds.

---

## 9.80 Future Branch Policy Improvement

A later production enhancement could use:

```groovy
when {
    branch 'main'
}
```

for:

```text
Push Docker Image
Deploy
Commit Version Update
```

while allowing test/build validation on feature branches.

Alternatively:

```text
develop
→ integration deployment

main
→ release deployment
```

This is a future workflow decision.

Do not silently add it to the historical capstone implementation.

---

## 9.81 Install Ignore Committer Strategy

Nana's training solution for the Jenkins commit loop is the Jenkins plugin:

```text
Ignore Committer Strategy
```

Install:

```text
Jenkins
→ Manage Jenkins
→ Plugins
→ Available plugins
→ search "Ignore Committer Strategy"
→ Install
```

Restart Jenkins only if the plugin installation process requires it.

---

## 9.82 Why Ignore Committer Strategy Is Required

The pipeline contains:

```text
Increment Version
...
Commit Version Update
```

Jenkins changes:

```text
pom.xml
```

and pushes:

```text
ci: version bump
```

back to GitHub.

Without protection:

```text
developer push
→ Jenkins starts
→ Jenkins pushes version commit
→ repository event
→ Jenkins starts again
→ version changes again
→ Jenkins pushes again
→ infinite loop
```

---

## 9.83 Jenkins Commit Identity

The project deliberately uses:

```text
Name:
jenkins

Email:
jenkins@example.com
```

The final shared-library call is:

```groovy
commitVersion(
    appDir,
    gitCredentialsId,
    repositoryUrl,
    env.BRANCH_NAME,
    'ci: version bump',
    'jenkins',
    'jenkins@example.com'
)
```

This gives the machine-generated commit a predictable identity.

---

## 9.84 Configure Ignore Committer Strategy

Open:

```text
Jenkins
→ <Multibranch Pipeline>
→ Configure
→ Branch Sources
→ Build Strategies
→ Add
→ Ignore Committer Strategy
```

Ignored author/committer:

```text
jenkins@example.com
```

Enable the option equivalent to:

```text
Allow builds for all other authors
```

Save.

---

## 9.85 Expected Ignore-Committer Behavior

Expected workflow:

```text
Gafari pushes developer commit
        ↓
GitHub event
        ↓
Jenkins runs pipeline
        ↓
Jenkins commits pom.xml
        ↓
GitHub receives Jenkins commit
        ↓
Jenkins evaluates committer
        ↓
jenkins@example.com matches ignore rule
        ↓
no recursive pipeline
```

Nana's training verified this behavior by confirming that the Jenkins-generated commit did not create another pipeline run.

---

## 9.86 Why This Is Preferred to Skipping Stages

Another approach would be:

```text
pipeline starts
→ checkout
→ inspect author
→ skip the stages
```

That still creates a build entry for every machine commit.

Ignore Committer Strategy prevents the unwanted build from starting.

For this learning project, this is cleaner.

---

## 9.87 Configure GitHub Webhook

Repository side:

```text
GitHub
→ complete-jenkins-cicd-pipeline-eks-ecr
→ Settings
→ Webhooks
→ Add webhook
```

Learning/reproducible payload:

```text
http://<JENKINS_SERVER>:8080/github-webhook/
```

Content type:

```text
application/json
```

Event selection:

```text
Just the push event
```

Enable the webhook.

Do not put Jenkins administrator credentials in the webhook URL.

---

## 9.88 Webhook Flow

The intended behavior is:

```text
developer git push
        ↓
GitHub
        ↓
webhook
        ↓
Jenkins
        ↓
Multibranch branch indexing / event handling
        ↓
branch Jenkinsfile
        ↓
pipeline
```

This removes the need for a person to repeatedly click:

```text
Build Now
```

after every developer commit.

---

## 9.89 Training vs Current Git Provider

Nana's original training examples often used GitLab.

The underlying concept is the same:

```text
SCM receives push
→ SCM notifies Jenkins
→ Jenkins evaluates branch/job
→ pipeline starts
```

This capstone uses:

```text
GitHub
```

as the primary Jenkins-connected repository.

GitLab is the synchronized secondary repository.

Do not configure two competing webhook sources for the same normal build flow unless that is intentional.

---

## 9.90 Webhook Security Note

The simple training architecture may expose Jenkins through:

```text
http://<SERVER_IP>:8080
```

A production Jenkins webhook endpoint should instead normally use:

```text
HTTPS
domain name
reverse proxy or load balancer
controlled firewall/security rules
```

This is a future production hardening improvement.

The capstone remains intentionally close to Nana's basic learning setup.

---

## 9.91 Manual Repository Scan

After saving the Multibranch configuration, Jenkins can be instructed to:

```text
Scan Multibranch Pipeline Now
```

The scan should:

```text
connect to GitHub
discover branches
find Jenkinsfile
create branch jobs
```

This is useful before relying solely on automatic webhook events.

---

## 9.92 Verify Jenkinsfile Discovery

The Jenkins branch-indexing or branch console output should include behavior equivalent to:

```text
Obtained Jenkinsfile from <commit>
```

If Jenkins says no Jenkinsfile exists:

verify:

```text
repository URL
branch
Script Path
Jenkinsfile committed
Jenkinsfile pushed
credential access
```

---

## 9.93 Verify Shared Library Load

Expected output:

```text
Loading library jenkins-shared-library@master
```

Then Jenkins should resolve the branch.

The verified successful capstone run loaded:

```text
jenkins-shared-library@master
```

at shared-library commit:

```text
5bcdcbd9316227c545052ea94fbdc159b5b7ace0
```

at that point in the project's history.

That commit represented:

```text
merge: fix EKS deployment AWS credentials
```

The exact SHA will naturally change after future library commits.

---

## 9.94 Verify Application Checkout

Expected console behavior includes:

```text
using credential github-token
```

and:

```text
Fetching upstream changes from:
https://github.com/younghadiz/complete-jenkins-cicd-pipeline-eks-ecr.git
```

A successful final build checked out the intended `develop` revision.

---

## 9.95 Jenkins Git Tool Warning

The successful Jenkins console also displayed:

```text
Selected Git installation does not exist. Using Default
The recommended git tool is: NONE
```

The pipeline still completed successfully because Git itself was available in the Jenkins runtime.

Therefore this was:

```text
non-blocking in the verified project
```

but it indicates Jenkins Git-tool configuration could be cleaned up later.

Future improvement:

```text
Manage Jenkins
→ Tools
→ configure a valid Git installation
```

or allow Jenkins to use the expected system Git configuration consistently.

Do not treat this warning as evidence that the pipeline failed.

---

## 9.96 Shared Library Parameter Mapping

The application Jenkinsfile provides:

| Parameter           | Value in this project                                                | Purpose                          |
| ------------------- | -------------------------------------------------------------------- | -------------------------------- |
| `appName`           | `java-maven-app`                                                     | Application and Deployment name  |
| `appDir`            | `.`                                                                  | Application root                 |
| `manifestDir`       | `kubernetes`                                                         | Kubernetes manifests             |
| `registryType`      | `ecr`                                                                | Container registry selection     |
| `imageName`         | `<AWS_ACCOUNT_ID>.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app` | Complete ECR image repository    |
| `awsRegion`         | `ca-central-1`                                                       | AWS region                       |
| `ecrRegistryServer` | `<AWS_ACCOUNT_ID>.dkr.ecr.ca-central-1.amazonaws.com`                | ECR registry host                |
| `ecrCredentialsId`  | `aws_ecr_creds`                                                      | Jenkins AWS credential reference |
| `gitCredentialsId`  | `github-token`                                                       | Jenkins Git push credential      |
| `repositoryUrl`     | GitHub capstone repository                                           | Version commit destination       |
| `namespace`         | `default`                                                            | Kubernetes namespace             |

The actual verified account ID can be inserted for the deployed project, but reusable copies of the runbook should use:

```text
<AWS_ACCOUNT_ID>
```

---

## 9.97 Pipeline Environment Values Created at Runtime

The shared pipeline creates:

```text
APP_VERSION
IMAGE_TAG
```

Example:

```text
APP_VERSION=1.1.1

BUILD_NUMBER=2

IMAGE_TAG=1.1.1-2
```

This means the Docker tag is not hardcoded in the Jenkinsfile.

---

## 9.98 Expected Jenkins Pipeline Stages

The shared-library pipeline exposes:

```text
Increment Version
Build Application
Build Docker Image
Push Docker Image
Deploy
Commit Version Update
```

Jenkins also creates Declarative internal stages such as:

```text
Declarative: Checkout SCM
Declarative: Tool Install
Declarative: Post Actions
```

These internal stages are expected Jenkins behavior and are not additional project phases.

---

## 9.99 Pipeline Preparation Static Verification

Before cloud deployment, verify the application repository:

```bash
pwd

git status

git branch --show-current

cat Jenkinsfile

cat kubernetes/deployment.yaml

cat kubernetes/service.yaml
```

Verify placeholders:

```bash
grep -R \
  -nE '\$\{APP_NAME\}|\$\{IMAGE_NAME\}|\$\{IMAGE_TAG\}' \
  kubernetes/
```

Expected variables:

```text
APP_NAME
IMAGE_NAME
IMAGE_TAG
```

---

## 9.100 Verify No Secrets Are Present

Run:

```bash
grep -R \
  --exclude-dir=.git \
  --exclude=RUNBOOK.md \
  -nE \
  'AWS_SECRET_ACCESS_KEY=|AWS_ACCESS_KEY_ID=|ghp_|github_pat_|BEGIN (RSA|OPENSSH|PRIVATE) KEY' \
  . \
  || echo "No obvious committed secret patterns found."
```

This is only a basic local check.

It is not a replacement for a dedicated secret scanner.

---

## 9.101 Verify Git-Traced Files

Run:

```bash
git ls-files
```

Expected important pipeline files include:

```text
Jenkinsfile
Dockerfile
.dockerignore
kubernetes/deployment.yaml
kubernetes/service.yaml
```

Sensitive local files must not appear.

---

## 9.102 Shared Library Local Verification

In:

```text
/Users/younghadiz/Documents/tech-workspace/jenkins-shared-library
```

run:

```bash
git status

git branch --show-current

git remote -v

find vars src resources test \
  -type f \
  -print | sort
```

Verify the final shared-library structure before Jenkins loads it.

---

## 9.103 Current Shared-Library Testing Limitation

The shared library currently does not contain automated Jenkins Pipeline Unit tests.

Validation occurs through:

```text
Jenkins integration execution
```

using consuming application pipelines.

This is acceptable for the learning project.

Future improvement:

```text
Jenkins Pipeline Unit
Groovy unit tests
automated shared-library CI
```

---

## 9.104 Pipeline Preparation Troubleshooting — Shared Library Not Found

Symptom:

```text
Library not found
Could not resolve library
```

Check:

```text
Global Pipeline Library name
Repository URL
Default Version
Git connectivity
Requested @Library name
Requested branch/tag
```

The names must match exactly:

```text
Global library:
jenkins-shared-library

Jenkinsfile:
@Library('jenkins-shared-library') _
```

---

## 9.105 Pipeline Preparation Troubleshooting — Maven Tool Not Found

Symptom:

```text
No such tool
Maven installation not found
```

Check Jenkins Maven tool configuration.

Shared library expects:

```text
Maven
```

not:

```text
Maven3.9
```

unless the code is changed.

---

## 9.106 Pipeline Preparation Troubleshooting — GitHub Checkout Fails

Check:

```text
repository URL
github-token credential ID
token validity
token repository permission
network access
branch existence
```

Do not paste the token into the Jenkinsfile while troubleshooting.

---

## 9.107 Pipeline Preparation Troubleshooting — Jenkins Cannot Push Version

Possible causes:

```text
github-token lacks write permission
wrong repositoryUrl
target branch does not exist
GitHub rejects authentication
credential kind incorrect
```

The push form should remain:

```text
HEAD:<branch>
```

because Jenkins may be in detached HEAD state.

---

## 9.108 Pipeline Preparation Troubleshooting — Recursive Builds

Check:

```text
Ignore Committer Strategy plugin installed
build strategy configured
ignored email exactly matches Jenkins commit email
```

Expected ignored email:

```text
jenkins@example.com
```

Verify Jenkins commits are actually authored/committed with that identity.

---

## 9.109 Pipeline Preparation Troubleshooting — Webhook Does Not Trigger

First verify that manual:

```text
Scan Multibranch Pipeline Now
```

works.

Then inspect:

```text
GitHub webhook delivery status
Jenkins accessibility from GitHub
payload URL
firewall/network rule
push-event selection
branch discovery settings
```

Do not weaken unrelated server security rules without identifying the actual connectivity problem.

---

## 9.110 Evidence to Capture

Capture screenshots or text evidence of:

```text
Jenkins Maven tool named Maven

Global Pipeline Library:
jenkins-shared-library

Default library version:
master

Shared library repository URL

Multibranch Pipeline configuration

GitHub application repository URL

github-token credential ID
but never its token value

branch discovery configuration

Jenkinsfile script path

Ignore Committer Strategy

ignored email:
jenkins@example.com

GitHub webhook configuration
without exposing secrets

successful branch indexing

Obtained Jenkinsfile message

Loading library jenkins-shared-library@master

application checkout using github-token
```

---

## 9.111 Security Review

Pipeline Preparation follows these rules:

```text
credentials stored only in Jenkins
credential IDs committed to Git
no access keys in Jenkinsfile
no tokens in shared library
no kubeconfig in repository
no private keys in repository
only pom.xml automatically committed by Jenkins
temporary GIT_ASKPASS deleted after push
```

Known future improvements include:

```text
shared-library version tags
short-lived AWS authentication
more restrictive branch deployment policies
HTTPS Jenkins endpoint
automated secret scanning
shared-library unit tests
```

---

## 9.112 Cost Review

Jenkins configuration itself does not create new AWS application infrastructure.

Possible existing cost:

```text
DigitalOcean Jenkins server
```

The following should still not be created merely to finish this phase:

```text
EKS cluster
EC2 worker nodes
Kubernetes LoadBalancer
ECR image storage
```

Those belong to later phases.

---

## 9.113 Phase 9 Final Completion Checklist

```text
[ ] Jenkins Shared Library restructured
[ ] shared-library source committed
[ ] shared-library master updated
[ ] Maven tool named Maven configured
[ ] github-token credential configured
[ ] aws_ecr_creds reference planned/configured securely
[ ] Jenkins Global Pipeline Library registered
[ ] library name is jenkins-shared-library
[ ] default version is master
[ ] Modern SCM configured
[ ] shared-library Git repository configured
[ ] Multibranch Pipeline created
[ ] application GitHub repository configured
[ ] branch discovery configured
[ ] Script Path set to Jenkinsfile
[ ] Ignore Committer Strategy plugin installed
[ ] jenkins@example.com ignored
[ ] all other developer authors allowed
[ ] GitHub webhook configured
[ ] manual branch scan succeeds
[ ] Jenkinsfile discovered
[ ] shared library loads
[ ] application repository checkout works
[ ] no credentials committed to Git
[ ] Kubernetes manifests remain unapplied during local preparation
[ ] pipeline preparation committed and pushed
```

---

## 9.114 Phase 9 Final State

The verified project now has everything needed to progress from local application development to infrastructure preparation:

```text
Java application
        ✅

automated test
        ✅

JAR artifact
        ✅

Dockerfile
        ✅

local Docker validation
        ✅

Jenkins Shared Library
        ✅

Maven version increment
        ✅

Docker build helper
        ✅

ECR push helper
        ✅

EKS deployment helper
        ✅

Git commit-back helper
        ✅

Ignore Committer Strategy design
        ✅

Multibranch Pipeline design
        ✅

Kubernetes Deployment manifest
        ✅

Kubernetes Service manifest
        ✅

application Jenkinsfile
        ✅
```

Pipeline Preparation is now complete.

---

# 10. Infrastructure Preparation

## 10.1 Objective

Prepare the AWS infrastructure plan, names, configuration values, tooling, verification commands, cost expectations, and reproducible provisioning commands before actually creating chargeable cloud resources.

The project order remains:

```text
pipeline preparation
→ infrastructure preparation
→ security configuration
→ server and cloud provisioning
```

Therefore this phase answers:

```text
What resources will be created?
What will they be called?
Where will they run?
What configuration will they use?
What commands will create them?
What will they cost?
What must exist before provisioning?
How will success be verified?
How will they later be removed?
```

The resources are actually created in:

```text
Phase 12 — Server and Cloud Provisioning
```

---

## 10.2 Nana-Aligned Infrastructure Scope

The project intentionally uses a simple infrastructure design close to the TechWorld with Nana learning approach.

Required cloud components:

```text
DigitalOcean
└── Jenkins server

AWS
├── Amazon ECR
│   └── java-maven-app
│
└── Amazon EKS
    ├── cluster: java-maven-eks
    └── managed nodegroup: java-maven-nodes
```

The Kubernetes application later creates:

```text
Deployment
└── java-maven-app

Service
└── java-maven-app
    └── type: LoadBalancer
```

The `LoadBalancer` Service later causes AWS to provision an external load balancer.

---

## 10.3 What Is Not Required

This Nana-aligned implementation deliberately avoids additional infrastructure such as:

```text
Terraform
Helm
Argo CD
AWS Load Balancer Controller
Ingress
Route 53
ACM
NAT Gateway custom design
custom VPC Terraform modules
Fargate
multiple node groups
multi-environment clusters
Prometheus/Grafana infrastructure
service mesh
```

Those may be useful in larger production systems but are not necessary for this learning project.

---

## 10.4 AWS Region

Verified project region:

```text
ca-central-1
```

All AWS resources in this project should use the same region unless there is a clear reason otherwise.

Set locally:

```bash
export AWS_REGION=ca-central-1
export AWS_DEFAULT_REGION=ca-central-1
```

Verify:

```bash
printf 'AWS_REGION=%s\n' "$AWS_REGION"
printf 'AWS_DEFAULT_REGION=%s\n' "$AWS_DEFAULT_REGION"
```

Expected:

```text
AWS_REGION=ca-central-1
AWS_DEFAULT_REGION=ca-central-1
```

---

## 10.5 Verify AWS CLI Availability

LOCAL:

```bash
aws --version
```

Verify authentication without exposing credentials:

```bash
aws sts get-caller-identity
```

This command may display:

```text
AWS account ID
IAM ARN
user/role identifier
```

These are identifiers rather than secret keys.

Never display:

```text
AWS_SECRET_ACCESS_KEY
AWS_SESSION_TOKEN
passwords
private keys
```

in the runbook or screenshots.

---

## 10.6 Determine the AWS Account ID Programmatically

The ECR registry URL contains the AWS account ID.

Do not hardcode a reusable runbook around another person's account.

Retrieve it:

```bash
export AWS_ACCOUNT_ID="$(
  aws sts get-caller-identity \
    --query Account \
    --output text
)"
```

Verify:

```bash
printf 'AWS account: %s\n' "$AWS_ACCOUNT_ID"
```

Verified account used by this specific project:

```text
002184382122
```

For reusable documentation use:

```text
<AWS_ACCOUNT_ID>
```

---

## 10.7 Verify Required Local Cloud Tools

Run:

```bash
echo "===== AWS CLI ====="
aws --version

echo
echo "===== eksctl ====="
eksctl version

echo
echo "===== kubectl ====="
kubectl version --client

echo
echo "===== Docker ====="
docker --version
```

Verified `eksctl` version during final source recovery:

```text
0.229.0-dev+489531af5.2026-07-01T23:39:06Z
```

The runbook does not require that exact build.

The important requirement is a supported `eksctl` version compatible with the target EKS Kubernetes version.

---

## 10.8 Resource Naming Plan

Use predictable application-specific names.

```text
Application:
java-maven-app

AWS Region:
ca-central-1

ECR Repository:
java-maven-app

EKS Cluster:
java-maven-eks

EKS Nodegroup:
java-maven-nodes

Kubernetes Namespace:
default
```

Define them:

```bash
export APP_NAME=java-maven-app
export ECR_REPOSITORY=java-maven-app
export EKS_CLUSTER_NAME=java-maven-eks
export EKS_NODEGROUP_NAME=java-maven-nodes
export K8S_NAMESPACE=default
```

Verify:

```bash
printf 'APP_NAME=%s\n' "$APP_NAME"
printf 'ECR_REPOSITORY=%s\n' "$ECR_REPOSITORY"
printf 'EKS_CLUSTER_NAME=%s\n' "$EKS_CLUSTER_NAME"
printf 'EKS_NODEGROUP_NAME=%s\n' "$EKS_NODEGROUP_NAME"
printf 'K8S_NAMESPACE=%s\n' "$K8S_NAMESPACE"
```

---

## 10.9 Construct the ECR Registry Values

Registry server:

```text
<AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com
```

Repository URI:

```text
<AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/java-maven-app
```

Create the shell values:

```bash
export ECR_REGISTRY_SERVER="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

export ECR_IMAGE_NAME="${ECR_REGISTRY_SERVER}/${ECR_REPOSITORY}"
```

Verify:

```bash
printf 'ECR registry: %s\n' "$ECR_REGISTRY_SERVER"
printf 'ECR image repository: %s\n' "$ECR_IMAGE_NAME"
```

Verified project values were:

```text
Registry:
002184382122.dkr.ecr.ca-central-1.amazonaws.com

Repository:
002184382122.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app
```

---

## 10.10 ECR Configuration Plan

Verified final ECR repository configuration:

```text
Repository:
java-maven-app

Region:
ca-central-1

Tag mutability:
MUTABLE

Scan on push:
true

Encryption:
AES256
```

This is the configuration the reusable command must reproduce.

---

## 10.11 Reproducible ECR Creation Command

The verbatim original creation command is not exposed in the recovered capstone transcript.

The following command explicitly reproduces the verified final configuration.

```text
REPRODUCIBLE IMPLEMENTATION
```

```bash
aws ecr create-repository \
  --repository-name java-maven-app \
  --image-tag-mutability MUTABLE \
  --image-scanning-configuration scanOnPush=true \
  --encryption-configuration encryptionType=AES256 \
  --region ca-central-1
```

Do **not execute this command during Infrastructure Preparation**.

It belongs to:

```text
Phase 12 — Server and Cloud Provisioning
```

At this phase, save and review the command only.

---

## 10.12 Why Scan on Push Is Enabled

Verified:

```text
ScanOnPush: true
```

This allows ECR to perform image scanning when container images are pushed.

For this project it adds a useful security control without introducing another external scanning system.

Additional image-security tooling such as Trivy may be added later.

---

## 10.13 ECR Tag Mutability

Verified:

```text
MUTABLE
```

However, the CI/CD pipeline produces unique tags such as:

```text
1.1.1-1
1.1.1-2
```

Therefore normal operation still avoids intentionally overwriting the same deployment tag.

A future production improvement would be:

```text
IMMUTABLE
```

to make repository-level enforcement stronger.

Do not silently change the historical project configuration.

---

## 10.14 ECR Encryption

Verified:

```text
AES256
```

This uses ECR's server-side encryption.

A customer-managed KMS key is not required for this basic learning project.

Future environments with compliance requirements may use KMS-based encryption.

---

## 10.15 Planned ECR Verification Commands

After provisioning later:

```bash
aws ecr describe-repositories \
  --repository-names java-maven-app \
  --region ca-central-1 \
  --query 'repositories[0].{
    RepositoryName:repositoryName,
    RepositoryUri:repositoryUri,
    TagMutability:imageTagMutability,
    ScanOnPush:imageScanningConfiguration.scanOnPush,
    EncryptionType:encryptionConfiguration.encryptionType
  }' \
  --output yaml
```

Expected:

```text
RepositoryName: java-maven-app
TagMutability: MUTABLE
ScanOnPush: true
EncryptionType: AES256
```

---

## 10.16 EKS Configuration Plan

Verified final EKS cluster:

```text
Cluster:
java-maven-eks

Region:
ca-central-1

Created with:
eksctl

Kubernetes version:
1.35
```

Verified nodegroup:

```text
Name:
java-maven-nodes

Type:
managed

Capacity:
ON_DEMAND

Instance type:
t3.small

Minimum:
1

Desired:
1

Maximum:
2

AMI family/result:
AL2023_x86_64_STANDARD
```

---

## 10.17 Why a Small Managed Nodegroup Is Used

This capstone is a learning environment.

The application requires only one replica.

Therefore a small nodegroup is sufficient:

```text
minimum = 1
desired = 1
maximum = 2
```

This limits unnecessary EC2 cost while still allowing the managed nodegroup to scale up to two nodes if required.

---

## 10.18 Reproducible EKS Creation Command

The exact historical creation command is not currently exposed verbatim in the recoverable thread.

The following command follows the same Nana-style `eksctl` approach and reproduces the verified configuration.

```text
REPRODUCIBLE IMPLEMENTATION
```

```bash
eksctl create cluster \
  --name java-maven-eks \
  --region ca-central-1 \
  --version 1.35 \
  --nodegroup-name java-maven-nodes \
  --node-type t3.small \
  --nodes 1 \
  --nodes-min 1 \
  --nodes-max 2 \
  --managed
```

Do **not run this command during Infrastructure Preparation**.

It will create chargeable AWS resources.

Execute it only during:

```text
Phase 12 — Server and Cloud Provisioning
```

after Security Configuration has been completed.

---

## 10.19 What `eksctl create cluster` Will Create

A simple `eksctl` cluster creation may create or manage AWS resources including:

```text
EKS control plane
VPC networking
public/private subnets as selected by eksctl defaults
security groups
IAM roles
EC2 managed node group
Auto Scaling Group
launch template
network interfaces
CloudFormation stacks
```

Do not assume the EKS cluster is the only chargeable resource involved.

---

## 10.20 Verified Resulting Cluster Networking

The final cluster later showed:

```text
EndpointPublicAccess:
true

EndpointPrivateAccess:
false

PublicAccessCidrs:
0.0.0.0/0
```

This describes the **verified project state**.

It should not be interpreted as the preferred security posture for every production cluster.

Restricting EKS API access is discussed in:

```text
Phase 11 — Security Configuration
```

---

## 10.21 Verified EKS VPC Outcome

The final environment used an `eksctl`-created VPC and subnets.

The exact IDs are environment-specific and must not be hardcoded into a reusable project.

Examples of environment-specific values include:

```text
vpc-...
subnet-...
sg-...
```

A new `eksctl create cluster` execution will normally produce different IDs.

Therefore reusable commands should use resource names and configuration rather than copying old generated AWS IDs.

---

## 10.22 Planned EKS Verification

After provisioning:

```bash
eksctl get cluster \
  --region ca-central-1
```

Expected cluster:

```text
java-maven-eks
```

Then:

```bash
eksctl get nodegroup \
  --cluster java-maven-eks \
  --region ca-central-1
```

Expected:

```text
java-maven-nodes
ACTIVE
managed
t3.small
```

---

## 10.23 Planned AWS API Verification

Cluster:

```bash
aws eks describe-cluster \
  --name java-maven-eks \
  --region ca-central-1 \
  --query 'cluster.{
    Name:name,
    Version:version,
    Status:status,
    EndpointPublicAccess:resourcesVpcConfig.endpointPublicAccess,
    EndpointPrivateAccess:resourcesVpcConfig.endpointPrivateAccess
  }' \
  --output yaml
```

Nodegroup:

```bash
aws eks describe-nodegroup \
  --cluster-name java-maven-eks \
  --nodegroup-name java-maven-nodes \
  --region ca-central-1 \
  --query 'nodegroup.{
    Name:nodegroupName,
    Status:status,
    CapacityType:capacityType,
    InstanceTypes:instanceTypes,
    Desired:scalingConfig.desiredSize,
    Minimum:scalingConfig.minSize,
    Maximum:scalingConfig.maxSize,
    AmiType:amiType
  }' \
  --output yaml
```

Expected configuration:

```text
CapacityType:
ON_DEMAND

InstanceTypes:
t3.small

Desired:
1

Minimum:
1

Maximum:
2
```

---

## 10.24 Kubeconfig Planning

The local workstation must later be able to communicate with EKS.

The standard configuration command is:

```bash
aws eks update-kubeconfig \
  --name java-maven-eks \
  --region ca-central-1
```

This writes or updates local Kubernetes configuration.

Do not commit:

```text
~/.kube/config
```

to Git.

The project `.gitignore` already protects common kubeconfig locations/files.

---

## 10.25 Planned Kubernetes Verification

After the cluster is created and kubeconfig is configured:

```bash
kubectl config current-context
```

Then:

```bash
kubectl cluster-info
```

Then:

```bash
kubectl get nodes -o wide
```

Expected:

```text
1 Ready node
instance type t3.small
architecture amd64/x86_64
```

The verified final worker node used:

```text
Amazon Linux 2023
amd64
t3.small
```

---

## 10.26 Architecture Compatibility

The local Apple Silicon development image was:

```text
arm64
```

The EKS node is:

```text
amd64
```

Therefore the image deployed to EKS should be built on an AMD64-compatible Jenkins environment or explicitly built for:

```text
linux/amd64
```

The project resolves this naturally because Jenkins on DigitalOcean builds the image that is pushed to ECR.

Do not push the locally built Mac ARM64 validation image as the EKS deployment image unless multi-platform compatibility has been confirmed.

---

## 10.27 ECR Authentication Plan

After the repository is created, Docker authenticates using a temporary ECR password generated by AWS CLI.

Command:

```bash
aws ecr get-login-password \
  --region ca-central-1 \
  | docker login \
      --username AWS \
      --password-stdin \
      "${AWS_ACCOUNT_ID}.dkr.ecr.ca-central-1.amazonaws.com"
```

The ECR password itself must not be stored in:

```text
Jenkinsfile
Git
README
RUNBOOK
shell script
```

The Jenkins Shared Library later generates the temporary password at runtime.

---

## 10.28 Jenkins AWS Runtime Requirements

Before Jenkins can push and deploy, its runtime must eventually contain:

```text
Docker
AWS CLI
kubectl
envsubst
AWS/EKS authentication support
kubeconfig access
```

Preparation checklist:

```text
Docker available in Jenkins
AWS CLI planned
kubectl planned
envsubst planned
kubeconfig location planned
AWS credential ID planned
```

Installation belongs to:

```text
Phase 12 — Server and Cloud Provisioning
```

---

## 10.29 Jenkins AWS Configuration Values

The final application Jenkinsfile requires:

```text
registryType:
ecr

awsRegion:
ca-central-1

ecrCredentialsId:
aws_ecr_creds

ecrRegistryServer:
<AWS_ACCOUNT_ID>.dkr.ecr.ca-central-1.amazonaws.com

imageName:
<AWS_ACCOUNT_ID>.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app
```

These values can be planned before provisioning.

The ECR repository URI should be verified after actual resource creation before the pipeline is run.

---

## 10.30 AWS Deployment Feature Branch

The historical application repository later used:

```text
feature/configure-aws-deployment
```

to configure the project-specific ECR values.

That work produced:

```text
86a5f33
ci: configure AWS ECR deployment
```

and was merged into `develop` as:

```text
e217b83
merge: configure AWS ECR deployment
```

This configuration came after the initial generic pipeline preparation.

Do not combine:

```text
b6185a1
```

and:

```text
86a5f33
```

into one historical commit.

They represent separate steps.

---

## 10.31 Generic Jenkinsfile Before AWS Values

Before project-specific cloud values are known:

```groovy
@Library('jenkins-shared-library') _

singleServicePipeline(
    appName: 'java-maven-app',

    appDir: '.',

    manifestDir: 'kubernetes',

    registryType: 'ecr',

    imageName: '<AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/java-maven-app',

    awsRegion: '<AWS_REGION>',

    ecrRegistryServer: '<AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com',

    ecrCredentialsId: 'aws_ecr_creds',

    gitCredentialsId: 'github-token',

    repositoryUrl: 'https://github.com/younghadiz/complete-jenkins-cicd-pipeline-eks-ecr.git',

    namespace: 'default'
)
```

This is safe to prepare before cloud creation.

---

## 10.32 Final Verified AWS Values

The project eventually used:

```text
AWS_ACCOUNT_ID:
002184382122

AWS_REGION:
ca-central-1

ECR_REPOSITORY:
java-maven-app
```

Result:

```text
ECR_REGISTRY_SERVER:
002184382122.dkr.ecr.ca-central-1.amazonaws.com
```

and:

```text
IMAGE_NAME:
002184382122.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app
```

Again, the account ID is an AWS resource identifier, not a secret credential.

---

## 10.33 Infrastructure Dependency Order

The cloud work must follow this dependency sequence:

```text
AWS authentication verified
        ↓
IAM/security prepared
        ↓
ECR repository created
        ↓
EKS cluster created
        ↓
managed nodegroup becomes Ready
        ↓
kubeconfig configured
        ↓
Jenkins AWS/EKS tooling verified
        ↓
pipeline allowed to push/deploy
```

Do not run the deployment pipeline against resources that do not yet exist.

---

## 10.34 Cost Warning Before EKS Provisioning

Amazon EKS is chargeable while the cluster exists.

The project can also create charges from:

```text
EKS control plane
EC2 t3.small worker node
EBS storage
Elastic Load Balancer
public IPv4 usage where applicable
data transfer
ECR storage
```

The Kubernetes `LoadBalancer` Service later adds another chargeable AWS resource.

Do not create the EKS cluster until you are ready to continue through deployment, verification, documentation evidence, and cleanup.

---

## 10.35 DigitalOcean Cost Warning

Jenkins is hosted on a DigitalOcean Droplet.

That server may continue incurring cost independently of AWS.

Do not automatically delete the Jenkins server during project cleanup if it is used for other TechWorld with Nana exercises.

Review its purpose before deletion.

---

## 10.36 ECR Cost Consideration

ECR charges for image storage beyond any applicable free allowances.

The pipeline creates uniquely tagged images such as:

```text
1.1.1-1
1.1.1-2
```

and container pushes may also leave untagged manifest objects.

A future production improvement is an ECR lifecycle policy to remove:

```text
old images
untagged images
superseded development images
```

Do not introduce lifecycle deletion while evidence is still needed for this capstone.

---

## 10.37 Prepare Cleanup Commands Before Provisioning

Before creating cloud resources, know how they will be removed.

Application Service:

```bash
kubectl delete service \
  java-maven-app \
  --namespace default
```

EKS cluster:

```bash
eksctl delete cluster \
  --name java-maven-eks \
  --region ca-central-1
```

ECR repository, only if intentionally removing it:

```bash
aws ecr delete-repository \
  --repository-name java-maven-app \
  --region ca-central-1 \
  --force
```

Actual cleanup occurs in:

```text
Phase 20 — Cleanup
```

---

## 10.38 Why Delete the LoadBalancer Service First

The Kubernetes Service contains:

```yaml
type: LoadBalancer
```

AWS therefore provisions an external load balancer.

During cleanup, remove the Kubernetes `LoadBalancer` Service before deleting the EKS cluster where practical.

This gives Kubernetes/AWS an opportunity to clean up the cloud load balancer normally.

Then verify the load balancer has actually disappeared before considering cleanup complete.

---

## 10.39 Infrastructure Preparation Security Boundary

This phase deliberately does **not** yet create:

```text
AWS access keys
Jenkins AWS users/roles
IAM policies
EKS access authorization
security-group rules
```

Those belong to:

```text
Phase 11 — Security Configuration
```

The project order must remain:

```text
infrastructure preparation
→ security configuration
→ provisioning
```

---

## 10.40 Do Not Use AWS Root Credentials

The AWS root account must not be used by Jenkins or normal project automation.

Use an appropriate IAM identity.

The exact Jenkins IAM/permissions design is documented in the next phase.

---

## 10.41 Infrastructure Preparation Verification Commands

Run locally:

```bash
echo "===== AWS CLI ====="
aws --version

echo
echo "===== AWS Identity ====="
aws sts get-caller-identity

echo
echo "===== eksctl ====="
eksctl version

echo
echo "===== kubectl ====="
kubectl version --client

echo
echo "===== Docker ====="
docker --version
```

Then:

```bash
export AWS_REGION=ca-central-1

export AWS_ACCOUNT_ID="$(
  aws sts get-caller-identity \
    --query Account \
    --output text
)"

export ECR_REPOSITORY=java-maven-app

export ECR_REGISTRY_SERVER="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

export ECR_IMAGE_NAME="${ECR_REGISTRY_SERVER}/${ECR_REPOSITORY}"
```

Verify:

```bash
echo "Region: $AWS_REGION"
echo "ECR repository: $ECR_REPOSITORY"
echo "ECR registry server: $ECR_REGISTRY_SERVER"
echo "ECR image name: $ECR_IMAGE_NAME"
```

Do not print secret environment variables.

---

## 10.42 Check Whether Resources Already Exist

Before any creation command, always check first.

ECR:

```bash
aws ecr describe-repositories \
  --repository-names java-maven-app \
  --region ca-central-1
```

If the repository does not exist, AWS returns a repository-not-found error.

Do not create a duplicate simply because a script assumed it was absent.

EKS:

```bash
eksctl get cluster \
  --region ca-central-1
```

Check specifically for:

```text
java-maven-eks
```

Then:

```bash
aws eks describe-cluster \
  --name java-maven-eks \
  --region ca-central-1
```

Do not issue another create-cluster command if the intended cluster already exists.

---

## 10.43 Idempotency Limitation

The simple Nana-style commands:

```bash
aws ecr create-repository ...
```

and:

```bash
eksctl create cluster ...
```

are resource-creation commands.

They are not intended to be rerun blindly.

Always perform existence checks first.

A future Infrastructure-as-Code approach could manage desired state more declaratively.

Terraform is intentionally not required in this version.

---

## 10.44 Evidence to Capture Before Provisioning

Capture:

```text
aws --version
eksctl version
kubectl client version
Docker version

AWS region
resource-name plan

ECR planned configuration
EKS planned configuration

cost warning acknowledged
cleanup commands prepared
```

Do not capture:

```text
AWS secret keys
session tokens
Jenkins tokens
GitHub tokens
private keys
```

---

## 10.45 Verified Final Infrastructure for Comparison

After provisioning and deployment, the final project was later verified as:

```text
ECR
├── repository: java-maven-app
├── region: ca-central-1
├── scan-on-push: true
├── tag mutability: MUTABLE
└── encryption: AES256

EKS
├── cluster: java-maven-eks
├── version: 1.35
├── region: ca-central-1
└── nodegroup
    ├── name: java-maven-nodes
    ├── type: managed
    ├── capacity: ON_DEMAND
    ├── instance: t3.small
    ├── minimum: 1
    ├── desired: 1
    └── maximum: 2
```

These verified values are the target state for the reproducible commands above.

---

## 10.46 Infrastructure Preparation Completion Checklist

```text
[ ] AWS region selected
[ ] AWS CLI available
[ ] AWS identity can be verified safely
[ ] AWS account ID retrievable programmatically
[ ] eksctl available
[ ] kubectl available
[ ] Docker available
[ ] application resource names fixed
[ ] ECR repository name fixed
[ ] ECR registry format understood
[ ] ECR target configuration documented
[ ] ECR reproducible creation command prepared
[ ] EKS cluster name fixed
[ ] EKS nodegroup name fixed
[ ] EKS version planned
[ ] node type planned
[ ] min/desired/max nodes planned
[ ] EKS reproducible creation command prepared
[ ] kubeconfig command prepared
[ ] cluster verification commands prepared
[ ] nodegroup verification commands prepared
[ ] architecture compatibility reviewed
[ ] Jenkins AWS tool requirements identified
[ ] cleanup commands prepared
[ ] EKS cost warning reviewed
[ ] EC2 cost warning reviewed
[ ] load-balancer cost warning reviewed
[ ] ECR storage cost reviewed
[ ] no new chargeable resource created during preparation
```

---

## 10.47 Phase 10 Final State

Infrastructure Preparation is complete when the project has a fully defined target state and executable provisioning plan without having prematurely created resources.

```text
AWS Region
    ✅ ca-central-1

ECR Repository
    ✅ java-maven-app

ECR Scan on Push
    ✅ true

ECR Encryption
    ✅ AES256

EKS Cluster
    ✅ java-maven-eks

Managed Nodegroup
    ✅ java-maven-nodes

Node Type
    ✅ t3.small

Node Scaling
    ✅ 1 / 1 / 2

Kubernetes Version
    ✅ 1.35

Provisioning Commands
    ✅ prepared

Verification Commands
    ✅ prepared

Cleanup Commands
    ✅ prepared

Cost Warning
    ✅ documented

Resources Provisioned
    ❌ not yet — intentionally
```

---

# 11. Security Configuration

## 11.1 Objective

Prepare the security controls required before Jenkins is allowed to authenticate to GitHub, Amazon ECR, Amazon EKS, or Kubernetes.

The authoritative project sequence is:

```text
infrastructure preparation
→ security configuration
→ server and cloud provisioning
→ deployment
```

This phase therefore establishes:

```text
Git secret protection
Jenkins credential storage
AWS authentication model
ECR permissions
EKS authentication requirements
kubeconfig protection
Jenkins Git authentication
pipeline credential scoping
container/security limitations
network-access limitations
```

No secret value should ever be written into the repository.

---

## 11.2 Nana's Security Learning Approach

The original TechWorld with Nana EKS lesson teaches that Jenkins requires four pieces before it can deploy to EKS:

```text
kubectl
AWS IAM authentication
kubeconfig
AWS credentials
```

The training sequence was:

```text
install kubectl
→ install aws-iam-authenticator
→ provide kubeconfig
→ provide AWS credentials
→ Jenkins can authenticate to EKS
```

Nana explicitly noted that using a dedicated IAM user for Jenkins with reduced permissions is better practice.

However, for training simplicity, the lesson reused an existing AWS administrative user.

That distinction must remain visible:

```text
NANA TRAINING IMPLEMENTATION
Existing AWS user reused for simplicity

NANA BEST-PRACTICE NOTE
Dedicated Jenkins IAM identity
with reduced permissions

CURRENT CAPSTONE
AWS credentials stored in Jenkins as:
aws_ecr_creds

FUTURE PRODUCTION IMPROVEMENT
Dedicated least-privilege Jenkins identity
or short-lived role-based authentication
```

Do not rewrite the historical lesson to make it appear more advanced than it was.

---

## 11.3 Security Principle — No Secrets in Git

Never commit:

```text
AWS Access Key ID
AWS Secret Access Key
AWS session tokens

GitHub Personal Access Token
GitLab token

Docker Hub password
Docker Hub token

Jenkins passwords

private SSH keys

kubeconfig files

.env files containing secrets
```

Only references such as:

```text
aws_ecr_creds
github-token
dockerhub-creds
```

belong in source code.

---

## 11.4 Project `.gitignore` Security Controls

The project `.gitignore` contains:

```gitignore
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
```

These patterns reduce the risk of accidentally tracking local authentication material.

They do not replace careful review before commits.

---

## 11.5 Verify Sensitive Files Are Ignored

Run:

```bash
git check-ignore -v \
  .env \
  .aws/credentials \
  .kube/config \
  kubeconfig \
  example.pem \
  example.key \
  2>/dev/null || true
```

The command should show the matching `.gitignore` rules.

Do not create real secret files just to test this.

Use harmless temporary names or inspect the rules directly.

---

## 11.6 Search for Obvious Secret Patterns

Before important pushes:

```bash
grep -R \
  --exclude-dir=.git \
  --exclude=RUNBOOK.md \
  -nE \
  'AWS_SECRET_ACCESS_KEY=|AWS_ACCESS_KEY_ID=|ghp_|github_pat_|BEGIN (RSA|OPENSSH|PRIVATE) KEY' \
  . \
  || echo "No obvious secret patterns found."
```

This is a basic check only.

It does not replace a real secret-scanning tool.

---

## 11.7 Review Staged Files Before Every Security-Sensitive Commit

Always inspect:

```bash
git status

git diff --cached
```

Do not automatically use:

```bash
git add .
```

when a focused set of security-sensitive files is being committed.

Prefer explicit staging such as:

```bash
git add Jenkinsfile
```

or:

```bash
git add \
  kubernetes/deployment.yaml \
  kubernetes/service.yaml
```

---

# Jenkins Credentials

## 11.8 Jenkins Credential Store

Secrets used by pipelines belong in:

```text
Jenkins
→ Manage Jenkins
→ Credentials
→ System
→ Global credentials
```

Source code stores only the credential ID.

Example:

```groovy
ecrCredentialsId: 'aws_ecr_creds'
```

Jenkins stores the actual key material.

---

## 11.9 GitHub Credential

Credential ID:

```text
github-token
```

Purpose:

```text
checkout application repository
push automated pom.xml version update
```

Nana-aligned representation:

```text
Kind:
Username with password

Username:
<GITHUB_USERNAME>

Password:
<GITHUB_PERSONAL_ACCESS_TOKEN>

ID:
github-token
```

For this project the username is:

```text
younghadiz
```

Do not store the PAT in:

```text
Git
Jenkinsfile
RUNBOOK
README
shell scripts
screenshots
```

---

## 11.10 GitHub Credential Scope

The token should have only the repository access required for:

```text
read repository
checkout branches
push Jenkins-generated version commit
```

The pipeline does not need unrestricted access to unrelated GitHub repositories.

---

## 11.11 AWS Credential

Credential ID:

```text
aws_ecr_creds
```

Current representation:

```text
Kind:
Username with password

Username:
AWS Access Key ID

Password:
AWS Secret Access Key
```

The shared library maps these fields to:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
```

Example:

```groovy
usernamePassword(
    credentialsId: ecrCredentialsId,
    usernameVariable: 'AWS_ACCESS_KEY_ID',
    passwordVariable: 'AWS_SECRET_ACCESS_KEY'
)
```

The credential ID is safe to commit.

The actual AWS key values are not.

---

## 11.12 Why the Name `aws_ecr_creds` Became Broader Than ECR

The credential was originally introduced primarily for ECR authentication.

The project later discovered that deployment to EKS also requires an authenticated AWS identity.

Therefore the same Jenkins credential is currently used for:

```text
Amazon ECR authentication
+
EKS authentication during kubectl execution
```

The name:

```text
aws_ecr_creds
```

is therefore slightly narrower than its final role.

This is acceptable for the training implementation.

A future cleanup could rename it to something such as:

```text
aws-jenkins-creds
```

but that would require updating Jenkins and all pipeline references together.

Do not rename it only for documentation appearance.

---

# Amazon ECR Permissions

## 11.13 ECR Authentication Method

The shared library performs:

```bash
aws ecr get-login-password \
  --region "$AWS_REGION_VALUE" |
  docker login \
    --username AWS \
    --password-stdin "$ECR_REGISTRY_SERVER_VALUE"
```

The generated ECR authorization token is temporary.

Do not store a previously generated ECR login token.

Generate a new one during each pipeline execution.

---

## 11.14 ECR IAM Actions Identified by the Project

The shared-library documentation identifies permissions such as:

```text
ecr:GetAuthorizationToken
ecr:BatchCheckLayerAvailability
ecr:CompleteLayerUpload
ecr:UploadLayerPart
ecr:InitiateLayerUpload
ecr:PutImage
ecr:BatchGetImage
```

These support the ECR authentication and image-push workflow used by the pipeline.

Exact production permissions should be reviewed against the final pipeline behavior and AWS resource scope.

---

## 11.15 ECR Least-Privilege Direction

A future dedicated Jenkins AWS policy should restrict repository-specific operations where AWS supports resource scoping.

Conceptually:

```text
authorization-token access
        ↓
specific ECR repository
        ↓
upload layers
        ↓
put image
```

Do not grant:

```text
AdministratorAccess
```

merely because ECR authentication failed.

---

# Amazon EKS Authentication

## 11.16 EKS Authentication Components

Nana's EKS-from-Jenkins training explicitly requires:

```text
kubectl
aws-iam-authenticator
kubeconfig
AWS credentials
```

The reasoning is:

```text
kubectl
→ reads kubeconfig

kubeconfig
→ identifies EKS endpoint and authentication method

AWS authentication
→ generates a Kubernetes authentication token

EKS
→ validates AWS identity

Kubernetes
→ authorizes that identity
```

---

## 11.17 Original Nana Tooling

Inside the Jenkins container, the training installed:

```bash
curl -LO \
  https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl

chmod +x ./kubectl

mv ./kubectl \
  /usr/local/bin/kubectl
```

Verify:

```bash
kubectl version --client
```

The lesson then installed AWS IAM Authenticator:

```bash
curl -Lo aws-iam-authenticator \
  https://github.com/kubernetes-sigs/aws-iam-authenticator/releases/download/v0.6.11/aws-iam-authenticator_0.6.11_linux_amd64

chmod +x ./aws-iam-authenticator

mv ./aws-iam-authenticator \
  /usr/local/bin
```

This is the recovered Nana training method.

---

## 11.18 Historical AWS IAM Authenticator Note

The training also contained a later generic/latest-release-style command:

```bash
curl -Lo aws-iam-authenticator \
  https://github.com/kubernetes-sigs/aws-iam-authenticator/releases/latest/download/aws-iam-authenticator_$(uname -s)_amd64
```

The precise download URL can change across releases.

When rebuilding, verify the current official installation instructions instead of assuming an old release URL is still valid.

The learning objective remains:

```text
Jenkins must have the EKS authentication tool
required by its kubeconfig
```

---

# Kubeconfig Security

## 11.19 Why Jenkins Needs Its Own Kubeconfig

The local developer machine having:

```text
~/.kube/config
```

does not automatically make that configuration available to Jenkins.

Jenkins runs in a separate environment.

The Nana workflow therefore created a kubeconfig for Jenkins itself.

---

## 11.20 Nana's Kubeconfig Workflow

Training sequence:

```text
create config on DigitalOcean host
→ create ~/.kube inside Jenkins container
→ docker cp config into Jenkins home
→ verify file inside Jenkins
```

Inside Jenkins:

```bash
mkdir ~/.kube
```

Safer reusable form:

```bash
mkdir -p ~/.kube
```

Then from the DigitalOcean host:

```bash
docker cp \
  config \
  <container-id>:/var/jenkins_home/.kube/config
```

The historical lesson placed the file at:

```text
/var/jenkins_home/.kube/config
```

---

## 11.21 Kubeconfig Content

The training explained that the file contains information including:

```text
EKS API server endpoint
certificate-authority data
cluster identification
AWS authentication exec configuration
```

Do not publish the complete operational kubeconfig in Git.

Even when some contents are not passwords, it is authentication configuration and should remain outside the repository.

---

## 11.22 Kubeconfig Permissions

After placing a kubeconfig on a Linux Jenkins system, use restrictive filesystem permissions where possible:

```bash
chmod 600 \
  /var/jenkins_home/.kube/config
```

Directory:

```bash
chmod 700 \
  /var/jenkins_home/.kube
```

The exact ownership must allow the Jenkins process to read the file.

Do not solve a permission problem by making kubeconfig world-readable.

---

## 11.23 Do Not Commit Kubeconfig

The application `.gitignore` protects:

```text
.kube/
kubeconfig
kubeconfig.*
```

The Jenkins server copy belongs outside the source repository.

---

# AWS Identity Design

## 11.24 Nana's Original Identity Choice

The training explicitly states that for simplicity the existing AWS admin-style user was reused.

That does not mean administrative access is required.

The lesson itself explains that a better solution is:

```text
dedicated Jenkins IAM user
+
more limited permissions
```

---

## 11.25 Current Capstone Security Position

The capstone successfully used the Jenkins credential:

```text
aws_ecr_creds
```

for AWS CLI operations.

The precise historical IAM policy attached to the AWS principal behind those keys has not been recovered as an authoritative capstone artifact.

Therefore the runbook must not claim:

```text
Jenkins used policy X
```

unless that policy is directly verified.

What is verified is:

```text
AWS credential existed in Jenkins
ECR authentication succeeded
ECR image push succeeded
EKS authentication succeeded after credential scope was corrected
```

---

## 11.26 Reusable Production Direction

A stronger implementation would create a dedicated Jenkins AWS principal or role that has only the capabilities required to:

```text
authenticate to ECR
push images to java-maven-app

obtain EKS authentication
access java-maven-eks

perform the Kubernetes operations
authorized for the deployment
```

AWS IAM authentication alone is not the complete Kubernetes authorization model.

The AWS principal must also be recognized by the EKS/Kubernetes access configuration.

---

## 11.27 EKS AWS Authentication vs Kubernetes Authorization

These are separate layers:

```text
AWS authentication
        ↓
Who is this AWS principal?

EKS/Kubernetes authorization
        ↓
What is this principal allowed to do?
```

Successful AWS credentials do not automatically mean:

```text
kubectl apply
```

is authorized.

The identity must have appropriate cluster access.

---

## 11.28 Verify AWS Identity from Jenkins

When troubleshooting AWS access inside Jenkins, run only inside a credential-bound block.

Example conceptual test:

```bash
aws sts get-caller-identity
```

This confirms which AWS principal Jenkins is using.

Do not echo:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
```

to prove that credentials exist.

Identity verification is safer than credential-value printing.

---

## 11.29 Verify Kubernetes Authorization

Once Jenkins has valid kubeconfig and AWS authentication:

```bash
kubectl auth can-i get deployments \
  --namespace default
```

For this pipeline, useful permissions include the Kubernetes actions needed by:

```text
kubectl apply
kubectl rollout status
```

on the project Deployment and Service.

Do not grant cluster-admin automatically just because a narrower authorization fails.

---

# EKS API Endpoint Security

## 11.30 Verified Current Cluster Endpoint

The final capstone cluster was verified with:

```text
EndpointPublicAccess:
true

EndpointPrivateAccess:
false

PublicAccessCidrs:
0.0.0.0/0
```

This means the EKS API endpoint is publicly reachable from any IPv4 address at the network layer.

Authentication is still required, but the network exposure is broad.

---

## 11.31 Current vs Production Security Position

For the learning environment:

```text
public endpoint:
enabled

public CIDR:
0.0.0.0/0
```

keeps Jenkins connectivity simple.

For a stronger production environment consider:

```text
restrict public endpoint CIDRs
```

for example to trusted administrative/Jenkins egress addresses, or:

```text
enable private endpoint access
```

with appropriate private connectivity.

Do not silently change the live capstone endpoint configuration during documentation.

---

## 11.32 Why Public Endpoint Access Was Useful Here

Jenkins is hosted outside AWS on DigitalOcean.

A publicly reachable EKS API makes it possible for:

```text
DigitalOcean Jenkins
        ↓
Internet
        ↓
EKS public API endpoint
```

to communicate without a site-to-site VPN or private network integration.

That simplicity matches the learning objective.

---

## 11.33 Future Network Security Improvement

A stronger architecture could use one of:

```text
restricted EKS public CIDR
VPN
private connectivity
AWS-hosted Jenkins
private EKS API endpoint
```

Each adds infrastructure and operational complexity.

They are future improvements, not hidden requirements of this capstone.

---

# Jenkins Credential Scope

## 11.34 Keep Credentials Around Only When Needed

Jenkins Credentials Binding should wrap only the stages that need the secret.

Good pattern:

```groovy
withCredentials([
    usernamePassword(
        credentialsId: ecrCredentialsId,
        usernameVariable: 'AWS_ACCESS_KEY_ID',
        passwordVariable: 'AWS_SECRET_ACCESS_KEY'
    )
]) {
    // AWS operation
}
```

Outside that block the pipeline should not depend on those credential variables.

---

## 11.35 Initial ECR Credential Scope

The ECR stage already used credentials correctly:

```text
Push Docker Image
        ↓
withCredentials
        ↓
aws ecr get-login-password
        ↓
docker push
```

After the stage finished, those environment variables were no longer in scope.

That became important during deployment.

---

# Confirmed Security/Authentication Troubleshooting

## 11.36 Deployment Authentication Failure

The first EKS deployment design called:

```groovy
deployToEks(...)
```

without wrapping the Deploy stage in AWS credentials.

However, Jenkins kubeconfig authentication required AWS credentials at deployment time.

Conceptually:

```text
Push Docker Image stage
        │
        ├── AWS credentials available
        │
        └── stage ends
                ↓
AWS credential scope ends

Deploy stage
        ↓
kubectl
        ↓
kubeconfig authentication
        ↓
AWS token generation
        ↓
AWS credentials required
```

Therefore ECR authentication succeeding did not prove that the later EKS deployment had AWS credentials.

---

## 11.37 Root Cause

The AWS credential binding existed only around:

```text
ECR push
```

and not around:

```text
kubectl deployment
```

The kubeconfig authentication process required AWS identity at the exact time `kubectl` connected to EKS.

This was a credential-scope problem rather than:

```text
bad Kubernetes YAML
bad Docker image
bad Service selector
```

---

## 11.38 Fix Branch

Shared-library fix branch:

```text
fix/eks-deployment-aws-credentials
```

Verified commit:

```text
9aae486
fix: provide AWS credentials during EKS deployment
```

The fix was later merged to `master` as:

```text
5bcdcbd
merge: fix EKS deployment AWS credentials
```

---

## 11.39 Confirmed Deploy-Stage Fix

The Deploy stage was changed from:

```groovy
stage('Deploy') {
    steps {
        script {
            deployToEks(
                appDir,
                manifestDir,
                appName,
                imageName,
                env.IMAGE_TAG,
                namespace
            )
        }
    }
}
```

to:

```groovy
stage('Deploy') {
    steps {
        script {
            withCredentials([
                usernamePassword(
                    credentialsId: ecrCredentialsId,
                    usernameVariable: 'AWS_ACCESS_KEY_ID',
                    passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                )
            ]) {
                withEnv([
                    "AWS_DEFAULT_REGION=${awsRegion}",
                    "AWS_REGION=${awsRegion}"
                ]) {
                    deployToEks(
                        appDir,
                        manifestDir,
                        appName,
                        imageName,
                        env.IMAGE_TAG,
                        namespace
                    )
                }
            }
        }
    }
}
```

This made AWS credentials available only while EKS deployment required them.

---

## 11.40 Why the Fix Belongs in the Pipeline Layer

`KubernetesUtils.groovy` remains responsible for:

```text
envsubst
kubectl apply
kubectl rollout status
```

The pipeline layer provides:

```text
AWS credentials
AWS region
pipeline execution context
```

Architecture:

```text
singleServicePipeline
        │
        ├── AWS credentials
        ├── AWS region
        │
        ▼
deployToEks
        │
        ▼
KubernetesUtils
        │
        ▼
kubectl
        │
        ▼
EKS authentication
```

This keeps credential ownership out of the lower-level Kubernetes utility.

---

## 11.41 Fix Verification Strategy

The shared-library fix was not merged blindly.

A temporary application branch was created:

```text
bugfix/verify-eks-deployment-credentials
```

and the Jenkinsfile temporarily loaded:

```text
jenkins-shared-library@fix/eks-deployment-aws-credentials
```

Jenkins successfully resolved shared-library commit:

```text
9aae486
```

and tested it before the fix was merged.

This is a strong reusable troubleshooting pattern:

```text
library fix branch
        +
consumer test branch
        ↓
integration verification
        ↓
merge library fix
```

---

## 11.42 Temporary Verification Commit History

Application verification branch included:

```text
1f772e5
test: verify EKS deployment credentials fix

2fbd4ef
ci: version bump

48a5467
ci: restore shared library default version
```

The temporary branch is not intended to be merged into `develop`.

It exists as verification history until cleanup.

---

# Git Commit Security

## 11.43 Jenkins Commit Identity

Automated version commit:

```text
jenkins <jenkins@example.com>
```

Commit message:

```text
ci: version bump
```

This predictable identity allows:

```text
Ignore Committer Strategy
```

to suppress recursive builds.

---

## 11.44 Stage Only `pom.xml`

The commit helper deliberately performs:

```bash
git add pom.xml
```

not:

```bash
git add .
```

This reduces the risk that:

```text
generated files
logs
temporary files
rendered manifests
credentials
```

accidentally enter the automated version commit.

---

## 11.45 Git Authentication Without Credential in Remote URL

The final helper uses temporary:

```text
GIT_ASKPASS
```

rather than permanently rewriting the Git remote to:

```text
https://username:password@github.com/...
```

This reduces accidental credential exposure.

Temporary helper cleanup:

```bash
trap 'rm -f "$GIT_ASKPASS"' EXIT
```

---

# Docker Security

## 11.46 Dockerfile Contains No Secrets

Verified Dockerfile:

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/java-maven-app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

No:

```text
AWS credential
Git token
password
private key
```

is baked into the image.

---

## 11.47 Known Container Security Limitation

The Dockerfile does not specify:

```dockerfile
USER
```

so the application uses the base image's default user.

This is a known basic-training limitation.

Future improvement:

```text
create non-root user
→ assign required permissions
→ run Java as non-root
```

Do not silently rewrite the historical Dockerfile during documentation.

---

# Kubernetes Manifest Security

## 11.48 No Secret Values in Deployment Manifest

Current deployment manifest contains:

```text
APP_NAME
IMAGE_NAME
IMAGE_TAG
```

only.

No AWS or Git credentials are injected into Kubernetes YAML.

This application itself does not require database/application secrets.

Therefore a Kubernetes Secret object is not required for the current application.

---

## 11.49 `envsubst` Security Consideration

The deployment process uses:

```bash
envsubst
```

to substitute:

```text
APP_NAME
IMAGE_NAME
IMAGE_TAG
```

These are non-secret deployment metadata.

Do not later extend the same pattern to render plaintext passwords into committed or logged YAML without considering secret handling.

---

# Jenkins Logging Security

## 11.50 Jenkins Masks Bound Credentials

Jenkins Credentials Binding masks known secret values in pipeline output.

However:

```text
masking
```

is not permission to intentionally echo credentials.

Never run:

```bash
echo "$AWS_SECRET_ACCESS_KEY"
```

or:

```bash
env
```

inside a credential-bearing stage when it would print sensitive values.

---

## 11.51 Avoid Shell Tracing Around Secrets

Avoid:

```bash
set -x
```

inside shell blocks where credentials may appear in commands.

The shared-library AWS shell blocks use:

```bash
set -e
```

rather than enabling command tracing intentionally.

---

# Credential Rotation

## 11.52 Long-Lived Access Key Limitation

The Nana-style implementation uses:

```text
AWS Access Key ID
AWS Secret Access Key
```

stored in Jenkins.

This is understandable for training but represents long-lived credentials.

Future production improvement:

```text
short-lived credentials
IAM role assumption
OIDC / workload identity
AWS-hosted role-based Jenkins access
```

depending on the Jenkins hosting architecture.

---

## 11.53 GitHub PAT Limitation

Similarly:

```text
github-token
```

uses a long-lived GitHub credential.

Future improvement:

```text
fine-grained PAT
GitHub App
shorter expiry
repository-specific permissions
regular rotation
```

---

# Security Verification

## 11.54 Repository Security Check

Application repository:

```bash
git status

git ls-files

git check-ignore -v .env 2>/dev/null || true
git check-ignore -v .kube/config 2>/dev/null || true
git check-ignore -v kubeconfig 2>/dev/null || true
```

Verify no sensitive files appear in:

```bash
git ls-files
```

---

## 11.55 Inspect Application Pipeline References

Run:

```bash
grep -nE \
  'CredentialsId|credentialsId|github-token|aws_ecr_creds' \
  Jenkinsfile
```

Expected:

```text
credential IDs only
```

No credential values.

---

## 11.56 Inspect Shared Library Security References

In:

```text
/Users/younghadiz/Documents/tech-workspace/jenkins-shared-library
```

run:

```bash
grep -R \
  --exclude-dir=.git \
  -nE \
  'withCredentials|usernamePassword|AWS_ACCESS_KEY_ID|AWS_SECRET_ACCESS_KEY|GIT_ASKPASS' \
  vars src
```

Review that credential variables appear only inside intended credential-handling logic.

---

## 11.57 Verify Current EKS Endpoint Security

After cluster provisioning:

```bash
aws eks describe-cluster \
  --name java-maven-eks \
  --region ca-central-1 \
  --query 'cluster.resourcesVpcConfig.{
    Public:endpointPublicAccess,
    Private:endpointPrivateAccess,
    CIDRs:publicAccessCidrs
  }' \
  --output yaml
```

Verified historical result:

```text
Public: true
Private: false
CIDRs:
- 0.0.0.0/0
```

Record this honestly as a known security limitation.

---

## 11.58 Verify AWS Identity Without Printing Keys

From an authenticated environment:

```bash
aws sts get-caller-identity
```

Inside Jenkins this command should only be executed while the AWS credential binding is active.

Do not print credential variables.

---

## 11.59 Verify EKS Authorization

Once cluster access is configured:

```bash
kubectl auth can-i get deployments \
  --namespace default
```

Additional useful checks:

```bash
kubectl auth can-i create deployments \
  --namespace default

kubectl auth can-i patch deployments \
  --namespace default

kubectl auth can-i create services \
  --namespace default

kubectl auth can-i patch services \
  --namespace default
```

The exact permissions should match the operations performed by the deployment pipeline.

---

## 11.60 Security Troubleshooting — ECR Authentication Fails

Verify:

```text
AWS credential exists
credential ID matches
AWS identity valid
AWS region correct
ECR permissions present
repository exists
AWS CLI available
Docker available
```

Test identity:

```bash
aws sts get-caller-identity
```

Do not troubleshoot by embedding keys directly into the Jenkinsfile.

---

## 11.61 Security Troubleshooting — EKS Authentication Fails

Check separately:

```text
kubectl available
authentication tool available
kubeconfig exists
kubeconfig readable by Jenkins
AWS credentials available during Deploy stage
AWS region available
AWS identity recognized by EKS
Kubernetes authorization sufficient
EKS endpoint reachable
```

This project specifically encountered:

```text
AWS credentials unavailable during Deploy stage
```

and confirmed the credential-scope fix described earlier.

---

## 11.62 Security Troubleshooting — Kubeconfig Permission Denied

Check:

```bash
ls -ld \
  /var/jenkins_home/.kube

ls -l \
  /var/jenkins_home/.kube/config
```

Recommended:

```text
directory:
700

config:
600
```

with ownership readable by the Jenkins process.

Do not use:

```bash
chmod 777
```

as the permanent solution.

---

## 11.63 Security Troubleshooting — Jenkins Cannot Reach EKS

Authentication errors and network errors are different.

Authentication-style problem:

```text
Unauthorized
credentials missing
token-generation problem
```

Network-style problem:

```text
connection timeout
DNS failure
TCP connection failure
```

Do not change IAM permissions to solve a network timeout.

Do not widen firewall/CIDR access to solve a credential error.

Identify which layer failed first.

---

## 11.64 Security Evidence to Capture

Capture:

```text
Jenkins credential IDs
without values

github-token entry
without token

aws_ecr_creds entry
without AWS keys

.gitignore secret protections

Ignore Committer Strategy configuration

Jenkins Git identity:
jenkins@example.com

AWS sts get-caller-identity
with identifiers reviewed/redacted as desired

EKS endpoint configuration

kubectl auth can-i results

shared-library fix history:
9aae486
5bcdcbd
```

Never capture:

```text
AWS Secret Access Key
GitHub PAT
passwords
full private keys
session tokens
```

---

## 11.65 Security Cost Impact

Security preparation itself adds no AWS infrastructure charge.

Possible future security features may add cost, including:

```text
VPN
private network connectivity
NAT Gateway
customer-managed KMS key
advanced security/monitoring services
```

They are not required in the basic capstone.

---

## 11.66 Current Security Controls

Verified or implemented:

```text
✅ secrets excluded from Git
✅ Jenkins Credentials used
✅ GitHub token referenced by ID
✅ AWS credentials referenced by ID
✅ ECR authentication generated at runtime
✅ AWS secret key not committed
✅ kubeconfig outside repository
✅ only pom.xml staged by Jenkins
✅ temporary GIT_ASKPASS used
✅ Jenkins-generated commits identifiable
✅ Ignore Committer Strategy used
✅ ECR scan-on-push enabled
✅ ECR AES256 encryption
✅ AWS credentials scoped to Deploy after fix
```

---

## 11.67 Known Security Limitations

Intentionally simple / future improvements:

```text
🟡 long-lived AWS access keys
🟡 credential name aws_ecr_creds now serves ECR + EKS
🟡 exact Jenkins IAM policy not captured as a dedicated least-privilege policy
🟡 EKS public API endpoint enabled
🟡 EKS public access CIDR is 0.0.0.0/0
🟡 container does not explicitly run as non-root
🟡 shared library default version uses master
🟡 no dedicated automated secret scanner
🟡 no Jenkins Shared Library unit test framework
```

These limitations must be documented rather than hidden.

---

## 11.68 Future Production Security Improvements

Possible later improvements:

```text
dedicated Jenkins IAM role/user
least-privilege ECR policy
least-privilege EKS access
short-lived AWS credentials
restricted EKS API CIDR
private EKS endpoint where appropriate
HTTPS Jenkins endpoint
fine-grained GitHub authentication
credential rotation
non-root application container
Trivy or equivalent scanning
automated secret scanner
versioned Jenkins Shared Library releases
```

Add only improvements justified by the target environment.

---

## 11.69 Phase 11 Completion Checklist

```text
[ ] .gitignore protects local secret material
[ ] no secrets tracked in Git
[ ] github-token stored only in Jenkins
[ ] aws_ecr_creds stored only in Jenkins
[ ] AWS key values absent from repositories
[ ] ECR required actions understood
[ ] ECR runtime authentication understood
[ ] Jenkins EKS authentication requirements documented
[ ] kubectl requirement documented
[ ] aws-iam-authenticator training method documented
[ ] kubeconfig location documented
[ ] kubeconfig excluded from Git
[ ] kubeconfig permissions planned
[ ] AWS identity approach documented
[ ] Nana admin-user simplification documented
[ ] dedicated Jenkins IAM identity documented as improvement
[ ] AWS authentication vs Kubernetes authorization distinguished
[ ] EKS endpoint exposure documented
[ ] current 0.0.0.0/0 CIDR limitation documented
[ ] Jenkins credential scope reviewed
[ ] confirmed EKS deployment credential failure documented
[ ] root cause documented
[ ] fix commit 9aae486 documented
[ ] merge commit 5bcdcbd documented
[ ] integration verification branch documented
[ ] Ignore Committer Strategy security purpose documented
[ ] no chargeable security infrastructure created unnecessarily
```

---

## 11.70 Phase 11 Final State

Security Configuration is ready for the basic Nana-aligned infrastructure:

```text
Git secret protection
        ✅

Jenkins Git credentials
        ✅

Jenkins AWS credential reference
        ✅

ECR permissions identified
        ✅

EKS authentication model understood
        ✅

kubeconfig protection
        ✅

credential scope corrected
        ✅

recursive commit protection
        ✅

security limitations documented
        ✅

production improvements separated
        ✅
```

The project can now move to actual server and cloud resource creation.

---

# 12. Server and Cloud Provisioning

## 12.1 Objective

Provision the real runtime infrastructure required by the CI/CD project after local preparation and security configuration are complete.

The authoritative project sequence is:

```text
security configuration
→ server and cloud provisioning
→ deployment
```

This phase creates and prepares:

```text
DigitalOcean Jenkins server
→ Docker runtime
→ Jenkins container
→ persistent Jenkins storage
→ Docker access from Jenkins
→ Maven/Jenkins tools
→ AWS CLI
→ kubectl
→ EKS authentication support
→ envsubst
→ Jenkins kubeconfig
→ AWS ECR repository
→ Amazon EKS cluster
→ managed EKS nodegroup
```

Creating the server and AWS infrastructure may generate real charges.

Do not provision resources until the project files, pipeline, security plan, verification plan, rollback plan, and cleanup commands are understood.

---

## 12.2 Phase 12 Is Split into Two Parts

Part 1:

```text
DigitalOcean
→ Docker
→ Jenkins
→ Docker access
→ Jenkins initialization
→ Jenkins runtime tools
→ kubectl
→ AWS/EKS authentication
→ kubeconfig
→ envsubst
```

Part 2:

```text
Amazon ECR
→ EKS cluster
→ managed nodegroup
→ kubeconfig/context verification
→ cluster connectivity
→ Jenkins-to-EKS verification
→ final infrastructure comparison
```

Phase 12 is not complete until both parts have been verified.

---

# DigitalOcean Jenkins Server

## 12.3 Nana's Original Jenkins Hosting Model

TechWorld with Nana runs Jenkins on a separate DigitalOcean Droplet.

Architecture:

```text
Developer Mac
      │
      │ SSH / browser
      ▼
DigitalOcean Droplet
      │
      ├── Docker daemon
      │
      └── Jenkins container
              │
              ├── Jenkins jobs
              ├── credentials
              ├── plugins
              ├── Maven
              ├── Docker CLI
              ├── AWS CLI
              ├── kubectl
              ├── envsubst
              └── kubeconfig
```

The Jenkins application itself runs as a Docker container rather than being installed directly on the Ubuntu host.

---

## 12.4 DigitalOcean Droplet Details

The relevant Nana notes confirm:

```text
Cloud provider:
DigitalOcean

Operating system:
Ubuntu

Purpose:
Dedicated Jenkins server
```

The exact original capstone Droplet plan, datacenter, CPU size, and memory specification are not recoverable from the relevant capstone/Nana notes.

Record this accurately as:

```text
Original detail currently unavailable.
```

Do not substitute the specifications from another unrelated exercise and present them as this project's historical configuration.

For a rebuild, select a Linux Droplet with enough memory and CPU to run:

```text
Jenkins
Maven
Java build
Docker image build
AWS CLI
kubectl
```

without resource exhaustion.

---

## 12.5 Cost Warning — DigitalOcean

A DigitalOcean Droplet is chargeable while it exists.

Before creating it, remember:

```text
Jenkins server:
chargeable

AWS EKS:
chargeable

EC2 worker:
chargeable

AWS load balancer later:
chargeable
```

The Jenkins Droplet may be shared with other learning projects.

Therefore:

```text
do not automatically delete Jenkins
during this capstone cleanup
```

without first checking whether other projects depend on it.

---

## 12.6 Connect to the Jenkins Server

Nana's original method used root SSH access to the training Droplet:

```bash
ssh root@<JENKINS_SERVER_IP>
```

Expected result:

```text
Ubuntu shell on DigitalOcean server
```

Verify:

```bash
hostname

whoami

uname -a
```

During Nana's training:

```text
user:
root
```

was commonly used for server administration.

A future hardened server should use a dedicated administrative user with sudo rather than routine root SSH access.

---

## 12.7 Update the Ubuntu Server

Nana's simple training setup begins with:

```bash
apt update
```

A reproducible server setup should also apply available upgrades before installing project tooling:

```bash
apt update
apt upgrade -y
```

This runs on:

```text
DIGITALOCEAN HOST
```

not inside Jenkins.

---

# Docker on DigitalOcean

## 12.8 Nana's Original Docker Installation

The original Jenkins training used the straightforward Ubuntu package:

```bash
apt install docker.io
```

Verify:

```bash
docker --version

docker ps
```

The learning objective is:

```text
DigitalOcean host
→ Docker daemon available
```

The Docker daemon remains on the host.

Jenkins later communicates with that daemon through:

```text
/var/run/docker.sock
```

---

## 12.9 Current Package-Management Improvement

A later rebuild can install Docker using Docker's maintained Ubuntu package repository.

However, that is an implementation modernization rather than Nana's original training command.

Preserve the distinction:

```text
NANA ORIGINAL METHOD
apt install docker.io

LATER PRODUCTION/MAINTENANCE OPTION
Docker official Ubuntu repository
```

Do not silently replace Nana's teaching sequence in the historical section.

---

# Jenkins Persistent Storage

## 12.10 Create Jenkins Volume

Create the persistent Docker volume:

```bash
docker volume create jenkins_home
```

Verify:

```bash
docker volume ls
```

Expected:

```text
jenkins_home
```

---

## 12.11 Why `jenkins_home` Is Critical

Jenkins stores important state under:

```text
/var/jenkins_home
```

including:

```text
jobs
plugins
users
credentials
configuration
build metadata
workspaces
```

The volume mapping is:

```text
jenkins_home
        │
        ▼
/var/jenkins_home
```

This means the Jenkins container can be replaced without destroying its persistent Jenkins configuration.

---

## 12.12 Container vs Volume

Core Docker principle from Nana's lesson:

```text
container
→ replaceable

jenkins_home volume
→ persistent state
```

Removing only the Jenkins container should not erase:

```text
jobs
plugins
users
credentials
```

as long as:

```text
jenkins_home
```

is preserved.

Deleting the volume is destructive.

---

# Initial Jenkins Container

## 12.13 Nana's Initial Jenkins Container Command

Original training structure:

```bash
docker run \
  -p 8080:8080 \
  -p 50000:50000 \
  -d \
  -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts
```

The important elements are:

```text
8080
→ Jenkins web UI

50000
→ inbound Jenkins agent communication

-d
→ detached mode

jenkins_home
→ persistent Jenkins state
```

---

## 12.14 Java Runtime for This Capstone

This application is Java 17 based.

A reproducible Jenkins container can therefore use:

```text
jenkins/jenkins:lts-jdk17
```

Example:

```bash
docker run -d \
  --name jenkins \
  --restart unless-stopped \
  --publish 8080:8080 \
  --publish 50000:50000 \
  --volume jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts-jdk17
```

This is a reproducible adaptation for the verified Java 17 project.

The exact initial historical Jenkins image tag used when the user's original server was first created is not required to reproduce the final pipeline behavior.

---

## 12.15 Verify Jenkins Container

Run on the DigitalOcean host:

```bash
docker ps
```

Expected:

```text
Jenkins container
running
port 8080 published
```

Inspect:

```bash
docker ps \
  --filter name=jenkins
```

---

# Jenkins Initialization

## 12.16 Retrieve Initial Jenkins Password

Inside the container:

```bash
docker exec -it jenkins bash
```

Then:

```bash
cat \
  /var/jenkins_home/secrets/initialAdminPassword
```

Or directly from the host:

```bash
docker exec jenkins \
  cat /var/jenkins_home/secrets/initialAdminPassword
```

Do not publish this password in screenshots.

---

## 12.17 Open Jenkins

Browser:

```text
http://<JENKINS_SERVER_IP>:8080
```

Nana's initialization sequence:

```text
Unlock Jenkins
→ Install suggested plugins
→ Create admin user
→ Open dashboard
```

The suggested plugins include foundational functionality such as Git, Pipeline, and Credentials support.

---

## 12.18 Jenkins Port Security

The simple training setup exposes:

```text
8080
```

for the Jenkins web interface.

Future production hardening should use:

```text
HTTPS
reverse proxy
domain
restricted network access
```

rather than relying permanently on:

```text
http://IP:8080
```

This is intentionally not introduced into the basic capstone implementation.

---

# Docker From Inside Jenkins

## 12.19 Why Jenkins Needs Docker

The pipeline performs:

```text
Maven package
→ Docker build
→ Docker login
→ Docker push
```

Therefore the Jenkins environment must be able to execute Docker commands.

Initially:

```text
Docker daemon:
DigitalOcean host

Jenkins:
container
```

The Jenkins container cannot automatically access the host Docker daemon.

---

## 12.20 Nana's Docker-Outside-of-Docker Method

Nana mounts:

```text
/var/run/docker.sock
```

from the host into the Jenkins container.

Architecture:

```text
Jenkins container
      │
      │ Docker CLI
      ▼
/var/run/docker.sock
      │
      ▼
DigitalOcean host Docker daemon
```

This is commonly called:

```text
Docker-outside-of-Docker
```

rather than running another Docker daemon inside Jenkins.

---

## 12.21 Recreate Jenkins With Docker Socket

Before recreating:

```bash
docker ps

docker volume ls
```

Confirm:

```text
jenkins_home exists
```

Stop:

```bash
docker stop jenkins
```

Remove only the container:

```bash
docker rm jenkins
```

Do **not** remove:

```text
jenkins_home
```

Then recreate:

```bash
docker run -d \
  --name jenkins \
  --restart unless-stopped \
  --publish 8080:8080 \
  --publish 50000:50000 \
  --volume jenkins_home:/var/jenkins_home \
  --volume /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk17
```

Verify:

```bash
docker ps \
  --filter name=jenkins
```

Jenkins should retain its previous persistent configuration because the same `jenkins_home` volume is mounted.

---

## 12.22 Docker CLI Is Also Required

Mounting:

```text
/var/run/docker.sock
```

gives access to the host Docker daemon.

It does not automatically guarantee the command:

```bash
docker
```

exists inside the Jenkins container.

The Jenkins runtime must therefore also contain the Docker CLI.

Nana's training installed/made Docker available inside the Jenkins environment before testing:

```bash
docker pull redis
```

and later:

```bash
docker build
docker login
docker push
```

---

## 12.23 Nana's Historical Docker-Socket Permission Method

After mounting the socket, Nana entered Jenkins as root:

```bash
docker exec \
  -u 0 \
  -it jenkins \
  bash
```

or equivalently:

```bash
docker exec \
  -it \
  -u root \
  jenkins \
  bash
```

Inspect:

```bash
ls -l \
  /var/run/docker.sock
```

Nana's demo then used:

```bash
chmod 666 \
  /var/run/docker.sock
```

to grant read/write access to everyone so the Jenkins user could execute Docker commands.

Verify:

```bash
ls -l \
  /var/run/docker.sock
```

Then test as Jenkins:

```bash
exit

docker exec \
  -it \
  jenkins \
  bash
```

Inside:

```bash
docker pull redis
```

or preferably for this project:

```bash
docker version
```

---

## 12.24 Security Warning — `chmod 666`

This is Nana's **training/demo solution**.

It makes the Docker socket writable by every local user with access to the socket.

Docker socket access effectively provides high control over the host.

Therefore:

```text
chmod 666 /var/run/docker.sock
```

is preserved here because it is part of Nana's original learning method, but it should not be presented as the preferred production configuration.

---

## 12.25 Simple Safer Improvement

A safer Docker-socket approach is to preserve group-based access instead of world read/write access.

On the DigitalOcean host:

```bash
getent group docker
```

Capture the group ID:

```bash
export DOCKER_GID="$(
  getent group docker \
  | cut -d: -f3
)"
```

Then recreate Jenkins with:

```bash
--group-add "${DOCKER_GID}"
```

Example:

```bash
docker run -d \
  --name jenkins \
  --restart unless-stopped \
  --publish 8080:8080 \
  --publish 50000:50000 \
  --group-add "${DOCKER_GID}" \
  --volume jenkins_home:/var/jenkins_home \
  --volume /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk17
```

This is a later security improvement.

Do not mislabel it as Nana's original `chmod 666` method.

---

# Jenkins Maven

## 12.26 Maven Configuration

The pipeline contains:

```groovy
tools {
    maven 'Maven'
}
```

Jenkins must therefore have a Maven tool named exactly:

```text
Maven
```

Configure:

```text
Manage Jenkins
→ Tools
→ Maven installations
→ Add Maven
```

Name:

```text
Maven
```

Verify later through the pipeline:

```text
Declarative: Tool Install
```

followed by successful Maven commands.

---

# EKS Tooling Inside Jenkins

## 12.27 Nana's EKS Jenkins Prerequisites

The EKS deployment lesson identifies four requirements:

```text
kubectl
AWS IAM authentication tool
kubeconfig
AWS credentials
```

The current capstone additionally requires:

```text
AWS CLI
envsubst
Docker CLI
```

because the final shared library explicitly runs:

```text
aws ecr get-login-password
envsubst
kubectl
docker
```

---

# kubectl

## 12.28 Enter Jenkins Container as Root

From DigitalOcean:

```bash
docker ps
```

Then:

```bash
docker exec \
  -it \
  -u root \
  jenkins \
  bash
```

Nana entered as root because installing binaries under:

```text
/usr/local/bin
```

requires elevated permissions.

---

## 12.29 Nana's kubectl Installation

Recovered command:

```bash
curl -LO \
  https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl

chmod +x ./kubectl

mv ./kubectl \
  /usr/local/bin/kubectl
```

The training sometimes used this as a combined command:

```bash
curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl; \
chmod +x ./kubectl; \
mv ./kubectl /usr/local/bin/kubectl
```

Verify:

```bash
kubectl version --client
```

At this point the command can exist even before Jenkins has authenticated to a cluster.

---

## 12.30 kubectl Version Compatibility

For a rebuild, use a `kubectl` version compatible with the EKS cluster.

Verified cluster:

```text
Kubernetes:
1.35
```

The exact original `kubectl` patch version installed inside Jenkins is not required by the assignment and was not captured as a fixed project requirement.

---

# AWS IAM Authenticator — Nana Method

## 12.31 Nana's Historical AWS IAM Authenticator Installation

Recovered training command:

```bash
curl -Lo aws-iam-authenticator \
  https://github.com/kubernetes-sigs/aws-iam-authenticator/releases/download/v0.6.11/aws-iam-authenticator_0.6.11_linux_amd64

chmod +x ./aws-iam-authenticator

mv ./aws-iam-authenticator \
  /usr/local/bin
```

Verify:

```bash
aws-iam-authenticator help
```

This preserves Nana's EKS lesson.

---

## 12.32 Important Capstone Authentication Distinction

The final capstone must not be described as depending exclusively on the old authenticator binary.

The confirmed EKS-deployment failure showed that the deployment path needed AWS credentials available while Kubernetes authentication occurred.

The final fix bound:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION
```

during the Deploy stage.

Therefore the reusable capstone requirement is:

```text
AWS CLI authentication available to kubectl/EKS
```

not merely:

```text
aws-iam-authenticator binary exists
```

Nana's authenticator installation is preserved as the historical training method.

---

# AWS CLI

## 12.33 Why AWS CLI Is Required Inside Jenkins

The final shared library explicitly executes:

```bash
aws ecr get-login-password
```

and EKS authentication can require:

```bash
aws eks get-token
```

Therefore:

```text
aws
```

must exist inside the Jenkins runtime.

---

## 12.34 Original AWS CLI Installation Command

The exact original capstone command used to install AWS CLI inside the existing Jenkins container is not recoverable verbatim from the relevant history.

Record:

```text
Original detail currently unavailable.
```

Do not invent an exact historical command.

---

## 12.35 Reproducible AWS CLI Installation

Inside the Jenkins container as root:

```bash
apt-get update

apt-get install -y \
  curl \
  unzip
```

Download AWS CLI v2:

```bash
curl \
  "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" \
  -o "/tmp/awscliv2.zip"
```

Extract:

```bash
unzip \
  /tmp/awscliv2.zip \
  -d /tmp
```

Install:

```bash
/tmp/aws/install
```

Cleanup:

```bash
rm -rf \
  /tmp/aws \
  /tmp/awscliv2.zip
```

Verify:

```bash
aws --version
```

This is the reproducible implementation required by the final shared-library behavior.

---

# envsubst

## 12.36 Why `envsubst` Is Required

The Kubernetes manifests contain:

```text
${APP_NAME}
${IMAGE_NAME}
${IMAGE_TAG}
```

The shared library performs:

```bash
envsubst \
  < kubernetes/deployment.yaml \
  | kubectl apply -f -
```

and the same for the Service.

Therefore Jenkins requires:

```text
envsubst
```

---

## 12.37 Nana's `envsubst` Installation

Nana explicitly entered the Jenkins container as root and installed:

```bash
apt-get update

apt-get install -y \
  gettext-base
```

`gettext-base` provides:

```text
envsubst
```

Verify:

```bash
command -v envsubst

envsubst --version
```

---

# Verify Jenkins Runtime Tools

## 12.38 Tool Verification

While inside Jenkins:

```bash
echo "===== Java ====="
java -version

echo
echo "===== Git ====="
git --version

echo
echo "===== Maven ====="
mvn --version || true

echo
echo "===== Docker ====="
docker --version

echo
echo "===== AWS CLI ====="
aws --version

echo
echo "===== kubectl ====="
kubectl version --client

echo
echo "===== envsubst ====="
envsubst --version
```

If Maven is managed entirely through Jenkins Tools, the shell's system Maven installation may differ from the pipeline-provided Maven.

The pipeline verification is authoritative for the Jenkins Maven tool.

---

# Jenkins Kubeconfig

## 12.39 Why Jenkins Needs Its Own Kubeconfig

Your Mac kubeconfig:

```text
~/.kube/config
```

belongs to the local machine.

Jenkins runs:

```text
inside a Docker container
on DigitalOcean
```

so it requires its own Kubernetes connection configuration.

Nana explicitly created a separate Jenkins kubeconfig.

---

## 12.40 Nana's Manual Kubeconfig Method

The original lesson created a file named:

```text
config
```

on the DigitalOcean host.

The file contained:

```text
cluster name
EKS API server
certificate-authority-data
authentication exec configuration
```

The exact original generic YAML in Nana's notes was not preserved completely enough to claim an exact file.

Do not invent the missing original file.

---

## 12.41 Current Reproducible Kubeconfig Method

For the capstone, use AWS CLI to generate the current kubeconfig structure rather than manually copying old authentication syntax:

```bash
aws eks update-kubeconfig \
  --name java-maven-eks \
  --region ca-central-1
```

When this runs as the Jenkins user, the default path becomes:

```text
/var/jenkins_home/.kube/config
```

This method also aligns with the final authentication behavior that required AWS credentials during `kubectl` execution.

The cluster must already exist before this command can succeed.

Therefore actual generation will be completed after EKS creation in Phase 12 Part 2.

---

## 12.42 Nana's Manual Copy Workflow

The original training workflow is preserved for reconstruction.

Inside Jenkins:

```bash
cd ~

pwd
```

Expected:

```text
/var/jenkins_home
```

Create:

```bash
mkdir -p \
  ~/.kube
```

Exit:

```bash
exit
```

From the DigitalOcean host:

```bash
docker cp \
  config \
  jenkins:/var/jenkins_home/.kube/config
```

Nana originally used the container ID rather than the fixed name:

```bash
docker cp \
  config \
  <container-id>:/var/jenkins_home/.kube/config
```

---

## 12.43 Secure Kubeconfig Permissions

Inside Jenkins as root:

```bash
chown -R \
  jenkins:jenkins \
  /var/jenkins_home/.kube
```

Then:

```bash
chmod 700 \
  /var/jenkins_home/.kube

chmod 600 \
  /var/jenkins_home/.kube/config
```

This prevents the file from becoming world-readable.

---

## 12.44 Verify Kubeconfig Exists Safely

Do **not** paste the entire kubeconfig into project documentation.

Verify metadata instead:

```bash
ls -ld \
  /var/jenkins_home/.kube

ls -l \
  /var/jenkins_home/.kube/config
```

Then:

```bash
kubectl config current-context
```

after the cluster has been created and authentication works.

---

# Jenkins AWS Credentials

## 12.45 Create `aws_ecr_creds`

Browser:

```text
Jenkins
→ Manage Jenkins
→ Credentials
→ System
→ Global credentials
→ Add Credentials
```

Current implementation:

```text
Kind:
Username with password

Username:
<AWS_ACCESS_KEY_ID>

Password:
<AWS_SECRET_ACCESS_KEY>

ID:
aws_ecr_creds
```

Never place these values in the runbook.

---

## 12.46 Create `github-token`

Browser:

```text
Jenkins
→ Manage Jenkins
→ Credentials
→ System
→ Global credentials
→ Add Credentials
```

Configuration:

```text
Kind:
Username with password

Username:
<GITHUB_USERNAME>

Password:
<GITHUB_PERSONAL_ACCESS_TOKEN>

ID:
github-token
```

The verified application checkout later reported:

```text
using credential github-token
```

---

# Jenkins Plugins

## 12.47 Plugin Baseline

Initial Jenkins setup:

```text
Install suggested plugins
```

Then ensure the project-required capabilities are available, including:

```text
Pipeline
Git
GitHub Branch Source
Credentials Binding
Ignore Committer Strategy
```

Depending on the Jenkins installation, some dependencies are installed automatically.

Do not install unrelated plugins only to make the environment look more complex.

---

# Provisioning Verification

## 12.48 Jenkins Host Verification

On DigitalOcean host:

```bash
echo "===== Docker Host ====="
docker version

echo
echo "===== Jenkins Container ====="
docker ps \
  --filter name=jenkins

echo
echo "===== Jenkins Volume ====="
docker volume ls \
  --filter name=jenkins_home
```

Expected:

```text
Docker running
Jenkins running
jenkins_home present
```

---

## 12.49 Jenkins Container Tool Verification

Run:

```bash
docker exec \
  jenkins \
  bash -lc '
    echo "===== Java ====="
    java -version

    echo
    echo "===== Git ====="
    git --version

    echo
    echo "===== Docker ====="
    docker --version

    echo
    echo "===== AWS CLI ====="
    aws --version

    echo
    echo "===== kubectl ====="
    kubectl version --client

    echo
    echo "===== envsubst ====="
    envsubst --version
  '
```

Do not print environment variables containing credentials.

---

## 12.50 Jenkins Docker Access Verification

Test:

```bash
docker exec \
  jenkins \
  docker version
```

Both:

```text
Client
Server
```

information should be available if the Jenkins container can communicate with the host Docker daemon.

If only the client is available and the daemon connection fails, inspect:

```bash
ls -l \
  /var/run/docker.sock
```

and the container's group/socket configuration.

---

## 12.51 Do Not Use Docker Socket Failure as a Reason to Expose It Globally

Nana's:

```bash
chmod 666 /var/run/docker.sock
```

is preserved as the original learning method.

For a rebuild, prefer fixing:

```text
group membership
group ID
socket ownership
container group access
```

rather than permanently giving all users host Docker control.

---

# Persistence Limitation

## 12.52 Manually Installing Tools Inside Jenkins Container

Commands such as:

```bash
apt-get install
curl ...
mv ... /usr/local/bin
```

modify the current running container filesystem.

If that container is deleted and recreated from the original Jenkins image:

```text
those manually installed binaries may disappear
```

while:

```text
/var/jenkins_home
```

persists.

This is an important limitation of Nana's simple learning method.

---

## 12.53 Future Reproducible Jenkins Image

A later production improvement is to build a custom Jenkins image containing:

```text
AWS CLI
kubectl
Docker CLI
envsubst
other required command-line tools
```

This makes the Jenkins runtime reproducible.

For this capstone, keep that as:

```text
Future production improvement
```

rather than making it a requirement of Nana's basic implementation.

---

# Server Security Notes

## 12.54 Do Not Store AWS Credentials in the Container Image

Never place:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
GitHub PAT
```

in a custom Jenkins Dockerfile.

Secrets belong in:

```text
Jenkins Credentials
```

not Docker image layers.

---

## 12.55 Do Not Commit Jenkins Kubeconfig

Never copy:

```text
/var/jenkins_home/.kube/config
```

into:

```text
application repository
shared-library repository
```

The repository `.gitignore` already excludes local kubeconfig-style files.

---

## 12.56 Server Network Security

The Jenkins server must allow the access required for:

```text
SSH administration
Jenkins browser/webhook access
GitHub
AWS APIs
ECR
EKS Kubernetes API
```

Avoid opening unrelated inbound ports.

Port:

```text
50000
```

is not necessary for this project unless inbound Jenkins agents are being used.

Nana exposed it in the original training Jenkins command for future worker communication.

For a hardened rebuild, do not expose it publicly unless needed.

---

# Part 1 Cost Review

## 12.57 Chargeable Resource Created

At this point:

```text
DigitalOcean Jenkins Droplet
✅ real chargeable resource
```

The following AWS resources should not yet be marked complete from Part 1:

```text
ECR repository
EKS cluster
managed nodegroup
Kubernetes LoadBalancer
```

These are handled in Part 2.

---

# Evidence to Capture

## 12.58 Part 1 Evidence

Capture:

```text
DigitalOcean Jenkins server running

docker --version on host

jenkins_home volume

Jenkins container running

port 8080 mapping

Jenkins UI accessible

Docker socket mounted

Docker command works from Jenkins

kubectl version --client

aws --version

envsubst --version

Jenkins Maven tool named Maven

credential IDs:
github-token
aws_ecr_creds

.kube directory location
without exposing kubeconfig contents
```

Never capture:

```text
initial Jenkins password after setup
AWS Secret Access Key
GitHub PAT
private keys
full kubeconfig
```

---

# Troubleshooting

## 12.59 Jenkins Container Missing After Docker Restart

Check:

```bash
docker ps

docker ps -a
```

If Jenkins is stopped:

```bash
docker start jenkins
```

Verify:

```bash
docker ps \
  --filter name=jenkins
```

---

## 12.60 Jenkins Data Appears Missing

Stop.

Do not initialize a new Jenkins environment immediately.

Check:

```bash
docker volume ls

docker inspect jenkins
```

Confirm the container is using:

```text
jenkins_home:/var/jenkins_home
```

A new empty volume would produce a seemingly fresh Jenkins environment.

---

## 12.61 Docker Command Not Found Inside Jenkins

This means:

```text
Docker socket may be mounted
but Docker CLI is missing
```

Verify:

```bash
command -v docker
```

The CLI must be installed or included in the Jenkins runtime.

---

## 12.62 Permission Denied on Docker Socket

Inspect:

```bash
ls -l \
  /var/run/docker.sock
```

Nana's demo fix:

```bash
chmod 666 \
  /var/run/docker.sock
```

Safer rebuild:

```text
match Docker group access
with Jenkins container group membership
```

Do not confuse:

```text
command not found
```

with:

```text
permission denied
```

They are different problems.

---

## 12.63 `kubectl` Exists but Cannot Connect

At the tool-install stage this may be expected.

Check separately:

```text
kubectl installed?
kubeconfig exists?
AWS credentials available?
EKS cluster exists?
API endpoint reachable?
AWS identity authorized?
```

Do not reinstall `kubectl` merely because cluster authentication has not yet been configured.

---

## 12.64 `envsubst: command not found`

Inside Jenkins as root:

```bash
apt-get update

apt-get install -y \
  gettext-base
```

Verify:

```bash
command -v envsubst
```

---

## 12.65 `aws: command not found`

Install AWS CLI in the Jenkins runtime.

Verify:

```bash
aws --version
```

Do not work around this by moving AWS authentication logic back into the application repository.

The Jenkins runtime is responsible for providing the required CLI.

---

# Phase 12 Part 1 Checklist

## 12.66 Completion Criteria

```text
[ ] DigitalOcean Jenkins host exists
[ ] Ubuntu host reachable by SSH
[ ] Docker installed on host
[ ] Docker daemon running
[ ] jenkins_home volume created
[ ] Jenkins runs in Docker
[ ] Jenkins UI reachable
[ ] Jenkins initialization complete
[ ] suggested plugins installed
[ ] Jenkins persistent state verified
[ ] Docker socket mounted
[ ] Docker CLI available inside Jenkins
[ ] Jenkins can communicate with host Docker daemon
[ ] Maven tool named Maven configured
[ ] kubectl available
[ ] Nana aws-iam-authenticator method documented
[ ] AWS CLI available
[ ] envsubst available
[ ] Jenkins .kube location prepared
[ ] github-token exists in Jenkins
[ ] aws_ecr_creds exists in Jenkins
[ ] secret values remain outside Git
[ ] DigitalOcean cost acknowledged
```

---

## 12.67 Phase 12 Status

```text
DigitalOcean Jenkins server
✅ documented

Docker host
✅ documented

Jenkins container
✅ documented

Jenkins persistence
✅ documented

Docker-outside-of-Docker
✅ documented

Nana chmod 666 method
✅ preserved

safer group-based improvement
✅ distinguished

kubectl
✅ documented

aws-iam-authenticator
✅ Nana historical method preserved

AWS CLI
✅ reproducible capstone requirement documented

envsubst
✅ documented

Jenkins kubeconfig workflow
✅ documented

Jenkins credentials
✅ documented

Amazon ECR provisioning
🟡 Part 2

Amazon EKS provisioning
🟡 Part 2

managed nodegroup
🟡 Part 2

cluster connectivity
🟡 Part 2
```

## 12.68 AWS Provisioning Scope

With the Jenkins runtime prepared, provision the AWS resources required by the CI/CD pipeline.

Provisioning order:

```text
verify AWS identity
→ verify region
→ check whether ECR already exists
→ create ECR if absent
→ verify ECR configuration
→ check whether EKS already exists
→ create EKS if absent
→ wait for cluster/nodegroup readiness
→ configure local kubeconfig
→ verify Kubernetes control plane
→ verify worker node
→ configure/verify Jenkins kubeconfig
→ verify Jenkins can authenticate to EKS
```

Do not create duplicate resources.

---

# AWS Identity Pre-Check

## 12.69 Working Environment

The AWS infrastructure can be provisioned from the authenticated local workstation.

Verified project working directory:

```text
/Users/younghadiz/Documents/tech-workspace/devops-capstone-projects/complete-jenkins-cicd-pipeline-eks-ecr
```

Verify:

```bash
pwd

aws --version

eksctl version

kubectl version --client
```

---

## 12.70 Set the Region

```bash
export AWS_REGION=ca-central-1
export AWS_DEFAULT_REGION=ca-central-1
```

Verify:

```bash
printf 'AWS_REGION=%s\n' "$AWS_REGION"
printf 'AWS_DEFAULT_REGION=%s\n' "$AWS_DEFAULT_REGION"
```

Expected:

```text
ca-central-1
```

---

## 12.71 Verify AWS Identity

Run:

```bash
aws sts get-caller-identity
```

Do not display or capture:

```text
AWS_SECRET_ACCESS_KEY
AWS_SESSION_TOKEN
```

Retrieve the account ID programmatically:

```bash
export AWS_ACCOUNT_ID="$(
  aws sts get-caller-identity \
    --query Account \
    --output text
)"
```

Verify:

```bash
printf 'AWS account: %s\n' "$AWS_ACCOUNT_ID"
```

The verified account used by this project was:

```text
002184382122
```

Reusable projects should use:

```text
<AWS_ACCOUNT_ID>
```

rather than copying this value.

---

# Amazon ECR Provisioning

## 12.72 ECR Resource Name

Repository:

```text
java-maven-app
```

Region:

```text
ca-central-1
```

Define:

```bash
export ECR_REPOSITORY=java-maven-app

export ECR_REGISTRY_SERVER="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

export ECR_IMAGE_NAME="${ECR_REGISTRY_SERVER}/${ECR_REPOSITORY}"
```

Verify:

```bash
echo "$ECR_REGISTRY_SERVER"
echo "$ECR_IMAGE_NAME"
```

---

## 12.73 Always Check Before Creating ECR

Run:

```bash
aws ecr describe-repositories \
  --repository-names java-maven-app \
  --region ca-central-1
```

Two valid situations:

```text
repository exists
→ inspect and reuse it

RepositoryNotFoundException
→ create it
```

Do not blindly create another repository.

---

## 12.74 Original ECR Creation Command

The exact literal historical creation command was not independently recovered from the capstone transcript.

Record:

```text
Original detail currently unavailable.
```

The final AWS state, however, was fully verified.

---

## 12.75 Reproducible ECR Creation Command

The following command reproduces the verified repository configuration:

```text
REPRODUCIBLE IMPLEMENTATION
```

```bash
aws ecr create-repository \
  --repository-name java-maven-app \
  --image-tag-mutability MUTABLE \
  --image-scanning-configuration scanOnPush=true \
  --encryption-configuration encryptionType=AES256 \
  --region ca-central-1
```

Expected important values:

```text
repositoryName:
java-maven-app

imageTagMutability:
MUTABLE

scanOnPush:
true

encryptionType:
AES256
```

---

## 12.76 Verified ECR Creation Result

The live project later reported:

```text
RepositoryName:
java-maven-app

RepositoryUri:
002184382122.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app

CreatedAt:
2026-09-20T17:11:36.610000-07:00

TagMutability:
MUTABLE

ScanOnPush:
true

EncryptionType:
AES256
```

This is the authoritative resulting configuration.

---

## 12.77 Verify ECR Configuration

Run:

```bash
aws ecr describe-repositories \
  --repository-names java-maven-app \
  --region ca-central-1 \
  --query 'repositories[0].{
    RepositoryName:repositoryName,
    RepositoryUri:repositoryUri,
    CreatedAt:createdAt,
    TagMutability:imageTagMutability,
    ScanOnPush:imageScanningConfiguration.scanOnPush,
    EncryptionType:encryptionConfiguration.encryptionType
  }' \
  --output yaml
```

Expected:

```text
RepositoryName: java-maven-app
TagMutability: MUTABLE
ScanOnPush: true
EncryptionType: AES256
```

---

## 12.78 Capture the Repository URI Dynamically

Do not repeatedly type the AWS account ID by hand.

Use:

```bash
export ECR_IMAGE_NAME="$(
  aws ecr describe-repositories \
    --repository-names java-maven-app \
    --region ca-central-1 \
    --query 'repositories[0].repositoryUri' \
    --output text
)"
```

Verify:

```bash
echo "$ECR_IMAGE_NAME"
```

Expected format:

```text
<AWS_ACCOUNT_ID>.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app
```

---

## 12.79 Do Not Push an Image Yet

At this point:

```text
ECR repository
✅ provisioned
```

but:

```text
pipeline image push
❌ not part of infrastructure provisioning
```

Image creation/push belongs to the actual CI/CD deployment flow.

---

# Amazon EKS Provisioning

## 12.80 EKS Resource Names

Cluster:

```text
java-maven-eks
```

Managed nodegroup:

```text
java-maven-nodes
```

Region:

```text
ca-central-1
```

Instance type:

```text
t3.small
```

Scaling:

```text
minimum:
1

desired:
1

maximum:
2
```

Kubernetes version:

```text
1.35
```

---

## 12.81 Check Existing Clusters First

Run:

```bash
eksctl get cluster \
  --region ca-central-1
```

If:

```text
java-maven-eks
```

already exists, inspect it instead of running another create command.

The verified project eventually returned:

```text
NAME            REGION          EKSCTL CREATED
java-maven-eks  ca-central-1    True
```

---

## 12.82 Check AWS Directly

Also verify:

```bash
aws eks describe-cluster \
  --name java-maven-eks \
  --region ca-central-1
```

If the cluster does not exist, AWS returns a not-found error.

Only then proceed with creation.

---

## 12.83 Original `eksctl create cluster` Command

The exact literal command from the original capstone execution was not independently recovered.

Record:

```text
Original detail currently unavailable.
```

What is fully recovered is the resulting cluster and nodegroup configuration.

---

## 12.84 Reproducible `eksctl` Command

This command follows Nana's simple `eksctl` approach and reproduces the verified target configuration:

```text
REPRODUCIBLE IMPLEMENTATION
```

```bash
eksctl create cluster \
  --name java-maven-eks \
  --region ca-central-1 \
  --version 1.35 \
  --nodegroup-name java-maven-nodes \
  --node-type t3.small \
  --nodes 1 \
  --nodes-min 1 \
  --nodes-max 2 \
  --managed
```

This creates real chargeable AWS resources.

---

## 12.85 What `eksctl` Provisions

With this simplified approach, `eksctl` manages supporting AWS infrastructure for the cluster.

Resulting resources can include:

```text
EKS control plane

VPC

subnets

route tables

internet/NAT-related networking
depending on generated topology

security groups

IAM cluster role

IAM worker-node role

managed nodegroup

EC2 instance

Auto Scaling Group

launch template

CloudFormation stacks
```

This is why cluster deletion must later be performed through `eksctl` rather than manually deleting random pieces first.

---

# Verify EKS Control Plane

## 12.86 Verify Using `eksctl`

Run:

```bash
eksctl get cluster \
  --region ca-central-1
```

Expected:

```text
java-maven-eks
ca-central-1
True
```

---

## 12.87 Verify Cluster With AWS CLI

Run:

```bash
aws eks describe-cluster \
  --name java-maven-eks \
  --region ca-central-1 \
  --query 'cluster.{
    Name:name,
    Version:version,
    Status:status,
    PlatformVersion:platformVersion,
    Endpoint:endpoint,
    RoleArn:roleArn,
    VpcId:resourcesVpcConfig.vpcId,
    SubnetIds:resourcesVpcConfig.subnetIds,
    SecurityGroupIds:resourcesVpcConfig.securityGroupIds,
    ClusterSecurityGroupId:resourcesVpcConfig.clusterSecurityGroupId,
    EndpointPublicAccess:resourcesVpcConfig.endpointPublicAccess,
    EndpointPrivateAccess:resourcesVpcConfig.endpointPrivateAccess,
    PublicAccessCidrs:resourcesVpcConfig.publicAccessCidrs
  }' \
  --output yaml
```

---

## 12.88 Verified Cluster State

The final live cluster reported:

```text
Name:
java-maven-eks

Status:
ACTIVE

Version:
1.35

PlatformVersion:
eks.23

VpcId:
vpc-06de21d476d9521c8

EndpointPublicAccess:
true

EndpointPrivateAccess:
false

PublicAccessCidrs:
0.0.0.0/0
```

It also contained six generated subnets and dedicated EKS security groups.

The VPC, subnet, security-group, endpoint, and IAM-role IDs are generated environment-specific values.

Do not hardcode them into a reusable build command.

---

# Managed Nodegroup

## 12.89 Verify Nodegroup With `eksctl`

Run:

```bash
eksctl get nodegroup \
  --cluster java-maven-eks \
  --region ca-central-1
```

Verified result identified:

```text
Cluster:
java-maven-eks

Nodegroup:
java-maven-nodes

Status:
ACTIVE

Minimum:
1

Maximum:
2

Desired:
1

Instance:
t3.small

Image:
AL2023_x86_64_STANDARD

Type:
managed
```

---

## 12.90 Verify Nodegroup Through AWS CLI

Run:

```bash
aws eks describe-nodegroup \
  --cluster-name java-maven-eks \
  --nodegroup-name java-maven-nodes \
  --region ca-central-1 \
  --query 'nodegroup.{
    Name:nodegroupName,
    Status:status,
    Version:version,
    CapacityType:capacityType,
    InstanceTypes:instanceTypes,
    DiskSize:diskSize,
    Desired:scalingConfig.desiredSize,
    Minimum:scalingConfig.minSize,
    Maximum:scalingConfig.maxSize,
    Subnets:subnets,
    NodeRole:nodeRole,
    AmiType:amiType
  }' \
  --output yaml
```

Verified:

```text
Name:
java-maven-nodes

Status:
ACTIVE

Version:
1.35

CapacityType:
ON_DEMAND

InstanceTypes:
- t3.small

Desired:
1

Minimum:
1

Maximum:
2

AmiType:
AL2023_x86_64_STANDARD
```

---

## 12.91 Verified Nodegroup Creation Record

The live `eksctl` output recorded the nodegroup creation at:

```text
2026-09-21T00:30:17Z
```

This is useful historical evidence, but a recreated environment will naturally have a different creation timestamp.

---

# Local kubeconfig

## 12.92 Configure Local Kubernetes Access

Once the cluster is ACTIVE:

```bash
aws eks update-kubeconfig \
  --name java-maven-eks \
  --region ca-central-1
```

This updates:

```text
~/.kube/config
```

on the local workstation.

Do not commit that file.

---

## 12.93 Verify Current Context

Run:

```bash
kubectl config current-context
```

Expected ARN format:

```text
arn:aws:eks:ca-central-1:<AWS_ACCOUNT_ID>:cluster/java-maven-eks
```

Do not depend on the complete ARN as application configuration.

---

# Kubernetes Cluster Connectivity

## 12.94 Verify Control Plane

Run:

```bash
kubectl cluster-info
```

The verified project reported the Kubernetes control plane at its EKS API endpoint and CoreDNS through the same cluster.

Expected pattern:

```text
Kubernetes control plane is running at https://...
CoreDNS is running at https://...
```

---

## 12.95 Verify Nodes

Run:

```bash
kubectl get nodes \
  -o wide
```

The verified node was:

```text
Status:
Ready

Kubernetes:
v1.35.8-eks-a887778

OS:
Amazon Linux 2023.12.20260914

Architecture:
amd64

Container runtime:
containerd://2.2.7+unknown
```

---

## 12.96 Verified Worker Architecture

The live node later showed:

```text
Architecture:
amd64

Instance type:
t3.small

Capacity type:
ON_DEMAND

Nodegroup:
java-maven-nodes
```

This confirms why the Jenkins-built deployment image needed to be compatible with x86_64/AMD64 rather than relying on the local Apple Silicon ARM64 image.

---

## 12.97 Node Health Verification

Run:

```bash
kubectl describe node \
  "$(kubectl get nodes -o jsonpath='{.items[0].metadata.name}')"
```

Important healthy conditions:

```text
MemoryPressure:
False

DiskPressure:
False

PIDPressure:
False

Ready:
True
```

The verified node showed all four healthy states.

---

# Jenkins kubeconfig After EKS Exists

## 12.98 Nana Method — Manual Config Copy

Nana's historical sequence remains:

```text
prepare config file on DigitalOcean host
→ create /var/jenkins_home/.kube
→ docker cp config into Jenkins
→ verify file
```

Example:

```bash
docker exec \
  -it \
  jenkins \
  bash
```

Inside:

```bash
mkdir -p ~/.kube
exit
```

Host:

```bash
docker cp \
  config \
  jenkins:/var/jenkins_home/.kube/config
```

This reproduces Nana's training approach.

---

## 12.99 Preferred Reproducible Capstone Method

Because AWS CLI is already required inside Jenkins, a cleaner current rebuild can generate Jenkins' kubeconfig directly after the cluster exists.

The command must run with the Jenkins AWS credential temporarily available.

Conceptually:

```bash
mkdir -p \
  /var/jenkins_home/.kube

aws eks update-kubeconfig \
  --name java-maven-eks \
  --region ca-central-1 \
  --kubeconfig /var/jenkins_home/.kube/config

chmod 600 \
  /var/jenkins_home/.kube/config
```

Do not hardcode the AWS access key into this command.

Use Jenkins Credentials Binding.

---

## 12.100 Why AWS Credentials Are Still Needed Later

`aws eks update-kubeconfig` does not permanently replace AWS authentication.

Typical EKS kubeconfig authentication uses an executable AWS token flow.

Therefore when Jenkins later runs:

```bash
kubectl apply
```

AWS credentials must still be available to generate an EKS authentication token.

This is exactly why the later Deploy-stage credential fix was required.

---

# Jenkins-to-EKS Verification

## 12.101 Verify Jenkins Has Required Tools

From DigitalOcean:

```bash
docker exec \
  jenkins \
  bash -lc '
    aws --version
    kubectl version --client
    command -v envsubst
  '
```

All commands should resolve.

---

## 12.102 Do Not Test AWS Authentication Without Credential Scope

Running:

```bash
docker exec jenkins aws sts get-caller-identity
```

will not necessarily work merely because the Jenkins credential exists in the Jenkins UI.

Jenkins Credentials are injected into builds only when requested by the pipeline.

Therefore AWS identity testing should occur:

```text
inside a Jenkins build
+
inside withCredentials(...)
```

not by expecting secrets to exist permanently in the container environment.

---

## 12.103 Safe Jenkins AWS Verification Pattern

Inside a temporary pipeline/script stage:

```groovy
withCredentials([
    usernamePassword(
        credentialsId: 'aws_ecr_creds',
        usernameVariable: 'AWS_ACCESS_KEY_ID',
        passwordVariable: 'AWS_SECRET_ACCESS_KEY'
    )
]) {
    withEnv([
        'AWS_REGION=ca-central-1',
        'AWS_DEFAULT_REGION=ca-central-1'
    ]) {
        sh '''
            set -e

            aws sts get-caller-identity

            kubectl get nodes
        '''
    }
}
```

Do not echo the credential variables.

---

## 12.104 Verify Jenkins Can Read the Cluster

Minimum connectivity test:

```bash
kubectl get nodes
```

Expected:

```text
java-maven-nodes worker:
Ready
```

Do not deploy the application yet.

Deployment belongs to Phase 13.

---

## 12.105 Verify Kubernetes Authorization

Useful authorization checks:

```bash
kubectl auth can-i get deployments \
  --namespace default

kubectl auth can-i create deployments \
  --namespace default

kubectl auth can-i patch deployments \
  --namespace default

kubectl auth can-i create services \
  --namespace default

kubectl auth can-i patch services \
  --namespace default
```

The Jenkins identity must be authorized for the operations performed by:

```text
kubectl apply
kubectl rollout status
```

---

# EKS Public API Verification

## 12.106 Verify API Endpoint Mode

Run:

```bash
aws eks describe-cluster \
  --name java-maven-eks \
  --region ca-central-1 \
  --query 'cluster.resourcesVpcConfig.{
    Public:endpointPublicAccess,
    Private:endpointPrivateAccess,
    CIDRs:publicAccessCidrs
  }' \
  --output yaml
```

Verified result:

```text
Public:
true

Private:
false

CIDRs:
- 0.0.0.0/0
```

This made access from DigitalOcean simple but is a known security limitation.

---

# ECR Authentication Verification

## 12.107 Verify ECR Login Manually Only When Needed

With a valid AWS identity:

```bash
aws ecr get-login-password \
  --region ca-central-1 \
  | docker login \
      --username AWS \
      --password-stdin \
      "${AWS_ACCOUNT_ID}.dkr.ecr.ca-central-1.amazonaws.com"
```

Expected:

```text
Login Succeeded
```

Do not store the generated password.

The Jenkins shared library performs this operation automatically later.

---

# Infrastructure Versus Deployment Boundary

## 12.108 What Must Exist Before Deployment

At the end of Phase 12:

```text
DigitalOcean Jenkins
✅

Docker runtime
✅

Jenkins tools
✅

AWS credentials in Jenkins
✅

ECR repository
✅

EKS control plane
✅

managed nodegroup
✅

kubeconfig
✅

cluster connectivity
✅

worker Ready
✅
```

But these remain Phase 13 work:

```text
increment Maven version
❌

build Jenkins JAR
❌

build final Docker image
❌

push versioned image to ECR
❌

kubectl apply Deployment
❌

kubectl apply Service
❌

wait for rollout
❌

Jenkins commit-back
❌
```

Do not collapse provisioning and deployment into one runbook phase.

---

# Verified Final Infrastructure Snapshot

## 12.109 ECR

```text
Repository:
java-maven-app

Region:
ca-central-1

URI:
002184382122.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app

Scan on push:
true

Tag mutability:
MUTABLE

Encryption:
AES256
```

---

## 12.110 EKS

```text
Cluster:
java-maven-eks

Region:
ca-central-1

Status:
ACTIVE

Version:
1.35

Platform:
eks.23

eksctl created:
true
```

---

## 12.111 Nodegroup

```text
Name:
java-maven-nodes

Status:
ACTIVE

Type:
managed

Capacity:
ON_DEMAND

Instance:
t3.small

Minimum:
1

Desired:
1

Maximum:
2

AMI:
AL2023_x86_64_STANDARD

Architecture:
amd64
```

---

## 12.112 Verified Kubernetes Node

The final worker node was verified as:

```text
Ready

Amazon Linux 2023

amd64

t3.small

containerd
```

with no:

```text
MemoryPressure
DiskPressure
PIDPressure
```

and:

```text
Ready=True
```

---

# Provisioning Troubleshooting

## 12.113 ECR Already Exists

If:

```bash
aws ecr create-repository ...
```

returns a repository-already-exists error:

do not delete it automatically.

Inspect:

```bash
aws ecr describe-repositories \
  --repository-names java-maven-app \
  --region ca-central-1
```

Reuse it if its configuration is correct.

---

## 12.114 EKS Cluster Already Exists

If:

```text
java-maven-eks
```

appears in:

```bash
eksctl get cluster \
  --region ca-central-1
```

do not run another `create cluster`.

Inspect the existing resource first.

---

## 12.115 `eksctl` Creation Fails

Possible categories:

```text
AWS IAM permissions

AWS service quota

unsupported Kubernetes version

EC2 capacity

network/VPC problem

CloudFormation failure
```

Inspect:

```bash
eksctl get cluster \
  --region ca-central-1
```

and AWS CloudFormation events.

Do not repeatedly rerun the create command without understanding whether partial resources already exist.

---

## 12.116 Cluster ACTIVE but No Ready Node

Check:

```bash
eksctl get nodegroup \
  --cluster java-maven-eks \
  --region ca-central-1

kubectl get nodes
```

Then:

```bash
aws eks describe-nodegroup \
  --cluster-name java-maven-eks \
  --nodegroup-name java-maven-nodes \
  --region ca-central-1
```

Cluster readiness and nodegroup readiness are separate.

---

## 12.117 `kubectl` Reports Unauthorized

This is primarily an authentication/authorization problem.

Check:

```text
AWS credentials

kubeconfig

AWS principal

EKS access

Kubernetes permissions

Deploy-stage credential scope
```

Do not rebuild the cluster first.

---

## 12.118 `kubectl` Times Out

This is more likely:

```text
network connectivity

EKS endpoint access

DNS

firewall

routing
```

rather than an IAM policy problem.

Separate network failures from authentication failures.

---

## 12.119 Wrong Kubernetes Context

Run:

```bash
kubectl config current-context
```

Then:

```bash
kubectl config get-contexts
```

Ensure commands target:

```text
java-maven-eks
```

before changing resources.

---

# Cost Verification

## 12.120 Chargeable Infrastructure Now Active

After this phase, the following are potentially generating charges:

```text
DigitalOcean Jenkins Droplet

Amazon EKS control plane

EC2 t3.small worker node

worker EBS storage

public IPv4 resources where applicable

ECR image storage once images are pushed
```

The external Kubernetes LoadBalancer is **not created until Phase 13 deployment applies the Service**.

---

## 12.121 Do Not Delete Yet

The infrastructure is needed for:

```text
deployment
network verification
monitoring
end-to-end testing
rollback
documentation evidence
release verification
```

Cleanup belongs only to:

```text
Phase 20
```

unless cost or safety requires early termination.

---

# Evidence to Capture

## 12.122 ECR Evidence

Capture:

```bash
aws ecr describe-repositories \
  --repository-names java-maven-app \
  --region ca-central-1 \
  --query 'repositories[0].{
    RepositoryName:repositoryName,
    RepositoryUri:repositoryUri,
    TagMutability:imageTagMutability,
    ScanOnPush:imageScanningConfiguration.scanOnPush,
    EncryptionType:encryptionConfiguration.encryptionType
  }' \
  --output yaml
```

---

## 12.123 EKS Evidence

Capture:

```bash
eksctl get cluster \
  --region ca-central-1

eksctl get nodegroup \
  --cluster java-maven-eks \
  --region ca-central-1
```

Then:

```bash
kubectl cluster-info

kubectl get nodes \
  -o wide
```

---

## 12.124 Security-Safe Screenshots

Safe evidence can show:

```text
cluster name
region
Kubernetes version
nodegroup name
node type
node Ready status
ECR repository name
scan-on-push state
encryption type
```

Consider hiding or cropping:

```text
full AWS account identifiers
public IPs
API endpoints
IAM role ARNs
security-group IDs
subnet IDs
```

when publishing screenshots publicly.

They are not authentication secrets, but unnecessary environment identifiers need not be advertised.

---

# Phase 12 Completion

## 12.125 Final Checklist

```text
[ ] DigitalOcean Jenkins server available
[ ] Docker host available
[ ] Jenkins container available
[ ] Jenkins persistent volume verified
[ ] Docker accessible from Jenkins
[ ] Maven configured
[ ] AWS CLI available
[ ] kubectl available
[ ] envsubst available
[ ] Jenkins credentials configured

[ ] AWS identity verified
[ ] region ca-central-1 verified

[ ] ECR existence checked
[ ] java-maven-app created/reused
[ ] ECR repository URI verified
[ ] scan-on-push true
[ ] encryption AES256
[ ] tag mutability MUTABLE

[ ] EKS existence checked
[ ] java-maven-eks created/reused
[ ] cluster ACTIVE
[ ] Kubernetes version 1.35
[ ] java-maven-nodes ACTIVE
[ ] managed nodegroup confirmed
[ ] t3.small confirmed
[ ] capacity ON_DEMAND confirmed
[ ] minimum 1 confirmed
[ ] desired 1 confirmed
[ ] maximum 2 confirmed
[ ] AL2023_x86_64_STANDARD confirmed

[ ] local kubeconfig configured
[ ] correct context selected
[ ] kubectl cluster-info succeeds
[ ] worker node Ready
[ ] worker architecture amd64 verified

[ ] Jenkins kubeconfig available
[ ] AWS credentials available during authenticated Jenkins operations
[ ] Jenkins can reach EKS
[ ] Kubernetes authorization sufficient

[ ] cost-producing resources recorded
[ ] cleanup plan retained
```

---

## 12.126 Phase 12 Final State

```text
DigitalOcean Jenkins
✅

Docker runtime
✅

Jenkins persistence
✅

Jenkins cloud tooling
✅

ECR
✅

EKS control plane
✅

managed worker node
✅

local Kubernetes connectivity
✅

Jenkins EKS prerequisites
✅

cost warning
✅
```

**Server and Cloud Provisioning is complete.**

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
