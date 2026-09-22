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
