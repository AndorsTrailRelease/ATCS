#!/bin/bash
ATCS_DIR="$(dirname "$(readlink -f "$0" || greadlink -f "$0" || stat -f "$0")")"
echo "ATCS_DIR: '${ATCS_DIR}'"

MAX_MEM="512M"
JAVA="java"
JAVA_OPTS='-DFONT_SCALE=1.0 -Dswing.aatext=true'

ENV_FILE="${ATCS_DIR}/ATCS.env"

if [ -f "${ENV_FILE}" ]; then
	source "${ENV_FILE}"
else
  {
    echo "#MAX_MEM=\"${MAX_MEM}\""
    echo "#JAVA=\"${JAVA}\""
    echo "#JAVA_OPTS=\"${JAVA_OPTS}\""
    echo ""
  }>"${ENV_FILE}"
fi

export ENV_FILE

# shellcheck disable=SC2086
# (spellchecker is disabled for this line, because we want it to be split into multiple arguments)
$JAVA ${JAVA_OPTS} -Xmx${MAX_MEM} -jar "${ATCS_DIR}/ATCS.jar"
