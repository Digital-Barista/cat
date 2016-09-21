node {
   def mvnHome
   stage 'Configure'
        mvnHome = tool 'maven-global'
        properties(
            [buildDiscarder(
                logRotator(
                    artifactDaysToKeepStr: '', 
                    artifactNumToKeepStr: '', 
                    daysToKeepStr: '5', 
                    numToKeepStr: '5')), 
            disableConcurrentBuilds(), 
            pipelineTriggers([])]
        )
   stage 'Checkout'
        git branch: 'develop', 
            credentialsId: 'Nate-GitHub', 
            url: 'https://github.com/Digital-Barista/cat.git'
   
   stage 'Build'
        sh "${mvnHome}/bin/mvn clean install -Pbuild-flex -U"
   
   stage 'Publish Tests'
        junit '**/target/surefire-reports/**/TEST**.xml'
   
   stage 'Archive'
       
}