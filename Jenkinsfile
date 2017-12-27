pipeline {
  agent {
    kubernetes {
      label 'shopRunner_snowplow_tracker' 
      containerTemplate {
        name 'gradle' // name for the container, can be any sequence of characters
        image 'openjdk:8-jdk'
        resourceRequestCpu '1500m'
        resourceRequestMemory '2Gi'
        resourceLimitMemory '2Gi'
 
        // By default the openjdk container will run and quit, we want it to stay running so jenkins can
        // interact with it.  We set the command to something that will continue to run.
        ttyEnabled true
        command 'cat'
      }
    }
  }
 
  options {
    buildDiscarder(logRotator(numToKeepStr: '50'))
    timeout(time: 20, unit: 'MINUTES')
    timestamps()
  }
 
  stages {
    
    stage('Publish Artifacts') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'artifactory_jenkins', passwordVariable: 'artifactory_password', usernameVariable: 'artifactory_user')]) {
          sh "./gradlew --console plain artifactoryPublish -Partifactory_user=$artifactory_user -Partifactory_password='$artifactory_password'"
        }
      }
    }
  }
}