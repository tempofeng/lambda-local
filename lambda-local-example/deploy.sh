#!/usr/bin/env bash
PROJECT_NAME='lambda-local-example'

echo 'Building ...'
../gradlew build test
if [ "$?" != 0 ]; then
   exit 1
fi

echo 'Deploying to AWS ...'
../gradlew $PROJECT_NAME-deployer:exe
if [ "$?" != 0 ]; then
   exit 2
fi

echo 'Done ...'
 
