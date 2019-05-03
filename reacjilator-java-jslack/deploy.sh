#!/bin/bash

if [ "${SERVERLESS_STAGE}" == "" ]; then
  export SERVERLESS_STAGE=dev
fi

export SLS_DEBUG=*

mvn clean package &&
  ./node_modules/serverless/bin/serverless deploy --stage ${SERVERLESS_STAGE} -v &&
  ./node_modules/serverless/bin/serverless invoke --function warmup
