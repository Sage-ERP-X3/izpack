#!groovy

env.IZPACK_VERSION = '5.2.0.5'
//env.IZPACK_VERSION = 'X3_293748_AdxAdmin'

node {
    withEnv(["CI_DEST=${WORKSPACE}/tmp/customer_image"]) {
        def tag
        if ("${BRANCH_NAME}" =~ /^release\//) {
                 tag = "${BRANCH_NAME}".split('/')[1]
                 // tag = "${env.IZPACK_VERSION}"
             }
             else {
                tag = "${BRANCH_NAME}".split('/')[1]
             }
             
        // if("${BRANCH_NAME}" == 'master') {
        //     tag = 'latest'
        //     env.SYRACUSE_RELEASE = '2.999'
        // } else {
        //     if ("${BRANCH_NAME}" =~ /^release\//) {
        //         tag = "${BRANCH_NAME}".split('/')[1]
        //     }
        //     // Temporary: to REMOVE
        //     else if ("${BRANCH_NAME}" =~ /^feature\//) {
        //         tag = "${BRANCH_NAME}".split('/')[1]
        //     }
        // }
        
        stage('Checkout SCM')  {
            checkout scm
        }
        


        // stage("Build com.sage.izpack.jar ${IZPACK_VERSION}") {
        //     docker.withRegistry('https://repository.sagex3.com', 'jenkins_platform') {
        //         env.kDevelopDrive  = "${WORKSPACE}"
        //         env.kDevelopPath   = "."
        //         env.kSrcPath       = "."
        //         env.kConstructionHome = "${WORKSPACE}/izpack/izPackCustomActions/com.sage.izpack.jar"
        //         env.kVersion    = env.BRANCH_NAME
        //         env.kVersName   = "all"
        //         env.kPlateform  = "linux"
        //         env.kJavaTargetVersion = "11" // "1.8"
        //         env.kDevelopHome = "/izpack"

        //         def image
        //         image = docker.image("izpack:${IZPACK_VERSION}")

        //         image.pull()
        //         sh('cp -r /var/jenkins_home/userContent/binary/ant-lib .')
        //         image.inside('-u root') {

        //             sh '''
        //                 cd  /izpack/
        //                 git pull
        //                 cd  /izpack/izPackCustomActions
        //                 ls -Rla
        //                 wCmde="ant"
        //                 wCmde="${wCmde} -DkJavaPath=${kJavaPath}"
        //                 wCmde="${wCmde} -DkJavaPathJRE=${kJavaPathJRE}"
        //                 wCmde="${wCmde} -Djava.target_version=${kJavaTargetVersion}"
        //                 wCmde="${wCmde} -Dant.build.javac.target=${kJavaTargetVersion}"
        //                 wCmde="${wCmde} -DkVersion=${kVersion}"
        //                 wCmde="${wCmde} -DkVersName=${kVersName}"
        //                 wCmde="${wCmde} -DkVersNum=${kVersNum}"
        //                 wCmde="${wCmde} -DkDevelopDrive=${kDevelopDrive}"
        //                 wCmde="${wCmde} -DkDevelopPath=${kDevelopPath}"
        //                 wCmde="${wCmde} -DkDevelopHome=${kDevelopHome}"
        //                 wCmde="${wCmde} -DkConstructionHome=${kConstructionHome}"
        //                 wCmde="${wCmde} -DkPlateform=${kPlateform}"
        //                 wCmde="${wCmde} -DkSrcPath=${kSrcPath}"
        //                 wCmde="${wCmde} -DkPasseNum=${kPasseNum}"

        //                 wCmdeAll="ant splashscreen -buildfile build.xml"
        //                 wCmdeAll="${wCmde} all -buildfile build.xml"
        //                 ${wCmdeAll}
 
        //                 ls -la /izpack/izPackCustomActions/bin/com.sage.izpack.jar
        //                 ls -la /izpack/izPackCustomActions/com.sage.izpack.jar
        //                 cp /izpack/izPackCustomActions/bin/com.sage.izpack.jar /izpack/izPackCustomActions/com.sage.izpack.jar
        //                 ls -la /izpack/izPackCustomActions/com.sage.izpack.jar
        //             '''
        //         }
               
        //     }
        // }

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
                        sh("echo ${tag} image pushed")
                    }
                }   
            }

	        if (izPackImage != null) sh("docker rmi -f ${izPackImage.id}")
        }
    }
}
