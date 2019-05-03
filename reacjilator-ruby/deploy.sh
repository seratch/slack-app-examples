#!/bin/bash
if [ "${SERVERLESS_STAGE}" == "" ]; then
  export SERVERLESS_STAGE=dev
fi
export SLS_DEBUG=*
serverless deploy --stage ${SERVERLESS_STAGE} -v
