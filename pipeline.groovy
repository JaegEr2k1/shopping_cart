pipeline {
    agent any
    // Define tools
    tools {
        jdk 'jdk17'
        maven 'maven'
    }    
    
    environment {
        SCANNER_HOME = tool 'sonar-scanner'
        AWS_ACCESS_KEY_ID = credentials('awscre') 
        AWS_SECRET_ACCESS_KEY = credentials('awscre')
        SUBNET_ID = 'subnet-066e98d4b6bf1fda7'
        SONAR_TOKEN = 'squ_78ae7d422ee8715e05372b040e91622252d67494'
        SG_ID = 'sg-0165755a348f3bf3a'
        PROJECT_NAME = 'shopping_cart'
        AWS_REGION = "ap-southeast-1"
        REPO = 'https://github.com/JaegEr2k1/shopping_cart.git'
    }
    
    stages {
        stage('Git-Checkout') {
            steps {
                git branch: 'main', url: "${REPO}"
            }
        }
        stage('Compile') {
            // Clean and compile java source code
            steps {
                sh "mvn clean compile"
            }
        }
        stage('Sonarqube Analysis') {
            // Check code quality
            steps {
                sh ''' 
                $SCANNER_HOME/bin/sonar-scanner \
                -Dsonar.url=http://172.16.5.84:9000/ \
                -Dsonar.login=${SONAR_TOKEN} \
                -Dsonar.projectName=${PROJECT_NAME} \
                -Dsonar.java.binaries=. \
                -Dsonar.projectKey=${PROJECT_NAME}-key 
                '''
            }
        }
        stage('Build application') {
            steps {
                sh 'mvn clean install -DskipTests=true'
            }
        }
        stage('Build and push image') {
            // Build and push docker images to ECR
            steps {
                sh '''
                aws ecr get-login-password --region ap-southeast-1 | docker login --username AWS --password-stdin 325842618176.dkr.ecr.ap-southeast-1.amazonaws.com
                docker build -t ${PROJECT_NAME}:latest -f docker/Dockerfile .
                docker tag ${PROJECT_NAME}:latest 325842618176.dkr.ecr.ap-southeast-1.amazonaws.com/${PROJECT_NAME}:latest
                docker push 325842618176.dkr.ecr.ap-southeast-1.amazonaws.com/${PROJECT_NAME}:latest
                '''
            }
        }
        stage('Deploy') {
            // Deploy application to ECS
            steps {
                sh '''
                aws ecs update-service --cluster ${PROJECT_NAME}-ecs-cluster \
                --service ${PROJECT_NAME} \
                --region ${AWS_REGION} \
                --force-new-deployment \
                --desired-count 1 \
                --network-configuration "awsvpcConfiguration={subnets=[\"${SUBNET_ID}\"],securityGroups=[\"${SG_ID}\"],assignPublicIp=ENABLED}" 
                '''
              }
        }
    }
}

