#!/bin/sh

echo "Downloading WhiteSource agent..."
curl -LJO https://github.com/whitesource/unified-agent-distribution/releases/latest/download/wss-unified-agent.jar

echo "Running WhiteSource scan..."
java -jar wss-unified-agent.jar -userKey ${MEND_USER_KEY} -apiKey ${MEND_API_KEY} -projectVersion ${PROJECT_VERSION} -project ${PROJECT_NAME} -productVersion ${PRODUCT_VERSION} -product ${PRODUCT_NAME} -c ws.config -d ../