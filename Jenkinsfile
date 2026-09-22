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