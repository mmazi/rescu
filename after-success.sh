#!/bin/bash

GIT_BRANCH=`git branch | sed -n -e 's/^\* \(.*\)/\1/p'`

echo "Travis branch:       " ${TRAVIS_BRANCH}
echo "Travis pull request: " ${TRAVIS_PULL_REQUEST}
echo "Travis JDK version:  " ${TRAVIS_JDK_VERSION}
if [ "${TRAVIS_JDK_VERSION}" == "openjdk7" -a "${TRAVIS_BRANCH}" == "develop" -a "${TRAVIS_PULL_REQUEST}" == "false" ]; then
    mvn deploy --settings settings.xml
else
    echo "Not deploying artefacts. This is only done with non-pull-request commits to develop branch with Java 7 builds."
fi
