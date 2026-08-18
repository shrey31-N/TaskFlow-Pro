pipeline {

    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // =========================
        // USER SERVICE
        // =========================

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

        // =========================
        // PROJECT SERVICE
        // =========================

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

        // =========================
        // TASK SERVICE
        // =========================

        stage('Build Task Service') {
            steps {
                dir('backend/task-service') {
                    bat 'mvnw.cmd package -DskipTests'
                }
            }
        }

        /*
         * Temporarily skipped because the integration tests
         * require dependent microservices/Eureka.
         *
         * We will fix these tests later.
         */

        // =========================
        // NOTIFICATION SERVICE
        // =========================

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

        // =========================
        // API GATEWAY
        // =========================

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

        // =========================
        // EUREKA SERVER
        // =========================

        stage('Build Eureka Server') {
            steps {
                dir('backend/discovery-server') {
                    bat 'mvnw.cmd package -DskipTests'
                }
            }
        }

        // =========================
        // DOCKER BUILD
        // =========================

        stage('Docker Build') {
            steps {

                bat '''
                echo ========================================
                echo        DOCKER VERSION
                echo ========================================

                docker --version

                echo ========================================
                echo        BUILD USER SERVICE
                echo ========================================

                docker build -t taskflow-user-service:1.0 backend\\user-service

                echo ========================================
                echo        BUILD PROJECT SERVICE
                echo ========================================

                docker build -t taskflow-project-service:1.0 backend\\project-service

                echo ========================================
                echo        BUILD TASK SERVICE
                echo ========================================

                docker build -t taskflow-task-service:1.0 backend\\task-service

                echo ========================================
                echo        BUILD NOTIFICATION SERVICE
                echo ========================================

                docker build -t taskflow-notification-service:1.0 backend\\notification-service

                echo ========================================
                echo        BUILD API GATEWAY
                echo ========================================

                docker build -t taskflow-api-gateway:1.0 backend\\api-gateway

                echo ========================================
                echo        BUILD EUREKA SERVER
                echo ========================================

                docker build -t taskflow-discovery-server:1.0 backend\\discovery-server
                '''
            }
        }

        // =========================
        // VERIFY DOCKER IMAGES
        // =========================

        stage('Verify Docker Images') {
            steps {

                bat '''
                echo ========================================
                echo        TASKFLOW DOCKER IMAGES
                echo ========================================

                docker images | findstr taskflow-
                '''
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t taskflow-user-service:ci ./backend/user-service'
                bat 'docker build -t taskflow-project-service:ci ./backend/project-service'
                bat 'docker build -t taskflow-task-service:ci ./backend/task-service'
                bat 'docker build -t taskflow-notification-service:ci ./backend/notification-service'
                bat 'docker build -t taskflow-api-gateway:ci ./backend/api-gateway'
                bat 'docker build -t taskflow-discovery-server:ci ./backend/discovery-server'
            }
        }

        stage('Docker Image Verification') {
            steps {
                bat 'docker images taskflow-user-service'
                bat 'docker images taskflow-project-service'
                bat 'docker images taskflow-task-service'
                bat 'docker images taskflow-notification-service'
                bat 'docker images taskflow-api-gateway'
                bat 'docker images taskflow-discovery-server'
            }
        }

    }


    post {

        success {
            echo '========================================'
            echo 'TaskFlow-Pro CI/CD Build Successful!'
            echo 'Docker images built successfully!'
            echo '========================================'
        }

        failure {
            echo '========================================'
            echo 'TaskFlow-Pro Pipeline Failed!'
            echo 'Check the failed stage above.'
            echo '========================================'
        }
    }
}