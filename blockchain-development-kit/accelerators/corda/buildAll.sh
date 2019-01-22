#!/usr/bin/env bash

# A master script for building all components in the correct order.
# Can be used as the base for a CI build process or a quick way
# of building and testing locally

# nice named parameters
POSITIONAL=()
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -t|--test)
    TEST="Y"
    shift # past argument
    ;;
    --tag)
    DOCKER_TAG="$2"
    shift # past argument
    shift # past value
    ;;
    -p|--publish)
    PUBLISH="Y"
    shift # past argument
    ;;
    *)    # unknown option
    POSITIONAL+=("$1") # save it in an array for later
    shift # past argument
    ;;
esac
done
set -- "${POSITIONAL[@]}" # restore positional parameters

echo "Flags are"
echo "--test : $TEST"
echo "--tag : $DOCKER_TAG"
echo "--publish : $PUBLISH"


# Setup build flags
script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
if ${TEST}; then
  testflags="test"
  cleanflags="clean"
else
  testflags="-x test"
  cleanflags=""
fi
if [[ -z "${DOCKER_TAG}" ]]; then
  DOCKER_TAG=$(git rev-parse HEAD  | cut -c 1-8)
fi

echo "Running from $script_dir"
echo "Additional gradle flags are '$cleanflags $testflags'"


###  Building shared jars

echo "Building 'commons'"
cd $script_dir/service-bus-integration/commons
./gradlew $cleanflags $testflags jar copyJarToLib &&
if (($?)); then exit -1 ; fi

echo "Building 'corda-reflections'"
cd $script_dir/service-bus-integration/corda-reflections
./gradlew $cleanflags $testflags jar copyJarToLib
if (($?)); then exit -1 ; fi


### Building Corda Apps

echo "Building 'chat' CorDapp"
ls $script_dir/cordapps/chat
cd $script_dir/cordapps/chat
./gradlew $cleanflags $testflags assemble
if (($?)); then exit -1 ; fi

cp cordapp/build/libs/chat-0.1.jar ../jars/chat.jar


echo "Building 'simple-marketplace' CorDapp"
cd $script_dir/cordapps/simple-marketplace
./gradlew $cleanflags $testflags assemble
if (($?)); then exit -1 ; fi
cp cordapp/build/libs/cordapp-simpleMarketplace-0.1.jar ../jars/simple-marketplace.jar


echo "Building 'refrigerated-transportation' CorDapp"
cd $script_dir/cordapps/refrigerated-transportation
./gradlew $cleanflags $testflags assemble
if (($?)); then exit -1 ; fi
cp cordapp/build/libs/cordapp-0.1.jar ../jars/refrigerated-transportation.jar


echo "Building 'basic-provenance' CorDapp"
cd $script_dir/cordapps/basic-provenance
./gradlew $cleanflags $testflags assemble
if (($?)); then exit -1 ; fi
cp cordapp/build/libs/cordapp-example-0.1.jar ../jars/basic-provenance.jar


### Building Services

echo "Building 'corda-local-network' "
cd $script_dir/service-bus-integration/corda-local-network
./gradlew $cleanflags $testflags jar
if (($?)); then exit -1 ; fi
docker build -t corda-local-network  .
if (($?)); then exit -1 ; fi
if [[ ! -z ${PUBLISH} ]]; then
  ./publishDocker.sh ${DOCKER_TAG}
  if (($?)); then exit -1 ; fi
fi


echo "Building 'corda-transaction-builder' "
cd $script_dir/service-bus-integration/corda-transaction-builder
./gradlew $cleanflags $testflags jar
if (($?)); then exit -1 ; fi
docker build -t corda-transaction-builder  .
if (($?)); then exit -1 ; fi
if [[ ! -z ${PUBLISH} ]]; then
  ./publishDocker.sh ${DOCKER_TAG}
  if (($?)); then exit -1 ; fi
fi


echo "Building 'service-bus-listener' "
cd $script_dir/service-bus-integration/service-bus-listener
./gradlew $cleanflags $testflags jar
if (($?)); then exit -1 ; fi
docker build -t service-bus-listener  .
if (($?)); then exit -1 ; fi
if [[ ! -z ${PUBLISH} ]]; then
  ./publishDocker.sh ${DOCKER_TAG}
  if (($?)); then exit -1 ; fi
fi

echo "Success - everything was built :) "












