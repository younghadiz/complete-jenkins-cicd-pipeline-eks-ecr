Verification Evidence
Purpose

This document is the project evidence index.

It identifies the commands and verified states used to prove the CI/CD implementation worked from source control through the externally reachable application.

Generated infrastructure identifiers are historical observations and may differ in a recreated environment.

1. Source-Control Evidence
Current Application Commit

Verified application commit after Jenkins version commit-back:

09c96af
ci: version bump

Full SHA:

09c96af58fd465c9adb18b6a54aba839109a90b6

Verify:

git log \
  -1 \
  --oneline
Local / GitHub / GitLab Synchronization
git fetch github
git fetch gitlab

printf 'Local develop:  '
git rev-parse develop

printf 'GitHub develop: '
git rev-parse github/develop

printf 'GitLab develop: '
git rev-parse gitlab/develop

Verified:

Local:
09c96af58fd465c9adb18b6a54aba839109a90b6

GitHub:
09c96af58fd465c9adb18b6a54aba839109a90b6

GitLab:
09c96af58fd465c9adb18b6a54aba839109a90b6
2. Maven Evidence

Read version:

mvn help:evaluate \
  -Dexpression=project.version \
  -q \
  -DforceStdout

Verified:

1.1.1
3. Unit-Test Evidence

Verified Jenkins Maven test result:

Tests run: 1
Failures: 0
Errors: 0
Skipped: 0

Build result:

BUILD SUCCESS

Test class:

com.example.ApplicationTest
4. JAR Evidence

Verified Jenkins artifact:

target/java-maven-app-1.1.1.jar

The Spring Boot Maven plugin repackaged it as the executable application JAR.

5. Jenkins Evidence

Verified final pipeline stages:

Increment Version
Build Application
Build Docker Image
Push Docker Image
Deploy
Commit Version Update

Final result:

Finished: SUCCESS

Verified shared-library revision used by the successful pipeline:

5bcdcbd
merge: fix EKS deployment AWS credentials
6. Versioning Evidence

Pipeline input:

1.1.0-SNAPSHOT

Pipeline application version:

1.1.1

Jenkins build number:

2

Container image tag:

1.1.1-2
7. ECR Repository Evidence

Repository:

java-maven-app

Region:

ca-central-1

Verified repository configuration:

Tag mutability:
MUTABLE

Scan on push:
true

Encryption:
AES256

Verify:

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
8. ECR Image Evidence

Verify:

aws ecr describe-images \
  --repository-name java-maven-app \
  --image-ids imageTag=1.1.1-2 \
  --region ca-central-1 \
  --query 'imageDetails[0].{
    Tags:imageTags,
    Digest:imageDigest,
    PushedAt:imagePushedAt,
    Size:imageSizeInBytes
  }' \
  --output table

Verified tag:

1.1.1-2

Verified digest:

sha256:aec124d06875b4bb3d262209b34bab67194bb8e1e1eec97e54d4671f0cca7d5b
9. EKS Cluster Evidence

Cluster:

java-maven-eks

Region:

ca-central-1

Verified:

Status:
ACTIVE

Kubernetes version:
1.35

eksctl created:
true

Verify:

eksctl get cluster \
  --region ca-central-1

and:

aws eks describe-cluster \
  --name java-maven-eks \
  --region ca-central-1
10. EKS Nodegroup Evidence

Nodegroup:

java-maven-nodes

Verified configuration:

managed:
true

capacity:
ON_DEMAND

instance:
t3.small

minimum:
1

desired:
1

maximum:
2

AMI:
AL2023_x86_64_STANDARD

Verify:

eksctl get nodegroup \
  --cluster java-maven-eks \
  --region ca-central-1
11. Worker-Node Evidence

Verify:

kubectl get nodes \
  -o wide

Verified:

Status:
Ready

OS:
Amazon Linux 2023

Architecture:
amd64

Runtime:
containerd

Healthy conditions:

MemoryPressure=False
DiskPressure=False
PIDPressure=False
Ready=True
12. Kubernetes Deployment Evidence

Verify:

kubectl get deployment \
  java-maven-app \
  --namespace default \
  -o wide

Verified:

READY:
1/1

UP-TO-DATE:
1

AVAILABLE:
1

Image:

002184382122.dkr.ecr.ca-central-1.amazonaws.com/java-maven-app:1.1.1-2
13. Pod Evidence

Verify:

kubectl get pods \
  --namespace default \
  -l app=java-maven-app \
  -o jsonpath='{range .items[*]}Pod={.metadata.name}{"\n"}Image={.spec.containers[0].image}{"\n"}ImageID={.status.containerStatuses[0].imageID}{"\n"}Ready={.status.containerStatuses[0].ready}{"\n"}Restarts={.status.containerStatuses[0].restartCount}{"\n\n"}{end}'

Verified:

Ready:
true

Restarts:
0

Runtime digest:

sha256:aec124d06875b4bb3d262209b34bab67194bb8e1e1eec97e54d4671f0cca7d5b
14. ECR-to-Pod Digest Evidence

Verified:

ECR digest
=
running Pod ImageID digest

Digest:

sha256:aec124d06875b4bb3d262209b34bab67194bb8e1e1eec97e54d4671f0cca7d5b

This proves the expected registry image content reached the runtime.

15. Application Log Evidence

Verify:

kubectl logs \
  deployment/java-maven-app \
  --namespace default \
  --tail=100

Verified log characteristics:

Spring Boot 3.5.5

Java 17.0.20

Tomcat initialized on port 8080

Java app started

Tomcat started on port 8080

Started Application
16. Service Evidence

Verify:

kubectl get service \
  java-maven-app \
  --namespace default \
  -o wide

Stable configuration:

Type:
LoadBalancer

port:
80

targetPort:
8080

selector:
app=java-maven-app

Historical generated values during verification:

ClusterIP:
10.100.197.178

NodePort:
30321

These generated values may change after recreation.

17. EndpointSlice Evidence

Verify:

kubectl get endpointslice \
  --namespace default \
  -l kubernetes.io/service-name=java-maven-app \
  -o wide

Historical backend:

192.168.50.149:8080

Pod IP is ephemeral.

18. AWS Load Balancer Evidence

AWS verification showed the Service provisioned:

Classic Load Balancer

Scheme:

internet-facing

Verify:

aws elb describe-load-balancers \
  --region ca-central-1 \
  --query 'LoadBalancerDescriptions[].{
    Name:LoadBalancerName,
    DNSName:DNSName,
    Scheme:Scheme
  }' \
  --output table

The project did not use:

ALB
NLB
Ingress

for this workload.

19. ELB Health Evidence

Verify:

aws elb describe-instance-health \
  --load-balancer-name <CLASSIC_ELB_NAME> \
  --region ca-central-1

Verified backend:

InService

Historical health-check target:

TCP:30321

The NodePort may differ in a recreated environment.

20. External HTTP Evidence

Retrieve endpoint:

export APP_HOST="$(
  kubectl get service \
    java-maven-app \
    --namespace default \
    -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
)"

Test:

curl -sS \
  -o /dev/null \
  -w 'HTTP status: %{http_code}\n' \
  --connect-timeout 10 \
  --max-time 20 \
  "http://${APP_HOST}/"

Verified:

HTTP status: 200

Content:

curl -fsS \
  "http://${APP_HOST}/" \
  | grep -F \
      "Welcome to Java Maven Application"

Verified:

<h1>Welcome to Java Maven Application</h1>
21. Metrics Evidence

Pod:

kubectl top pod \
  --namespace default \
  -l app=java-maven-app

Historical observation:

CPU:
2m

Memory:
150Mi

Node:

kubectl top node

Historical observation:

CPU:
43m / 2%

Memory:
830Mi / 57%

These are point-in-time observations, not fixed requirements.

22. EKS Control-Plane Logging Evidence

Verify:

aws eks describe-cluster \
  --name java-maven-eks \
  --region ca-central-1 \
  --query 'cluster.logging.clusterLogging' \
  --output json

Verified control-plane logging:

api:
disabled

audit:
disabled

authenticator:
disabled

controllerManager:
disabled

scheduler:
disabled
23. Rollback Evidence

Initial:

revision 1:
1.1.1-1

revision 2:
1.1.1-2

Rollback command:

kubectl rollout undo \
  deployment/java-maven-app \
  --namespace default \
  --to-revision=1

Verified rollback runtime:

image:
1.1.1-1

digest:
sha256:4878a7f7d6b67f10149459944b0f76c07a88d5421f1ee1cb069d53108bc76efe

Ready:
true

Restarts:
0

HTTP:
200
24. Restoration Evidence

The intended current image was restored:

kubectl set image \
  deployment/java-maven-app \
  java-maven-app="$CURRENT_IMAGE" \
  --namespace default

Verified restored image:

1.1.1-2

Verified digest:

sha256:aec124d06875b4bb3d262209b34bab67194bb8e1e1eec97e54d4671f0cca7d5b

Application:

Ready=true
Restarts=0
HTTP=200
25. Verification Summary
Git synchronization
PASS

Maven version
PASS

unit test
PASS

Jenkins pipeline
PASS

JAR artifact
PASS

Docker image
PASS

ECR
PASS

EKS
PASS

worker node
PASS

Deployment
PASS

Pod
PASS

registry/runtime digest
PASS

Service
PASS

Classic ELB
PASS

HTTP
PASS

monitoring
PASS

rollback
PASS

restoration
PASS