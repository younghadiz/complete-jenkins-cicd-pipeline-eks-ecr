# Complete CI/CD Pipeline with EKS and AWS ECR

A hands-on DevOps portfolio project implementing a complete CI/CD workflow for a Java Maven application using Jenkins, Docker, Amazon ECR, Kubernetes, and Amazon EKS.

> This repository is being developed incrementally as part of my practical DevOps engineering work. The implementation, verification evidence, architecture documentation, troubleshooting records, and operational runbook will be expanded as each project phase is completed and validated.

## Project Objective

The goal of this project is to implement a CI/CD pipeline that:

1. Increments the Java Maven application version.
2. Builds the application artifact with Maven.
3. Builds a Docker image for the application.
4. Pushes the versioned image to a private Amazon ECR repository.
5. Deploys the new application version to an Amazon EKS cluster.
6. Commits the updated application version back to Git.

Jenkins will run on a DigitalOcean server, while Amazon Web Services will provide the container registry and Kubernetes infrastructure.

## Technology Stack

- Java
- Maven
- Git
- Jenkins
- Docker
- Linux
- Kubernetes
- Amazon ECR
- Amazon EKS
- DigitalOcean
- kubectl
- eksctl

## Application Attribution

The baseline Java Maven application used in this project was supplied as part of the **TechWorld with Nana DevOps Bootcamp**.

Upstream application:

```text
https://gitlab.com/twn-devops-bootcamp/latest/08-jenkins/java-maven-app

Imported branch:

starting-code

Imported baseline commit:

f2a092e5375f015993151d078df7dc5dc0deae79

The supplied Java Maven application is not presented as my original application code.

My independent work in this repository covers the DevOps implementation around the application, including repository workflow, application build and validation, containerization, Jenkins CI/CD automation, Jenkins Shared Library integration, Amazon ECR, Amazon EKS, Kubernetes deployment, security configuration, testing, troubleshooting, documentation, rollback, and operational cleanup.

Repository Strategy

This project is maintained in two synchronized remote repositories:

GitHub — primary repository
GitLab — secondary repository

Development follows a branch workflow based on:
main
develop
feature/*
bugfix/*
hotfix/*
docs/*

Feature branches are merged using non-fast-forward merges to retain meaningful project history.

Current Status
Phase 1 — Requirements: Complete
Phase 2 — Repository Setup: In progress

Additional implementation documentation will be added as each project phase is completed and verified.

Author

Gafari Salaudeen

DevOps / Cloud Engineering Portfolio Project

