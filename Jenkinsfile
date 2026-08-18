pipeline {

    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build User Service') {
            steps {
                dir('backend/user-service') {
                    bat 'mvnw.cmd package -DskipTests'
                }
            }
        }

        stage('Test User Service') {
            steps {
                dir('backend/user-service') {
                    bat 'mvnw.cmd test'
                }
            }
        }

        stage('Build Project Service') {
            steps {
                dir('backend/project-service') {
                    bat 'mvnw.cmd package -DskipTests'
                }
            }
        }

        stage('Test Project Service') {
            steps {
                dir('backend/project-service') {
                    bat 'mvnw.cmd test'
                }
            }
        }

        stage('Build Task Service') {
            steps {
                dir('backend/task-service') {
                    bat 'mvnw.cmd package -DskipTests'
                }
            }
        }

        stage('Test Task Service') {
            steps {
                dir('backend/task-service') {
                    bat 'mvnw.cmd test'
                }
            }
        }

        stage('Build Notification Service') {
            steps {
                dir('backend/notification-service') {
                    bat 'mvnw.cmd package -DskipTests'
                }
            }
        }

        stage('Test Notification Service') {
            steps {
                dir('backend/notification-service') {
                    bat 'mvnw.cmd test'
                }
            }
        }

        stage('Build API Gateway') {
            steps {
                dir('backend/api-gateway') {
                    bat 'mvnw.cmd package -DskipTests'
                }
            }
        }

        stage('Test API Gateway') {
            steps {
                dir('backend/api-gateway') {
                    bat 'mvnw.cmd test'
                }
            }
        }
    }

    post {

        success {
            echo 'TaskFlow-Pro CI Pipeline Successful!'
        }

        failure {
            echo 'TaskFlow-Pro CI Pipeline Failed!'
        }
    }
}