# Troubleshooting Record

## Purpose

This document records problems, warnings and operational observations encountered while implementing and verifying the project.

It focuses on **verified project history** rather than generic troubleshooting advice.

Each entry records:

```text
symptom
context
root cause
fix
verification
lesson
```

---

# 1. JAR Inspection Failed with `NoSuchFileException`

## Symptom

After a successful Maven package operation, the JAR existed at:

```text
target/java-maven-app-1.1.0-SNAPSHOT.jar
```

The following command was run from the repository root:

```bash
jar tf java-maven-app-1.1.0-SNAPSHOT.jar
```

Result:

```text
java.nio.file.NoSuchFileException:
java-maven-app-1.1.0-SNAPSHOT.jar
```

## Root Cause

The JAR was not located in the repository root.

It was under:

```text
target/
```

The build itself had succeeded.

The failure was only caused by using the wrong relative path.

## Fix

Use:

```bash
jar tf \
  target/java-maven-app-1.1.0-SNAPSHOT.jar
```

or:

```bash
cd target

jar tf \
  java-maven-app-1.1.0-SNAPSHOT.jar
```

## Verification

First identify generated artifacts:

```bash
find target \
  -maxdepth 1 \
  -type f \
  -print \
  | sort
```

Then inspect the exact path returned.

## Lesson

Before diagnosing an artifact as missing:

```text
verify the filesystem path
```

A successful Maven build followed by `NoSuchFileException` does not necessarily indicate a build failure.

---

# 2. Local Docker Container Stopped with Exit Code 143

## Observation

The local container was stopped intentionally:

```bash
docker stop \
  java-maven-app-local
```

Afterward:

```bash
docker ps -a \
  --filter name=java-maven-app-local
```

showed:

```text
Exited (143)
```

## Meaning

Exit code:

```text
143
```

is:

```text
128 + 15
```

where signal 15 is:

```text
SIGTERM
```

Docker normally sends SIGTERM during a graceful:

```bash
docker stop
```

operation.

## Verification

Before stop:

```text
HTTP status:
200
```

After stop:

```text
HTTP status after stop:
000
```

The application was no longer listening because the container had been deliberately stopped.

## Lesson

In this context:

```text
Exit 143
```

and:

```text
HTTP 000 after stop
```

were expected behavior rather than an application defect.

---

# 3. Jenkins Git Tool Warning

## Symptom

Jenkins displayed:

```text
Selected Git installation does not exist. Using Default
```

and:

```text
The recommended git tool is: NONE
```

during repository checkout.

## Observation

Despite the warning, Jenkins also reported a working Git version and successfully:

```text
fetched repository
checked out application branch
loaded Jenkins Shared Library
continued the pipeline
```

The final pipeline completed successfully.

## Root Cause

The configured Jenkins Git tool entry did not correspond to a named Git installation Jenkins wanted to recommend.

However, Git itself existed in the Jenkins environment and was usable.

## Fix

No emergency pipeline fix was required.

A configuration cleanup can explicitly configure the Git executable under Jenkins tools if desired.

## Verification

Jenkins showed:

```text
git version 2.47.3
```

and successfully checked out both:

```text
application repository
Jenkins Shared Library
```

## Lesson

Differentiate:

```text
warning
```

from:

```text
blocking failure
```

Do not modify a working pipeline solely because a non-blocking warning is present.

---

# 4. EKS Deployment Failed with `NoCredentials`

## Symptom

The pipeline successfully completed:

```text
Increment Version
Build Application
Build Docker Image
Push Docker Image
```

Image:

```text
java-maven-app:1.1.1-1
```

was successfully pushed to ECR.

The Deploy stage then ran:

```bash
envsubst \
  < kubernetes/deployment.yaml \
  | kubectl apply \
      --namespace default \
      -f -
```

and failed with:

```text
NoCredentials
Unable to locate credentials
```

The authentication executable used by the EKS kubeconfig could not obtain an AWS token.

Jenkins subsequently skipped:

```text
Commit Version Update
```

and reported:

```text
Finished: FAILURE
```

## Initial Important Observation

ECR push had already succeeded.

Therefore:

```text
AWS credentials existed in Jenkins
```

The problem was not simply:

```text
wrong AWS key
```

or:

```text
credential missing from Jenkins
```

## Root Cause

AWS credentials were scoped only around the:

```text
Push Docker Image
```

stage.

Conceptually:

```text
Push Docker Image
    │
    └── withCredentials(...)
            │
            ├── ECR login
            └── Docker push
    │
    ▼
credential scope ends

Deploy
    │
    ▼
kubectl
    │
    ▼
EKS AWS token authentication
    │
    ▼
AWS credentials required again
```

By the time `kubectl` requested EKS authentication, the credentials from the previous stage were no longer available.

## Fix

Shared-library branch:

```text
fix/eks-deployment-aws-credentials
```

Fix commit:

```text
9aae486
fix: provide AWS credentials during EKS deployment
```

The Deploy stage was wrapped with:

```groovy
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
```

## Verification Strategy

The library fix was not merged immediately.

A temporary consumer branch was used:

```text
bugfix/verify-eks-deployment-credentials
```

Application verification commit:

```text
1f772e5
test: verify EKS deployment credentials fix
```

The application temporarily loaded:

```text
jenkins-shared-library@fix/eks-deployment-aws-credentials
```

The test pipeline then successfully created/configured the Kubernetes resources and completed rollout.

## Final Merge

Shared-library fix was merged as:

```text
5bcdcbd
merge: fix EKS deployment AWS credentials
```

The application returned to:

```groovy
@Library('jenkins-shared-library') _
```

## Lesson

Credential existence and credential **scope** are different problems.

A credential working in one Jenkins stage does not make it automatically available in another stage.

---

# 5. ECR Push Succeeded but Deployment Failed

## Symptom

Because ECR push occurred before the EKS authentication failure, image:

```text
1.1.1-1
```

already existed in ECR even though the full pipeline failed.

## Risk

Blindly rerunning the pipeline can leave:

```text
multiple successfully pushed images
failed deployment attempts
version changes in Jenkins workspaces
partial infrastructure state
```

## Correct Approach

Before rerunning:

```text
identify last successful stage
identify exact failed stage
inspect ECR
inspect Kubernetes
fix root cause
then rerun
```

## Lesson

A Jenkins build is a sequence of effects.

`Finished: FAILURE` does not mean every earlier stage was rolled back automatically.

---

# 6. Jenkins Commit Stage Was Correctly Skipped After Deployment Failure

## Observation

After the EKS authentication failure Jenkins showed:

```text
Stage "Commit Version Update" skipped due to earlier failure(s)
```

## Why This Is Good

The pipeline order is:

```text
Deploy
→ Commit Version Update
```

Therefore a Maven version should not be committed back as successfully deployed if deployment itself failed.

## Lesson

Pipeline stage ordering can enforce release integrity.

---

# 7. Docker Login Credential Warning

## Symptom

After successful ECR authentication Docker warned that credentials were stored unencrypted in:

```text
/var/jenkins_home/.docker/config.json
```

## Impact

The warning did not fail:

```text
ECR login
Docker push
pipeline
```

## Current State

This is a known learning-environment limitation.

## Future Improvement

Use:

```text
Docker credential helper
```

or a temporary Docker configuration directory appropriate for the pipeline environment.

## Lesson

Record security warnings even when they do not block delivery.

---

# 8. Kubernetes Rollback `last-applied-configuration` Warning

## Symptom

Rollback command:

```bash
kubectl rollout undo \
  deployment/java-maven-app \
  --namespace default \
  --to-revision=1
```

reported that the Deployment had previously been managed with:

```text
kubectl apply
```

and warned that rollback would not update:

```text
kubectl.kubernetes.io/last-applied-configuration
```

## Result

The rollback still completed successfully.

Deployment changed to:

```text
1.1.1-1
```

The replacement Pod became:

```text
Ready=true
Restarts=0
```

The public application continued returning:

```text
HTTP 200
```

## Root Cause of Warning

Two different Kubernetes state-management operations were used:

```text
normal deployment:
kubectl apply

rollback test:
kubectl rollout undo
```

`rollout undo` changes the live Deployment revision but does not rewrite the apply annotation.

## Operational Meaning

A later:

```bash
kubectl apply
```

can reconcile runtime back to the manifest-generated desired image.

## Lesson

A runtime rollback should eventually be reconciled with source-controlled desired state.

---

# 9. Rollback Changes Revision Numbers

## Observation

Initial history:

```text
revision 1 → 1.1.1-1
revision 2 → 1.1.1-2
```

After rollback to revision 1:

```text
revision 3 → 1.1.1-1
```

After restoring the current image:

```text
revision 4 → 1.1.1-2
```

## Lesson

Rollback does not rewind Kubernetes revision numbering.

Always inspect:

```bash
kubectl rollout history \
  deployment/java-maven-app \
  --namespace default
```

before choosing a future revision.

Do not assume:

```text
revision 1
```

will remain the rollback target forever.

---

# 10. `kubectl logs --previous` Returned No Previous Container

## Observation

The current Pod had:

```text
Restarts:
0
```

Attempting to read previous-container logs had no prior terminated container available.

## Meaning

This was expected.

`--previous` is useful when:

```text
RestartCount > 0
```

such as:

```text
CrashLoopBackOff
application crash
OOMKilled
```

## Lesson

“No previous container” is not itself evidence of failure.

---

# 11. Maven Encoding Warning

## Observation

Maven reported warnings indicating that platform encoding was being used.

The build still completed:

```text
BUILD SUCCESS
```

## Current Impact

Non-blocking.

## Future Improvement

Configure encoding explicitly in Maven, for example project build/source encoding properties.

## Lesson

Warnings should be tracked separately from failures.

---

# 12. Documentation Branch Upstream Changed to GitLab

## Symptom

The documentation branch was first pushed with:

```bash
git push -u \
  github \
  docs/complete-project-documentation
```

which configured:

```text
github/docs/complete-project-documentation
```

as upstream.

The same branch was then pushed with:

```bash
git push -u \
  gitlab \
  docs/complete-project-documentation
```

which changed the local upstream to:

```text
gitlab/docs/complete-project-documentation
```

## Root Cause

`-u` / `--set-upstream` changes the branch's configured upstream.

Using it against the secondary mirror after using it against the primary changes which remote normal:

```bash
git pull
git push
```

operations track.

## Fix

Restore GitHub:

```bash
git branch \
  --set-upstream-to=github/docs/complete-project-documentation \
  docs/complete-project-documentation
```

Verify:

```bash
git rev-parse \
  --abbrev-ref \
  --symbolic-full-name \
  '@{u}'
```

Expected:

```text
github/docs/complete-project-documentation
```

## Correct Future Push Pattern

First-time primary push:

```bash
git push -u \
  github \
  <branch>
```

Secondary mirror:

```bash
git push \
  gitlab \
  <branch>
```

No second `-u`.

## Lesson

One local branch normally has one configured upstream.

A secondary mirror should not automatically replace the primary tracking branch.

---

# 13. Docker Socket Permission Model

## Training Method

Nana's learning setup demonstrated:

```bash
chmod 666 \
  /var/run/docker.sock
```

so Jenkins could access the Docker daemon.

## Security Concern

Docker socket access provides extensive control over the host.

Making it world-writable is unsuitable as a preferred production design.

## Safer Direction

Use:

```text
Docker group / matching GID
controlled container group membership
or an isolated build architecture
```

## Lesson

Preserve the training method historically while clearly distinguishing later security improvements.

---

# 14. Troubleshooting Order

When the pipeline fails, troubleshoot by layer:

```text
Git checkout
        ↓
Jenkins Shared Library
        ↓
Maven version
        ↓
Maven tests
        ↓
JAR
        ↓
Docker build
        ↓
ECR authentication
        ↓
ECR push
        ↓
EKS authentication
        ↓
Kubernetes apply
        ↓
rollout
        ↓
Service / EndpointSlice
        ↓
ELB
        ↓
HTTP
```

Do not jump directly to AWS infrastructure when the failure occurred in Maven.

Do not rewrite application code when the Pod is healthy but ELB routing is broken.

---

# 15. Troubleshooting Principle

For every failure:

```text
observe
→ identify failed layer
→ preserve evidence
→ determine root cause
→ make the smallest justified fix
→ verify
→ document
```

Avoid:

```text
random configuration changes
recreating infrastructure unnecessarily
hardcoding credentials
rerunning blindly
```
