#!groovy

node {
    withEnv(["CI_DEST=${WORKSPACE}/tmp/customer_image"]) {
        def tag
        if("${BRANCH_NAME}" == 'master') {
            tag = 'latest'
            env.SYRACUSE_RELEASE = '2.999'
        } else {
            if ("${BRANCH_NAME}" =~ /^release\//) {
                tag = "${BRANCH_NAME}".split('/')[1]
            }
        }
        
        stage('Checkout SCM')  {
            checkout scm
        }
        

        docker.withRegistry('https://repository.sagex3.com', 'jenkins_platform') {
            def izPackImage
            def buildRandom = sh(script: 'echo $(cat /dev/urandom | tr -cd "a-f0-9" | head -c 10)', returnStdout: true).substring(0,9)
			def stageTag = "stage_${BUILD_ID}_${buildRandom}"
            // Add ant composant
            sh('cp -r /var/jenkins_home/userContent/binary/ant-lib .')
            stage('Build docker image') {
                izPackImage = docker.build("izpack:${stageTag}", '-f docker/Dockerfile-izpack \
                    --build-arg "https_proxy=${HTTP_PROXY}" \
                    --build-arg "http_proxy=${HTTPS_PROXY}" \
		            --pull \
                    "${WORKSPACE}/"')
            }
           

            if ((currentBuild.result == null) || (currentBuild.result == "SUCCESS")) {
                if (tag) {
                    stage('Push image') {
                        izPackImage.push(tag)
                    }
                }   
            }

	        if (izPackImage != null) sh("docker rmi -f ${izPackImage.id}")
        }
    }
}
